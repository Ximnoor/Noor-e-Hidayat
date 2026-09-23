package com.example.essentials

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.Calendar

object ContentHttp {
    val client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).callTimeout(45, TimeUnit.SECONDS).build()
    suspend fun json(url: String): JSONObject = withContext(Dispatchers.IO) {
        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            check(response.isSuccessful) { "Service unavailable (${response.code}). Please retry." }
            JSONObject(response.body?.string() ?: error("Empty response"))
        }
    }
}
fun openSource(context: Context, url: String) {
    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    catch (_: Exception) { Toast.makeText(context, "No browser is available.", Toast.LENGTH_SHORT).show() }
}
@Composable
fun DuaContent(dua: DailyDua) {
    val context = LocalContext.current
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(dua.title, style = MaterialTheme.typography.titleMedium)
            Text(dua.arabic, Modifier.fillMaxWidth(), fontSize = 26.sp, lineHeight = 44.sp, textAlign = TextAlign.Right)
            Text(dua.urdu, Modifier.fillMaxWidth(), fontSize = 20.sp, lineHeight = 34.sp, textAlign = TextAlign.Right)
            TextButton(onClick = { openSource(context, dua.source) }) { Text(dua.reference) }
        }
    }
}
@Composable
fun DailyDuaCard() {
    var index by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % DailyDuas.all.size) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("روزمرہ کی دعا • Daily dua", style = MaterialTheme.typography.titleLarge)
        DuaContent(DailyDuas.all[index])
        TextButton(onClick = { index = (index + 1) % DailyDuas.all.size }) { Text("اگلی دعا • Next dua") }
    }
}
@Composable
fun MasnoonDuasScreen() {
    var query by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("مسنون اور قرآنی دعائیں", style = MaterialTheme.typography.headlineSmall) }
        item { OutlinedTextField(query, { query = it }, label = { Text("Search دعا") }, modifier = Modifier.fillMaxWidth()) }
        items(DailyDuas.all.filter { query.isBlank() || (it.title + it.urdu + it.reference).contains(query, true) }) { DuaContent(it) }
    }
}
@Composable
fun StoriesScreen() {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("قصص الانبیاء", style = MaterialTheme.typography.headlineMedium) }
        item { Text("قرآن کی روشنی میں مختصر خلاصے۔ مکمل واقعہ اور تفسیر کے لیے حوالہ کھولیں۔") }
        item { OutlinedTextField(query, { query = it }, label = { Text("Search prophet / نبی کا نام") }, modifier = Modifier.fillMaxWidth()) }
        items(ProphetStories.all.filter { it.name.contains(query, true) }) { story ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(story.name, style = MaterialTheme.typography.titleLarge)
                    Text(story.summary, Modifier.fillMaxWidth(), fontSize = 20.sp, lineHeight = 34.sp, textAlign = TextAlign.Right)
                    Text("Quran ${story.passage}")
                    TextButton(onClick = { openSource(context, "https://quran.com/ur/${story.chapter}/${story.verse}") }) { Text("قرآن میں پڑھیں • Read source") }
                }
            }
        }
    }
}

@Composable
fun QuranReader(surah: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    var verses by remember(surah) { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var error by remember(surah) { mutableStateOf<String?>(null) }
    var attempt by remember { mutableIntStateOf(0) }
    var loading by remember(surah) { mutableStateOf(true) }
    LaunchedEffect(surah, attempt) {
        loading = true; error = null
        try {
            val data = ContentHttp.json("https://api.alquran.cloud/v1/surah/$surah/editions/quran-uthmani,ur.jalandhry").getJSONArray("data")
            val arabic = data.getJSONObject(0).getJSONArray("ayahs")
            val urdu = data.getJSONObject(1).getJSONArray("ayahs")
            check(arabic.length() == urdu.length()) { "The editions did not match. Please retry." }
            verses = (0 until arabic.length()).map { i ->
                check(arabic.getJSONObject(i).getInt("numberInSurah") == urdu.getJSONObject(i).getInt("numberInSurah"))
                arabic.getJSONObject(i).getString("text") to urdu.getJSONObject(i).getString("text")
            }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { error = e.message ?: "Unable to load Quran. Check your connection." }
        finally { loading = false }
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { TextButton(onClick = onBack) { Text("Back to Surahs") } }
        item { Text("قرآن پاک • Surah $surah", style = MaterialTheme.typography.headlineSmall) }
        item { Text("Arabic: Tanzil Uthmani • Urdu: Fateh Muhammad Jalandhry\nDelivered by Al Quran Cloud. Tafseer opens Quran.com online.") }
        if (loading) item { CircularProgressIndicator() }
        if (error != null) item { Text(error!!); Button(onClick = { attempt++ }) { Text("Retry") } }
        items(verses.size) { index ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("$surah:${index + 1}", color = MaterialTheme.colorScheme.primary)
                    Text(verses[index].first, Modifier.fillMaxWidth(), fontSize = 28.sp, lineHeight = 48.sp, textAlign = TextAlign.Right)
                    Text(verses[index].second, Modifier.fillMaxWidth(), fontSize = 20.sp, lineHeight = 36.sp, textAlign = TextAlign.Right)
                    TextButton(onClick = { openSource(context, "https://quran.com/$surah:${index + 1}/tafsirs/tafseer-ibn-e-kaseer-urdu") }) { Text("تفسیر ابن کثیر • Urdu tafseer ↗") }
                }
            }
        }
    }
}
