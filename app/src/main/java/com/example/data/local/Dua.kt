package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "duas")
data class Dua(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val title: String,
    val arabicText: String,
    val translation: String,
    val reference: String,
    val isFavorite: Boolean = false
)
