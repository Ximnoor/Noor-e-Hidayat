package com.example.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private val _fajrEnabled = MutableStateFlow(prefs.getBoolean("fajr_enabled", true))
    val fajrEnabled: StateFlow<Boolean> = _fajrEnabled.asStateFlow()

    private val _dhuhrEnabled = MutableStateFlow(prefs.getBoolean("dhuhr_enabled", true))
    val dhuhrEnabled: StateFlow<Boolean> = _dhuhrEnabled.asStateFlow()

    private val _asrEnabled = MutableStateFlow(prefs.getBoolean("asr_enabled", true))
    val asrEnabled: StateFlow<Boolean> = _asrEnabled.asStateFlow()

    private val _maghribEnabled = MutableStateFlow(prefs.getBoolean("maghrib_enabled", true))
    val maghribEnabled: StateFlow<Boolean> = _maghribEnabled.asStateFlow()

    private val _ishaEnabled = MutableStateFlow(prefs.getBoolean("isha_enabled", true))
    val ishaEnabled: StateFlow<Boolean> = _ishaEnabled.asStateFlow()

    private val _azanSound = MutableStateFlow(prefs.getString("azan_sound", "Classic")?.takeIf { it in listOf("Classic", "Default", "Custom") } ?: "Classic")
    val azanSound: StateFlow<String> = _azanSound.asStateFlow()

    private val _appTheme = MutableStateFlow(prefs.getString("app_theme", "System Default") ?: "System Default")
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    fun togglePrayer(prayer: String, enabled: Boolean) {
        prefs.edit().putBoolean("${prayer.lowercase()}_enabled", enabled).apply()
        com.example.essentials.PrayerAlerts.restore(getApplication())
        when (prayer.lowercase()) {
            "fajr" -> _fajrEnabled.value = enabled
            "dhuhr" -> _dhuhrEnabled.value = enabled
            "asr" -> _asrEnabled.value = enabled
            "maghrib" -> _maghribEnabled.value = enabled
            "isha" -> _ishaEnabled.value = enabled
        }
    }

    fun setAzanSound(sound: String) {
        prefs.edit().putString("azan_sound", sound).apply()
        _azanSound.value = sound
    }

    fun setAppTheme(theme: String) {
        prefs.edit().putString("app_theme", theme).apply()
        _appTheme.value = theme
    }
}

