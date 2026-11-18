package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.data.TransactionType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class IncomeSortOption(val label: String) {
    NEWEST("En Yeni"),
    HIGHEST("En Yüksek"),
    LOWEST("En Düşük")
}

private fun isRegularIncome(tx: Transaction): Boolean {
    val haystack = "${tx.category} ${tx.title}".lowercase(Locale("tr", "TR"))
    return haystack.contains("maaş") ||
        haystack.contains("maas") ||
        haystack.contains("kira") ||
        haystack.contains("emekli") ||
        haystack.contains("sabit") ||
        haystack.contains("düzenli") ||
        haystack.contains("duzenli") ||
        haystack.contains("burs") ||
        haystack.contains("nafaka") ||
        (tx.installments != null && tx.installments > 1)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IncomesScreen(
    transactions: List<Transaction>,
    onAddIncome: (() -> Unit)? = null,
    onEditIncome: ((Transaction) -> Unit)? = null,
    onDeleteIncome: ((Transaction) -> Unit)? = null,
    onRepeatIncome: ((Transaction) -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)
    val indigo = if (isDark) CyberNeonPurple else Color(0xFF6366F1)
    val dividerColor = if (isDark) Color(0xFF1F293D) else Color(0xFFE2E8F0)

    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale("tr", "TR")) }
    val monthYearSdf = remember { SimpleDateFormat("MMMM yyyy", Locale("tr", "TR")) }
    val shortMonthSdf = remember { SimpleDateFormat("MMM", Locale("tr", "TR")) }
    val currentMonthName = remember { monthYearSdf.format(Date()).uppercase(Locale("tr", "TR")) }

    val timeBounds = remember {
        val calThisStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val thisStart = calThisStart.timeInMillis
        val thisEnd = (calThisStart.clone() as Calendar).apply { add(Calendar.MONTH, 1) }.timeInMillis
        val prevStart = (calThisStart.clone() as Calendar).apply { add(Calendar.MONTH, -1) }.timeInMillis
        val threeMStart = (calThisStart.clone() as Calendar).apply { add(Calendar.MONTH, -2) }.timeInMillis
        listOf(thisStart, thisEnd, prevStart, threeMStart)
    }
    val thisMonthStart = timeBounds[0]
    val thisMonthEnd = timeBounds[1]
    val prevMonthStart = timeBounds[2]
    val threeMonthsStart = timeBounds[3]

    val incomeTx = remember(transactions) {
        transactions.filter { it.type == TransactionType.INCOME }
    }

    val thisMonthIncomes = remember(incomeTx, thisMonthStart, thisMonthEnd) {
        incomeTx.filter { it.timestamp in thisMonthStart until thisMonthEnd }
    }
    val prevMonthIncomes = remember(incomeTx, prevMonthStart, thisMonthStart) {
        incomeTx.filter { it.timestamp in prevMonthStart until thisMonthStart }
    }

    val totalThisMonth = remember(thisMonthIncomes) { thisMonthIncomes.sumOf { it.amount } }
    val totalPrevMonth = remember(prevMonthIncomes) { prevMonthIncomes.sumOf { it.amount } }
    val momPct = remember(totalThisMonth, totalPrevMonth) {
        if (totalPrevMonth > 0.0) ((totalThisMonth - totalPrevMonth) / totalPrevMonth) * 100.0 else null
    }

    // 6-Month Income Trend Series
    val sixMonthSeries = remember(incomeTx) {
        (5 downTo 0).map { offset ->
            val c = Calendar.getInstance().apply {
                add(Calendar.MONTH, -offset)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = c.timeInMillis
            val end = (c.clone() as Calendar).apply { add(Calendar.MONTH, 1) }.timeInMillis
            val sum = incomeTx.filter { it.timestamp in start until end }.sumOf { it.amount }
            shortMonthSdf.format(Date(start)) to sum
        }
    }

    var isBalanceHidden by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf("Bu Ay") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedPerson by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(IncomeSortOption.NEWEST) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showBreakdownDetails by remember { mutableStateOf(true) }

    val periodIncomes = remember(incomeTx, selectedPeriod, thisMonthStart, thisMonthEnd, prevMonthStart, threeMonthsStart) {
        when (selectedPeriod) {
            "Bu Ay" -> incomeTx.filter { it.timestamp in thisMonthStart until thisMonthEnd }
            "Önceki Ay" -> incomeTx.filter { it.timestamp in prevMonthStart until thisMonthStart }
            "3 Ay" -> incomeTx.filter { it.timestamp in threeMonthsStart until thisMonthEnd }
            else -> incomeTx
        }
    }

    val uniqueCategories = remember(periodIncomes) {
        periodIncomes.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val uniquePersons = remember(periodIncomes) {
        periodIncomes.mapNotNull { it.person?.takeIf { p -> p.isNotBlank() } }.distinct().sorted()
    }

    val categoryBreakdown = remember(periodIncomes) {
        val total = periodIncomes.sumOf { it.amount }.coerceAtLeast(1.0)
        periodIncomes
            .groupBy { it.category.ifBlank { "Diğer" } }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                Triple(cat, sum, (sum / total).toFloat().coerceIn(0f, 1f))
            }
            .sortedByDescending { it.second }
    }

    val personBreakdown = remember(periodIncomes) {
        val total = periodIncomes.sumOf { it.amount }.coerceAtLeast(1.0)
        periodIncomes
            .filter { !it.person.isNullOrBlank() }
            .groupBy { it.person!!.trim() }
            .map { (person, list) ->
                val sum = list.sumOf { it.amount }
                Triple(person, sum, (sum / total).toFloat().coerceIn(0f, 1f))
            }
            .sortedByDescending { it.second }
    }

    val displayedIncomes = remember(periodIncomes, selectedCategory, selectedPerson, searchQuery, sortOption) {
        val q = searchQuery.trim().lowercase(Locale("tr", "TR"))
        val filtered = periodIncomes.filter { tx ->
            val catOk = selectedCategory == null || tx.category == selectedCategory
            val personOk = selectedPerson == null || tx.person == selectedPerson
            val searchOk = q.isEmpty() ||
                tx.title.lowercase(Locale("tr", "TR")).contains(q) ||
                tx.category.lowercase(Locale("tr", "TR")).contains(q) ||
                (tx.person?.lowercase(Locale("tr", "TR"))?.contains(q) == true)
            catOk && personOk && searchOk
        }
        when (sortOption) {
            IncomeSortOption.NEWEST -> filtered.sortedByDescending { it.timestamp }
            IncomeSortOption.HIGHEST -> filtered.sortedByDescending { it.amount }
            IncomeSortOption.LOWEST -> filtered.sortedBy { it.amount }
        }
    }

    val displayedTotal = remember(displayedIncomes) { displayedIncomes.sumOf { it.amount } }
    val regularTotal = remember(displayedIncomes) {
        displayedIncomes.filter { isRegularIncome(it) }.sumOf { it.amount }
    }
    val extraTotal = (displayedTotal - regularTotal).coerceAtLeast(0.0)
    val avgDisplayed = if (displayedIncomes.isNotEmpty()) displayedTotal / displayedIncomes.size else 0.0
    val maxDisplayedTx = remember(displayedIncomes) { displayedIncomes.maxByOrNull { it.amount } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 6.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- 1. PERIOD SELECTOR SEGMENTED CAPSULE ---
        item {
            val periods = listOf("Bu Ay", "Önceki Ay", "3 Ay", "Tümü")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) CyberDarkSurface else Color(0xFFEAEFF5))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                periods.forEach { period ->
                    val selected = selectedPeriod == period
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) {
                                    if (isDark) CyberDarkSurfaceVariant else Color.White
                                } else Color.Transparent
                            )
                            .clickable {
                                selectedPeriod = period
                                selectedCategory = null
                                selectedPerson = null
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = period,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            softWrap = false,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // --- 2. EXECUTIVE INCOME HERO CARD (Predictability + MoM Growth + 3-Col Ledger Footer) ---
        item {
            val regularRatio = if (displayedTotal > 0.0) (regularTotal / displayedTotal).toFloat().coerceIn(0f, 1f) else 0f
            val regularPct = (regularRatio * 100).toInt()
            val extraPct = (100 - regularPct).coerceAtLeast(0)
            val trackBg = if (isDark) Color(0xFF1B2436) else Color(0xFFE8EEF5)

            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(20.dp)
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
                            text = "TOPLAM GELİR",
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
                                text = if (selectedPeriod == "Bu Ay") currentMonthName else selectedPeriod.uppercase(Locale("tr", "TR")),
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
                        if (momPct != null && selectedPeriod == "Bu Ay") {
                            val isUp = momPct >= 0.0
                            val badgeColor = if (isUp) mint else coral
                            Surface(
                                color = badgeColor.copy(alpha = 0.13f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = String.format(Locale("tr"), "%s%.1f%%", if (isUp) "↗ +" else "↘ ", momPct),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor,
                                    softWrap = false,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = { isBalanceHidden = !isBalanceHidden },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("incomes_visibility_toggle")
                        ) {
                            Icon(
                                imageVector = if (isBalanceHidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = "Gelirleri Gizle veya Göster",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isBalanceHidden) "•••••••• ₺" else format.format(displayedTotal),
                    style = TabularHeroStyle,
                    fontSize = 36.sp,
                    color = mint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${displayedIncomes.size} gelir kaydı • Ortalama ${if (isBalanceHidden) "••••" else format.format(avgDisplayed)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Predictability Split Bar (Düzenli Maaş vs Ek Gelir)
                if (displayedTotal > 0.0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(trackBg)
                    ) {
                        if (regularRatio > 0f) {
                            Box(modifier = Modifier.weight(regularRatio.coerceAtLeast(0.01f)).fillMaxHeight().background(mint))
                        }
                        if (regularRatio < 1f) {
                            Box(modifier = Modifier.weight((1f - regularRatio).coerceAtLeast(0.01f)).fillMaxHeight().background(sky))
                        }
                    }
                    Spacer(modifier = Modifier.height(7.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(mint))
                            Text(
                                text = "Düzenli %$regularPct (${if (isBalanceHidden) "••••" else format.format(regularTotal)})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(sky))
                            Text(
                                text = "Ek %$extraPct (${if (isBalanceHidden) "••••" else format.format(extraTotal)})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 0.7.dp, color = dividerColor)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IncomeFooterCol(
                        label = "Bu Ay",
                        value = if (isBalanceHidden) "••••" else format.format(totalThisMonth),
                        accentColor = mint,
                        modifier = Modifier.weight(1f)
                    )
                    Box(modifier = Modifier.width(0.8.dp).height(30.dp).background(dividerColor))
                    IncomeFooterCol(
                        label = "Geçen Ay",
                        value = if (isBalanceHidden) "••••" else format.format(totalPrevMonth),
                        accentColor = sky,
                        modifier = Modifier.weight(1f)
                    )
                    Box(modifier = Modifier.width(0.8.dp).height(30.dp).background(dividerColor))
                    IncomeFooterCol(
                        label = "En Yüksek",
                        value = if (isBalanceHidden) "••••" else if (maxDisplayedTx != null) format.format(maxDisplayedTx.amount) else "0 ₺",
                        accentColor = amber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- 3. INTERACTIVE 6-MONTH INCOME TREND CHART ---
        if (sixMonthSeries.any { it.second > 0.0 }) {
            item {
                IncomeSixMonthTrendCard(
                    series = sixMonthSeries,
                    isBalanceHidden = isBalanceHidden,
                    mint = mint,
                    isDark = isDark,
                    format = format
                )
            }
        }

        // --- 4. FAMILY CONTRIBUTION & INCOME SOURCES INSIGHTS CARD ---
        if (personBreakdown.size >= 2 || categoryBreakdown.size >= 2) {
            item {
                val trackColor = if (isDark) Color(0xFF1B2436) else Color(0xFFE8EEF5)
                val palette = listOf(mint, sky, indigo, amber, coral)

                CyberCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBreakdownDetails = !showBreakdownDetails },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (personBreakdown.size >= 2) "Aile Katkısı & Kaynaklar" else "Kaynak Dağılımı",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = if (showBreakdownDetails) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = if (showBreakdownDetails) "Daralt" else "Genişlet",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = showBreakdownDetails) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Family Member Proportional Bar (when 2+ contributors exist)
                            if (personBreakdown.size >= 2) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "KİŞİ KATKISI",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(CircleShape)
                                            .background(trackColor),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        personBreakdown.forEachIndexed { idx, (_, _, ratio) ->
                                            val c = palette[idx % palette.size]
                                            Box(
                                                modifier = Modifier
                                                    .weight(ratio.coerceAtLeast(0.02f))
                                                    .fillMaxHeight()
                                                    .background(c)
                                            )
                                        }
                                    }
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        personBreakdown.forEachIndexed { idx, (person, sum, ratio) ->
                                            val c = palette[idx % palette.size]
                                            val isSelected = selectedPerson == person
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        selectedPerson = if (isSelected) null else person
                                                    }
                                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(c))
                                                Text(
                                                    text = "$person %${(ratio * 100).toInt()} (${if (isBalanceHidden) "••••" else format.format(sum)})",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                    softWrap = false,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                                if (categoryBreakdown.size >= 2) {
                                    HorizontalDivider(thickness = 0.6.dp, color = dividerColor)
                                }
                            }

                            // Interactive Category Source Breakdown
                            if (categoryBreakdown.size >= 2) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    if (personBreakdown.size >= 2) {
                                        Text(
                                            text = "KAYNAK DAĞILIMI (FİLTRELEMEK İÇİN DOKUN)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.6.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    categoryBreakdown.take(5).forEachIndexed { idx, (cat, amount, ratio) ->
                                        val color = palette[idx % palette.size]
                                        val isCatSelected = selectedCategory == cat
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    selectedCategory = if (isCatSelected) null else cat
                                                }
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                                                    Text(
                                                        text = cat,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isCatSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Text(
                                                    text = "%${(ratio * 100).toInt()} • ${if (isBalanceHidden) "••••" else format.format(amount)}",
                                                    style = TabularAmountStyle,
                                                    fontSize = 12.sp,
                                                    color = color,
                                                    softWrap = false,
                                                    maxLines = 1
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(5.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(5.dp)
                                                    .clip(CircleShape)
                                                    .background(trackColor)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(ratio.coerceAtLeast(0.03f))
                                                        .clip(CircleShape)
                                                        .background(color)
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
        }

        // --- 5. COMPACT SEARCH, SORT & FILTER BAR ---
        if (periodIncomes.isNotEmpty() || searchQuery.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Gelirlerde ara...", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Temizle", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDark) CyberDarkSurface else Color.White,
                                unfocusedContainerColor = if (isDark) CyberDarkSurface else Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)
                            )
                        )

                        Box {
                            Surface(
                                onClick = { showSortMenu = true },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isDark) CyberDarkSurface else Color.White,
                                border = BorderStroke(0.8.dp, if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.SwapVert,
                                        contentDescription = "Sırala",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = sortOption.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        softWrap = false,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                IncomeSortOption.entries.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt.label, fontWeight = if (opt == sortOption) FontWeight.Bold else FontWeight.Normal) },
                                        leadingIcon = {
                                            if (opt == sortOption) {
                                                Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            }
                                        },
                                        onClick = {
                                            sortOption = opt
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (uniqueCategories.size > 1 || uniquePersons.size > 1) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (selectedCategory != null || selectedPerson != null) {
                                item {
                                    AssistChip(
                                        onClick = {
                                            selectedCategory = null
                                            selectedPerson = null
                                        },
                                        label = { Text("Filtreyi Sıfırla", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        leadingIcon = { Icon(Icons.Rounded.ClearAll, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                            if (uniqueCategories.size > 1) {
                                items(uniqueCategories) { cat ->
                                    FilterChip(
                                        selected = selectedCategory == cat,
                                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                                        label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                            if (uniquePersons.size > 1) {
                                items(uniquePersons) { person ->
                                    FilterChip(
                                        selected = selectedPerson == person,
                                        onClick = { selectedPerson = if (selectedPerson == person) null else person },
                                        label = { Text(person, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 6. BANK LEDGER GROUPED INCOME LIST ---
        if (displayedIncomes.isEmpty()) {
            item {
                CyberCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = PaddingValues(28.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(mint.copy(alpha = 0.13f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = mint,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedCategory != null || selectedPerson != null) {
                                "Filtreye uygun gelir bulunamadı."
                            } else {
                                "Bu dönem için gelir kaydı yok."
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Maaş, kira veya ek gelir ekleyerek aile nakit akışını canlı takip edebilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (onAddIncome != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = onAddIncome,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("empty_add_income_btn")
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gelir Ekle", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            val groupedByMonth = displayedIncomes.groupBy { tx ->
                val c = Calendar.getInstance().apply {
                    timeInMillis = tx.timestamp
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                c.timeInMillis
            }

            if (sortOption == IncomeSortOption.NEWEST) {
                groupedByMonth.forEach { (monthMs, monthList) ->
                    val monthTitle = monthYearSdf.format(Date(monthMs)).uppercase(Locale("tr", "TR"))
                    val monthSum = monthList.sumOf { it.amount }
                    item(key = "ledger_$monthMs") {
                        IncomeMonthLedgerCard(
                            headerTitle = monthTitle,
                            headerCountText = "${monthList.size} kayıt",
                            headerTotalText = if (isBalanceHidden) "••••" else "+${format.format(monthSum)}",
                            transactions = monthList,
                            sdf = sdf,
                            format = format,
                            isBalanceHidden = isBalanceHidden,
                            mint = mint,
                            dividerColor = dividerColor,
                            isDark = isDark,
                            onEdit = onEditIncome,
                            onDelete = onDeleteIncome,
                            onRepeat = onRepeatIncome
                        )
                    }
                }
            } else {
                item(key = "ledger_sorted") {
                    IncomeMonthLedgerCard(
                        headerTitle = "GELİR DEFTERİ",
                        headerCountText = "${displayedIncomes.size} kayıt",
                        headerTotalText = if (isBalanceHidden) "••••" else "+${format.format(displayedTotal)}",
                        transactions = displayedIncomes,
                        sdf = sdf,
                        format = format,
                        isBalanceHidden = isBalanceHidden,
                        mint = mint,
                        dividerColor = dividerColor,
                        isDark = isDark,
                        onEdit = onEditIncome,
                        onDelete = onDeleteIncome,
                        onRepeat = onRepeatIncome
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeFooterCol(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(accentColor))
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
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            softWrap = false,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Interactive 6-Month Income Bar Chart with Dashed Average Benchmark Line.
 */
@Composable
private fun IncomeSixMonthTrendCard(
    series: List<Pair<String, Double>>,
    isBalanceHidden: Boolean,
    mint: Color,
    isDark: Boolean,
    format: java.text.NumberFormat
) {
    var selectedIdx by remember(series.size) { mutableIntStateOf((series.size - 1).coerceAtLeast(0)) }
    val nonZeroAvg = remember(series) {
        val active = series.map { it.second }.filter { it > 0.0 }
        if (active.isNotEmpty()) active.average() else 0.0
    }
    val maxVal = remember(series, nonZeroAvg) {
        maxOf(series.maxOfOrNull { it.second } ?: 1.0, nonZeroAvg).coerceAtLeast(1.0)
    }
    val avgLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)

    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "6 Aylık Gelir Trendi",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val (selLabel, selVal) = series[selectedIdx.coerceIn(0, series.lastIndex)]
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isBalanceHidden) {
                        "$selLabel ayı seçili • 6 ay ort. gizli"
                    } else {
                        "$selLabel: ${format.format(selVal)}  •  Ort: ${format.format(nonZeroAvg)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = mint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                color = if (isDark) CyberDarkSurfaceVariant else CyberLightSurfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "6 Ay Ort.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
        ) {
            val w = size.width
            val h = size.height
            val bottomPad = 4.dp.toPx()
            val usableH = h - bottomPad - 8.dp.toPx()
            val count = series.size.coerceAtLeast(1)
            val colW = w / count
            val barW = (colW * 0.42f).coerceAtMost(24.dp.toPx())

            series.forEachIndexed { i, (_, value) ->
                val x = i * colW + (colW - barW) / 2f
                val isSel = i == selectedIdx
                val barH = ((value / maxVal) * usableH).toFloat().coerceAtLeast(if (value > 0) 4.dp.toPx() else 2.dp.toPx())
                drawRoundRect(
                    color = when {
                        value <= 0.0 -> avgLineColor.copy(alpha = 0.25f)
                        isSel -> mint
                        else -> mint.copy(alpha = 0.48f)
                    },
                    topLeft = Offset(x, h - bottomPad - barH),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }

            if (nonZeroAvg > 0.0) {
                val avgY = h - bottomPad - ((nonZeroAvg / maxVal) * usableH).toFloat()
                drawLine(
                    color = avgLineColor,
                    start = Offset(0f, avgY),
                    end = Offset(w, avgY),
                    strokeWidth = 1.2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            series.forEachIndexed { idx, (lbl, _) ->
                val isSel = idx == selectedIdx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedIdx = idx }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lbl,
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) mint else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Clean Bank Statement Ledger Card grouping income rows with 0.6.dp hairline dividers.
 */
@Composable
private fun IncomeMonthLedgerCard(
    headerTitle: String,
    headerCountText: String,
    headerTotalText: String,
    transactions: List<Transaction>,
    sdf: SimpleDateFormat,
    format: java.text.NumberFormat,
    isBalanceHidden: Boolean,
    mint: Color,
    dividerColor: Color,
    isDark: Boolean,
    onEdit: ((Transaction) -> Unit)?,
    onDelete: ((Transaction) -> Unit)?,
    onRepeat: ((Transaction) -> Unit)?
) {
    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = headerTitle,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.7.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• $headerCountText",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = headerTotalText,
                style = TabularAmountStyle,
                fontSize = 13.sp,
                color = mint,
                softWrap = false,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(thickness = 0.8.dp, color = dividerColor)

        transactions.forEachIndexed { idx, tx ->
            IncomeLedgerRow(
                tx = tx,
                dateText = sdf.format(Date(tx.timestamp)),
                amountText = if (isBalanceHidden) "••••" else "+${format.format(tx.amount)}",
                mint = mint,
                isDark = isDark,
                onEdit = onEdit,
                onDelete = onDelete,
                onRepeat = onRepeat
            )
            if (idx < transactions.lastIndex) {
                HorizontalDivider(thickness = 0.6.dp, color = dividerColor)
            }
        }
    }
}

@Composable
private fun IncomeLedgerRow(
    tx: Transaction,
    dateText: String,
    amountText: String,
    mint: Color,
    isDark: Boolean,
    onEdit: ((Transaction) -> Unit)?,
    onDelete: ((Transaction) -> Unit)?,
    onRepeat: ((Transaction) -> Unit)?
) {
    var expandedMenu by remember { mutableStateOf(false) }
    val hasMenuActions = onEdit != null || onDelete != null || onRepeat != null

    val icon = when {
        tx.category.lowercase(Locale("tr", "TR")).contains("maaş") -> Icons.Rounded.AccountBalanceWallet
        tx.category.lowercase(Locale("tr", "TR")).contains("prim") || tx.category.lowercase(Locale("tr", "TR")).contains("bonus") -> Icons.Rounded.WorkspacePremium
        tx.category.lowercase(Locale("tr", "TR")).contains("kira") -> Icons.Rounded.Home
        tx.category.lowercase(Locale("tr", "TR")).contains("yatırım") || tx.category.lowercase(Locale("tr", "TR")).contains("faiz") || tx.category.lowercase(Locale("tr", "TR")).contains("temettü") -> Icons.Rounded.ShowChart
        else -> Icons.Rounded.TrendingUp
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = hasMenuActions) { expandedMenu = true }
            .padding(vertical = 11.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(mint.copy(alpha = if (isDark) 0.14f else 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = mint,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (!tx.person.isNullOrBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.11f),
                            shape = RoundedCornerShape(5.dp)
                        ) {
                            Text(
                                text = tx.person,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                softWrap = false,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                val meta = buildString {
                    if (tx.category.isNotBlank() && !tx.category.equals(tx.title, ignoreCase = true)) {
                        append(tx.category)
                        append(" • ")
                    }
                    append(dateText)
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
                text = amountText,
                style = TabularAmountStyle,
                fontSize = 14.sp,
                color = mint,
                softWrap = false,
                maxLines = 1
            )
        }

        if (hasMenuActions) {
            DropdownMenu(
                expanded = expandedMenu,
                onDismissRequest = { expandedMenu = false }
            ) {
                if (onEdit != null) {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        text = { Text("Düzenle") },
                        onClick = {
                            expandedMenu = false
                            onEdit(tx)
                        }
                    )
                }
                if (onRepeat != null) {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Rounded.Replay, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                        text = { Text("Bugüne Tekrarla") },
                        onClick = {
                            expandedMenu = false
                            onRepeat(tx)
                        }
                    )
                }
                if (onDelete != null) {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                        text = { Text("Sil", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            expandedMenu = false
                            onDelete(tx)
                        }
                    )
                }
            }
        }
    }
}
