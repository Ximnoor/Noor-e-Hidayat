package com.example.data.repository

import com.example.data.local.CompletedSurah
import com.example.data.local.CompletedSurahDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuranRepository(private val completedSurahDao: CompletedSurahDao) {
    
    val completedSurahs: Flow<Set<Int>> = completedSurahDao.getAllCompletedSurahs()
        .map { list -> list.map { it.surahNumber }.toSet() }

    suspend fun toggleSurahCompletion(surahNumber: Int, isCompleted: Boolean) {
        if (isCompleted) {
            completedSurahDao.markSurahCompleted(CompletedSurah(surahNumber))
        } else {
            completedSurahDao.markSurahUncompleted(surahNumber)
        }
    }
}
