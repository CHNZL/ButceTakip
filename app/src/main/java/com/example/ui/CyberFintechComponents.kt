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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.text.NumberFormat
import kotlin.math.pow

/**
 * Standard Cyber Frosted Card matching the reference screenshots.
 * Deep dark slate background, rounded corners (22.dp), subtle border stroke.
 */
@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    backgroundColor: Color = CyberDarkSurface,
    borderColor: Color = CyberBorderStroke,
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

/**
 * Top bar header matching reference screenshots with title, search, bell, and profile.
 */
@Composable
fun CyberScreenHeader(
    title: String,
    subtitle: String? = null,
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    profileAvatar: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8F9CAE),
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF131826))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Ara",
                    tint = Color(0xFF8F9CAE),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF131826))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = "Bildirimler",
                    tint = Color(0xFF8F9CAE),
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A2338))
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                if (profileAvatar != null) {
                    profileAvatar()
                } else {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = "Profil",
                        tint = Color(0xFF00E5A3),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Top Net Worth Card with overlapping dual glowing Bezier waves, as in Image 4.
 */
@Composable
fun CyberNetWorthWaveCard(
    title: String = "Toplam Net Varlık",
    balance: Double,
    currencySymbol: String = "₺",
    isBalanceHidden: Boolean = false,
    onToggleVisibility: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }

    CyberCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = CyberBorderGlow
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isBalanceHidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = null,
                        tint = Color(0xFF8F9CAE),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1A2338)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = CyberNeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isBalanceHidden) "••••••••" else format.format(balance),
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Dual Wave Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            val w = size.width
            val h = size.height

            // 1. Purple wave (Back wave)
            val purplePath = Path().apply {
                moveTo(0f, h * 0.75f)
                cubicTo(
                    w * 0.25f, h * 0.70f,
                    w * 0.40f, h * 0.45f,
                    w * 0.60f, h * 0.45f
                )
                cubicTo(
                    w * 0.75f, h * 0.45f,
                    w * 0.85f, h * 0.65f,
                    w, h * 0.40f
                )
            }

            // Soft glow for purple wave
            drawPath(
                path = purplePath,
                color = CyberNeonPurple.copy(alpha = 0.35f),
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
            // Crisp stroke for purple wave
            drawPath(
                path = purplePath,
                color = CyberNeonPurple,
                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
            )

            // 2. Mint/Cyan wave (Front wave with fill)
            val mintPath = Path().apply {
                moveTo(0f, h * 0.85f)
                cubicTo(
                    w * 0.20f, h * 0.60f,
                    w * 0.35f, h * 0.75f,
                    w * 0.50f, h * 0.40f
                )
                cubicTo(
                    w * 0.65f, h * 0.20f,
                    w * 0.80f, h * 0.50f,
                    w, h * 0.25f
                )
            }

            // Fill area under mint wave
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
                        CyberNeonMint.copy(alpha = 0.32f),
                        CyberNeonMint.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                )
            )

            // Soft glow for mint wave
            drawPath(
                path = mintPath,
                color = CyberNeonMint.copy(alpha = 0.4f),
                style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
            )
            // Crisp stroke for mint wave
            drawPath(
                path = mintPath,
                color = CyberNeonMint,
                style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Bi-Directional Income vs Expense Bar Chart, as in Image 4 ("Gelir & Gider Dengesi").
 * Income bars extend upward in cyan/mint gradient; expense bars extend downward in coral/red.
 */
@Composable
fun CyberIncomeExpenseBalanceCard(
    incomes: List<Double> = listOf(22000.0, 31000.0, 27000.0, 42000.0, 54000.0),
    expenses: List<Double> = listOf(14000.0, 23000.0, 29000.0, 18000.0, 26000.0),
    labels: List<String> = listOf("Şub", "Mar", "Nis", "May", "Haz"),
    modifier: Modifier = Modifier
) {
    CyberCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Gelir & Gider Dengesi",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(14.dp))

        val maxVal = remember(incomes, expenses) {
            val m1 = incomes.maxOrNull() ?: 1.0
            val m2 = expenses.maxOrNull() ?: 1.0
            maxOf(m1, m2).coerceAtLeast(1.0)
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
        ) {
            val w = size.width
            val h = size.height
            val centerY = h * 0.52f

            // Baseline
            drawLine(
                color = Color(0xFF26334D),
                start = Offset(0f, centerY),
                end = Offset(w, centerY),
                strokeWidth = 1.dp.toPx()
            )

            val count = maxOf(incomes.size, expenses.size)
            if (count == 0) return@Canvas

            val totalBarWidth = w / count
            val barW = totalBarWidth * 0.45f

            for (i in 0 until count) {
                val inc = incomes.getOrElse(i) { 0.0 }
                val exp = expenses.getOrElse(i) { 0.0 }
                val x = i * totalBarWidth + (totalBarWidth - barW) / 2f

                // Positive bar (Income) - Goes UP from centerY
                if (inc > 0) {
                    val barH = ((inc / maxVal) * (centerY - 6.dp.toPx())).toFloat().coerceAtLeast(4.dp.toPx())
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(CyberNeonCyan, CyberNeonMint),
                            startY = centerY - barH,
                            endY = centerY
                        ),
                        topLeft = Offset(x, centerY - barH),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }

                // Negative bar (Expense) - Goes DOWN from centerY
                if (exp > 0) {
                    val barH = ((exp / maxVal) * (h - centerY - 6.dp.toPx())).toFloat().coerceAtLeast(4.dp.toPx())
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(CyberNeonCoral, Color(0xFFFF7A7A)),
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
    }
}

/**
 * Concentric Ring Gauge Card ("Yatırım Gücü Oranı"), as in Image 4.
 */
@Composable
fun CyberInvestmentGaugeCard(
    percentage: Int = 42,
    title: String = "Yatırım Gücü Oranı",
    modifier: Modifier = Modifier
) {
    CyberCard(modifier = modifier) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(80.dp)) {
                val strokeW = 7.dp.toPx()
                val radius = (size.minDimension - strokeW) / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Background full track (dark slate)
                drawCircle(
                    color = Color(0xFF1B2338),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeW)
                )

                // Inner track for concentric aesthetic
                drawCircle(
                    color = Color(0xFF161E30),
                    radius = radius - strokeW - 3.dp.toPx(),
                    center = center,
                    style = Stroke(width = strokeW * 0.7f)
                )

                // Outer Glowing Arc
                val sweepAngle = (percentage.coerceIn(0, 100) / 100f) * 360f

                // Outer glow
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(CyberNeonCyan.copy(alpha = 0.4f), CyberNeonMint.copy(alpha = 0.4f))
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeW + 4.dp.toPx(), cap = StrokeCap.Round)
                )

                // Crisp Arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(CyberNeonCyan, CyberNeonMint)
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }

            Text(
                text = "%$percentage",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

/**
 * Shield Progress Bar Card ("Acil Durum Fonu"), as in Image 4.
 */
@Composable
fun CyberEmergencyFundCard(
    currentAmount: Double = 65000.0,
    targetAmount: Double = 100000.0,
    title: String = "Acil Durum Fonu",
    modifier: Modifier = Modifier
) {
    val progress = (currentAmount / targetAmount.coerceAtLeast(1.0)).coerceIn(0.0, 1.0).toFloat()

    CyberCard(modifier = modifier) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Capsule progress track
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1B2338))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(CyberNeonCyan, CyberNeonMint)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Shield icon badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A2338)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = CyberNeonCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF161E30))
        )
    }
}

/**
 * Glowing Tri-Color Donut Chart with Category Bars, as in Image 3 ("Gelir & Gider").
 * Magenta: Zorunlu İhtiyaçlar (50%)
 * Cyan: İstekler (30%)
 * Orange: Yatırım (20%)
 */
@Composable
fun CyberIncomeExpenseDonutCard(
    essentialAmount: Double = 35000.0,
    desireAmount: Double = 18000.0,
    investAmount: Double = 12000.0,
    modifier: Modifier = Modifier
) {
    val total = (essentialAmount + desireAmount + investAmount).coerceAtLeast(1.0)
    val essentialRatio = (essentialAmount / total).toFloat()
    val desireRatio = (desireAmount / total).toFloat()
    val investRatio = (investAmount / total).toFloat()
    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing Circular Donut Canvas
        Box(
            modifier = Modifier
                .size(220.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 16.dp.toPx()
                val gapAngle = 4f
                var curAngle = -90f

                val segments = listOf(
                    Triple(essentialRatio, CyberNeonMagenta, "Zorunlu"),
                    Triple(desireRatio, CyberNeonCyan, "İstek"),
                    Triple(investRatio, CyberNeonOrange, "Yatırım")
                )

                segments.forEach { (ratio, color, _) ->
                    val sweep = (ratio * 360f - gapAngle).coerceAtLeast(1f)

                    // Outer soft glow
                    drawArc(
                        color = color.copy(alpha = 0.35f),
                        startAngle = curAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeW + 8.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Sharp arc
                    drawArc(
                        color = color,
                        startAngle = curAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )

                    curAngle += sweep + gapAngle
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3 Frosted Cards underneath
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CyberCategoryBarCard(
                dotColor = CyberNeonMagenta,
                categoryName = "Zorunlu İhtiyaçlar",
                ratio = essentialRatio,
                amountFormatted = format.format(essentialAmount)
            )

            CyberCategoryBarCard(
                dotColor = CyberNeonCyan,
                categoryName = "İstekler",
                ratio = desireRatio,
                amountFormatted = format.format(desireAmount)
            )

            CyberCategoryBarCard(
                dotColor = CyberNeonOrange,
                categoryName = "Yatırım",
                ratio = investRatio,
                amountFormatted = format.format(investAmount)
            )
        }
    }
}

@Composable
fun CyberCategoryBarCard(
    dotColor: Color,
    categoryName: String,
    ratio: Float,
    amountFormatted: String
) {
    CyberCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = categoryName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            Text(
                text = amountFormatted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8F9CAE)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Glowing progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF1B2338))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(ratio.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(dotColor, dotColor.copy(alpha = 0.7f))
                        )
                    )
            )
        }
    }
}

/**
 * Compound Interest Simulator Card ("Bileşik Getiri Simülatörü"), as in Image 2.
 * Glowing exponential projection curve ending at node dot + custom neon sliders.
 */
@Composable
fun CyberCompoundSimulatorCard(
    initialAmount: Double = 10000.0,
    monthlyAddition: Double = 5000.0,
    annualReturnRate: Double = 15.0,
    modifier: Modifier = Modifier
) {
    var monthlyInput by remember { mutableDoubleStateOf(monthlyAddition) }
    var rateInput by remember { mutableDoubleStateOf(annualReturnRate) }

    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }

    // Calculate 12-month projections
    val monthlyRate = (rateInput / 100.0) / 12.0
    val monthsData = remember(monthlyInput, rateInput) {
        val list = mutableListOf<Double>()
        var balance = initialAmount
        for (m in 1..12) {
            balance = (balance + monthlyInput) * (1.0 + monthlyRate)
            list.add(balance)
        }
        list
    }
    val finalVal = monthsData.lastOrNull() ?: 100000.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. PROJECTION CARD (Image 2 Top Card) ---
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = CyberBorderGlow
        ) {
            Text(
                text = "12 Aylık Projeksiyon",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Canvas with Grid and Glowing Exponential Curve
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                val w = size.width
                val h = size.height
                val paddingX = 20.dp.toPx()
                val paddingY = 20.dp.toPx()
                val graphW = w - paddingX * 2
                val graphH = h - paddingY * 2

                // Horizontal grid lines: 10k, 50k, 100k
                val gridLevels = listOf(0.2f, 0.55f, 0.9f)
                gridLevels.forEach { lvl ->
                    val y = paddingY + graphH * (1f - lvl)
                    drawLine(
                        color = Color(0xFF1E283E).copy(alpha = 0.7f),
                        start = Offset(paddingX, y),
                        end = Offset(w - paddingX, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Exponential Points
                val minVal = monthsData.minOrNull() ?: 0.0
                val maxV = monthsData.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

                val curvePath = Path()
                val fillPath = Path()

                monthsData.forEachIndexed { index, value ->
                    val normX = paddingX + (index.toFloat() / 11f) * graphW
                    val normY = paddingY + graphH * (1f - ((value - minVal) / (maxV - minVal)).toFloat() * 0.85f - 0.08f)

                    if (index == 0) {
                        curvePath.moveTo(normX, normY)
                        fillPath.moveTo(normX, paddingY + graphH)
                        fillPath.lineTo(normX, normY)
                    } else {
                        val prevX = paddingX + ((index - 1).toFloat() / 11f) * graphW
                        val prevY = paddingY + graphH * (1f - ((monthsData[index - 1] - minVal) / (maxV - minVal)).toFloat() * 0.85f - 0.08f)
                        val midX = (prevX + normX) / 2f
                        curvePath.cubicTo(midX, prevY, midX, normY, normX, normY)
                        fillPath.cubicTo(midX, prevY, midX, normY, normX, normY)
                    }
                }

                // Fill under curve
                fillPath.lineTo(w - paddingX, paddingY + graphH)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CyberNeonMint.copy(alpha = 0.28f),
                            CyberNeonMint.copy(alpha = 0.04f),
                            Color.Transparent
                        )
                    )
                )

                // Soft outer glow for curve
                drawPath(
                    path = curvePath,
                    color = CyberNeonMint.copy(alpha = 0.45f),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )

                // Sharp stroke for curve
                drawPath(
                    path = curvePath,
                    color = CyberNeonMint,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Glowing node dot at final point
                val finalX = paddingX + graphW
                val finalY = paddingY + graphH * (1f - 0.85f - 0.08f)

                // Node outer glow
                drawCircle(
                    color = CyberNeonMint.copy(alpha = 0.4f),
                    radius = 12.dp.toPx(),
                    center = Offset(finalX, finalY)
                )
                // Node inner solid
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(finalX, finalY)
                )
            }

            // Month labels: 1 2 3 ... 12
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                (1..12).forEach { m ->
                    Text(
                        text = "$m",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8F9CAE)
                    )
                }
            }
        }

        // --- 2. CONTROLS CARD (Image 2 Bottom Card) ---
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            // Slider 1: Monthly amount
            Text(
                text = "${format.format(monthlyInput)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Slider(
                value = monthlyInput.toFloat(),
                onValueChange = { monthlyInput = it.toDouble() },
                valueRange = 1000f..50000f,
                colors = SliderDefaults.colors(
                    thumbColor = CyberNeonMint,
                    activeTrackColor = CyberNeonMint,
                    inactiveTrackColor = Color(0xFF1B2338)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Slider 2: Annual rate
            Text(
                text = "%${rateInput.toInt()}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(4.dp))

            Slider(
                value = rateInput.toFloat(),
                onValueChange = { rateInput = it.toDouble() },
                valueRange = 5f..60f,
                colors = SliderDefaults.colors(
                    thumbColor = CyberNeonCyan,
                    activeTrackColor = CyberNeonCyan,
                    inactiveTrackColor = Color(0xFF1B2338)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Asset Summary Card with Glowing Icon & Sparkline, as in Image 1 ("Birikim & Portföy").
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
    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }

    CyberCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = format.format(amount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                // Change badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (changePercent >= 0) Color(0xFF0F3E2F) else Color(0xFF4C151F)
                ) {
                    Text(
                        text = "${if (changePercent >= 0) "+" else ""}${String.format(java.util.Locale.US, "%.1f", changePercent)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (changePercent >= 0) CyberNeonMint else CyberNeonCoral,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Mini Sparkline Canvas
                Canvas(modifier = Modifier.size(width = 65.dp, height = 24.dp)) {
                    val w = size.width
                    val h = size.height

                    val path = Path().apply {
                        moveTo(0f, h * 0.7f)
                        cubicTo(w * 0.3f, h * 0.8f, w * 0.6f, h * 0.2f, w, h * 0.3f)
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
