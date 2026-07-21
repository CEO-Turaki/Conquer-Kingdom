package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_history")
data class GameHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String, // "Local 2P", "Vs AI (Easy)", "Vs AI (Medium)", "Vs AI (Hard)"
    val winner: String, // "Red" (Top), "White" (Bottom), "Draw"
    val movesCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
