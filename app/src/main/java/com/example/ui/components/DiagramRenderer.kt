package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun DiagramRenderer(
    diagramType: String,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val isDark = MaterialTheme.colorScheme.background == Slate900

    val bgColor = if (isDark) Slate800 else Slate100
    val strokeColor = if (isDark) Slate200 else Slate800
    val accentBlue = EduBlueLight
    val accentEmerald = EduEmeraldSuccess
    val accentAmber = EduAmberAccent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        when (diagramType) {
            "BINARY_TREE" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val nodeRadius = 18f

                    // Coordinates for nodes:
                    // Level 0: 50
                    val root = Offset(w * 0.5f, h * 0.2f)
                    // Level 1: 25, 75
                    val left1 = Offset(w * 0.28f, h * 0.5f)
                    val right1 = Offset(w * 0.72f, h * 0.5f)
                    // Level 2: 12, 37, 65, 88
                    val leaf1 = Offset(w * 0.16f, h * 0.8f)
                    val leaf2 = Offset(w * 0.38f, h * 0.8f)
                    val leaf3 = Offset(w * 0.62f, h * 0.8f)
                    val leaf4 = Offset(w * 0.84f, h * 0.8f)

                    // Draw connecting edges
                    drawLine(strokeColor.copy(alpha = 0.6f), root, left1, strokeWidth = 3f)
                    drawLine(strokeColor.copy(alpha = 0.6f), root, right1, strokeWidth = 3f)
                    drawLine(strokeColor.copy(alpha = 0.6f), left1, leaf1, strokeWidth = 3f)
                    drawLine(strokeColor.copy(alpha = 0.6f), left1, leaf2, strokeWidth = 3f)
                    drawLine(strokeColor.copy(alpha = 0.6f), right1, leaf3, strokeWidth = 3f)
                    drawLine(strokeColor.copy(alpha = 0.6f), right1, leaf4, strokeWidth = 3f)

                    val nodes = listOf(
                        Pair(root, "50"),
                        Pair(left1, "25"),
                        Pair(right1, "75"),
                        Pair(leaf1, "12"),
                        Pair(leaf2, "37"),
                        Pair(leaf3, "65"),
                        Pair(leaf4, "88")
                    )

                    for ((center, label) in nodes) {
                        drawCircle(accentBlue, radius = nodeRadius, center = center)
                        drawCircle(Color.White, radius = nodeRadius, center = center, style = Stroke(2f))

                        val textLayout = textMeasurer.measure(
                            text = label,
                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(center.x - textLayout.size.width / 2, center.y - textLayout.size.height / 2)
                        )
                    }
                }
            }
            "SYNAPSE" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Presynaptic Terminal (Upper Bulb)
                    val bulbPath = Path().apply {
                        moveTo(w * 0.25f, 0f)
                        lineTo(w * 0.25f, h * 0.25f)
                        cubicTo(w * 0.15f, h * 0.45f, w * 0.85f, h * 0.45f, w * 0.75f, h * 0.25f)
                        lineTo(w * 0.75f, 0f)
                        close()
                    }
                    drawPath(bulbPath, color = accentBlue.copy(alpha = 0.15f))
                    drawPath(bulbPath, color = accentBlue, style = Stroke(width = 3f))

                    // Ca2+ Channels & Influx
                    drawRoundRect(
                        color = accentAmber,
                        topLeft = Offset(w * 0.22f, h * 0.28f),
                        size = Size(14f, 24f),
                        cornerRadius = CornerRadius(4f)
                    )
                    drawRoundRect(
                        color = accentAmber,
                        topLeft = Offset(w * 0.75f, h * 0.28f),
                        size = Size(14f, 24f),
                        cornerRadius = CornerRadius(4f)
                    )

                    // Vesicles inside bulb
                    val vesicles = listOf(
                        Offset(w * 0.40f, h * 0.20f),
                        Offset(w * 0.55f, h * 0.22f),
                        Offset(w * 0.46f, h * 0.32f),
                        Offset(w * 0.35f, h * 0.34f)
                    )
                    for (v in vesicles) {
                        drawCircle(accentEmerald, radius = 9f, center = v)
                        drawCircle(Color.White, radius = 3f, center = v)
                    }

                    // Neurotransmitter particles in synaptic cleft
                    val cleftParticles = listOf(
                        Offset(w * 0.42f, h * 0.50f),
                        Offset(w * 0.50f, h * 0.52f),
                        Offset(w * 0.58f, h * 0.49f),
                        Offset(w * 0.35f, h * 0.53f),
                        Offset(w * 0.65f, h * 0.51f)
                    )
                    for (p in cleftParticles) {
                        drawCircle(accentEmerald, radius = 3.5f, center = p)
                    }

                    // Postsynaptic Membrane (Lower arc)
                    drawLine(
                        color = strokeColor,
                        start = Offset(w * 0.1f, h * 0.62f),
                        end = Offset(w * 0.9f, h * 0.62f),
                        strokeWidth = 4f
                    )

                    // Receptors
                    val receptors = listOf(w * 0.32f, w * 0.48f, w * 0.64f)
                    for (rx in receptors) {
                        drawRoundRect(
                            color = accentBlue,
                            topLeft = Offset(rx - 8f, h * 0.60f),
                            size = Size(16f, 16f),
                            cornerRadius = CornerRadius(4f)
                        )
                    }

                    // Labels
                    val preLabel = textMeasurer.measure(
                        "Presynaptic Axon Terminal",
                        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = strokeColor)
                    )
                    drawText(preLabel, topLeft = Offset(w * 0.32f, 8f))

                    val caLabel = textMeasurer.measure(
                        "Ca²⁺ Influx ➔",
                        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accentAmber)
                    )
                    drawText(caLabel, topLeft = Offset(12f, h * 0.3f))

                    val cleftLabel = textMeasurer.measure(
                        "Synaptic Cleft",
                        style = TextStyle(fontSize = 10.sp, color = strokeColor.copy(alpha = 0.8f))
                    )
                    drawText(cleftLabel, topLeft = Offset(w * 0.05f, h * 0.48f))

                    val postLabel = textMeasurer.measure(
                        "Postsynaptic Receptors",
                        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = strokeColor)
                    )
                    drawText(postLabel, topLeft = Offset(w * 0.34f, h * 0.75f))
                }
            }
            "LOGIC_GATE" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Input wires
                    // Input A: top wire
                    drawLine(accentEmerald, Offset(w * 0.15f, h * 0.35f), Offset(w * 0.35f, h * 0.35f), strokeWidth = 3f)
                    // Input B: bottom wire
                    drawLine(accentAmber, Offset(w * 0.15f, h * 0.65f), Offset(w * 0.35f, h * 0.65f), strokeWidth = 3f)

                    // XOR gate body outline
                    val xorPath = Path().apply {
                        moveTo(w * 0.35f, h * 0.25f)
                        cubicTo(w * 0.42f, h * 0.25f, w * 0.55f, h * 0.40f, w * 0.60f, h * 0.50f)
                        cubicTo(w * 0.55f, h * 0.60f, w * 0.42f, h * 0.75f, w * 0.35f, h * 0.75f)
                        cubicTo(w * 0.40f, h * 0.50f, w * 0.40f, h * 0.50f, w * 0.35f, h * 0.25f)
                        close()
                    }
                    drawPath(xorPath, color = accentBlue.copy(alpha = 0.25f))
                    drawPath(xorPath, color = accentBlue, style = Stroke(width = 3f))

                    // XOR back curved line
                    val xorBackLine = Path().apply {
                        moveTo(w * 0.30f, h * 0.25f)
                        cubicTo(w * 0.36f, h * 0.50f, w * 0.36f, h * 0.50f, w * 0.30f, h * 0.75f)
                    }
                    drawPath(xorBackLine, color = accentBlue, style = Stroke(width = 3f))

                    // Output wire to AND gate
                    drawLine(strokeColor, Offset(w * 0.60f, h * 0.50f), Offset(w * 0.75f, h * 0.50f), strokeWidth = 3f)

                    // Output marker
                    drawCircle(accentEmerald, radius = 6f, center = Offset(w * 0.85f, h * 0.50f))
                    drawLine(strokeColor, Offset(w * 0.75f, h * 0.50f), Offset(w * 0.85f, h * 0.50f), strokeWidth = 3f)

                    // Labels
                    val textA = textMeasurer.measure("Input A = 1", TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentEmerald))
                    drawText(textA, topLeft = Offset(12f, h * 0.30f))

                    val textB = textMeasurer.measure("Input B = 0", TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentAmber))
                    drawText(textB, topLeft = Offset(12f, h * 0.60f))

                    val textGate = textMeasurer.measure("XOR", TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentBlue))
                    drawText(textGate, topLeft = Offset(w * 0.43f, h * 0.45f))

                    val textOut = textMeasurer.measure("Output Y = ?", TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = strokeColor))
                    drawText(textOut, topLeft = Offset(w * 0.70f, h * 0.25f))
                }
            }
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Visual Scientific Diagram",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Educational Reference Chart ($diagramType)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
