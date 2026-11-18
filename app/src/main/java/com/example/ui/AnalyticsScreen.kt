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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    goldPrices: List<com.example.data.GoldPrice> = emptyList(),
    bankRates: List<com.example.data.BankRate> = emptyList(),
    preferenceManager: com.example.data.PreferenceManager? = null,
    ziraatRates: List<com.example.data.BankRate> = emptyList()
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Genel Bakış, 1: Kişi & Kategori, 2: Aylık Kıyas

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

    Column(modifier = Modifier.fillMaxSize()) {
        // Sleek Segmented Switcher (TopAppBar already displays "Gelir & Gider")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) CyberDarkSurface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                Triple(0, "Genel Bakış", Icons.Default.PieChart),
                Triple(1, "Kişi", Icons.Default.Person),
                Triple(2, "Aylık Kıyas", Icons.Rounded.TrendingUp)
            )
            tabs.forEach { (index, title, icon) ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
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

@Composable
fun GeneralAnalysisScreen(
    transactions: List<Transaction>,
    goldPrices: List<com.example.data.GoldPrice>,
    bankRates: List<com.example.data.BankRate>,
    preferenceManager: com.example.data.PreferenceManager?,
    ziraatRates: List<com.example.data.BankRate> = emptyList()
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
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

    // Interactive Donut Chart Category Switcher: 0 = Gider, 1 = Gelir, 2 = Birikim
    var selectedChartMode by remember { mutableIntStateOf(0) }

    // 6-Month Balance Data
    val monthSdf = remember { SimpleDateFormat("MMM", Locale("tr", "TR")) }
    val (sixLabels, sixIncomes, sixExpenses) = remember(transactions) {
        val lbls = mutableListOf<String>()
        val incs = mutableListOf<Double>()
        val exps = mutableListOf<Double>()
        for (i in 5 downTo 0) {
            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -i) }
            val start = cal.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val end = cal.apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.timeInMillis
            val mTxs = transactions.filter { it.timestamp in start..end }
            lbls.add(monthSdf.format(Date(start)))
            incs.add(mTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount })
            exps.add(mTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount })
        }
        Triple(lbls, incs, exps)
    }

    val neonIncome = if (isDark) CyberNeonMint else Color(0xFF059669)
    val neonExpense = if (isDark) CyberNeonCoral else Color(0xFFDC2626)
    val neonSaving = if (isDark) CyberNeonCyan else Color(0xFF0284C7)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 84.dp)
    ) {
        // --- 1. 1-TAP HORIZONTAL PERIOD FILTER STRIP ---
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(timeFilters) { tf ->
                    val selected = selectedTime == tf
                    FilterChip(
                        selected = selected,
                        onClick = { selectedTime = tf },
                        label = {
                            Text(
                                text = tf,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        // --- 2. UNIFIED CASHFLOW & HEALTH SCORE CARD ---
        item {
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
                healthScore >= 80 -> neonIncome
                healthScore >= 60 -> CyberNeonOrange
                healthScore > 0 -> neonExpense
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            val healthLabel = when (healthScore) {
                100 -> "Sağlık: $healthScore • Mükemmel"
                80 -> "Sağlık: $healthScore • İyi"
                60 -> "Sağlık: $healthScore • Orta"
                40 -> "Sağlık: $healthScore • Dikkat"
                20 -> "Sağlık: $healthScore • Riskli"
                else -> "Veri Bekleniyor"
            }

            val totalOutflowAndSav = maxOf(inc, exp + sav).coerceAtLeast(1.0)
            val expRatioBar = (exp / totalOutflowAndSav).toFloat().coerceIn(0f, 1f)
            val savRatioBar = (sav / totalOutflowAndSav).toFloat().coerceIn(0f, 1f - expRatioBar)
            val trackColor = if (isDark) Color(0xFF1B2338) else Color(0xFFE2E8F0)

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
                            text = "NET NAKİT AKIŞI ($selectedTime)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = format.format(bal),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = if (bal >= 0) neonIncome else neonExpense
                        )
                    }

                    Surface(
                        color = healthColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, healthColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = healthLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = healthColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stacked Cashflow Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(trackColor)
                ) {
                    if (expRatioBar > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(expRatioBar)
                                .fillMaxHeight()
                                .background(neonExpense)
                        )
                    }
                    if (savRatioBar > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(savRatioBar)
                                .fillMaxHeight()
                                .background(neonSaving)
                        )
                    }
                    val rem = (1f - expRatioBar - savRatioBar).coerceAtLeast(0f)
                    if (rem > 0f && inc > 0) {
                        Box(
                            modifier = Modifier
                                .weight(rem)
                                .fillMaxHeight()
                                .background(neonIncome)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CyberMiniMetricTile("GELİR", format.format(inc), neonIncome, Modifier.weight(1f))
                    CyberMiniMetricTile("GİDER", format.format(exp), neonExpense, Modifier.weight(1f))
                    CyberMiniMetricTile("BİRİKİM (%$savingRate)", format.format(sav), neonSaving, Modifier.weight(1f))
                }
            }
        }

        // --- 3. AKILLI FİNANSAL İÇGÖRÜLER (SMART PRACTICAL TOOLS) ---
        item {
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

        // --- 4. SINGLE INTERACTIVE GLOW DONUT CHART ([GİDER | GELİR | BİRİKİM]) ---
        item {
            val palette = listOf(
                CyberNeonMagenta,
                CyberNeonCyan,
                CyberNeonOrange,
                CyberNeonPurple,
                CyberNeonMint,
                Color(0xFF3B82F6),
                CyberNeonCoral
            )

            val expenseSegments = remember(filteredTx) {
                filteredTx.filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.category.ifBlank { "Diğer" } }
                    .mapValues { it.value.sumOf { tx -> tx.amount } }
                    .toList()
                    .sortedByDescending { it.second }
                    .take(6)
                    .mapIndexed { idx, (cat, amt) -> CyberDonutSegment(cat, amt, palette[idx % palette.size]) }
            }

            val incomeSegments = remember(filteredTx) {
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
            val savingSegments = remember(savingValuationList) {
                savingValuationList.take(6).mapIndexed { idx, (cat, valAmt, _) ->
                    CyberDonutSegment(cat, valAmt, palette[idx % palette.size])
                }
            }

            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Kategori Dağılımı",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (selectedChartMode == 2) "Güncel piyasa değerine göre (Tüm Zamanlar)" else "$selectedTime içindeki dağılım",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // In-card 3-way Segmented Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF1A2338) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(0 to "Gider", 1 to "Gelir", 2 to "Birikim").forEach { (idx, title) ->
                        val active = selectedChartMode == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { selectedChartMode = idx }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Maliyet", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(format.format(totalCost), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Net Getiri", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    val c = if (pnl >= 0) neonIncome else neonExpense
                                    Text(
                                        text = "${if (pnl >= 0) "+" else ""}${format.format(pnl)} (%${String.format(Locale.US, "%.1f", pnlPct)})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = c
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 5. 6 AYLIK GELİR & GİDER DENGESİ ---
        item {
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
private fun SmartInsightsCard(
    transactions: List<Transaction>,
    filteredTx: List<Transaction>,
    selectedTime: String,
    inc: Double,
    exp: Double,
    format: NumberFormat,
    isDark: Boolean
) {
    val cal = remember { Calendar.getInstance() }
    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH).coerceAtLeast(28)

    // 1. Daily burn rate & month-end projection (meaningful especially for "Bu Ay")
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

    // 2. Top expense category in filtered period
    val topExpenseCat = remember(filteredTx) {
        filteredTx.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category.ifBlank { "Diğer" } }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
            .maxByOrNull { it.value }
    }

    // 3. Unpaid bills in filtered period
    val unpaidList = remember(filteredTx) {
        filteredTx.filter { it.type == TransactionType.EXPENSE && !it.isPaid }
    }
    val unpaidSum = remember(unpaidList) { unpaidList.sumOf { it.amount } }

    // 4. Month-over-Month comparison for "Bu Ay"
    val prevMonthExpense = remember(transactions) {
        val prevBounds = getTimeBounds("Önceki Ay")
        transactions.filter { it.type == TransactionType.EXPENSE && it.timestamp in prevBounds.first until prevBounds.second }
            .sumOf { it.amount }
    }

    CyberCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(CyberNeonCyan.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    tint = if (isDark) CyberNeonCyan else Color(0xFF0284C7),
                    modifier = Modifier.size(17.dp)
                )
            }
            Column {
                Text(
                    text = "Akıllı Finansal Özet",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Kayıtlarınıza dayalı pratik hesaplamalar",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (exp <= 0.0 && inc <= 0.0) {
                Text(
                    text = "Bu dönemde henüz işlem yok. Gelir ve gider ekledikçe harcama hızı, ay sonu tahmini ve kategori payları burada otomatik hesaplanır.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Insight 1: Daily Burn Rate & Projection
                if (exp > 0.0) {
                    val subtitle = if (selectedTime == "Bu Ay") {
                        val fitText = if (inc > 0 && projectedMonthEnd > inc) " • Geliri aşabilir" else if (inc > 0) " • Bütçe içinde" else ""
                        "Ay sonu tahmini: ${format.format(projectedMonthEnd)}$fitText"
                    } else {
                        "Seçili dönem ortalama günlük gider"
                    }
                    SmartInsightRow(
                        icon = Icons.Rounded.Speed,
                        tint = CyberNeonCyan,
                        title = "Günlük Harcama Hızı: ${format.format(dailyAvg)} / gün",
                        subtitle = subtitle
                    )
                }

                // Insight 2: Top Category Share
                if (topExpenseCat != null && exp > 0.0) {
                    val sharePct = ((topExpenseCat.value / exp) * 100).toInt()
                    SmartInsightRow(
                        icon = Icons.Rounded.LocalFireDepartment,
                        tint = CyberNeonOrange,
                        title = "En Yüksek Gider: ${topExpenseCat.key} (%$sharePct)",
                        subtitle = "Toplam ${format.format(topExpenseCat.value)} harcama bu kalemde yoğunlaştı"
                    )
                }

                // Insight 3: Month-over-month diff
                if (selectedTime == "Bu Ay" && prevMonthExpense > 0.0 && exp > 0.0) {
                    val diffPct = (((exp - prevMonthExpense) / prevMonthExpense) * 100).toInt()
                    val isLower = diffPct <= 0
                    SmartInsightRow(
                        icon = if (isLower) Icons.Rounded.TrendingDown else Icons.Rounded.TrendingUp,
                        tint = if (isLower) CyberNeonMint else CyberNeonCoral,
                        title = if (isLower) "Geçen Aya Göre %${abs(diffPct)} Daha Az Harcama" else "Geçen Aya Göre %$diffPct Artış",
                        subtitle = "Önceki ay toplam gideri: ${format.format(prevMonthExpense)}"
                    )
                }

                // Insight 4: Unpaid bills
                SmartInsightRow(
                    icon = if (unpaidList.isEmpty()) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
                    tint = if (unpaidList.isEmpty()) CyberNeonMint else CyberNeonMagenta,
                    title = if (unpaidList.isEmpty()) "Bekleyen Ödeme Yok" else "${unpaidList.size} Bekleyen Ödeme (${format.format(unpaidSum)})",
                    subtitle = if (unpaidList.isEmpty()) "Bu dönemdeki tüm gider kayıtları ödendi işaretli" else "Ödemeler sekmesinden tek dokunuşla kapatabilirsiniz"
                )
            }
        }
    }
}

@Composable
private fun SmartInsightRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CyberMiniMetricTile(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = if (isDark) 0.12f else 0.08f))
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PersonAnalysisScreen(transactions: List<Transaction>) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
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

    val palette = listOf(CyberNeonMagenta, CyberNeonCyan, CyberNeonOrange, CyberNeonPurple, CyberNeonMint)
    val categorySegments = remember(filteredTxs) {
        filteredTxs.groupBy { it.category.ifBlank { "Diğer" } }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(6)
            .mapIndexed { idx, (cat, amt) -> CyberDonutSegment(cat, amt, palette[idx % palette.size]) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 84.dp)
    ) {
        // 1. 1-Tap Person Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(persons) { person ->
                    FilterChip(
                        selected = selectedPerson == person,
                        onClick = { selectedPerson = person },
                        label = { Text(person, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        // 2. Time + Type Filter Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    TransactionType.EXPENSE to "Gider",
                    TransactionType.INCOME to "Gelir",
                    TransactionType.SAVING to "Birikim"
                ).forEach { (type, label) ->
                    val active = selectedType == type
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedType = type },
                        color = if (active) MaterialTheme.colorScheme.primaryContainer else if (isDark) CyberDarkSurface else Color.White,
                        border = BorderStroke(
                            1.dp,
                            if (active) MaterialTheme.colorScheme.primary else if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(timeFilters) { tf ->
                    FilterChip(
                        selected = selectedTime == tf,
                        onClick = { selectedTime = tf },
                        label = { Text(tf, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // 3. KPI Hero Card
        item {
            val typeLabel = when (selectedType) {
                TransactionType.INCOME -> "TOPLAM GELİR"
                TransactionType.EXPENSE -> "TOPLAM HARCAMA"
                TransactionType.SAVING -> "TOPLAM BİRİKİM"
            }
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${selectedPerson.uppercase(Locale("tr"))} • $typeLabel",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = format.format(totalAmount),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${filteredTxs.size} işlem", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Ort: ${format.format(avgAmount)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 4. Category Glow Donut Breakdown
        item {
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Kategori Dağılımı",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                CyberGlowDonutContent(
                    segments = categorySegments,
                    totalLabel = "SEÇİLİ DÖNEM",
                    emptyMessage = "Seçilen kişi ve dönemde kayıt bulunamadı."
                )
            }
        }

        // 5. Top Transactions
        val topTxs = filteredTxs.sortedByDescending { it.amount }.take(6)
        if (topTxs.isNotEmpty()) {
            item {
                Text(
                    text = "En Büyük İşlemler",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("tr"))
            items(topTxs, key = { it.id }) { tx ->
                CyberCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${tx.category} • ${sdf.format(Date(tx.timestamp))}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = format.format(tx.amount),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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
