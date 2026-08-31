package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DuaDao {
    @Query("SELECT * FROM duas")
    fun getAllDuas(): Flow<List<Dua>>

    @Query("SELECT * FROM duas WHERE category = :category")
    fun getDuasByCategory(category: String): Flow<List<Dua>>

    @Query("SELECT COUNT(*) FROM duas")
    suspend fun getDuaCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(duas: List<Dua>)

    @Query("UPDATE duas SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)
}
