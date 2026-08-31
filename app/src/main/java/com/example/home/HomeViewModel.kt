package com.example.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.remote.Content
import com.example.data.remote.GeminiNetworkModule
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.Part
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val _dailyReflection = MutableStateFlow<String?>(null)
    val dailyReflection: StateFlow<String?> = _dailyReflection.asStateFlow()

    private val _isLoadingReflection = MutableStateFlow(false)
    val isLoadingReflection: StateFlow<Boolean> = _isLoadingReflection.asStateFlow()

    init {
        generateDailyReflection()
    }

    private fun generateDailyReflection() {
        viewModelScope.launch {
            _isLoadingReflection.value = true
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    _dailyReflection.value = "Please configure your Gemini API Key in the settings to view your daily reflection."
                    _isLoadingReflection.value = false
                    return@launch
                }

                val currentDate = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
                val prompt = "Generate a short, inspiring personalized Islamic quote or lesson for today ($currentDate). Keep it concise (2-3 sentences max) and deeply reflective. Don't use heavy formatting, just text."

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = prompt)))
                    ),
                    systemInstruction = Content(
                        parts = listOf(Part(text = "You are a wise and comforting Islamic guide giving short daily reflections."))
                    )
                )

                val response = GeminiNetworkModule.api.generateContent(apiKey, request)
                val reflectionText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                _dailyReflection.value = reflectionText ?: "May peace and blessings be upon you today."
            } catch (e: Exception) {
                e.printStackTrace()
                _dailyReflection.value = "Could not load today's reflection. Please check your internet connection."
            } finally {
                _isLoadingReflection.value = false
            }
        }
    }
}
