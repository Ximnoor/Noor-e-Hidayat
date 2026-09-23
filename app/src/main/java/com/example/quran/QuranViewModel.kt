package com.example.quran

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.QuranRepository
import com.example.essentials.ContentHttp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class Reciter(val name: String, val surahEdition: String, val ayahEdition: String)
class QuranViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        val reciters = listOf(
            Reciter("Mishary Rashid Alafasy", "ar.alafasy", "ar.alafasy"),
            Reciter("Abdul Basit • Murattal", "ar.abdulbasitmurattal", "ar.abdulbasitmurattal"),
            Reciter("Minshawi • Mujawwad", "ar.muhammadsiddiqalminshawimujawwad", "ar.minshawimujawwad"),
            Reciter("Saud Al-Shuraim", "ar.saudalshuraim", "ar.saoodshuraym"),
            Reciter("Muhammad Ayyoub", "ar.muhammadayyub", "ar.muhammadayyoub")
        )
    }
    private val prefs = application.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    private val repository = QuranRepository(AppDatabase.getDatabase(application).completedSurahDao())
    val completedSurahs = repository.completedSurahs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
    val reciter = MutableStateFlow(reciters.firstOrNull { it.surahEdition == prefs.getString("qari", "") } ?: reciters[0])
    val currentlyPlayingSurah = MutableStateFlow<Int?>(null)
    val currentlyPlayingAyah = MutableStateFlow<Int?>(null)
    val isPlaying = MutableStateFlow(false)
    val isLoadingAudio = MutableStateFlow(false)
    val playbackError = MutableStateFlow<String?>(null)
    private var player: MediaPlayer? = null
    private var loadJob: Job? = null
    private var generation = 0

    fun selectReciter(value: Reciter) {
        stop(); reciter.value = value
        prefs.edit().putString("qari", value.surahEdition).apply()
    }
    fun stop() {
        generation++; loadJob?.cancel(); player?.release(); player = null
        isPlaying.value = false; isLoadingAudio.value = false
        currentlyPlayingSurah.value = null; currentlyPlayingAyah.value = null
    }
    fun toggleSurahCompletion(surahNumber: Int, isCompleted: Boolean) {
        viewModelScope.launch { repository.toggleSurahCompletion(surahNumber, isCompleted) }
    }
    private suspend fun audioUrl(qari: Reciter, surah: Int, ayah: Int?): String {
        require(surah in 1..114)
        if (ayah == null) return "https://cdn.islamic.network/quran/audio-surah/128/${qari.surahEdition}/$surah.mp3"
        require(ayah in 1..SurahData.surahs[surah - 1].ayahs)
        return ContentHttp.json("https://api.alquran.cloud/v1/ayah/$surah:$ayah/${qari.ayahEdition}").getJSONObject("data").getString("audio").also {
            require(it.startsWith("https://")) { "An insecure audio URL was rejected." }
        }
    }
    fun downloadAudio(surahNumber: Int, ayahNumber: Int? = null) {
        val qari = reciter.value
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val url = audioUrl(qari, surahNumber, ayahNumber)
                val name = "${qari.surahEdition}_surah_${surahNumber}_${ayahNumber ?: "full"}.mp3"
                val request = DownloadManager.Request(Uri.parse(url))
                    .setTitle("${qari.name} — Surah $surahNumber")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_MUSIC, name)
                    .setAllowedOverRoaming(false)
                (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
                Toast.makeText(context, "Downloading to app audio storage", Toast.LENGTH_SHORT).show()
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { playbackError.value = "Download failed. Please retry with internet access." }
        }
    }
    fun playAudio(surahNumber: Int, ayahNumber: Int? = null) {
        if (currentlyPlayingSurah.value == surahNumber && currentlyPlayingAyah.value == ayahNumber && player != null) {
            if (isLoadingAudio.value) return
            try { if (player!!.isPlaying) player!!.pause() else player!!.start(); isPlaying.value = player!!.isPlaying }
            catch (_: IllegalStateException) { stop() }
            return
        }
        stop(); val token = generation; val qari = reciter.value
        currentlyPlayingSurah.value = surahNumber; currentlyPlayingAyah.value = ayahNumber
        isLoadingAudio.value = true; playbackError.value = null
        loadJob = viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val local = java.io.File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "${qari.surahEdition}_surah_${surahNumber}_${ayahNumber ?: "full"}.mp3")
                // Only completed DownloadManager downloads are eligible for offline playback.
                val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                var offline: String? = null
                manager.query(DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL))?.use { cursor ->
                    val uriColumn = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                    while (cursor.moveToNext()) {
                        val uri = cursor.getString(uriColumn)
                        if (Uri.parse(uri).path == local.absolutePath && local.length() > 0) offline = local.absolutePath
                    }
                }
                val url = offline ?: audioUrl(qari, surahNumber, ayahNumber)
                if (token != generation) return@launch
                val media = MediaPlayer(); player = media
                media.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                media.setOnPreparedListener { if (token == generation) { isLoadingAudio.value = false; it.start(); isPlaying.value = true } }
                media.setOnCompletionListener { if (token == generation) stop() }
                media.setOnErrorListener { _, _, _ -> if (token == generation) { stop(); playbackError.value = "Recitation unavailable. Check internet or select another Qari." }; true }
                media.setDataSource(url); media.prepareAsync()
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { if (token == generation) { stop(); playbackError.value = "Unable to play recitation. Please retry." } }
        }
    }
    override fun onCleared() { stop(); super.onCleared() }
}
