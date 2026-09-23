package com.example.essentials

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.util.Locale

@Composable
fun ChoiceButton(label: String, options: List<String>, onSelected: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    OutlinedButton(onClick = { query = ""; open = true }, modifier = Modifier.fillMaxWidth()) { Text(label) }
    if (open) AlertDialog(onDismissRequest = { open = false }, confirmButton = { TextButton(onClick = { open = false }) { Text("Close") } }, title = { Text(label) }, text = {
        Column {
            if (options.size > 10) OutlinedTextField(query, { query = it }, label = { Text("Search") })
            LazyColumn(Modifier.heightIn(max = 360.dp)) {
                items(options.filter { it.contains(query, true) }) { option ->
                    TextButton(onClick = { onSelected(option); open = false }) { Text(option) }
                }
            }
        }
    })
}
@Composable
fun WorldPrayerScreen() {
    val context = LocalContext.current
    val prefs = remember { PrayerPreferences.prefs(context) }
    val countries = remember { Locale.getISOCountries().map { it to Locale("",it).getDisplayCountry(Locale.ENGLISH) }.sortedBy { it.second } }
    var country by remember { mutableStateOf(prefs.getString("country", "PK")!!) }
    var city by remember { mutableStateOf(prefs.getString("city", "")!!) }
    var method by remember { mutableIntStateOf(prefs.getInt("method", 1)) }
    var school by remember { mutableIntStateOf(prefs.getInt("school", 1)) }
    var latitude by remember { mutableIntStateOf(prefs.getInt("latitude", 3)) }
    var day by remember { mutableStateOf<PrayerDay?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    fun refresh() {
        if (busy) return
        if (city.isBlank()) { error = "Enter your city."; return }
        val changed = city.trim() != prefs.getString("city", "") || country != prefs.getString("country", "PK") || method != prefs.getInt("method",1) || school != prefs.getInt("school",1) || latitude != prefs.getInt("latitude",3)
        if (changed) { PrayerAlerts.clear(context); prefs.edit().remove("last_day").apply(); day = null }
        prefs.edit().putString("city",city.trim()).putString("country",country).putInt("method",method).putInt("school",school).putInt("latitude",latitude).apply()
        scope.launch {
            busy = true; error = null
            try { day = WorldPrayer.fetch(context); PrayerPreferences.startWorker(context) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { error = e.message ?: "Could not load prayer times." }
            finally { busy = false }
        }
    }
    LaunchedEffect(Unit) { if (city.isNotBlank()) refresh() }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = { tab = 0 }) { Text("Prayer times") }
            TextButton(onClick = { tab = 1 }) { Text("قبلہ • Qibla") }
        }
        if (tab == 1) QiblaScreen() else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text("Worldwide prayer times", style = MaterialTheme.typography.headlineSmall) }
            item { Text("Choose a country and city. Times follow that city's timezone. Match the calculation method to your local mosque.") }
            item { ChoiceButton("Country: ${countries.firstOrNull { it.first == country }?.second ?: country}", countries.map { it.second }) { selected -> country = countries.first { it.second == selected }.first } }
            item { OutlinedTextField(city, { city = it }, label = { Text("City / شہر") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
            item { ChoiceButton("Method: ${PrayerPreferences.methods.entries.firstOrNull { it.value == method }?.key}", PrayerPreferences.methods.keys.toList()) { method = PrayerPreferences.methods.getValue(it) } }
            item { ChoiceButton(if (school == 1) "Asr: Hanafi" else "Asr: Standard (Shafi / Maliki / Hanbali)", listOf("Hanafi", "Standard (Shafi / Maliki / Hanbali)")) { school = if (it == "Hanafi") 1 else 0 } }
            item { ChoiceButton("High latitude: ${listOf("Middle of night","One seventh","Angle based")[latitude-1]}", listOf("Middle of night","One seventh","Angle based")) { latitude = listOf("Middle of night","One seventh","Angle based").indexOf(it)+1 } }
            item { Button(onClick = { refresh() }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(if (busy) "Loading…" else "Save & load timings") } }
            if (error != null) item { Text(error!!, color = MaterialTheme.colorScheme.error) }
            day?.let { current ->
                item { Text("${current.label} • ${current.zone}", style = MaterialTheme.typography.titleMedium) }
                items(current.entries) { (name,time) -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(name); Text(time) } }
                item { Text("AlAdhan calculated times. Local mosque timetables may differ. Open the app online daily; background refresh depends on Android battery settings.") }
            }
            item { OutlinedButton(onClick = {
                if (Build.VERSION.SDK_INT >= 33) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                if (Build.VERSION.SDK_INT >= 31 && !(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()) {
                    try { context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, android.net.Uri.parse("package:${context.packageName}"))) }
                    catch (_: Exception) { error = "Enable Alarms & reminders in Android app settings." }
                } else PrayerAlerts.restore(context)
            }) { Text("Enable prayer alerts / exact alarms") } }
            item { Text("Use Settings to choose Azan sound and enable or disable each prayer. If exact alarms are unavailable, alerts may be delayed.") }
        }
    }
}
