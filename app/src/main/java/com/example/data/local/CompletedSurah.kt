package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completed_surahs")
data class CompletedSurah(
    @PrimaryKey val surahNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)
