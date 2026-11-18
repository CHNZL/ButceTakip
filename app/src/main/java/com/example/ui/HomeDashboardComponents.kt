package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BankRate
import com.example.data.GoldPrice
import com.example.data.Transaction
import com.example.data.TransactionType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Compact 1-line live market ticker strip shown cleanly at the top of Anasayfa.
 * Tapping opens the full interactive Market Bottom Sheet without wasting scroll space.
 */
@Composable
fun HomeLiveTickerStrip(
    goldPrices: List<GoldPrice>,
    bankRates: List<BankRate>,
    ziraatRates: List<BankRate>,
    isFetching: Boolean,
    onOpenMarketSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val surfaceBg = if (isDark) CyberDarkSurface else Color.White
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)
    val amber = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)

    val gramGold = remember(goldPrices) {
        goldPrices.firstOrNull { it.name.contains("gram", ignoreCase = true) } ?: goldPrices.firstOrNull()
    }
    val ceyrekGold = remember(goldPrices) {
        goldPrices.firstOrNull {
            it.name.contains("çeyrek", ignoreCase = true) || it.name.contains("ceyrek", ignoreCase = true)
        }
    }
    val usdRate = remember(bankRates, ziraatRates) {
        bankRates.firstOrNull { it.code.equals("USD", ignoreCase = true) }
            ?: ziraatRates.firstOrNull { it.code.equals("USD", ignoreCase = true) }
    }
    val eurRate = remember(bankRates, ziraatRates) {
        bankRates.firstOrNull { it.code.equals("EUR", ignoreCase = true) }
            ?: ziraatRates.firstOrNull { it.code.equals("EUR", ignoreCase = true) }
    }

    val tickers = remember(gramGold, ceyrekGold, usdRate, eurRate) {
        buildList {
            if (gramGold != null) add(Triple("GRAM", gramGold.sell.ifBlank { gramGold.buy }, amber))
            if (usdRate != null && usdRate.sell != "0,00" && usdRate.sell != "-") add(Triple("USD", usdRate.sell, mint))
            if (eurRate != null && eurRate.sell != "0,00" && eurRate.sell != "-") add(Triple("EUR", eurRate.sell, mint))
            if (ceyrekGold != null) add(Triple("ÇEYREK", ceyrekGold.sell.ifBlank { ceyrekGold.buy }, amber))
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onOpenMarketSheet() }
            .testTag("home_live_ticker_strip"),
        shape = RoundedCornerShape(14.dp),
        color = surfaceBg,
        border = BorderStroke(0.8.dp, borderColor),
        shadowElevation = if (isDark) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (isFetching) amber else mint)
            )
            Spacer(modifier = Modifier.width(8.dp))

            if (tickers.isEmpty()) {
                Text(
                    text = if (isFetching) "Canlı piyasa verileri güncelleniyor..." else "Canlı Piyasa Masası",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tickers.forEachIndexed { idx, (code, price, dotColor) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = code,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = dotColor,
                                softWrap = false,
                                maxLines = 1
                            )
                            Text(
                                text = price,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                softWrap = false,
                                maxLines = 1
                            )
                        }
                        if (idx < tickers.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(12.dp)
                                    .background(borderColor)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Tüm Piyasaları Aç",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Full Live Market Drawer / Sheet opened on tapping the Home Ticker Strip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMarketBottomSheet(
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
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var selectedSource by remember { mutableIntStateOf(0) }
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Canlı Piyasa Masası",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!currentLastUpdate.isNullOrBlank()) {
                        Text(
                            text = "Son güncelleme: $currentLastUpdate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = {
                        when (selectedSource) {
                            0 -> onRefreshGold()
                            1 -> onRefreshBank()
                            else -> onRefreshZiraat()
                        }
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    if (currentFetching) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) CyberDarkSurfaceVariant else CyberLightSurfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MarketTabPill("Altın (Sivas)", selectedSource == 0, Modifier.weight(1f)) { selectedSource = 0 }
                MarketTabPill("Yapı Kredi", selectedSource == 1, Modifier.weight(1f)) { selectedSource = 1 }
                MarketTabPill("Ziraat", selectedSource == 2, Modifier.weight(1f)) { selectedSource = 2 }
            }

            val items = when (selectedSource) {
                0 -> goldPrices.map { Triple(it.name, "Altın", it.buy to it.sell) }
                1 -> bankRates.map { Triple(it.code, it.name, it.buy to it.sell) }
                else -> ziraatRates.map { Triple(it.code, it.name, it.buy to it.sell) }
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentFetching) "Piyasa kurları alınıyor..." else "Veri alınamadı, yenilemeyi deneyin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isDark) CyberDarkSurfaceVariant.copy(alpha = 0.55f) else Color(0xFFF8FAFC))
                ) {
                    items.forEachIndexed { idx, (title, sub, prices) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = sub,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Alış", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = prices.first,
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Satış", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = prices.second,
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        if (idx < items.lastIndex) {
                            HorizontalDivider(
                                thickness = 0.6.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Private Wealth Family CFO Hero Card.
 * Replaces cluttered mini-boxes with authoritative typography, daily spending allowance,
 * month pace insight, proportional cashflow waterfall bar, and tappable 3-column footer.
 */
@Composable
fun HomeWealthHeroCard(
    monthLabel: String,
    balance: Double,
    income: Double,
    expense: Double,
    saving: Double,
    elapsedMonthFraction: Float,
    remainingDaysInMonth: Int,
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

    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)

    val safePool = maxOf(income, expense + saving).coerceAtLeast(1.0)
    val expenseRatio by animateFloatAsState(
        targetValue = (expense / safePool).toFloat().coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "heroExpenseRatio"
    )
    val savingRatio by animateFloatAsState(
        targetValue = (saving / safePool).toFloat().coerceIn(0f, 1f - expenseRatio),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "heroSavingRatio"
    )
    val remainingRatio = (1f - expenseRatio - savingRatio).coerceAtLeast(0f)

    // Budget Pace intelligence (Financial Expert logic)
    val spentFraction = if (income > 0.0) (expense / income).toFloat() else 0f
    val (paceLabel, paceColor) = when {
        income == 0.0 && expense == 0.0 && saving == 0.0 -> "Yeni Dönem" to MaterialTheme.colorScheme.primary
        balance < 0.0 -> "Kasa Ekside" to coral
        income > 0.0 && spentFraction > elapsedMonthFraction + 0.15f -> "Harcama Hızlı" to amber
        income > 0.0 && (saving / income) >= 0.20 -> "Tasarruf Güçlü" to mint
        else -> "Tempo Dengeli" to mint
    }

    val dailyAllowance = if (balance > 0.0) balance / remainingDaysInMonth.coerceAtLeast(1) else 0.0
    val dividerColor = if (isDark) Color(0xFF1F293D) else Color(0xFFE2E8F0)
    val trackBg = if (isDark) Color(0xFF1B2436) else Color(0xFFE8EEF5)

    CyberCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0),
        contentPadding = PaddingValues(20.dp)
    ) {
        // 1. Header Strip: Label + Month + Pace Pill + Eye Toggle
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
                Text(
                    text = "SERBEST NAKİT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    softWrap = false,
                    maxLines = 1
                )
                Surface(
                    color = if (isDark) CyberDarkSurfaceVariant else CyberLightSurfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = monthLabel,
                        style = MaterialTheme.typography.labelSmall,
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
                    color = paceColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(paceColor)
                        )
                        Text(
                            text = paceLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = paceColor,
                            softWrap = false,
                            maxLines = 1
                        )
                    }
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

        // 2. Giant Tabular Net Balance
        Text(
            text = if (isBalanceHidden) "•••••••• ₺" else format.format(balance),
            style = TabularHeroStyle,
            fontSize = 38.sp,
            color = if (balance < 0.0 && !isBalanceHidden) coral else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Calm CFO Pace Insight
        Text(
            text = buildAnnotatedString {
                when {
                    isBalanceHidden -> {
                        append("Gizlilik modu aktif • Ay sonuna $remainingDaysInMonth gün")
                    }
                    income == 0.0 && expense == 0.0 && saving == 0.0 -> {
                        append("Bu ay henüz kayıt yok • İlk işlemi ekleyerek başlayın")
                    }
                    balance > 0.0 -> {
                        append("Ay sonuna $remainingDaysInMonth gün • Günde ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = mint)) {
                            append(format.format(dailyAllowance))
                        }
                        append(" serbest bütçe")
                    }
                    else -> {
                        append("Bu ay harcama ve birikim geliri ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = coral)) {
                            append(format.format(abs(balance)))
                        }
                        append(" aştı")
                    }
                }
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Clean Proportional Allocation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(trackBg)
        ) {
            if (income > 0.0 || expense > 0.0 || saving > 0.0) {
                if (expenseRatio > 0f) {
                    Box(modifier = Modifier.weight(expenseRatio.coerceAtLeast(0.01f)).fillMaxHeight().background(coral))
                }
                if (savingRatio > 0f) {
                    Box(modifier = Modifier.weight(savingRatio.coerceAtLeast(0.01f)).fillMaxHeight().background(sky))
                }
                if (remainingRatio > 0f) {
                    Box(modifier = Modifier.weight(remainingRatio.coerceAtLeast(0.01f)).fillMaxHeight().background(mint))
                }
            }
        }

        if (income > 0.0) {
            val expPct = ((expense / income) * 100).toInt().coerceAtLeast(0)
            val savPct = ((saving / income) * 100).toInt().coerceAtLeast(0)
            val remPct = (100 - expPct - savPct).coerceAtLeast(0)
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = "Gelir dağılımı: %$expPct gider • %$savPct birikim • %$remPct kalan",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(thickness = 0.7.dp, color = dividerColor)
        Spacer(modifier = Modifier.height(14.dp))

        // 5. Clean 3-Column Typographic Ledger Footer (No nested boxes!)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeroLedgerColumn(
                label = "Gelir",
                value = if (isBalanceHidden) "••••" else format.format(income),
                accentColor = mint,
                onClick = onOpenIncomes,
                modifier = Modifier
                    .weight(1f)
                    .testTag("hero_income_tile")
            )
            Box(
                modifier = Modifier
                    .width(0.8.dp)
                    .height(34.dp)
                    .background(dividerColor)
            )
            HeroLedgerColumn(
                label = "Gider",
                value = if (isBalanceHidden) "••••" else format.format(expense),
                accentColor = coral,
                onClick = onOpenPayments,
                modifier = Modifier
                    .weight(1f)
                    .testTag("hero_expense_tile")
            )
            Box(
                modifier = Modifier
                    .width(0.8.dp)
                    .height(34.dp)
                    .background(dividerColor)
            )
            HeroLedgerColumn(
                label = "Birikim",
                value = if (isBalanceHidden) "••••" else format.format(saving),
                accentColor = sky,
                onClick = onOpenSavings,
                modifier = Modifier
                    .weight(1f)
                    .testTag("hero_saving_tile")
            )
        }
    }
}

@Composable
private fun HeroLedgerColumn(
    label: String,
    value: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                softWrap = false,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            style = TabularAmountStyle,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            softWrap = false,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Tactile 4-Pill Quick Command Row with haptic feedback.
 */
@Composable
fun HomeQuickCommandRow(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onAddSaving: () -> Unit,
    onOpenAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val haptic = LocalHapticFeedback.current
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val indigo = if (isDark) CyberNeonPurple else Color(0xFF6366F1)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickCommandPill(
            label = "Gelir",
            icon = Icons.Default.Add,
            accent = mint,
            isDark = isDark,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onAddIncome()
            },
            modifier = Modifier.weight(1f).testTag("quick_add_income")
        )
        QuickCommandPill(
            label = "Gider",
            icon = Icons.Rounded.Remove,
            accent = coral,
            isDark = isDark,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onAddExpense()
            },
            modifier = Modifier.weight(1f).testTag("quick_add_expense")
        )
        QuickCommandPill(
            label = "Birikim",
            icon = Icons.Rounded.Savings,
            accent = sky,
            isDark = isDark,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onAddSaving()
            },
            modifier = Modifier.weight(1f).testTag("quick_add_saving")
        )
        QuickCommandPill(
            label = "Analiz",
            icon = Icons.Rounded.Insights,
            accent = indigo,
            isDark = isDark,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onOpenAnalytics()
            },
            modifier = Modifier.weight(1f).testTag("quick_open_analytics")
        )
    }
}

@Composable
private fun QuickCommandPill(
    label: String,
    icon: ImageVector,
    accent: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isDark) CyberDarkSurface else Color.White
    val border = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)

    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = bg,
        border = BorderStroke(0.8.dp, border),
        shadowElevation = if (isDark) 0.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = if (isDark) 0.16f else 0.11f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accent,
                    modifier = Modifier.size(17.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                softWrap = false,
                maxLines = 1
            )
        }
    }
}

/**
 * Interactive "Yaklaşan Ödemeler" Unified Ledger Card with 1-Tap Pay button.
 */
@Composable
fun HomeActionBillsCard(
    upcomingBills: List<Transaction>,
    todayStartMillis: Long,
    isBalanceHidden: Boolean,
    onTogglePaid: (Transaction, Boolean) -> Unit,
    onOpenAllPayments: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val haptic = LocalHapticFeedback.current
    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }
    val dateSdf = remember { SimpleDateFormat("dd MMM", Locale("tr", "TR")) }
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)
    val dividerColor = if (isDark) Color(0xFF1F293D) else Color(0xFFE2E8F0)

    val totalUpcoming = remember(upcomingBills) { upcomingBills.sumOf { it.amount } }
    val hasOverdue = remember(upcomingBills, todayStartMillis) {
        upcomingBills.any { it.timestamp < todayStartMillis }
    }

    CyberCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        borderColor = if (hasOverdue) coral.copy(alpha = 0.40f) else null,
        contentPadding = PaddingValues(16.dp)
    ) {
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
                Text(
                    text = "Yaklaşan Ödemeler",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (upcomingBills.isNotEmpty()) {
                    Surface(
                        color = (if (hasOverdue) coral else amber).copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isBalanceHidden) "${upcomingBills.size} fatura" else format.format(totalUpcoming),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (hasOverdue) coral else amber,
                            softWrap = false,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            TextButton(
                onClick = onOpenAllPayments,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Tümü", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (upcomingBills.isEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(mint.copy(alpha = if (isDark) 0.10f else 0.08f))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = mint,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Tüm faturalar ödendi • Bu ay bekleyen ödeme yok",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                upcomingBills.forEachIndexed { idx, tx ->
                    val isOverdue = tx.timestamp < todayStartMillis
                    val badgeColor = if (isOverdue) coral else amber
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(badgeColor.copy(alpha = if (isDark) 0.15f else 0.10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isOverdue) Icons.Rounded.PriorityHigh else Icons.Rounded.ReceiptLong,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = dateSdf.format(Date(tx.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    softWrap = false,
                                    maxLines = 1
                                )
                                if (isOverdue) {
                                    Surface(
                                        color = coral.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(5.dp)
                                    ) {
                                        Text(
                                            text = "Gecikti",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = coral,
                                            softWrap = false,
                                            maxLines = 1,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                if (!tx.person.isNullOrBlank()) {
                                    Text(
                                        text = "• ${tx.person}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isBalanceHidden) "••••" else format.format(tx.amount),
                                style = TabularAmountStyle,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                softWrap = false,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onTogglePaid(tx, true)
                                    },
                                color = mint.copy(alpha = if (isDark) 0.18f else 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "${tx.title} öde",
                                        tint = mint,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Öde",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = mint,
                                        softWrap = false,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                    if (idx < upcomingBills.lastIndex) {
                        HorizontalDivider(thickness = 0.6.dp, color = dividerColor)
                    }
                }
            }
        }
    }
}

/**
 * Side-by-side Family Health Strip: Savings Discipline Ring + Live Portfolio Net Worth.
 */
@Composable
fun HomeFamilyHealthRow(
    savingRatioPct: Int,
    monthlySavingAmount: Double,
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
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val trackColor = if (isDark) Color(0xFF1B2436) else Color(0xFFE2E8F0)

    val clampedPct = savingRatioPct.coerceIn(0, 100)
    val sweepAnim by animateFloatAsState(
        targetValue = (clampedPct / 100f) * 360f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "savingRingSweep"
    )

    val grandPortfolio = portfolioValue + besTotal
    val netProfit = portfolioValue - portfolioCost
    val profitPct = if (portfolioCost > 0.0) (netProfit / portfolioCost) * 100.0 else 0.0
    val isProfit = netProfit >= 0.0

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Card 1: Tasarruf Disiplini
        CyberCard(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(22.dp))
                .clickable { onOpenSavings() },
            shape = RoundedCornerShape(22.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tasarruf Gücü",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Surface(
                    color = (if (clampedPct >= 20) mint else sky).copy(alpha = 0.14f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (clampedPct >= 20) "Hedef ✓" else "Hedef %20",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (clampedPct >= 20) mint else sky,
                        softWrap = false,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeW = 6.dp.toPx()
                        drawCircle(
                            color = trackColor,
                            style = Stroke(width = strokeW)
                        )
                        if (sweepAnim > 0f) {
                            drawArc(
                                brush = Brush.sweepGradient(listOf(sky, mint)),
                                startAngle = -90f,
                                sweepAngle = sweepAnim,
                                useCenter = false,
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Text(
                        text = "%$clampedPct",
                        style = TabularAmountStyle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBalanceHidden) "•••• ₺" else format.format(monthlySavingAmount),
                        style = TabularAmountStyle,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Bu ay biriken",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        // Card 2: Toplam Portföy
        CyberCard(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(22.dp))
                .clickable { onOpenSavings() },
            shape = RoundedCornerShape(22.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Portföy",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Portföy Detayı",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isBalanceHidden) "•••••• ₺" else format.format(grandPortfolio),
                style = TabularAmountStyle,
                fontSize = 19.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (portfolioCost > 0.0) {
                Surface(
                    color = (if (isProfit) mint else coral).copy(alpha = 0.13f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = String.format(
                            Locale("tr"),
                            "%s%.1f%% Net Getiri",
                            if (isProfit) "+" else "",
                            profitPct
                        ),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) mint else coral,
                        softWrap = false,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            } else {
                Text(
                    text = if (besTotal > 0) "BES birikimi dahil" else "Altın, Döviz & BES",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Interactive 6-Month Cashflow Chart:
 * Side-by-side Income & Expense bars + smooth Net Cash trend line.
 * Tapping any month column shows exact Gelir / Gider / Net breakdown in the header.
 */
@Composable
fun HomeInteractiveCashflowChart(
    labels: List<String>,
    incomes: List<Double>,
    expenses: List<Double>,
    isBalanceHidden: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val gridColor = if (isDark) Color(0xFF1F293D) else Color(0xFFE2E8F0)

    val count = minOf(labels.size, minOf(incomes.size, expenses.size))
    var selectedIdx by remember(count) { mutableIntStateOf((count - 1).coerceAtLeast(0)) }
    val hasData = remember(incomes, expenses) { incomes.any { it > 0.0 } || expenses.any { it > 0.0 } }

    CyberCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "6 Aylık Nakit Akışı",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (hasData && count > 0) {
                    val idx = selectedIdx.coerceIn(0, count - 1)
                    val selInc = incomes[idx]
                    val selExp = expenses[idx]
                    val selNet = selInc - selExp
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isBalanceHidden) {
                            "${labels[idx]} ayı seçili • Tutarlar gizli"
                        } else {
                            "${labels[idx]}: +${format.format(selInc)}  •  -${format.format(selExp)}  •  Net ${format.format(selNet)}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selNet >= 0) mint else coral,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LegendDotLabel("Gelir", mint)
                LegendDotLabel("Gider", coral)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!hasData || count == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Son 6 aya ait gelir veya gider hareketi henüz bulunmuyor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val maxVal = remember(incomes, expenses) {
                maxOf(incomes.maxOrNull() ?: 1.0, expenses.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
            }

            val selectedColumnTint = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.10f else 0.07f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
            ) {
                val w = size.width
                val h = size.height
                val bottomPad = 6.dp.toPx()
                val usableH = h - bottomPad - 10.dp.toPx()

                // Horizontal reference grid lines
                listOf(0.25f, 0.55f, 0.85f).forEach { lvl ->
                    val y = h - bottomPad - usableH * lvl
                    drawLine(
                        color = gridColor.copy(alpha = 0.7f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 0.8.dp.toPx()
                    )
                }

                val colW = w / count
                val barW = (colW * 0.24f).coerceAtMost(14.dp.toPx())
                val gap = 4.dp.toPx()
                val netPoints = mutableListOf<Offset>()

                for (i in 0 until count) {
                    val inc = incomes[i]
                    val exp = expenses[i]
                    val centerX = i * colW + colW / 2f
                    val isSel = i == selectedIdx

                    if (isSel) {
                        drawRoundRect(
                            color = selectedColumnTint,
                            topLeft = Offset(i * colW + 4.dp.toPx(), 0f),
                            size = Size(colW - 8.dp.toPx(), h),
                            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                        )
                    }

                    val incH = ((inc / maxVal) * usableH).toFloat().coerceAtLeast(if (inc > 0) 4.dp.toPx() else 0f)
                    if (incH > 0f) {
                        drawRoundRect(
                            color = if (isSel) mint else mint.copy(alpha = 0.65f),
                            topLeft = Offset(centerX - barW - gap / 2f, h - bottomPad - incH),
                            size = Size(barW, incH),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    val expH = ((exp / maxVal) * usableH).toFloat().coerceAtLeast(if (exp > 0) 4.dp.toPx() else 0f)
                    if (expH > 0f) {
                        drawRoundRect(
                            color = if (isSel) coral else coral.copy(alpha = 0.65f),
                            topLeft = Offset(centerX + gap / 2f, h - bottomPad - expH),
                            size = Size(barW, expH),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    val netVal = (inc - exp).coerceAtLeast(0.0)
                    val netY = h - bottomPad - ((netVal / maxVal) * usableH).toFloat()
                    netPoints.add(Offset(centerX, netY))
                }

                // Draw net trend line overlay
                if (netPoints.size >= 2) {
                    val path = Path().apply {
                        moveTo(netPoints.first().x, netPoints.first().y)
                        for (i in 1 until netPoints.size) {
                            val p0 = netPoints[i - 1]
                            val p1 = netPoints[i]
                            val midX = (p0.x + p1.x) / 2f
                            cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = sky,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                    netPoints.forEachIndexed { idx, pt ->
                        drawCircle(
                            color = if (idx == selectedIdx) sky else sky.copy(alpha = 0.8f),
                            radius = if (idx == selectedIdx) 4.5.dp.toPx() else 3.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Interactive Month Tap Selector Row
            Row(modifier = Modifier.fillMaxWidth()) {
                for (i in 0 until count) {
                    val isSel = i == selectedIdx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedIdx = i }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = labels[i],
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDotLabel(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Unified Banking Ledger Card for Recent Activity on Anasayfa.
 */
@Composable
fun HomeRecentLedgerCard(
    transactions: List<Transaction>,
    isBalanceHidden: Boolean,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }
    val dateSdf = remember { SimpleDateFormat("dd MMM", Locale("tr", "TR")) }
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val dividerColor = if (isDark) Color(0xFF1F293D) else Color(0xFFE2E8F0)

    CyberCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Son Hareketler",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(
                onClick = onOpenHistory,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Tüm Geçmiş", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        transactions.forEachIndexed { idx, tx ->
            val (accent, prefix, icon) = when (tx.type) {
                TransactionType.INCOME -> Triple(mint, "+", Icons.Rounded.NorthEast)
                TransactionType.EXPENSE -> Triple(coral, "-", Icons.Rounded.SouthWest)
                TransactionType.SAVING -> Triple(sky, "", Icons.Rounded.Savings)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accent.copy(alpha = if (isDark) 0.14f else 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val meta = buildString {
                        if (tx.category.isNotBlank() && !tx.category.equals(tx.title, ignoreCase = true)) {
                            append(tx.category)
                            append(" • ")
                        }
                        append(dateSdf.format(Date(tx.timestamp)))
                        if (!tx.person.isNullOrBlank()) {
                            append(" • ")
                            append(tx.person)
                        }
                    }
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (isBalanceHidden) "••••" else "$prefix${format.format(tx.amount)}",
                    style = TabularAmountStyle,
                    fontSize = 14.sp,
                    color = accent,
                    softWrap = false,
                    maxLines = 1
                )
            }

            if (idx < transactions.lastIndex) {
                HorizontalDivider(thickness = 0.6.dp, color = dividerColor)
            }
        }
    }
}
