package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Deterministic 29x29 UPI QR-style matrix renderer with sharp high-contrast
 * white backdrop and centered dark circular brand emblem matching the PhonePe QR card.
 */
@Composable
fun UpiQrMatrixCard(
    payload: String,
    modifier: Modifier = Modifier,
    moduleColor: Color = Color(0xFF0E0E12)
) {
    val matrix = remember(payload) { buildDeterministicQrMatrix(payload, size = 29) }

    Surface(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val n = matrix.size
                val cellSize = size.minDimension / n.toFloat()
                val radius = cellSize * 0.14f

                for (r in 0 until n) {
                    for (c in 0 until n) {
                        if (r in 11..17 && c in 11..17) continue
                        if (matrix[r][c]) {
                            drawRoundRect(
                                color = moduleColor,
                                topLeft = Offset(c * cellSize, r * cellSize),
                                size = Size(cellSize * 0.94f, cellSize * 0.94f),
                                cornerRadius = CornerRadius(radius, radius)
                            )
                        }
                    }
                }
            }

            // Center Circular Emblem matching PhonePe QR ('पे')
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF141418))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "पे",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun buildDeterministicQrMatrix(payload: String, size: Int): Array<BooleanArray> {
    val grid = Array(size) { BooleanArray(size) }
    val reserved = Array(size) { BooleanArray(size) }

    fun placeFinder(startR: Int, startC: Int) {
        for (dr in -1..7) {
            for (dc in -1..7) {
                val r = startR + dr
                val c = startC + dc
                if (r in 0 until size && c in 0 until size) {
                    reserved[r][c] = true
                    val inBox = dr in 0..6 && dc in 0..6
                    val onOuter = dr == 0 || dr == 6 || dc == 0 || dc == 6
                    val inCore = dr in 2..4 && dc in 2..4
                    grid[r][c] = inBox && (onOuter || inCore)
                }
            }
        }
    }

    placeFinder(0, 0)
    placeFinder(0, size - 7)
    placeFinder(size - 7, 0)

    for (i in 8 until size - 8) {
        reserved[6][i] = true
        reserved[i][6] = true
        grid[6][i] = i % 2 == 0
        grid[i][6] = i % 2 == 0
    }

    val alignCenter = size - 7
    for (dr in -2..2) {
        for (dc in -2..2) {
            val r = alignCenter + dr
            val c = alignCenter + dc
            reserved[r][c] = true
            grid[r][c] = (kotlin.math.abs(dr) == 2 || kotlin.math.abs(dc) == 2 || (dr == 0 && dc == 0))
        }
    }

    var seed = 0x811C9DC5L
    for (ch in payload) {
        seed = (seed xor ch.code.toLong()) * 0x01000193L
    }

    for (r in 0 until size) {
        for (c in 0 until size) {
            if (!reserved[r][c]) {
                seed = seed * 6364136223846793005L + 1442695040888963407L
                val bit = ((seed ushr 32) and 1L) == 1L
                val mask = ((r + c) % 2 == 0)
                grid[r][c] = bit xor mask
            }
        }
    }
    return grid
}
