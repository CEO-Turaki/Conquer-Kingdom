package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2

@Composable
fun GameBoard(
    board: Map<Position, Player?>,
    selectedPosition: Position?,
    validMoves: List<Position>,
    activePlayer: Player,
    onNodeSelected: (Position) -> Unit,
    modifier: Modifier = Modifier,
    isThinking: Boolean = false
) {
    val density = LocalDensity.current
    val marginDp = 32.dp

    // Pulse animation for AI thinking or current active player
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(0.75f)
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2E5E52), // Lighter radial center emerald
                        Color(0xFF0F362D), // Rich emerald
                        Color(0xFF081C17)  // Deep dark edge
                    )
                )
            )
            .padding(16.dp)
            .testTag("game_board_container")
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val marginPx = with(density) { marginDp.toPx() }

        // 1. Draw Connection Paths on Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineColor = Color(0xFF111111) // Crisp black paths as in image
            val strokeWidth = 11f // Thick and tactile

            // Vertical connections (Top-half prongs)
            drawLine(lineColor, getOffset(Position(0, 0), widthPx, heightPx, marginPx), getOffset(Position(1, 0), widthPx, heightPx, marginPx), strokeWidth)
            drawLine(lineColor, getOffset(Position(0, 2), widthPx, heightPx, marginPx), getOffset(Position(1, 2), widthPx, heightPx, marginPx), strokeWidth)

            // Central vertical spine connecting Top to Bottom
            drawLine(lineColor, getOffset(Position(0, 1), widthPx, heightPx, marginPx), getOffset(Position(3, 1), widthPx, heightPx, marginPx), strokeWidth)

            // Vertical connections (Bottom-half prongs)
            drawLine(lineColor, getOffset(Position(2, 0), widthPx, heightPx, marginPx), getOffset(Position(3, 0), widthPx, heightPx, marginPx), strokeWidth)
            drawLine(lineColor, getOffset(Position(2, 2), widthPx, heightPx, marginPx), getOffset(Position(3, 2), widthPx, heightPx, marginPx), strokeWidth)

            // Horizontal row 1 (Middle-High Rail) with arrows pointing outward
            val b1 = getOffset(Position(1, 0), widthPx, heightPx, marginPx)
            val b2 = getOffset(Position(1, 1), widthPx, heightPx, marginPx)
            val b3 = getOffset(Position(1, 2), widthPx, heightPx, marginPx)
            drawArrow(b2, b1, lineColor, strokeWidth)
            drawArrow(b2, b3, lineColor, strokeWidth)

            // Horizontal row 2 (Middle-Low Rail) with arrows pointing outward
            val c1 = getOffset(Position(2, 0), widthPx, heightPx, marginPx)
            val c2 = getOffset(Position(2, 1), widthPx, heightPx, marginPx)
            val c3 = getOffset(Position(2, 2), widthPx, heightPx, marginPx)
            drawArrow(c2, c1, lineColor, strokeWidth)
            drawArrow(c2, c3, lineColor, strokeWidth)
        }

        // 2. Lay down interactive overlays at precise board nodes
        for (r in 0..3) {
            for (c in 0..2) {
                val pos = Position(r, c)
                val offset = getOffset(pos, widthPx, heightPx, marginPx)

                val xDp = with(density) { offset.x.toDp() }
                val yDp = with(density) { offset.y.toDp() }

                val isSelected = (selectedPosition == pos)
                val isValidDestination = (pos in validMoves)
                val occupant = board[pos]

                NodeItem(
                    position = pos,
                    occupant = occupant,
                    isSelected = isSelected,
                    isValidDestination = isValidDestination,
                    activePlayer = activePlayer,
                    pulseAlpha = pulseAlpha,
                    onSelect = { onNodeSelected(pos) },
                    modifier = Modifier
                        .offset(x = xDp - 30.dp, y = yDp - 30.dp) // Offset centered on point
                )
            }
        }
    }
}

@Composable
fun NodeItem(
    position: Position,
    occupant: Player?,
    isSelected: Boolean,
    isValidDestination: Boolean,
    activePlayer: Player,
    pulseAlpha: Float,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Bounce and Scale when Selected
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isSelected) 12.dp else 4.dp,
        label = "elevation"
    )

    Box(
        modifier = modifier
            .size(60.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom visual states handled below
                onClick = onSelect
            )
            .testTag("node_${position.row}_${position.col}"),
        contentAlignment = Alignment.Center
    ) {
        // 1. Underlay / Path anchor ring
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(0x30000000), CircleShape)
                .border(2.dp, Color(0xFF111111), CircleShape)
        )

        // 2. Pulse highlighting when it's a valid destination
        if (isValidDestination) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0x25F4C430), CircleShape)
                    .border(
                        width = 3.dp,
                        color = Color(0xFFF4C430).copy(alpha = pulseAlpha),
                        shape = CircleShape
                    )
            )
        }

        // 3. Tactile 3D Coin rendering
        if (occupant != null) {
            val coinColor = if (occupant == Player.RED) {
                // Red tactile coin gradient (Red with bright highlight)
                Brush.radialGradient(
                    colors = listOf(Color(0xFFFF5252), Color(0xFFFF0000), Color(0xFF900000)),
                    center = Offset(40f, 40f)
                )
            } else {
                // White tactile coin gradient (Silver-grey 3D look)
                Brush.radialGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFF94A3B8)),
                    center = Offset(40f, 40f)
                )
            }

            val borderColor = if (occupant == Player.RED) Color(0xFF5E0000) else Color(0xFF475569)

            Box(
                modifier = Modifier
                    .size(46.dp * scale)
                    .shadow(shadowElevation, CircleShape)
                    .background(coinColor, CircleShape)
                    .border(2.5.dp, borderColor, CircleShape)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            ) {
                // Tactile ridge (inner circle representing coin edge)
                Box(
                    modifier = Modifier
                        .size(32.dp * scale)
                        .align(Alignment.Center)
                        .border(1.2.dp, borderColor.copy(alpha = 0.4f), CircleShape)
                )
            }
        }
    }
}

// Maps a row-column coordinate to an Offset in Canvas pixels
private fun getOffset(pos: Position, width: Float, height: Float, margin: Float): Offset {
    val usableWidth = width - 2 * margin
    val usableHeight = height - 2 * margin

    // Column positioning (0 -> Left, 1 -> Center, 2 -> Right)
    val x = margin + pos.col * (usableWidth / 2f)

    // Row positioning (0 -> Top, 1 -> Middle High, 2 -> Middle Low, 3 -> Bottom)
    // Modeled faithfully to match the image's dumbbell/neck shape layout.
    val rowFraction = when (pos.row) {
        0 -> 0.0f
        1 -> 0.22f
        2 -> 0.78f
        3 -> 1.0f
        else -> 0.5f
    }
    val y = margin + rowFraction * usableHeight

    return Offset(x, y)
}

// Draws a connector line with precise arrow heads pointing at endpoints
private fun DrawScope.drawArrow(start: Offset, end: Offset, color: Color, strokeWidth: Float) {
    // Main connection rail
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Calculate Arrow direction
    val angle = atan2(end.y - start.y, end.x - start.x)
    val arrowLength = 26f
    val arrowAngle = Math.PI / 5.5 // Sharp and elegant 32 degree angle

    // Draw Arrow Head Path
    val path = Path().apply {
        moveTo(end.x, end.y)
        lineTo(
            (end.x - arrowLength * cos(angle - arrowAngle)).toFloat(),
            (end.y - arrowLength * sin(angle - arrowAngle)).toFloat()
        )
        moveTo(end.x, end.y)
        lineTo(
            (end.x - arrowLength * cos(angle + arrowAngle)).toFloat(),
            (end.y - arrowLength * sin(angle + arrowAngle)).toFloat()
        )
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth - 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}
