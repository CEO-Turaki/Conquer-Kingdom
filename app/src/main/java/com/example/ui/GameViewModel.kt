package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameDatabase
import com.example.data.GameHistory
import com.example.data.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

enum class Player {
    RED,    // Top player (starts at row 0)
    WHITE   // Bottom player (starts at row 3)
}

enum class GameMode {
    LOCAL_2P,
    VS_AI_EASY,
    VS_AI_MEDIUM,
    VS_AI_HARD
}

data class Position(val row: Int, val col: Int)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository

    init {
        val database = GameDatabase.getDatabase(application)
        repository = GameRepository(database.gameHistoryDao())
    }

    val gameHistory: StateFlow<List<GameHistory>> = repository.allHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Game state properties
    var gameMode by mutableStateOf(GameMode.LOCAL_2P)
        private set

    var activePlayer by mutableStateOf(Player.WHITE) // White starts by default (bottom)
        private set

    var board by mutableStateOf(initialBoard())
        private set

    var selectedPosition by mutableStateOf<Position?>(null)
        private set

    var validMovesForSelected by mutableStateOf<List<Position>>(emptyList())
        private set

    var winner by mutableStateOf<Player?>(null)
        private set

    var isGameOver by mutableStateOf(false)
        private set

    var movesCount by mutableStateOf(0)
        private set

    var aiIsThinking by mutableStateOf(false)
        private set

    // Move history for Undo stack (holds copies of board states)
    private val boardHistory = mutableListOf<Map<Position, Player?>>()
    private val playerHistory = mutableListOf<Player>()

    // Core Board Connection Map
    companion object {
        val boardConnections = listOf(
            // Vertical top connections
            Pair(Position(0, 0), Position(1, 0)),
            Pair(Position(0, 1), Position(1, 1)),
            Pair(Position(0, 2), Position(1, 2)),
            
            // Horizontal row 1
            Pair(Position(1, 0), Position(1, 1)),
            Pair(Position(1, 1), Position(1, 2)),
            
            // Bottleneck Central Spine
            Pair(Position(1, 1), Position(2, 1)),
            
            // Horizontal row 2
            Pair(Position(2, 0), Position(2, 1)),
            Pair(Position(2, 1), Position(2, 2)),
            
            // Vertical bottom connections
            Pair(Position(2, 0), Position(3, 0)),
            Pair(Position(2, 1), Position(3, 1)),
            Pair(Position(2, 2), Position(3, 2))
        )

        fun getAdjacentPositions(pos: Position): List<Position> {
            val adjacent = mutableListOf<Position>()
            for (conn in boardConnections) {
                if (conn.first == pos) adjacent.add(conn.second)
                else if (conn.second == pos) adjacent.add(conn.first)
            }
            return adjacent
        }

        private fun initialBoard(): Map<Position, Player?> {
            val map = mutableMapOf<Position, Player?>()
            // Initialize all 12 positions
            for (r in 0..3) {
                for (c in 0..2) {
                    val pos = Position(r, c)
                    map[pos] = when (r) {
                        0 -> Player.RED    // Red starts at row 0
                        3 -> Player.WHITE  // White starts at row 3
                        else -> null
                    }
                }
            }
            return map
        }
    }

    fun selectMode(mode: GameMode) {
        gameMode = mode
        resetGame()
    }

    fun resetGame() {
        board = initialBoard()
        activePlayer = Player.WHITE // Bottom (White) always starts
        selectedPosition = null
        validMovesForSelected = emptyList()
        winner = null
        isGameOver = false
        movesCount = 0
        aiIsThinking = false
        boardHistory.clear()
        playerHistory.clear()
    }

    fun selectPosition(pos: Position) {
        if (isGameOver || aiIsThinking) return

        val occupant = board[pos]
        if (occupant == activePlayer) {
            // Select own coin
            selectedPosition = pos
            validMovesForSelected = getValidMoves(pos, board)
        } else if (selectedPosition != null && pos in validMovesForSelected) {
            // Move selected coin to empty adjacent node
            executeMove(selectedPosition!!, pos)
        } else {
            // Tap empty or opponent node -> clear selection
            selectedPosition = null
            validMovesForSelected = emptyList()
        }
    }

    private fun getValidMoves(from: Position, currentBoard: Map<Position, Player?>): List<Position> {
        val adjacent = getAdjacentPositions(from)
        // A move is valid if the destination is empty
        return adjacent.filter { currentBoard[it] == null }
    }

    private fun executeMove(from: Position, to: Position) {
        // Save history for Undo
        boardHistory.add(board.toMap())
        playerHistory.add(activePlayer)

        // Update board
        val newBoard = board.toMutableMap()
        newBoard[to] = board[from]
        newBoard[from] = null
        board = newBoard

        movesCount++
        selectedPosition = null
        validMovesForSelected = emptyList()

        // Check Win Condition
        if (checkWinCondition(activePlayer, board)) {
            declareWinner(activePlayer)
            return
        }

        // Switch turn
        activePlayer = if (activePlayer == Player.WHITE) Player.RED else Player.WHITE

        // Check if the next player is blocked (has no legal moves)
        if (isPlayerBlocked(activePlayer, board)) {
            // Blocked player loses -> opponent wins
            val otherPlayer = if (activePlayer == Player.WHITE) Player.RED else Player.WHITE
            declareWinner(otherPlayer)
            return
        }

        // If VS AI mode, trigger AI move
        if (!isGameOver && isAiTurn()) {
            triggerAiMove()
        }
    }

    private fun checkWinCondition(player: Player, currentBoard: Map<Position, Player?>): Boolean {
        // Red wins if all its coins are in Row 3 (White's starting row)
        // White wins if all its coins are in Row 0 (Red's starting row)
        val targetRow = if (player == Player.RED) 3 else 0
        return currentBoard.entries.count { it.key.row == targetRow && it.value == player } == 3
    }

    private fun isPlayerBlocked(player: Player, currentBoard: Map<Position, Player?>): Boolean {
        val playerCoins = currentBoard.filter { it.value == player }.keys
        for (coin in playerCoins) {
            if (getValidMoves(coin, currentBoard).isNotEmpty()) {
                return false
            }
        }
        return true
    }

    private fun declareWinner(player: Player) {
        winner = player
        isGameOver = true
        saveGameResult(player)
    }

    private fun saveGameResult(winnerPlayer: Player) {
        val modeStr = when (gameMode) {
            GameMode.LOCAL_2P -> "Local 2-Player"
            GameMode.VS_AI_EASY -> "Vs AI (Easy)"
            GameMode.VS_AI_MEDIUM -> "Vs AI (Medium)"
            GameMode.VS_AI_HARD -> "Vs AI (Hard)"
        }
        val winnerStr = if (winnerPlayer == Player.RED) "Red" else "White"
        
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertGame(
                GameHistory(
                    mode = modeStr,
                    winner = winnerStr,
                    movesCount = movesCount
                )
            )
        }
    }

    fun undoLastMove() {
        if (boardHistory.isEmpty() || aiIsThinking) return

        if (gameMode == GameMode.LOCAL_2P) {
            board = boardHistory.removeAt(boardHistory.size - 1)
            activePlayer = playerHistory.removeAt(playerHistory.size - 1)
            movesCount--
            winner = null
            isGameOver = false
            selectedPosition = null
            validMovesForSelected = emptyList()
        } else {
            // In VS AI mode, undoing needs to undo BOTH the AI's move and the player's last move
            // to return to the player's previous turn.
            if (boardHistory.size >= 2) {
                // Remove AI move
                boardHistory.removeAt(boardHistory.size - 1)
                playerHistory.removeAt(playerHistory.size - 1)
                
                // Restore Player board state
                board = boardHistory.removeAt(boardHistory.size - 1)
                activePlayer = playerHistory.removeAt(playerHistory.size - 1)
                
                movesCount -= 2
                winner = null
                isGameOver = false
                selectedPosition = null
                validMovesForSelected = emptyList()
            }
        }
    }

    fun clearStats() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllHistory()
        }
    }

    private fun isAiTurn(): Boolean {
        return when (gameMode) {
            GameMode.LOCAL_2P -> false
            else -> activePlayer == Player.RED // AI plays as Red (Top)
        }
    }

    private fun triggerAiMove() {
        aiIsThinking = true
        viewModelScope.launch {
            // Delay for natural-feeling pacing (800ms)
            delay(800)
            val aiMove = withContext(Dispatchers.Default) {
                calculateAiMove()
            }
            aiIsThinking = false
            if (aiMove != null) {
                executeMove(aiMove.first, aiMove.second)
            }
        }
    }

    private fun calculateAiMove(): Pair<Position, Position>? {
        val legalMoves = getAllLegalMoves(Player.RED, board)
        if (legalMoves.isEmpty()) return null

        return when (gameMode) {
            GameMode.VS_AI_EASY -> {
                // Pure random choice
                legalMoves.random()
            }
            GameMode.VS_AI_MEDIUM -> {
                // Tactical heuristic lookup
                calculateMediumMove(legalMoves)
            }
            GameMode.VS_AI_HARD -> {
                // Minimax search with Alpha-Beta Pruning (Depth 6)
                calculateHardMove()
            }
            else -> null
        }
    }

    private fun getAllLegalMoves(player: Player, currentBoard: Map<Position, Player?>): List<Pair<Position, Position>> {
        val moves = mutableListOf<Pair<Position, Position>>()
        val playerCoins = currentBoard.filter { it.value == player }.keys
        for (from in playerCoins) {
            val validDests = getValidMoves(from, currentBoard)
            for (to in validDests) {
                moves.add(Pair(from, to))
            }
        }
        return moves
    }

    private fun calculateMediumMove(legalMoves: List<Pair<Position, Position>>): Pair<Position, Position> {
        // 1. If any move results in an immediate win, do it!
        for (move in legalMoves) {
            val tempBoard = board.toMutableMap()
            tempBoard[move.second] = Player.RED
            tempBoard[move.first] = null
            if (checkWinCondition(Player.RED, tempBoard)) {
                return move
            }
        }

        // 2. If any opponent move results in an immediate win, block them if possible!
        val opponentMoves = getAllLegalMoves(Player.WHITE, board)
        val winningOpponentMoves = opponentMoves.filter { move ->
            val tempBoard = board.toMutableMap()
            tempBoard[move.second] = Player.WHITE
            tempBoard[move.first] = null
            checkWinCondition(Player.WHITE, tempBoard)
        }
        if (winningOpponentMoves.isNotEmpty()) {
            // See if we can occupy any of the destination positions they need
            val targetDestinations = winningOpponentMoves.map { it.second }.toSet()
            val blockingMoves = legalMoves.filter { it.second in targetDestinations }
            if (blockingMoves.isNotEmpty()) {
                return blockingMoves.random()
            }
        }

        // 3. Otherwise, prefer moving coins FORWARD (downwards for Red, towards row 3)
        val forwardMoves = legalMoves.filter { it.second.row > it.first.row }
        if (forwardMoves.isNotEmpty() && Random.nextFloat() < 0.75f) {
            return forwardMoves.random()
        }

        return legalMoves.random()
    }

    private fun calculateHardMove(): Pair<Position, Position> {
        val bestMove = minimaxAlphaBeta(
            board = board,
            depth = 6,
            alpha = Int.MIN_VALUE,
            beta = Int.MAX_VALUE,
            isMaximizing = true // Red is maximizing
        )
        return bestMove.second ?: getAllLegalMoves(Player.RED, board).random()
    }

    // Minimax search with alpha-beta pruning
    private fun minimaxAlphaBeta(
        board: Map<Position, Player?>,
        depth: Int,
        alpha: Int,
        beta: Int,
        isMaximizing: Boolean
    ): Pair<Int, Pair<Position, Position>?> {
        // Terminal state checks
        if (checkWinCondition(Player.RED, board)) {
            return Pair(10000 + depth, null) // Reward quicker wins
        }
        if (checkWinCondition(Player.WHITE, board)) {
            return Pair(-10000 - depth, null) // Penalize quicker losses
        }

        val redBlocked = isPlayerBlocked(Player.RED, board)
        val whiteBlocked = isPlayerBlocked(Player.WHITE, board)

        if (redBlocked && isMaximizing) {
            // Red has no moves -> Red loses
            return Pair(-10000, null)
        }
        if (whiteBlocked && !isMaximizing) {
            // White has no moves -> White loses
            return Pair(10000, null)
        }

        if (depth == 0) {
            return Pair(evaluateBoard(board), null)
        }

        var currentAlpha = alpha
        var currentBeta = beta

        if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            var bestMove: Pair<Position, Position>? = null
            val moves = getAllLegalMoves(Player.RED, board)
            
            // Order moves slightly to improve pruning (prefer forward movement)
            val sortedMoves = moves.sortedByDescending { it.second.row }

            for (move in sortedMoves) {
                val nextBoard = board.toMutableMap()
                nextBoard[move.second] = Player.RED
                nextBoard[move.first] = null

                val eval = minimaxAlphaBeta(nextBoard, depth - 1, currentAlpha, currentBeta, false).first
                if (eval > maxEval) {
                    maxEval = eval
                    bestMove = move
                }
                currentAlpha = maxOf(currentAlpha, eval)
                if (currentBeta <= currentAlpha) {
                    break // Beta prune
                }
            }
            return Pair(maxEval, bestMove)
        } else {
            var minEval = Int.MAX_VALUE
            var bestMove: Pair<Position, Position>? = null
            val moves = getAllLegalMoves(Player.WHITE, board)
            
            // Order moves for White (prefer moving up, which is row 0)
            val sortedMoves = moves.sortedBy { it.second.row }

            for (move in sortedMoves) {
                val nextBoard = board.toMutableMap()
                nextBoard[move.second] = Player.WHITE
                nextBoard[move.first] = null

                val eval = minimaxAlphaBeta(nextBoard, depth - 1, currentAlpha, currentBeta, true).first
                if (eval < minEval) {
                    minEval = eval
                    bestMove = move
                }
                currentBeta = minOf(currentBeta, eval)
                if (currentBeta <= currentAlpha) {
                    break // Alpha prune
                }
            }
            return Pair(minEval, bestMove)
        }
    }

    // Strategic Board Evaluator
    private fun evaluateBoard(board: Map<Position, Player?>): Int {
        var score = 0

        // 1. Progress Evaluation
        // Red is seeking to reach Row 3. Higher row is better for Red.
        // White is seeking to reach Row 0. Lower row is better for White.
        board.forEach { (pos, player) ->
            if (player == Player.RED) {
                score += when (pos.row) {
                    0 -> 0
                    1 -> 15
                    2 -> 45
                    3 -> 150 // Almost winning
                    else -> 0
                }
                // Reward center bottleneck control
                if (pos == Position(1, 1)) score += 30
                if (pos == Position(2, 1)) score += 25
            } else if (player == Player.WHITE) {
                score -= when (pos.row) {
                    3 -> 0
                    2 -> 15
                    1 -> 45
                    0 -> 150 // Almost winning
                    else -> 0
                }
                // Penalize opponent bottleneck control
                if (pos == Position(2, 1)) score -= 30
                if (pos == Position(1, 1)) score -= 25
            }
        }

        // 2. Mobility (Blockade) Evaluation
        // Reward states where we have more legal moves, and penalize if the opponent has fewer.
        val redMovesCount = getAllLegalMoves(Player.RED, board).size
        val whiteMovesCount = getAllLegalMoves(Player.WHITE, board).size
        score += redMovesCount * 5
        score -= whiteMovesCount * 5

        return score
    }
}
