package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletedSurahDao {
    @Query("SELECT * FROM completed_surahs")
    fun getAllCompletedSurahs(): Flow<List<CompletedSurah>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markSurahCompleted(surah: CompletedSurah)

    @Query("DELETE FROM completed_surahs WHERE surahNumber = :surahNumber")
    suspend fun markSurahUncompleted(surahNumber: Int)
}
