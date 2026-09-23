package com.example.essentials

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.CancellationException
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object PrayerPreferences {
    fun prefs(context: Context) = context.getSharedPreferences("world_prayer", Context.MODE_PRIVATE)
    val methods = linkedMapOf("Karachi (Pakistan)" to 1, "North America (ISNA)" to 2, "Muslim World League" to 3, "Umm Al-Qura (Makkah)" to 4, "Egyptian Authority" to 5, "Tehran" to 7, "Gulf Region" to 8, "Kuwait" to 9, "Qatar" to 10, "Singapore" to 11, "France" to 12, "Turkey" to 13, "Russia" to 14, "Moonsighting Committee" to 15)
    fun startWorker(context: Context) {
        val request = PeriodicWorkRequestBuilder<PrayerRefreshWorker>(12, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("world-prayer-refresh", ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
data class PrayerDay(val label: String, val zone: String, val entries: List<Pair<String,String>>, val alarms: List<Pair<String,Long>>)
object WorldPrayer {
    val names = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")
    fun parse(data: JSONObject): PrayerDay {
        val zone = data.getJSONObject("meta").getString("timezone")
        require(TimeZone.getAvailableIDs().contains(zone)) { "Unknown prayer timezone" }
        val times = data.getJSONObject("timings")
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).apply { isLenient = false }
        val alarms = names.filter { it != "Sunrise" }.mapNotNull { name ->
            val raw = times.optString(name)
            try { name to (parser.parse(raw)?.time ?: return@mapNotNull null) } catch (_: Exception) { null }
        }
        val entries = names.map { name -> name to times.optString(name).let { if (it.contains('T')) it.substringAfter('T').take(5) else "Unavailable" } }
        return PrayerDay(data.getJSONObject("date").getString("readable"), zone, entries, alarms)
    }
    suspend fun fetch(context: Context): PrayerDay {
        val prefs = PrayerPreferences.prefs(context)
        val city = prefs.getString("city", "")!!.trim()
        val country = prefs.getString("country", "PK")!!
        require(city.isNotBlank()) { "Select your country and enter your city first." }
        val signature = "$city|$country|${prefs.getInt("method",1)}|${prefs.getInt("school",1)}|${prefs.getInt("latitude",3)}"
        fun url(date: String? = null): String {
            val base = "https://api.aladhan.com/v1/timingsByCity" + (date?.let { "/$it" } ?: "")
            return base.toHttpUrl().newBuilder().addQueryParameter("city", city).addQueryParameter("country", country)
                .addQueryParameter("method", prefs.getInt("method",1).toString())
                .addQueryParameter("school", prefs.getInt("school",1).toString())
                .addQueryParameter("latitudeAdjustmentMethod", prefs.getInt("latitude",3).toString())
                .addQueryParameter("iso8601", "true").build().toString()
        }
        val first = ContentHttp.json(url())
        check(first.optInt("code") == 200) { "Prayer service could not find this city. Try the nearest larger city." }
        var todayData = first.getJSONObject("data")
        val zone = TimeZone.getTimeZone(todayData.getJSONObject("meta").getString("timezone"))
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US).apply { timeZone = zone }
        val today = Calendar.getInstance(zone)
        val actualDate = todayData.getJSONObject("date").getJSONObject("gregorian").getString("date")
        if (actualDate != dateFormat.format(today.time)) todayData = ContentHttp.json(url(dateFormat.format(today.time))).getJSONObject("data")
        val result = parse(todayData)
        // Discard a stale in-flight response if the user changes location/settings.
        val currentSignature = "${prefs.getString("city","")!!.trim()}|${prefs.getString("country","PK")}|${prefs.getInt("method",1)}|${prefs.getInt("school",1)}|${prefs.getInt("latitude",3)}"
        check(signature == currentSignature) { "Settings changed. Refresh timings." }
        val alarms = result.alarms.toMutableList()
        // Cache tomorrow as well, so alerts continue through midnight while offline.
        val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        try { alarms += parse(ContentHttp.json(url(dateFormat.format(tomorrow.time))).getJSONObject("data")).alarms }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { /* Today's valid times remain usable; worker will retry tomorrow. */ }
        val latestSignature = "${prefs.getString("city","")!!.trim()}|${prefs.getString("country","PK")}|${prefs.getInt("method",1)}|${prefs.getInt("school",1)}|${prefs.getInt("latitude",3)}"
        check(signature == latestSignature) { "Settings changed. Refresh timings." }
        prefs.edit().putString("last_day", todayData.toString()).putString("cache_signature", signature).apply()
        val serialized = JSONArray()
        alarms.forEach { (name, time) -> serialized.put(JSONObject().put("name",name).put("time",time)) }
        PrayerAlerts.replace(context, serialized)
        return result
    }
}
class PrayerRefreshWorker(context: Context, params: WorkerParameters): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (PrayerPreferences.prefs(applicationContext).getString("city", "").isNullOrBlank()) return Result.success()
        return try { WorldPrayer.fetch(applicationContext); Result.success() }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { Result.retry() }
    }
}
