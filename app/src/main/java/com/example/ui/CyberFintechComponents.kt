package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.math.pow

/**
 * Adaptive Cyber Frosted Card.
 * Deep obsidian glass in dark mode, crisp elevated white with subtle slate stroke in light mode.
 */
@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 0.8.dp,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val resolvedBg = backgroundColor ?: if (isDark) CyberDarkSurface else CyberLightSurface
    val resolvedBorder = borderColor ?: if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)

    Surface(
        modifier = modifier,
        shape = shape,
        color = resolvedBg,
        border = BorderStroke(borderWidth, resolvedBorder),
        shadowElevation = if (isDark) 0.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * Top Net Worth / Portfolio Hero Card with overlapping dual glowing Bezier waves.
 */
@Composable
fun CyberNetWorthWaveCard(
    title: String = "Toplam Net Varlık",
    balance: Double,
    subtitle: String? = null,
    badgeText: String? = null,
    badgePositive: Boolean = true,
    isBalanceHidden: Boolean = false,
    onToggleVisibility: () -> Unit = {},
    footerContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }
    val glowBorder = if (isDark) CyberBorderGlow else MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
    val iconWellBg = if (isDark) Color(0xFF1A2338) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)

    CyberCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = glowBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isBalanceHidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = "Bakiyeyi Gizle veya Göster",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (!badgeText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (badgePositive) {
                        if (isDark) Color(0xFF0F3E2F) else Color(0xFFD1FAE5)
                    } else {
                        if (isDark) Color(0xFF4C151F) else Color(0xFFFEE2E2)
                    }
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (badgePositive) {
                            if (isDark) CyberNeonMint else Color(0xFF047857)
                        } else {
                            if (isDark) CyberNeonCoral else Color(0xFFB91C1C)
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(iconWellBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalanceWallet,
                        contentDescription = null,
                        tint = if (isDark) CyberNeonCyan else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isBalanceHidden) "•••••••• ₺" else format.format(balance),
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Dual Wave Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
        ) {
            val w = size.width
            val h = size.height

            val purplePath = Path().apply {
                moveTo(0f, h * 0.75f)
                cubicTo(w * 0.25f, h * 0.70f, w * 0.40f, h * 0.45f, w * 0.60f, h * 0.45f)
                cubicTo(w * 0.75f, h * 0.45f, w * 0.85f, h * 0.65f, w, h * 0.40f)
            }

            drawPath(
                path = purplePath,
                color = CyberNeonPurple.copy(alpha = if (isDark) 0.35f else 0.22f),
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                path = purplePath,
                color = CyberNeonPurple,
                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
            )

            val mintPath = Path().apply {
                moveTo(0f, h * 0.85f)
                cubicTo(w * 0.20f, h * 0.60f, w * 0.35f, h * 0.75f, w * 0.50f, h * 0.40f)
                cubicTo(w * 0.65f, h * 0.20f, w * 0.80f, h * 0.50f, w, h * 0.25f)
            }

            val fillPath = Path().apply {
                addPath(mintPath)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CyberNeonMint.copy(alpha = if (isDark) 0.32f else 0.22f),
                        CyberNeonMint.copy(alpha = 0.04f),
                        Color.Transparent
                    )
                )
            )

            drawPath(
                path = mintPath,
                color = CyberNeonMint.copy(alpha = if (isDark) 0.4f else 0.25f),
                style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                path = mintPath,
                color = if (isDark) CyberNeonMint else Color(0xFF059669),
                style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        if (footerContent != null) {
            Spacer(modifier = Modifier.height(10.dp))
            footerContent()
        }
    }
}

/**
 * Bi-Directional Income vs Expense Bar Chart ("Gelir & Gider Dengesi").
 * Income bars extend upward in cyan/mint gradient; expense bars extend downward in coral/red.
 */
@Composable
fun CyberIncomeExpenseBalanceCard(
    incomes: List<Double>,
    expenses: List<Double>,
    labels: List<String>,
    title: String = "Gelir & Gider Dengesi",
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val baselineColor = if (isDark) Color(0xFF26334D) else Color(0xFFCBD5E1)
    val incomeDot = if (isDark) CyberNeonMint else Color(0xFF059669)
    val expenseDot = if (isDark) CyberNeonCoral else Color(0xFFDC2626)

    val hasAnyData = remember(incomes, expenses) {
        incomes.any { it > 0.0 } || expenses.any { it > 0.0 }
    }

    CyberCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(incomeDot))
                    Text("Gelir", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(expenseDot))
                    Text("Gider", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (!hasAnyData) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Son aylara ait gelir veya gider hareketi bulunmuyor.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val maxVal = remember(incomes, expenses) {
                val m1 = incomes.maxOrNull() ?: 1.0
                val m2 = expenses.maxOrNull() ?: 1.0
                maxOf(m1, m2).coerceAtLeast(1.0)
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val w = size.width
                val h = size.height
                val centerY = h * 0.50f

                drawLine(
                    color = baselineColor,
                    start = Offset(0f, centerY),
                    end = Offset(w, centerY),
                    strokeWidth = 1.dp.toPx()
                )

                val count = maxOf(incomes.size, expenses.size)
                if (count == 0) return@Canvas

                val totalBarWidth = w / count
                val barW = (totalBarWidth * 0.42f).coerceAtMost(28.dp.toPx())

                for (i in 0 until count) {
                    val inc = incomes.getOrElse(i) { 0.0 }
                    val exp = expenses.getOrElse(i) { 0.0 }
                    val x = i * totalBarWidth + (totalBarWidth - barW) / 2f

                    if (inc > 0) {
                        val barH = ((inc / maxVal) * (centerY - 6.dp.toPx())).toFloat().coerceAtLeast(4.dp.toPx())
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(CyberNeonCyan, incomeDot),
                                startY = centerY - barH,
                                endY = centerY
                            ),
                            topLeft = Offset(x, centerY - barH),
                            size = Size(barW, barH),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    if (exp > 0) {
                        val barH = ((exp / maxVal) * (h - centerY - 6.dp.toPx())).toFloat().coerceAtLeast(4.dp.toPx())
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(expenseDot, Color(0xFFFF7A7A)),
                                startY = centerY,
                                endY = centerY + barH
                            ),
                            topLeft = Offset(x, centerY),
                            size = Size(barW, barH),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }

            if (labels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    labels.forEach { lbl ->
                        Text(
                            text = lbl.uppercase(java.util.Locale("tr")),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Concentric Ring Gauge Card ("Yatırım Gücü Oranı").
 */
@Composable
fun CyberInvestmentGaugeCard(
    percentage: Int,
    title: String = "Tasarruf Gücü",
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val outerTrack = if (isDark) Color(0xFF1B2338) else Color(0xFFE2E8F0)
    val innerTrack = if (isDark) Color(0xFF161E30) else Color(0xFFF1F5F9)
    val clampedPct = percentage.coerceIn(0, 100)

    CyberCard(modifier = modifier) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(78.dp)) {
                val strokeW = 7.dp.toPx()
                val radius = (size.minDimension - strokeW) / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                drawCircle(
                    color = outerTrack,
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeW)
                )

                drawCircle(
                    color = innerTrack,
                    radius = radius - strokeW - 3.dp.toPx(),
                    center = center,
                    style = Stroke(width = strokeW * 0.7f)
                )

                val sweepAngle = (clampedPct / 100f) * 360f
                if (sweepAngle > 0f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(CyberNeonCyan.copy(alpha = 0.35f), CyberNeonMint.copy(alpha = 0.35f))
                        ),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeW + 4.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(CyberNeonCyan, if (isDark) CyberNeonMint else Color(0xFF059669))
                        ),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }
            }

            Text(
                text = "%$clampedPct",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Shield Progress Bar Card ("Acil Durum Fonu" / Yedek Hedefi).
 */
@Composable
fun CyberEmergencyFundCard(
    currentAmount: Double,
    targetAmount: Double,
    title: String = "Yedek Güvence",
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val progress = if (targetAmount > 0.0) (currentAmount / targetAmount).coerceIn(0.0, 1.0).toFloat() else 0f
    val pct = (progress * 100).toInt()
    val trackColor = if (isDark) Color(0xFF1B2338) else Color(0xFFE2E8F0)
    val badgeBg = if (isDark) Color(0xFF1A2338) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }

    CyberCard(modifier = modifier) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(trackColor)
            ) {
                if (progress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(CyberNeonCyan, if (isDark) CyberNeonMint else Color(0xFF059669))
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = if (isDark) CyberNeonCyan else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (targetAmount > 0.0) "%$pct • ${format.format(currentAmount)}" else "Henüz birikim yok",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = if (targetAmount > 0.0) "Hedef: ${format.format(targetAmount)}" else "3 aylık gider güvencesi",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

data class CyberDonutSegment(
    val label: String,
    val amount: Double,
    val color: Color
)

/**
 * Reusable Glowing Donut Ring + Ranked Cyber Category Bars.
 */
@Composable
fun CyberGlowDonutContent(
    segments: List<CyberDonutSegment>,
    totalLabel: String = "TOPLAM",
    emptyMessage: String = "Bu dönemde kayıtlı işlem bulunmuyor.",
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }
    val nonZeroSegments = remember(segments) { segments.filter { it.amount > 0.0 } }
    val total = remember(nonZeroSegments) { nonZeroSegments.sumOf { it.amount } }
    val emptyTrackColor = if (isDark) Color(0xFF1E283E) else Color(0xFFE2E8F0)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (total <= 0.0 || nonZeroSegments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.PieChartOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = emptyMessage,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 16.dp.toPx()
                    val gapAngle = if (nonZeroSegments.size > 1) 5f else 0f
                    var curAngle = -90f

                    if (nonZeroSegments.size == 1) {
                        val seg = nonZeroSegments.first()
                        if (isDark) {
                            drawCircle(
                                color = seg.color.copy(alpha = 0.15f),
                                radius = (size.minDimension - strokeW) / 2f,
                                style = Stroke(width = strokeW + 5.dp.toPx())
                            )
                        }
                        drawCircle(
                            color = seg.color,
                            radius = (size.minDimension - strokeW) / 2f,
                            style = Stroke(width = strokeW)
                        )
                    } else {
                        nonZeroSegments.forEach { seg ->
                            val ratio = (seg.amount / total).toFloat()
                            val sweep = (ratio * 360f - gapAngle).coerceAtLeast(2f)

                            if (isDark) {
                                drawArc(
                                    color = seg.color.copy(alpha = 0.14f),
                                    startAngle = curAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = Stroke(width = strokeW + 4.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            drawArc(
                                color = seg.color,
                                startAngle = curAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                            curAngle += ratio * 360f
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = totalLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = format.format(total),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                nonZeroSegments.forEach { seg ->
                    val ratio = (seg.amount / total).toFloat().coerceIn(0f, 1f)
                    val pct = (ratio * 100).toInt()
                    CyberCategoryBarRow(
                        dotColor = seg.color,
                        categoryName = seg.label,
                        ratio = ratio,
                        percentage = pct,
                        amountFormatted = format.format(seg.amount),
                        trackColor = emptyTrackColor
                    )
                }
            }
        }
    }
}

@Composable
fun CyberCategoryBarRow(
    dotColor: Color,
    categoryName: String,
    ratio: Float,
    percentage: Int,
    amountFormatted: String,
    trackColor: Color = Color(0xFF1B2338)
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = categoryName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "%$percentage",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = dotColor,
                    softWrap = false,
                    maxLines = 1
                )
                Text(
                    text = amountFormatted,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    softWrap = false,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(ratio.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(dotColor, dotColor.copy(alpha = 0.72f))
                        )
                    )
            )
        }
    }
}

/**
 * Legacy-compatible CyberCategoryBarCard wrapped cleanly inside CyberCard.
 */
@Composable
fun CyberCategoryBarCard(
    dotColor: Color,
    categoryName: String,
    ratio: Float,
    amountFormatted: String
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val pct = (ratio.coerceIn(0f, 1f) * 100).toInt()
    CyberCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
        CyberCategoryBarRow(
            dotColor = dotColor,
            categoryName = categoryName,
            ratio = ratio,
            percentage = pct,
            amountFormatted = amountFormatted,
            trackColor = if (isDark) Color(0xFF1B2338) else Color(0xFFE2E8F0)
        )
    }
}

/**
 * Compound Interest Simulator Card ("Bileşik Getiri Simülatörü").
 * Fully theme-aware with clear labels, principal vs profit breakdown, and horizon selector.
 */
@Composable
fun CyberCompoundSimulatorCard(
    initialAmount: Double = 10000.0,
    monthlyAddition: Double = 5000.0,
    annualReturnRate: Double = 35.0,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var initialInput by remember { mutableDoubleStateOf(initialAmount) }
    var monthlyInput by remember { mutableDoubleStateOf(monthlyAddition) }
    var rateInput by remember { mutableDoubleStateOf(annualReturnRate) }
    var yearsHorizon by remember { mutableIntStateOf(1) }

    val totalMonths = yearsHorizon * 12
    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }
    val inactiveTrack = if (isDark) Color(0xFF1B2338) else Color(0xFFE2E8F0)
    val gridColor = if (isDark) Color(0xFF1E283E).copy(alpha = 0.7f) else Color(0xFFE2E8F0)
    val primaryNeon = if (isDark) CyberNeonMint else Color(0xFF059669)

    val monthlyRate = (rateInput / 100.0) / 12.0
    val monthsData = remember(initialInput, monthlyInput, rateInput, totalMonths) {
        val list = mutableListOf<Double>()
        var balance = initialInput
        for (m in 1..totalMonths) {
            balance = (balance + monthlyInput) * (1.0 + monthlyRate)
            list.add(balance)
        }
        list
    }
    val finalVal = monthsData.lastOrNull() ?: initialInput
    val totalInvested = initialInput + monthlyInput * totalMonths
    val compoundGain = (finalVal - totalInvested).coerceAtLeast(0.0)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. PROJECTION CARD ---
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (isDark) CyberBorderGlow else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$totalMonths Aylık Tahmini Büyüme",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Vade sonu beklenen portföy",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1, 3, 5).forEach { y ->
                        val sel = yearsHorizon == y
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { yearsHorizon = y },
                            color = if (sel) primaryNeon else inactiveTrack,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${y}Y",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = format.format(finalVal),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = primaryNeon
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Anapara: ${format.format(totalInvested)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Getiri: +${format.format(compoundGain)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberNeonCyan
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                val w = size.width
                val h = size.height
                val paddingX = 12.dp.toPx()
                val paddingY = 14.dp.toPx()
                val graphW = w - paddingX * 2
                val graphH = h - paddingY * 2

                listOf(0.2f, 0.55f, 0.9f).forEach { lvl ->
                    val y = paddingY + graphH * (1f - lvl)
                    drawLine(
                        color = gridColor,
                        start = Offset(paddingX, y),
                        end = Offset(w - paddingX, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val minVal = monthsData.minOrNull() ?: 0.0
                val maxV = (monthsData.maxOrNull() ?: 1.0).coerceAtLeast(minVal + 1.0)
                val denominator = (monthsData.size - 1).coerceAtLeast(1).toFloat()

                val curvePath = Path()
                val fillPath = Path()

                monthsData.forEachIndexed { index, value ->
                    val normX = paddingX + (index.toFloat() / denominator) * graphW
                    val normY = paddingY + graphH * (1f - ((value - minVal) / (maxV - minVal)).toFloat() * 0.85f - 0.08f)

                    if (index == 0) {
                        curvePath.moveTo(normX, normY)
                        fillPath.moveTo(normX, paddingY + graphH)
                        fillPath.lineTo(normX, normY)
                    } else {
                        val prevX = paddingX + ((index - 1).toFloat() / denominator) * graphW
                        val prevY = paddingY + graphH * (1f - ((monthsData[index - 1] - minVal) / (maxV - minVal)).toFloat() * 0.85f - 0.08f)
                        val midX = (prevX + normX) / 2f
                        curvePath.cubicTo(midX, prevY, midX, normY, normX, normY)
                        fillPath.cubicTo(midX, prevY, midX, normY, normX, normY)
                    }
                }

                fillPath.lineTo(w - paddingX, paddingY + graphH)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryNeon.copy(alpha = 0.28f),
                            primaryNeon.copy(alpha = 0.04f),
                            Color.Transparent
                        )
                    )
                )
                drawPath(
                    path = curvePath,
                    color = primaryNeon.copy(alpha = 0.35f),
                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    path = curvePath,
                    color = primaryNeon,
                    style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
                )

                val finalX = paddingX + graphW
                val finalY = paddingY + graphH * (1f - 0.85f - 0.08f)
                drawCircle(
                    color = primaryNeon.copy(alpha = 0.35f),
                    radius = 10.dp.toPx(),
                    center = Offset(finalX, finalY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.5.dp.toPx(),
                    center = Offset(finalX, finalY)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1. Ay", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${totalMonths / 2}. Ay", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$totalMonths. Ay", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = primaryNeon)
            }
        }

        // --- 2. CONTROLS CARD ---
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            // Initial capital slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Başlangıç Sermayesi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(format.format(initialInput), fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            }
            Slider(
                value = initialInput.toFloat(),
                onValueChange = { initialInput = (it / 1000).toInt() * 1000.0 },
                valueRange = 0f..500000f,
                colors = SliderDefaults.colors(
                    thumbColor = CyberNeonPurple,
                    activeTrackColor = CyberNeonPurple,
                    inactiveTrackColor = inactiveTrack
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Monthly amount slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Aylık Düzenli Yatırım", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(format.format(monthlyInput), fontSize = 16.sp, fontWeight = FontWeight.Black, color = primaryNeon)
            }
            Slider(
                value = monthlyInput.toFloat(),
                onValueChange = { monthlyInput = (it / 500).toInt() * 500.0 },
                valueRange = 500f..50000f,
                colors = SliderDefaults.colors(
                    thumbColor = primaryNeon,
                    activeTrackColor = primaryNeon,
                    inactiveTrackColor = inactiveTrack
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Annual rate slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Beklenen Yıllık Getiri", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%${rateInput.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = CyberNeonCyan)
            }
            Slider(
                value = rateInput.toFloat(),
                onValueChange = { rateInput = it.toInt().toDouble() },
                valueRange = 5f..80f,
                colors = SliderDefaults.colors(
                    thumbColor = CyberNeonCyan,
                    activeTrackColor = CyberNeonCyan,
                    inactiveTrackColor = inactiveTrack
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Asset Summary Card with Glowing Icon & Sparkline.
 */
@Composable
fun CyberAssetSparklineCard(
    title: String,
    amount: Double,
    changePercent: Double,
    icon: ImageVector,
    iconColor: Color,
    sparklineColor: Color,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }

    CyberCard(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = format.format(amount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (changePercent >= 0) {
                        if (isDark) Color(0xFF0F3E2F) else Color(0xFFD1FAE5)
                    } else {
                        if (isDark) Color(0xFF4C151F) else Color(0xFFFEE2E2)
                    }
                ) {
                    Text(
                        text = "${if (changePercent >= 0) "+" else ""}${String.format(java.util.Locale.US, "%.1f", changePercent)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (changePercent >= 0) {
                            if (isDark) CyberNeonMint else Color(0xFF047857)
                        } else {
                            if (isDark) CyberNeonCoral else Color(0xFFB91C1C)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Canvas(modifier = Modifier.size(width = 60.dp, height = 22.dp)) {
                    val w = size.width
                    val h = size.height

                    val path = Path().apply {
                        if (changePercent >= 0) {
                            moveTo(0f, h * 0.75f)
                            cubicTo(w * 0.3f, h * 0.85f, w * 0.6f, h * 0.25f, w, h * 0.25f)
                        } else {
                            moveTo(0f, h * 0.25f)
                            cubicTo(w * 0.3f, h * 0.35f, w * 0.6f, h * 0.75f, w, h * 0.80f)
                        }
                    }

                    drawPath(
                        path = path,
                        color = sparklineColor.copy(alpha = 0.35f),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = path,
                        color = sparklineColor,
                        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
