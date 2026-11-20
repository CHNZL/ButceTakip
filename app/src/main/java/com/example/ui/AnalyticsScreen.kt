package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BankRate
import com.example.data.GoldPrice
import com.example.data.PreferenceManager
import com.example.data.Transaction
import com.example.data.TransactionType
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

fun getCurrencyFormat(): NumberFormat {
    return com.example.util.FormatUtil.getCurrencyFormat().apply {
        maximumFractionDigits = 0
    }
}

@Composable
fun AnalyticsScreen(
    transactions: List<Transaction>,
    goldPrices: List<GoldPrice> = emptyList(),
    bankRates: List<BankRate> = emptyList(),
    preferenceManager: PreferenceManager? = null,
    ziraatRates: List<BankRate> = emptyList()
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Genel Bakış, 1: Kişi, 2: Aylık Kıyas

    val currentMonthEnd = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val validTransactions = remember(transactions) {
        transactions.filter { it.timestamp < currentMonthEnd }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
        ) {
            // Flagship Segmented Tab Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.8.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        Triple(0, "Genel Bakış", Icons.Rounded.PieChart),
                        Triple(1, "Kişi", Icons.Rounded.Groups),
                        Triple(2, "Aylık Kıyas", Icons.Rounded.TrendingUp)
                    )
                    tabs.forEach { (index, title, icon) ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.20f else 0.13f)
                                    else Color.Transparent
                                )
                                .clickable { selectedTab = index }
                                .testTag("analytics_tab_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> GeneralAnalysisScreen(validTransactions, goldPrices, bankRates, preferenceManager, ziraatRates)
                1 -> PersonAnalysisScreen(validTransactions)
                else -> TrendAnalysisScreen(validTransactions, isDark = isDark)
            }
        }
    }
}

@Composable
fun GeneralAnalysisScreen(
    transactions: List<Transaction>,
    goldPrices: List<GoldPrice>,
    bankRates: List<BankRate>,
    preferenceManager: PreferenceManager?,
    ziraatRates: List<BankRate> = emptyList()
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)

    var selectedTime by remember { mutableStateOf("Bu Ay") }
    val timeFilters = listOf("Bu Ay", "Önceki Ay", "Son 3 Ay", "Son 6 Ay", "Bu Yıl", "Tümü")
    val format = remember { getCurrencyFormat() }

    val bounds = remember(selectedTime) {
        if (selectedTime == "Tümü") Pair(0L, Long.MAX_VALUE) else getTimeBounds(selectedTime)
    }
    val filteredTx = remember(transactions, bounds) {
        transactions.filter { it.timestamp in bounds.first until bounds.second }
    }

    val inc = remember(filteredTx) { filteredTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount } }
    val exp = remember(filteredTx) { filteredTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount } }
    val sav = remember(filteredTx) { filteredTx.filter { it.type == TransactionType.SAVING }.sumOf { it.amount } }
    val bal = inc - exp - sav

    var selectedChartMode by remember { mutableIntStateOf(0) } // 0: Gider, 1: Gelir, 2: Birikim

    val monthSdf = remember { SimpleDateFormat("MMM", Locale("tr", "TR")) }
    val (sixLabels, sixIncomes, sixExpenses) = remember(transactions) {
        val lbls = mutableListOf<String>()
        val incs = mutableListOf<Double>()
        val exps = mutableListOf<Double>()
        for (i in 5 downTo 0) {
            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -i) }
            val start = cal.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val end = cal.apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59)
            }.timeInMillis
            val mTxs = transactions.filter { it.timestamp in start..end }
            lbls.add(monthSdf.format(Date(start)))
            incs.add(mTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount })
            exps.add(mTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount })
        }
        Triple(lbls, incs, exps)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 6.dp, bottom = 96.dp)
    ) {
        // 1. PERIOD FILTER PILLS
        item(key = "analytics_periods") {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(timeFilters) { tf ->
                    val selected = selectedTime == tf
                    Surface(
                        onClick = { selectedTime = tf },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.8.dp, if (selected) MaterialTheme.colorScheme.primary else borderColor)
                    ) {
                        Text(
                            text = tf,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // 2. EXECUTIVE CASHFLOW & HEALTH SCORE HERO
        item(key = "analytics_cashflow_hero") {
            val savingRate = if (inc > 0) ((sav / inc) * 100).toInt().coerceIn(0, 999) else 0
            val healthScore = if (inc > 0) {
                val expRatio = exp / inc
                val savRatio = sav / inc
                when {
                    expRatio <= 0.6 && savRatio >= 0.2 -> 100
                    expRatio <= 0.7 && savRatio >= 0.1 -> 80
                    expRatio <= 0.85 -> 60
                    expRatio < 1.0 -> 40
                    else -> 20
                }
            } else 0
            val healthColor = when {
                healthScore >= 80 -> mint
                healthScore >= 60 -> amber
                healthScore > 0 -> coral
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            val healthLabel = when (healthScore) {
                100 -> "Skor $healthScore · Mükemmel"
                80 -> "Skor $healthScore · İyi"
                60 -> "Skor $healthScore · Dengeli"
                40 -> "Skor $healthScore · Dikkat"
                20 -> "Skor $healthScore · Riskli"
                else -> "Veri Bekleniyor"
            }

            val totalOutflowAndSav = maxOf(inc, exp + sav).coerceAtLeast(1.0)
            val expRatioBar = (exp / totalOutflowAndSav).toFloat().coerceIn(0f, 1f)
            val savRatioBar = (sav / totalOutflowAndSav).toFloat().coerceIn(0f, 1f - expRatioBar)
            val trackColor = if (isDark) Color(0xFF1B2436) else Color(0xFFE2E8F0)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.8.dp, borderColor),
                shadowElevation = if (isDark) 0.dp else 1.5.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = if (isDark) {
                                    listOf(Color(0xFF131B2A), MaterialTheme.colorScheme.surface)
                                } else {
                                    listOf((if (bal >= 0) mint else coral).copy(alpha = 0.05f), Color.White)
                                }
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NET NAKİT AKIŞI · ${selectedTime.uppercase(Locale("tr", "TR"))}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.8.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            color = healthColor.copy(alpha = if (isDark) 0.16f else 0.11f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = healthLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = healthColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = format.format(bal),
                        style = TabularHeroStyle,
                        color = if (bal >= 0) mint else coral
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(CircleShape)
                            .background(trackColor),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (expRatioBar > 0f) {
                            Box(modifier = Modifier.weight(expRatioBar).fillMaxHeight().clip(CircleShape).background(coral))
                        }
                        if (savRatioBar > 0f) {
                            Box(modifier = Modifier.weight(savRatioBar).fillMaxHeight().clip(CircleShape).background(sky))
                        }
                        val rem = (1f - expRatioBar - savRatioBar).coerceAtLeast(0f)
                        if (rem > 0f && inc > 0) {
                            Box(modifier = Modifier.weight(rem).fillMaxHeight().clip(CircleShape).background(mint))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 0.7.dp, color = borderColor.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnalyticsStatCell("Gelir", format.format(inc), mint, Modifier.weight(1f))
                        Box(modifier = Modifier.height(26.dp).width(0.8.dp).background(borderColor))
                        AnalyticsStatCell("Gider", format.format(exp), coral, Modifier.weight(1f))
                        Box(modifier = Modifier.height(26.dp).width(0.8.dp).background(borderColor))
                        AnalyticsStatCell("Birikim (%$savingRate)", format.format(sav), sky, Modifier.weight(1f))
                    }
                }
            }
        }

        // 3. SMART INSIGHTS CARD (GROUPED INSET STYLE)
        item(key = "analytics_insights") {
            SmartInsightsCard(
                transactions = transactions,
                filteredTx = filteredTx,
                selectedTime = selectedTime,
                inc = inc,
                exp = exp,
                format = format,
                isDark = isDark
            )
        }

        // 4. INTERACTIVE CATEGORY BREAKDOWN CARD
        item(key = "analytics_category_donut") {
            val palette = listOf(
                if (isDark) CyberNeonMint else Color(0xFF059669),
                if (isDark) CyberNeonCyan else Color(0xFF0284C7),
                if (isDark) CyberNeonOrange else Color(0xFFD97706),
                if (isDark) CyberNeonPurple else Color(0xFF6366F1),
                if (isDark) CyberNeonCoral else Color(0xFFE11D48),
                Color(0xFF0D9488)
            )

            val expenseSegments = remember(filteredTx, isDark) {
                filteredTx.filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.category.ifBlank { "Diğer" } }
                    .mapValues { it.value.sumOf { tx -> tx.amount } }
                    .toList()
                    .sortedByDescending { it.second }
                    .take(6)
                    .mapIndexed { idx, (cat, amt) -> CyberDonutSegment(cat, amt, palette[idx % palette.size]) }
            }

            val incomeSegments = remember(filteredTx, isDark) {
                filteredTx.filter { it.type == TransactionType.INCOME }
                    .groupBy { it.category.ifBlank { "Diğer" } }
                    .mapValues { it.value.sumOf { tx -> tx.amount } }
                    .toList()
                    .sortedByDescending { it.second }
                    .take(6)
                    .mapIndexed { idx, (cat, amt) -> CyberDonutSegment(cat, amt, palette[idx % palette.size]) }
            }

            val allSavingTxs = remember(transactions) { transactions.filter { it.type == TransactionType.SAVING } }
            val savingValuationList = remember(allSavingTxs, goldPrices, bankRates, ziraatRates) {
                allSavingTxs.groupBy { it.category.ifBlank { "Diğer" } }
                    .map { (cat, txList) ->
                        val qty = txList.sumOf { it.quantity ?: 0.0 }
                        val livePrice = resolveCurrentUnitPrice(cat, goldPrices, bankRates, preferenceManager, ziraatRates)
                        val latestPurchasePrice = txList.maxByOrNull { it.timestamp }?.unitPrice ?: 0.0
                        val unitPrice = livePrice ?: latestPurchasePrice
                        val currentValue = if (qty > 0.0 && unitPrice > 0.0) qty * unitPrice else txList.sumOf { it.amount }
                        val paidCost = txList.sumOf { it.amount }
                        Triple(cat, currentValue, paidCost)
                    }
                    .sortedByDescending { it.second }
            }
            val savingSegments = remember(savingValuationList, isDark) {
                savingValuationList.take(6).mapIndexed { idx, (cat, valAmt, _) ->
                    CyberDonutSegment(cat, valAmt, palette[idx % palette.size])
                }
            }

            Column {
                SettingsSectionHeader("Kategori Dağılımı")
                SettingsGroupCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (selectedChartMode == 2) "Güncel piyasa değerine göre (Tüm Zamanlar)" else "$selectedTime içindeki dağılım",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Segmented switcher
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            border = BorderStroke(0.7.dp, borderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(0 to "Gider", 1 to "Gelir", 2 to "Birikim").forEach { (idx, title) ->
                                    val active = selectedChartMode == idx
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(if (active) MaterialTheme.colorScheme.surface else Color.Transparent)
                                            .clickable { selectedChartMode = idx }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        when (selectedChartMode) {
                            0 -> CyberGlowDonutContent(
                                segments = expenseSegments,
                                totalLabel = "TOPLAM GİDER",
                                emptyMessage = "$selectedTime döneminde gider kaydı bulunmuyor."
                            )
                            1 -> CyberGlowDonutContent(
                                segments = incomeSegments,
                                totalLabel = "TOPLAM GELİR",
                                emptyMessage = "$selectedTime döneminde gelir kaydı bulunmuyor."
                            )
                            else -> {
                                CyberGlowDonutContent(
                                    segments = savingSegments,
                                    totalLabel = "PORTFÖY",
                                    emptyMessage = "Henüz kayıtlı birikim varlığı bulunmuyor."
                                )
                                if (savingValuationList.isNotEmpty()) {
                                    val totalCurrent = savingValuationList.sumOf { it.second }
                                    val totalCost = savingValuationList.sumOf { it.third }
                                    val pnl = totalCurrent - totalCost
                                    val pnlPct = if (totalCost > 0) (pnl / totalCost) * 100.0 else 0.0
                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(thickness = 0.7.dp, color = borderColor.copy(alpha = 0.75f))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Maliyet", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = format.format(totalCost),
                                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily, fontFeatureSettings = "tnum"),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Net Getiri", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            val c = if (pnl >= 0) mint else coral
                                            Text(
                                                text = "${if (pnl >= 0) "+" else ""}${format.format(pnl)} (%${String.format(Locale.US, "%.1f", pnlPct)})",
                                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily, fontFeatureSettings = "tnum"),
                                                fontWeight = FontWeight.Bold,
                                                color = c
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. 6-MONTH BALANCE CHART
        item(key = "analytics_six_month") {
            CyberIncomeExpenseBalanceCard(
                incomes = sixIncomes,
                expenses = sixExpenses,
                labels = sixLabels,
                title = "Son 6 Ay Gelir & Gider Dengesi"
            )
        }
    }
}

@Composable
private fun AnalyticsStatCell(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = JetBrainsMonoFamily,
                fontFeatureSettings = "tnum"
            ),
            fontWeight = FontWeight.Bold,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SmartInsightsCard(
    transactions: List<Transaction>,
    filteredTx: List<Transaction>,
    selectedTime: String,
    inc: Double,
    exp: Double,
    format: NumberFormat,
    isDark: Boolean
) {
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)

    val cal = remember { Calendar.getInstance() }
    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH).coerceAtLeast(28)

    val dailyAvg = if (selectedTime == "Bu Ay") exp / dayOfMonth else {
        val approxDays = when (selectedTime) {
            "Önceki Ay" -> 30
            "Son 3 Ay" -> 90
            "Son 6 Ay" -> 180
            "Bu Yıl" -> cal.get(Calendar.DAY_OF_YEAR).coerceAtLeast(1)
            else -> 30
        }
        exp / approxDays
    }
    val projectedMonthEnd = if (selectedTime == "Bu Ay") dailyAvg * daysInMonth else 0.0

    val topExpenseCat = remember(filteredTx) {
        filteredTx.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category.ifBlank { "Diğer" } }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
            .maxByOrNull { it.value }
    }

    val unpaidList = remember(filteredTx) {
        filteredTx.filter { it.type == TransactionType.EXPENSE && !it.isPaid }
    }
    val unpaidSum = remember(unpaidList) { unpaidList.sumOf { it.amount } }

    val prevMonthExpense = remember(transactions) {
        val prevBounds = getTimeBounds("Önceki Ay")
        transactions.filter { it.type == TransactionType.EXPENSE && it.timestamp in prevBounds.first until prevBounds.second }
            .sumOf { it.amount }
    }

    Column {
        SettingsSectionHeader("Akıllı Finansal Özet")
        SettingsGroupCard {
            if (exp <= 0.0 && inc <= 0.0) {
                Text(
                    text = "Bu dönemde henüz işlem yok. Gelir ve gider ekledikçe harcama hızı, ay sonu tahmini ve kategori payları burada otomatik hesaplanır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                val insights = buildList {
                    if (exp > 0.0) {
                        val subtitle = if (selectedTime == "Bu Ay") {
                            val fitText = if (inc > 0 && projectedMonthEnd > inc) " · Geliri aşabilir" else if (inc > 0) " · Bütçe içinde" else ""
                            "Ay sonu tahmini: ${format.format(projectedMonthEnd)}$fitText"
                        } else {
                            "Seçili dönem ortalama günlük harcama"
                        }
                        add(Triple(Icons.Rounded.Speed to sky, "Günlük Hız: ${format.format(dailyAvg)} / gün", subtitle))
                    }
                    if (topExpenseCat != null && exp > 0.0) {
                        val sharePct = ((topExpenseCat.value / exp) * 100).toInt()
                        add(Triple(Icons.Rounded.LocalFireDepartment to amber, "En Yüksek Gider: ${topExpenseCat.key} (%$sharePct)", "Toplam ${format.format(topExpenseCat.value)} harcama bu kalemde"))
                    }
                    if (selectedTime == "Bu Ay" && prevMonthExpense > 0.0 && exp > 0.0) {
                        val diffPct = (((exp - prevMonthExpense) / prevMonthExpense) * 100).toInt()
                        val isLower = diffPct <= 0
                        add(
                            Triple(
                                (if (isLower) Icons.Rounded.TrendingDown else Icons.Rounded.TrendingUp) to (if (isLower) mint else coral),
                                if (isLower) "Geçen Aya Göre %${abs(diffPct)} Tasarruf" else "Geçen Aya Göre %$diffPct Artış",
                                "Önceki ay toplam gideri: ${format.format(prevMonthExpense)}"
                            )
                        )
                    }
                    add(
                        Triple(
                            (if (unpaidList.isEmpty()) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule) to (if (unpaidList.isEmpty()) mint else amber),
                            if (unpaidList.isEmpty()) "Bekleyen Ödeme Yok" else "${unpaidList.size} Bekleyen Ödeme (${format.format(unpaidSum)})",
                            if (unpaidList.isEmpty()) "Bu dönemdeki tüm gider kayıtları ödendi işaretli" else "Ödemeler sekmesinden tek dokunuşla kapatabilirsiniz"
                        )
                    )
                }

                insights.forEachIndexed { idx, (iconPair, title, subtitle) ->
                    InsightInsetRow(
                        icon = iconPair.first,
                        tint = iconPair.second,
                        title = title,
                        subtitle = subtitle
                    )
                    if (idx != insights.lastIndex) {
                        SettingsInsetDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightInsetRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint.copy(alpha = if (isDark) 0.16f else 0.11f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PersonAnalysisScreen(transactions: List<Transaction>) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)
    val indigo = if (isDark) CyberNeonPurple else Color(0xFF6366F1)
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)

    val persons = remember(transactions) {
        listOf("Tümü") + transactions.mapNotNull { it.person }.filter { it.isNotBlank() }.distinct().sorted()
    }
    var selectedPerson by remember { mutableStateOf("Tümü") }
    var selectedTime by remember { mutableStateOf("Bu Ay") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    val format = remember { getCurrencyFormat() }
    val timeFilters = listOf("Bu Ay", "Önceki Ay", "Son 3 Ay", "Son 6 Ay", "Bu Yıl", "Tümü")

    val bounds = remember(selectedTime) {
        if (selectedTime == "Tümü") Pair(0L, Long.MAX_VALUE) else getTimeBounds(selectedTime)
    }
    val filteredTxs = remember(transactions, selectedPerson, selectedType, bounds) {
        transactions.filter {
            val personOk = selectedPerson == "Tümü" || it.person == selectedPerson
            personOk && it.type == selectedType && it.timestamp in bounds.first until bounds.second
        }
    }
    val totalAmount = remember(filteredTxs) { filteredTxs.sumOf { it.amount } }
    val avgAmount = if (filteredTxs.isNotEmpty()) totalAmount / filteredTxs.size else 0.0

    val palette = listOf(coral, sky, amber, indigo, mint)
    val categorySegments = remember(filteredTxs, isDark) {
        filteredTxs.groupBy { it.category.ifBlank { "Diğer" } }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(6)
            .mapIndexed { idx, (cat, amt) -> CyberDonutSegment(cat, amt, palette[idx % palette.size]) }
    }

    val activeAccent = when (selectedType) {
        TransactionType.INCOME -> mint
        TransactionType.EXPENSE -> coral
        TransactionType.SAVING -> sky
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 6.dp, bottom = 96.dp)
    ) {
        // 1. PERSON CHIPS
        item(key = "person_chips") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(persons) { person ->
                    val selected = selectedPerson == person
                    Surface(
                        onClick = { selectedPerson = person },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.8.dp, if (selected) MaterialTheme.colorScheme.primary else borderColor)
                    ) {
                        Text(
                            text = person,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // 2. TYPE SEGMENTED CONTROL + PERIOD PILLS
        item(key = "person_type_and_time") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            TransactionType.EXPENSE to "Gider",
                            TransactionType.INCOME to "Gelir",
                            TransactionType.SAVING to "Birikim"
                        ).forEach { (type, label) ->
                            val active = selectedType == type
                            val tabColor = when (type) {
                                TransactionType.INCOME -> mint
                                TransactionType.EXPENSE -> coral
                                TransactionType.SAVING -> sky
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (active) tabColor.copy(alpha = if (isDark) 0.20f else 0.12f) else Color.Transparent)
                                    .clickable { selectedType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                    color = if (active) tabColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(timeFilters) { tf ->
                        val selected = selectedTime == tf
                        Surface(
                            onClick = { selectedTime = tf },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) activeAccent.copy(alpha = if (isDark) 0.20f else 0.12f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.7.dp, if (selected) activeAccent else borderColor)
                        ) {
                            Text(
                                text = tf,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) activeAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. EXECUTIVE KPI HERO CARD
        item(key = "person_kpi") {
            val typeLabel = when (selectedType) {
                TransactionType.INCOME -> "TOPLAM GELİR"
                TransactionType.EXPENSE -> "TOPLAM HARCAMA"
                TransactionType.SAVING -> "TOPLAM BİRİKİM"
            }
            SettingsGroupCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${selectedPerson.uppercase(Locale("tr", "TR"))} · $typeLabel",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = format.format(totalAmount),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontFeatureSettings = "tnum"
                            ),
                            fontWeight = FontWeight.ExtraBold,
                            color = activeAccent
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${filteredTxs.size} işlem",
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily, fontFeatureSettings = "tnum"),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ort. ${format.format(avgAmount)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily, fontFeatureSettings = "tnum"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 4. CATEGORY BREAKDOWN
        item(key = "person_donut") {
            Column {
                SettingsSectionHeader("Kategori Dağılımı")
                SettingsGroupCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        CyberGlowDonutContent(
                            segments = categorySegments,
                            totalLabel = "SEÇİLİ DÖNEM",
                            emptyMessage = "Seçilen kişi ve dönemde kayıt bulunamadı."
                        )
                    }
                }
            }
        }

        // 5. TOP TRANSACTIONS IN SINGLE GROUPED CARD
        val topTxs = filteredTxs.sortedByDescending { it.amount }.take(6)
        if (topTxs.isNotEmpty()) {
            item(key = "person_top_txs") {
                val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale("tr", "TR")) }
                Column {
                    SettingsSectionHeader("En Büyük İşlemler")
                    SettingsGroupCard {
                        topTxs.forEachIndexed { idx, tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(activeAccent.copy(alpha = if (isDark) 0.16f else 0.11f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                                        fontWeight = FontWeight.Bold,
                                        color = activeAccent
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tx.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${tx.category} · ${sdf.format(Date(tx.timestamp))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = format.format(tx.amount),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (idx != topTxs.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 60.dp, end = 14.dp),
                                    thickness = 0.6.dp,
                                    color = borderColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getTimeBounds(filter: String): Pair<Long, Long> {
    val cal = Calendar.getInstance()
    var start = 0L
    var end = Long.MAX_VALUE

    when (filter) {
        "Bu Ay" -> {
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            start = cal.timeInMillis
        }
        "Önceki Ay" -> {
            cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            end = cal.timeInMillis
            cal.add(Calendar.MONTH, -1)
            start = cal.timeInMillis
        }
        "Son 3 Ay" -> {
            cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.MONTH, -2)
            start = cal.timeInMillis
        }
        "Son 6 Ay" -> {
            cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.MONTH, -5)
            start = cal.timeInMillis
        }
        "Son 1 Yıl" -> {
            cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.MONTH, -11)
            start = cal.timeInMillis
        }
        "Bu Yıl" -> {
            cal.set(Calendar.DAY_OF_YEAR, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            start = cal.timeInMillis
        }
        "Önceki Yıl" -> {
            cal.set(Calendar.DAY_OF_YEAR, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            end = cal.timeInMillis
            cal.add(Calendar.YEAR, -1)
            start = cal.timeInMillis
        }
    }
    return Pair(start, end)
}
