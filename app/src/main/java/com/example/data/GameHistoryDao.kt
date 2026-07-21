package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameHistoryDao {
    @Query("SELECT * FROM game_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<GameHistory>>

    @Insert
    suspend fun insertGame(game: GameHistory)

    @Query("DELETE FROM game_history")
    suspend fun clearAllHistory()
}
