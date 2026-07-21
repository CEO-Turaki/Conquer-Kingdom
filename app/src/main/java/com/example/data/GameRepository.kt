package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameHistoryDao: GameHistoryDao) {
    val allHistory: Flow<List<GameHistory>> = gameHistoryDao.getAllHistory()

    suspend fun insertGame(game: GameHistory) {
        gameHistoryDao.insertGame(game)
    }

    suspend fun clearAllHistory() {
        gameHistoryDao.clearAllHistory()
    }
}
