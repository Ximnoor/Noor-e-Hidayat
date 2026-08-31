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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = QuranRepository(db.completedSurahDao())

    val completedSurahs = repository.completedSurahs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    private var mediaPlayer: MediaPlayer? = null
    
    private val _currentlyPlayingSurah = MutableStateFlow<Int?>(null)
    val currentlyPlayingSurah: StateFlow<Int?> = _currentlyPlayingSurah.asStateFlow()
    
    private val _currentlyPlayingAyah = MutableStateFlow<Int?>(null)
    val currentlyPlayingAyah: StateFlow<Int?> = _currentlyPlayingAyah.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _isLoadingAudio = MutableStateFlow(false)
    val isLoadingAudio: StateFlow<Boolean> = _isLoadingAudio.asStateFlow()

    fun toggleSurahCompletion(surahNumber: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleSurahCompletion(surahNumber, isCompleted)
        }
    }
    
    fun downloadAudio(surahNumber: Int, ayahNumber: Int? = null) {
        val context = getApplication<Application>()
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        
        val url: String
        val title: String
        val fileName: String
        
        if (ayahNumber != null) {
            var absolute = 0
            for (i in 0 until surahNumber - 1) {
                absolute += SurahData.surahs[i].ayahs
            }
            absolute += ayahNumber
            url = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/$absolute.mp3"
            title = "Surah $surahNumber - Ayah $ayahNumber"
            fileName = "surah_${surahNumber}_ayah_${ayahNumber}.mp3"
        } else {
            url = "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/$surahNumber.mp3"
            title = "Surah $surahNumber"
            fileName = "surah_${surahNumber}.mp3"
        }
        
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(title)
                .setDescription("Downloading Quran Recitation")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                
            downloadManager.enqueue(request)
            Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun playAudio(surahNumber: Int, ayahNumber: Int? = null) {
        val isSameTrack = _currentlyPlayingSurah.value == surahNumber && _currentlyPlayingAyah.value == ayahNumber
        
        if (isSameTrack && mediaPlayer != null) {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                _isPlaying.value = false
            } else {
                mediaPlayer?.start()
                _isPlaying.value = true
            }
            return
        }

        mediaPlayer?.release()
        _currentlyPlayingSurah.value = surahNumber
        _currentlyPlayingAyah.value = ayahNumber
        _isLoadingAudio.value = true
        _isPlaying.value = false

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            try {
                val url: String
                if (ayahNumber != null) {
                    var absolute = 0
                    for (i in 0 until surahNumber - 1) {
                        absolute += SurahData.surahs[i].ayahs
                    }
                    absolute += ayahNumber
                    url = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/$absolute.mp3"
                } else {
                    url = "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/$surahNumber.mp3"
                }
                
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener {
                    _isLoadingAudio.value = false
                    start()
                    _isPlaying.value = true
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentlyPlayingSurah.value = null
                    _currentlyPlayingAyah.value = null
                }
                setOnErrorListener { _, _, _ ->
                    _isLoadingAudio.value = false
                    _isPlaying.value = false
                    _currentlyPlayingSurah.value = null
                    _currentlyPlayingAyah.value = null
                    true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoadingAudio.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}


