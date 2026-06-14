package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MiniSpinWheel(size: Dp = 80.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Color(0xFF121212), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(size.value.dp * 0.05f)) {
            val colors = listOf(
                Color(0xFFFF5252), Color(0xFFFFD740),
                Color(0xFF448AFF), Color(0xFF69F0AE),
                Color(0xFFFF4081), Color(0xFF7C4DFF),
                Color(0xFF18FFFF), Color(0xFFE040FB)
            )
            val segments = colors.size
            val sweepAngle = 360f / segments
            
            // Draw Segments
            for (i in 0 until segments) {
                drawArc(
                    color = colors[i],
                    startAngle = (i * sweepAngle) - 90f,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = this.size
                )
            }
            
            // Outer Ring Border
            drawCircle(
                color = Color.Black,
                radius = this.size.width / 2,
                style = Stroke(width = (size.value * 0.08f).dp.toPx())
            )
            
            // Outer Decorative "Lights"
            val dotCount = 12
            for (i in 0 until dotCount) {
                val angle = (i * (360f / dotCount)) * (PI / 180).toFloat()
                val ringRad = this.size.width / 2
                drawCircle(
                    color = Color(0xFFFFEB3B),
                    radius = (size.value * 0.025f).dp.toPx(),
                    center = Offset(
                        this.size.width / 2 + ringRad * cos(angle),
                        this.size.height / 2 + ringRad * sin(angle)
                    )
                )
            }
            
            // Center Cap
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = (size.value * 0.15f).dp.toPx()
            )
            drawCircle(
                color = Color.White,
                radius = (size.value * 0.05f).dp.toPx()
            )
        }
        
        // Pointer at the top
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (size.value * 0.02f).dp)
                .size((size.value * 0.15f).dp)
                .background(Color.White, RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp))
        )
    }
}

@Composable
fun MiniScratchCard() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFD700), Color(0xFFFF8F00))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background patterns
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacing = 20.dp.toPx()
            for (x in 0..size.width.toInt() step spacing.toInt()) {
                for (y in 0..size.height.toInt() step spacing.toInt()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        radius = 2.dp.toPx(),
                        center = Offset(x.toFloat(), y.toFloat())
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.White.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CardGiftcard,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "SCRATCH",
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = 18.sp,
                letterSpacing = 2.sp
            )
            Text(
                "WIN BONUS",
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun SpecialEarningCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    bgColor: Color,
    onClick: () -> Unit
) {
    val backgroundModifier = when (title) {
        "Daily Spin" -> Modifier.background(
            Brush.verticalGradient(
                listOf(
                    Color(0xFFFFD700).copy(alpha = 0.05f),
                    Color(0xFFFFA000).copy(alpha = 0.1f)
                )
            )
        )
        "Scratch Card" -> Modifier.background(
            Brush.verticalGradient(
                listOf(
                    Color(0xFFFFD700).copy(alpha = 0.1f),
                    Color(0xFFFFA000).copy(alpha = 0.2f)
                )
            )
        )
        else -> Modifier.background(bgColor.copy(alpha = 0.05f))
    }

    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .then(backgroundModifier)
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                if (title == "Daily Spin") {
                    AsyncImage(
                        model = "https://res.cloudinary.com/dhlzcea1t/image/upload/v1781072992/h2u9oefhd5lsdjsezila.png",
                        contentDescription = "Daily Spin",
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(1.4f)
                            .offset(y = (-5).dp),
                        contentScale = ContentScale.Fit
                    )
                } else if (title == "Scratch Card") {
                    MiniScratchCard()
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = bgColor,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}
