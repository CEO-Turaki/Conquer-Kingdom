package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.GameHistory
import java.text.SimpleDateFormat
import java.util.*

enum class ActiveScreen {
    MENU,
    PLAYING,
    STATS,
    RULES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(ActiveScreen.MENU) }
    val historyList by viewModel.gameHistory.collectAsState()

    // Base atmospheric background matching the image's gradient theme
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3F35), // Deep Teal Top
                        Color(0xFF2C594C), // Radiant Sand Mid-Teal
                        Color(0xFF0F2621)  // Rich Forest Bottom
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
            when (screen) {
                ActiveScreen.MENU -> {
                    MainMenuScreen(
                        onStartGame = { mode ->
                            viewModel.selectMode(mode)
                            currentScreen = ActiveScreen.PLAYING
                        },
                        onNavigateToRules = { currentScreen = ActiveScreen.RULES },
                        onNavigateToStats = { currentScreen = ActiveScreen.STATS }
                    )
                }
                ActiveScreen.PLAYING -> {
                    PlayingScreen(
                        viewModel = viewModel,
                        onBackToMenu = { currentScreen = ActiveScreen.MENU }
                    )
                }
                ActiveScreen.STATS -> {
                    StatsScreen(
                        history = historyList,
                        onClearStats = { viewModel.clearStats() },
                        onBack = { currentScreen = ActiveScreen.MENU }
                    )
                }
                ActiveScreen.RULES -> {
                    RulesScreen(
                        onBack = { currentScreen = ActiveScreen.MENU }
                    )
                }
            }
        }
    }
}

@Composable
fun MainMenuScreen(
    onStartGame: (GameMode) -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Title Section with gradient/shadow treatment
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text(
                text = "COIN CROSSING",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFF4C430), // Golden Accent
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("app_title")
            )
            
            Text(
                text = "The Ultimate Tactile Blockade Game",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Subtitle illustration placeholder (Tactile layout of nodes)
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(Color(0x15FFFFFF), RoundedCornerShape(100))
                .border(1.5.dp, Color(0x30FFFFFF), RoundedCornerShape(100)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini tactile visualization
                Box(modifier = Modifier.size(24.dp).background(Color(0xFFE53935), CircleShape))
                Box(modifier = Modifier.size(12.dp).background(Color(0xFFF4C430), CircleShape))
                Box(modifier = Modifier.size(24.dp).background(Color.White, CircleShape))
            }
        }

        // Action Buttons & Game Modes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Select Battle Mode",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Pass and play
            MenuButton(
                text = "Pass & Play (Local 2P)",
                icon = Icons.Rounded.People,
                accentColor = Color(0xFF4CAF50),
                onClick = { onStartGame(GameMode.LOCAL_2P) },
                testTag = "mode_local_btn"
            )

            // VS AI Easy
            MenuButton(
                text = "Vs AI (Easy)",
                icon = Icons.Rounded.Computer,
                accentColor = Color(0xFF81C784),
                onClick = { onStartGame(GameMode.VS_AI_EASY) },
                testTag = "mode_ai_easy_btn"
            )

            // VS AI Medium
            MenuButton(
                text = "Vs AI (Medium)",
                icon = Icons.Rounded.Computer,
                accentColor = Color(0xFFFFB74D),
                onClick = { onStartGame(GameMode.VS_AI_MEDIUM) },
                testTag = "mode_ai_medium_btn"
            )

            // VS AI Hard
            MenuButton(
                text = "Vs AI (Hard)",
                icon = Icons.Rounded.Computer,
                accentColor = Color(0xFFE57373),
                onClick = { onStartGame(GameMode.VS_AI_HARD) },
                testTag = "mode_ai_hard_btn"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rules & History
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedMenuButton(
                    text = "How to Play",
                    icon = Icons.Rounded.Info,
                    onClick = onNavigateToRules,
                    modifier = Modifier.weight(1f),
                    testTag = "rules_nav_btn"
                )
                OutlinedMenuButton(
                    text = "Stats & Logs",
                    icon = Icons.Rounded.History,
                    onClick = onNavigateToStats,
                    modifier = Modifier.weight(1f),
                    testTag = "stats_nav_btn"
                )
            }
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0x1AFFFFFF),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .testTag(testTag),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun OutlinedMenuButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp),
        border = ButtonDefaults.outlinedButtonBorder(true).run {
            BorderStroke(1.dp, Color(0x30FFFFFF))
        },
        modifier = modifier
            .height(48.dp)
            .testTag(testTag),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFF4C430),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PlayingScreen(
    viewModel: GameViewModel,
    onBackToMenu: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp > 600

    // Detect if we show Victory Dialog
    if (viewModel.isGameOver && viewModel.winner != null) {
        VictoryDialog(
            winner = viewModel.winner!!,
            moves = viewModel.movesCount,
            mode = viewModel.gameMode,
            onRestart = { viewModel.resetGame() },
            onMainMenu = {
                viewModel.resetGame()
                onBackToMenu()
            }
        )
    }

    if (isWideScreen) {
        // Adaptive side-by-side design for tablet/landscape layouts
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left control panel
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                TopActionBar(
                    onBack = onBackToMenu,
                    mode = viewModel.gameMode,
                    moves = viewModel.movesCount
                )

                TurnIndicator(
                    activePlayer = viewModel.activePlayer,
                    isThinking = viewModel.aiIsThinking
                )

                GameControls(
                    onUndo = { viewModel.undoLastMove() },
                    onReset = { viewModel.resetGame() },
                    enableUndo = viewModel.movesCount > 0
                )
            }

            // Right board viewport
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                GameBoard(
                    board = viewModel.board,
                    selectedPosition = viewModel.selectedPosition,
                    validMoves = viewModel.validMovesForSelected,
                    activePlayer = viewModel.activePlayer,
                    onNodeSelected = { viewModel.selectPosition(it) },
                    isThinking = viewModel.aiIsThinking,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
        }
    } else {
        // Mobile-optimized vertical stack layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopActionBar(
                onBack = onBackToMenu,
                mode = viewModel.gameMode,
                moves = viewModel.movesCount
            )

            TurnIndicator(
                activePlayer = viewModel.activePlayer,
                isThinking = viewModel.aiIsThinking
            )

            // Game Board occupies central stage
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                GameBoard(
                    board = viewModel.board,
                    selectedPosition = viewModel.selectedPosition,
                    validMoves = viewModel.validMovesForSelected,
                    activePlayer = viewModel.activePlayer,
                    onNodeSelected = { viewModel.selectPosition(it) },
                    isThinking = viewModel.aiIsThinking
                )
            }

            GameControls(
                onUndo = { viewModel.undoLastMove() },
                onReset = { viewModel.resetGame() },
                enableUndo = viewModel.movesCount > 0
            )
        }
    }
}

@Composable
fun TopActionBar(
    onBack: () -> Unit,
    mode: GameMode,
    moves: Int
) {
    val modeText = when (mode) {
        GameMode.LOCAL_2P -> "Local 2P"
        GameMode.VS_AI_EASY -> "Vs AI (Easy)"
        GameMode.VS_AI_MEDIUM -> "Vs AI (Medium)"
        GameMode.VS_AI_HARD -> "Vs AI (Hard)"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .background(Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(12.dp))
                .testTag("back_btn")
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Back to Menu",
                tint = Color.White
            )
        }

        // Battle Mode Badge
        Box(
            modifier = Modifier
                .background(Color(0x25F4C430), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFF4C430).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = modeText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF4C430)
            )
        }

        // Moves Counter Badge
        Box(
            modifier = Modifier
                .background(Color(0x15FFFFFF), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0x10FFFFFF), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Moves: $moves",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun TurnIndicator(
    activePlayer: Player,
    isThinking: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_turn")
    val alphaScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0x12FFFFFF)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Mini tactile indicator
            val coinColor = if (activePlayer == Player.RED) Color(0xFFFF3B30) else Color.White
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(coinColor, CircleShape)
                    .border(1.5.dp, if (activePlayer == Player.RED) Color(0xFF900000) else Color(0xFFCCCCCC), CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = when {
                    isThinking -> "AI is plotting..."
                    activePlayer == Player.RED -> "Red's Turn"
                    else -> "White's Turn"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .graphicsLayer(alpha = alphaScale)
                    .testTag("turn_indicator")
            )
        }
    }
}

@Composable
fun GameControls(
    onUndo: () -> Unit,
    onReset: () -> Unit,
    enableUndo: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Reset Button
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD32F2F),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("reset_btn")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Restart", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Undo Button
        Button(
            onClick = onUndo,
            enabled = enableUndo,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0x20FFFFFF),
                contentColor = Color.White,
                disabledContainerColor = Color(0x05FFFFFF),
                disabledContentColor = Color.White.copy(alpha = 0.25f)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .border(
                    width = 1.dp,
                    color = if (enableUndo) Color(0x25FFFFFF) else Color(0x05FFFFFF),
                    shape = RoundedCornerShape(14.dp)
                )
                .testTag("undo_btn")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Undo,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Undo Move", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatsScreen(
    history: List<GameHistory>,
    onClearStats: () -> Unit,
    onBack: () -> Unit
) {
    val totalGames = history.size
    val redWins = history.count { it.winner == "Red" }
    val whiteWins = history.count { it.winner == "White" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(12.dp))
                    .testTag("stats_back_btn")
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Battle Logs",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            IconButton(
                onClick = onClearStats,
                enabled = history.isNotEmpty(),
                modifier = Modifier
                    .background(
                        if (history.isNotEmpty()) Color(0x1AD32F2F) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        if (history.isNotEmpty()) Color(0x25D32F2F) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .testTag("clear_stats_btn")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Clear Statistics",
                    tint = if (history.isNotEmpty()) Color(0xFFEF5350) else Color.White.copy(alpha = 0.2f)
                )
            }
        }

        // Summary Scorecard Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScoreCard(
                title = "Total Matches",
                value = totalGames.toString(),
                color = Color(0xFFF4C430),
                modifier = Modifier.weight(1f)
            )
            ScoreCard(
                title = "White (Bottom) Wins",
                value = whiteWins.toString(),
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            ScoreCard(
                title = "Red (Top) Wins",
                value = redWins.toString(),
                color = Color(0xFFE53935),
                modifier = Modifier.weight(1f)
            )
        }

        // History Log List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0x10FFFFFF), RoundedCornerShape(20.dp))
                .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            if (history.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matches played yet.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(history) { log ->
                        HistoryRow(log)
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0x15FFFFFF)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.border(1.dp, Color(0x10FFFFFF), RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HistoryRow(log: GameHistory) {
    val dateStr = remember(log.timestamp) {
        val date = Date(log.timestamp)
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(date)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0x0AFFFFFF)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x08FFFFFF), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = log.mode,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .background(
                            if (log.winner == "White") Color.White.copy(alpha = 0.15f) else Color(0x25FF5252),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Winner: ${log.winner}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (log.winner == "White") Color.White else Color(0xFFFF5252)
                    )
                }
                Text(
                    text = "${log.movesCount} moves",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun RulesScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(12.dp))
                    .testTag("rules_back_btn")
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "How to Play",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        // Scrollable Rules breakdown
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0x10FFFFFF), RoundedCornerShape(20.dp))
                .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            item {
                RuleSection(
                    title = "1. Setup & Goal",
                    content = "Each player gets 3 coins placed on their side of the bottleneck board. Red starts on the top row, and White starts on the bottom row. Your goal is to move all 3 of your coins into the opponent's starting row."
                )
            }
            item {
                RuleSection(
                    title = "2. Turn & Movements",
                    content = "On your turn, tap one of your coins to view adjacent valid destinations. Tap a highlighted node to move there. Coins can travel along any connected vertical or horizontal lines to adjacent empty spots. No jumping or capturing is allowed."
                )
            }
            item {
                RuleSection(
                    title = "3. The Bottleneck Spine",
                    content = "The board narrows down in the center to a single bottleneck vertical spine connecting the top and bottom halves. Managing, blocking, or navigating through this central channel is the key to victory!"
                )
            }
            item {
                RuleSection(
                    title = "4. Win Conditions",
                    content = "There are two ways to win the game:\n" +
                            "• CROSSING GOAL: Move all 3 of your coins onto the opponent's starting row.\n" +
                            "• BLOCKADE VICTORY: Completely trap the opponent's coins so they have no legal moves on their turn."
                )
            }
        }
    }
}

@Composable
fun RuleSection(
    title: String,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF4C430)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = content,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f),
            lineHeight = 20.sp
        )
    }
}

@Composable
fun VictoryDialog(
    winner: Player,
    moves: Int,
    mode: GameMode,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E332E)
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFF4C430).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .testTag("victory_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Crown / Trophy symbol
                Text(
                    text = "🏆",
                    fontSize = 64.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = if (winner == Player.RED) "RED WINS!" else "WHITE WINS!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = if (winner == Player.RED) Color(0xFFEF5350) else Color.White,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "A tactical masterclass!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Victory Statistics Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x12FFFFFF)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x08FFFFFF), RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Battle Mode",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                text = when (mode) {
                                    GameMode.LOCAL_2P -> "Local 2-Player"
                                    GameMode.VS_AI_EASY -> "Vs AI (Easy)"
                                    GameMode.VS_AI_MEDIUM -> "Vs AI (Medium)"
                                    GameMode.VS_AI_HARD -> "Vs AI (Hard)"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Moves",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "$moves moves",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF4C430)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF4C430),
                        contentColor = Color(0xFF0F2621)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("victory_play_again_btn")
                ) {
                    Text(
                        text = "Play Again",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onMainMenu,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color(0x30FFFFFF)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("victory_menu_btn")
                ) {
                    Text(
                        text = "Main Menu",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
