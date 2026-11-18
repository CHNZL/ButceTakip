package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BankRate
import com.example.data.GoldPrice
import com.example.ui.theme.*

/**
 * Flagship Executive Hero Card for Anasayfa.
 * Shows net monthly position, live cashflow allocation bar, privacy toggle,
 * and interactive Income / Expense / Saving breakdown tiles.
 */
@Composable
fun HomeHeroCommandCard(
    monthLabel: String,
    balance: Double,
    income: Double,
    expense: Double,
    saving: Double,
    isBalanceHidden: Boolean,
    onToggleVisibility: () -> Unit,
    onOpenIncomes: () -> Unit,
    onOpenPayments: () -> Unit,
    onOpenSavings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }

    val incomeColor = if (isDark) CyberNeonMint else Color(0xFF059669)
    val expenseColor = if (isDark) CyberNeonCoral else Color(0xFFDC2626)
    val savingColor = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val trackBg = if (isDark) Color(0xFF1A2338) else Color(0xFFE2E8F0)
    val tileBg = if (isDark) Color(0xFF111827).copy(alpha = 0.85f) else Color(0xFFF8FAFC)
    val tileBorder = if (isDark) Color(0xFF243047) else Color(0xFFE2E8F0)

    val (statusLabel, statusColor) = when {
        income == 0.0 && expense == 0.0 && saving == 0.0 -> "Yeni Dönem" to MaterialTheme.colorScheme.primary
        balance >= 0 -> "Kasa Artıda ✓" to incomeColor
        else -> "Bütçe Ekside" to expenseColor
    }

    val totalPool = maxOf(income, expense + saving).coerceAtLeast(1.0)
    val expenseRatio = (expense / totalPool).toFloat().coerceIn(0f, 1f)
    val savingRatio = (saving / totalPool).toFloat().coerceIn(0f, 1f - expenseRatio)
    val remainingRatio = (1f - expenseRatio - savingRatio).coerceAtLeast(0f)

    val glowTint = if (balance >= 0) incomeColor else expenseColor

    CyberCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        borderColor = if (isDark) CyberBorderGlow else MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowTint.copy(alpha = if (isDark) 0.16f else 0.10f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.88f, h * 0.12f),
                            radius = w * 0.55f
                        ),
                        center = Offset(w * 0.88f, h * 0.12f),
                        radius = w * 0.55f
                    )
                    val wave = Path().apply {
                        moveTo(0f, h * 0.52f)
                        cubicTo(w * 0.30f, h * 0.44f, w * 0.58f, h * 0.62f, w, h * 0.36f)
                    }
                    drawPath(
                        path = wave,
                        color = CyberNeonPurple.copy(alpha = if (isDark) 0.14f else 0.08f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row: Live Period Pill + Status Badge + Eye Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = "NET BÜTÇE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = monthLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                softWrap = false,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            color = statusColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = statusLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = statusColor,
                                softWrap = false,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        IconButton(
                            onClick = onToggleVisibility,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("hero_visibility_toggle")
                        ) {
                            Icon(
                                imageVector = if (isBalanceHidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = "Bakiyeyi Gizle veya Göster",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Main Net Figure
                Text(
                    text = if (isBalanceHidden) "•••••••• ₺" else format.format(balance),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.8).sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cashflow Allocation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(CircleShape)
                        .background(trackBg)
                ) {
                    if (income == 0.0 && expense == 0.0 && saving == 0.0) {
                        // Subtle empty bar
                    } else {
                        if (expenseRatio > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(expenseRatio)
                                    .fillMaxHeight()
                                    .background(expenseColor)
                            )
                        }
                        if (savingRatio > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(savingRatio)
                                    .fillMaxHeight()
                                    .background(savingColor)
                            )
                        }
                        if (remainingRatio > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(remainingRatio)
                                    .fillMaxHeight()
                                    .background(incomeColor)
                            )
                        }
                    }
                }

                if (income > 0.0) {
                    val expPct = ((expense / income) * 100).toInt().coerceAtLeast(0)
                    val savPct = ((saving / income) * 100).toInt().coerceAtLeast(0)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Gelirin %$expPct'si gider • %$savPct'i birikime yönlendirildi",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive 3-Column Metrics Dock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeroMetricMiniTile(
                        label = "GELİR",
                        value = if (isBalanceHidden) "••••" else format.format(income),
                        icon = Icons.Rounded.NorthEast,
                        accent = incomeColor,
                        bgColor = tileBg,
                        borderColor = tileBorder,
                        onClick = onOpenIncomes,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hero_income_tile")
                    )
                    HeroMetricMiniTile(
                        label = "GİDER",
                        value = if (isBalanceHidden) "••••" else format.format(expense),
                        icon = Icons.Rounded.SouthWest,
                        accent = expenseColor,
                        bgColor = tileBg,
                        borderColor = tileBorder,
                        onClick = onOpenPayments,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hero_expense_tile")
                    )
                    HeroMetricMiniTile(
                        label = "BİRİKİM",
                        value = if (isBalanceHidden) "••••" else format.format(saving),
                        icon = Icons.Rounded.Savings,
                        accent = savingColor,
                        bgColor = tileBg,
                        borderColor = tileBorder,
                        onClick = onOpenSavings,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hero_saving_tile")
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMetricMiniTile(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    bgColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    softWrap = false,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = accent,
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 4-Tile Quick Command Bar cleanly aligned below Hero.
 */
@Composable
fun HomeQuickActionDock(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onAddSaving: () -> Unit,
    onOpenAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val incTint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val expTint = if (isDark) CyberNeonCoral else Color(0xFFDC2626)
    val savTint = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val anaTint = if (isDark) CyberNeonPurple else Color(0xFF7C3AED)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HomeCommandTile(
            label = "Gelir",
            icon = Icons.Default.Add,
            tint = incTint,
            isDark = isDark,
            onClick = onAddIncome,
            modifier = Modifier
                .weight(1f)
                .testTag("quick_add_income")
        )
        HomeCommandTile(
            label = "Gider",
            icon = Icons.Rounded.Remove,
            tint = expTint,
            isDark = isDark,
            onClick = onAddExpense,
            modifier = Modifier
                .weight(1f)
                .testTag("quick_add_expense")
        )
        HomeCommandTile(
            label = "Birikim",
            icon = Icons.Rounded.Savings,
            tint = savTint,
            isDark = isDark,
            onClick = onAddSaving,
            modifier = Modifier
                .weight(1f)
                .testTag("quick_add_saving")
        )
        HomeCommandTile(
            label = "Analiz",
            icon = Icons.Rounded.Insights,
            tint = anaTint,
            isDark = isDark,
            onClick = onOpenAnalytics,
            modifier = Modifier
                .weight(1f)
                .testTag("quick_open_analytics")
        )
    }
}

@Composable
private fun HomeCommandTile(
    label: String,
    icon: ImageVector,
    tint: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDark) CyberDarkSurface else Color.White
    val border = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, border),
        shadowElevation = if (isDark) 0.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 11.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = if (isDark) 0.16f else 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                softWrap = false,
                maxLines = 1
            )
        }
    }
}

/**
 * Dual Executive Insights Strip:
 * 1) Daily Remaining Budget Pace
 * 2) Pending & Overdue Bills Radar
 */
@Composable
fun HomeDailyRadarRow(
    remainingBalance: Double,
    remainingDaysInMonth: Int,
    overdueCount: Int,
    overdueTotal: Double,
    pendingThisMonthCount: Int,
    pendingThisMonthTotal: Double,
    isBalanceHidden: Boolean,
    onOpenPayments: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFDC2626)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)

    val safeDays = remainingDaysInMonth.coerceAtLeast(1)
    val dailyAllowance = if (remainingBalance > 0.0) remainingBalance / safeDays else 0.0

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Daily Allowance Card
        CyberCard(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            contentPadding = PaddingValues(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Speed,
                    contentDescription = null,
                    tint = if (remainingBalance >= 0) mint else coral,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Günlük Limit",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when {
                    isBalanceHidden -> "•••• ₺"
                    remainingBalance > 0.0 -> "${format.format(dailyAllowance)}/gün"
                    else -> "Limit Aşıldı"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = if (remainingBalance > 0.0) MaterialTheme.colorScheme.onSurface else coral,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Ay sonuna $safeDays gün",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        // Payment Radar Card
        val radarCount = overdueCount + pendingThisMonthCount
        val radarAmount = overdueTotal + pendingThisMonthTotal
        val radarColor = when {
            overdueCount > 0 -> coral
            pendingThisMonthCount > 0 -> amber
            else -> mint
        }

        CyberCard(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .clickable { onOpenPayments() },
            shape = RoundedCornerShape(18.dp),
            borderColor = if (overdueCount > 0) coral.copy(alpha = 0.55f) else null,
            contentPadding = PaddingValues(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (overdueCount > 0) Icons.Rounded.WarningAmber else Icons.Rounded.EventNote,
                    contentDescription = null,
                    tint = radarColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Ödeme Radarı",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when {
                    isBalanceHidden && radarCount > 0 -> "$radarCount Ödeme"
                    radarCount > 0 -> format.format(radarAmount)
                    else -> "Borç Yok ✓"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = if (radarCount > 0) radarColor else mint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when {
                    overdueCount > 0 -> "$overdueCount geciken • $radarCount toplam"
                    pendingThisMonthCount > 0 -> "$pendingThisMonthCount fatura bekliyor"
                    else -> "Bu ay tümü ödendi"
                },
                fontSize = 10.sp,
                color = if (overdueCount > 0) coral else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Live Portfolio Card paired beside Savings Rate Gauge on Home Screen.
 */
@Composable
fun HomePortfolioSpotlightCard(
    portfolioValue: Double,
    portfolioCost: Double,
    besTotal: Double,
    isBalanceHidden: Boolean,
    onOpenSavings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }
    val grandTotal = portfolioValue + besTotal
    val netProfit = portfolioValue - portfolioCost
    val profitPct = if (portfolioCost > 0.0) ((netProfit / portfolioCost) * 100) else 0.0
    val isProfit = netProfit >= 0.0
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFDC2626)
    val cyan = if (isDark) CyberNeonCyan else Color(0xFF0284C7)

    CyberCard(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable { onOpenSavings() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Portföy",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(cyan.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = cyan,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isBalanceHidden) "•••••• ₺" else format.format(grandTotal),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (portfolioCost > 0.0) {
            Surface(
                color = (if (isProfit) mint else coral).copy(alpha = 0.14f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = String.format(
                        java.util.Locale("tr"),
                        "%s%.1f%% Net Getiri",
                        if (isProfit) "+" else "",
                        profitPct
                    ),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isProfit) mint else coral,
                    softWrap = false,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        } else {
            Text(
                text = if (besTotal > 0) "BES birikimi dahil" else "Altın, Döviz & BES",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "Detayı Gör",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

/**
 * Clean, Compact Collapsible Market Ticker Hub for Anasayfa.
 * Shows top 4 live rates by default and expands on demand to keep the Home screen airy.
 */
@Composable
fun HomeCompactMarketCard(
    goldPrices: List<GoldPrice>,
    isFetchingGold: Boolean,
    lastGoldUpdate: String?,
    onRefreshGold: () -> Unit,
    bankRates: List<BankRate>,
    isFetchingBank: Boolean,
    lastBankUpdate: String?,
    onRefreshBank: () -> Unit,
    ziraatRates: List<BankRate>,
    isFetchingZiraat: Boolean,
    lastZiraatUpdate: String?,
    onRefreshZiraat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var selectedSource by remember { mutableIntStateOf(0) }
    var isExpanded by remember { mutableStateOf(false) }

    val currentFetching = when (selectedSource) {
        0 -> isFetchingGold
        1 -> isFetchingBank
        else -> isFetchingZiraat
    }
    val currentLastUpdate = when (selectedSource) {
        0 -> lastGoldUpdate
        1 -> lastBankUpdate
        else -> lastZiraatUpdate
    }
    val onRefreshCurrent = {
        when (selectedSource) {
            0 -> onRefreshGold()
            1 -> onRefreshBank()
            else -> onRefreshZiraat()
        }
    }

    val totalCount = when (selectedSource) {
        0 -> goldPrices.size
        1 -> bankRates.size
        else -> ziraatRates.size
    }

    CyberCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color(0xFFF59E0B).copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.ShowChart,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Canlı Piyasa",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!currentLastUpdate.isNullOrBlank()) {
                        Text(
                            text = "Güncelleme: $currentLastUpdate",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (totalCount > 4) {
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "Daralt" else "Tümü ($totalCount)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                IconButton(
                    onClick = onRefreshCurrent,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (currentFetching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Piyasa Verilerini Yenile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDark) Color(0xFF141B2D) else Color(0xFFF1F5F9))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MarketTabPill(
                title = "Altın (Sivas)",
                isSelected = selectedSource == 0,
                modifier = Modifier.weight(1f),
                onClick = { selectedSource = 0 }
            )
            MarketTabPill(
                title = "Yapı Kredi",
                isSelected = selectedSource == 1,
                modifier = Modifier.weight(1f),
                onClick = { selectedSource = 1 }
            )
            MarketTabPill(
                title = "Ziraat",
                isSelected = selectedSource == 2,
                modifier = Modifier.weight(1f),
                onClick = { selectedSource = 2 }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedSource) {
            0 -> MarketRateGridBody(
                items = goldPrices.map { Triple(it.name, "ALTIN", it.buy to it.sell) },
                isFetching = isFetchingGold,
                isExpanded = isExpanded,
                isDark = isDark
            )
            1 -> MarketRateGridBody(
                items = bankRates.map { Triple(it.code, it.name, it.buy to it.sell) },
                isFetching = isFetchingBank,
                isExpanded = isExpanded,
                isDark = isDark
            )
            else -> MarketRateGridBody(
                items = ziraatRates.map { Triple(it.code, it.name, it.buy to it.sell) },
                isFetching = isFetchingZiraat,
                isExpanded = isExpanded,
                isDark = isDark
            )
        }
    }
}

@Composable
private fun MarketRateGridBody(
    items: List<Triple<String, String, Pair<String, String>>>,
    isFetching: Boolean,
    isExpanded: Boolean,
    isDark: Boolean
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isFetching) "Canlı kurlar alınıyor..." else "Veri alınamadı, yenilemeyi deneyin.",
                fontSize = 11.sp,
                color = if (isFetching) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
            )
        }
        return
    }

    val previewRows = items.take(4).chunked(2)
    val extraRows = if (items.size > 4) items.drop(4).chunked(2) else emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        previewRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { (title, sub, prices) ->
                    ModernRateItemCard(
                        title = title,
                        subtitle = sub,
                        buyPrice = prices.first,
                        sellPrice = prices.second,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        AnimatedVisibility(
            visible = isExpanded && extraRows.isNotEmpty(),
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                extraRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { (title, sub, prices) ->
                            ModernRateItemCard(
                                title = title,
                                subtitle = sub,
                                buyPrice = prices.first,
                                sellPrice = prices.second,
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
