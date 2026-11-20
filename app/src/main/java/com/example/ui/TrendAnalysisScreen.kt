package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.data.TransactionType
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun TrendAnalysisScreen(
    transactions: List<Transaction>,
    isDark: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f
) {
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)

    val persons = remember(transactions) {
        listOf("Tüm Kişiler") + transactions.mapNotNull { it.person }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val categories = remember(transactions) {
        listOf("Tümü", "Tüm Gelirler", "Tüm Giderler", "Tüm Birikimler") +
            transactions.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }

    var selectedPerson by remember { mutableStateOf("Tüm Kişiler") }
    var selectedCategory by remember { mutableStateOf("Tümü") }

    val format = remember { getCurrencyFormat() }
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("tr", "TR")) }

    val filteredTransactions = remember(transactions, selectedPerson, selectedCategory) {
        transactions.filter {
            val personMatch = selectedPerson == "Tüm Kişiler" || it.person == selectedPerson
            val categoryMatch = when (selectedCategory) {
                "Tümü" -> true
                "Tüm Gelirler" -> it.type == TransactionType.INCOME
                "Tüm Giderler" -> it.type == TransactionType.EXPENSE
                "Tüm Birikimler" -> it.type == TransactionType.SAVING
                else -> it.category == selectedCategory
            }
            personMatch && categoryMatch
        }
    }

    val (thisMonthSummary, prevMonthSummary) = remember(filteredTransactions) {
        val thisBounds = getTimeBounds("Bu Ay")
        val prevBounds = getTimeBounds("Önceki Ay")
        val cmTxs = filteredTransactions.filter { it.timestamp in thisBounds.first until thisBounds.second }
        val pmTxs = filteredTransactions.filter { it.timestamp in prevBounds.first until prevBounds.second }
        Triple(
            cmTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
            cmTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
            cmTxs.filter { it.type == TransactionType.SAVING }.sumOf { it.amount }
        ) to Triple(
            pmTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
            pmTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
            pmTxs.filter { it.type == TransactionType.SAVING }.sumOf { it.amount }
        )
    }

    val monthlyData = remember(filteredTransactions) {
        val grouped = filteredTransactions.groupBy {
            Calendar.getInstance().apply {
                timeInMillis = it.timestamp
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        val sortedAsc = grouped.map { (time, txs) ->
            val income = txs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = txs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val saving = txs.filter { it.type == TransactionType.SAVING }.sumOf { it.amount }

            val primaryValue = when {
                income > 0 && expense == 0.0 && saving == 0.0 -> income
                expense > 0 && income == 0.0 && saving == 0.0 -> expense
                saving > 0 && income == 0.0 && expense == 0.0 -> saving
                else -> income - expense - saving
            }

            MonthlyComparisonRow(
                timestamp = time,
                income = income,
                expense = expense,
                saving = saving,
                txCount = txs.size,
                headlineValue = primaryValue,
                isSingleType = (income > 0 && expense == 0.0 && saving == 0.0) ||
                    (expense > 0 && income == 0.0 && saving == 0.0) ||
                    (saving > 0 && income == 0.0 && expense == 0.0),
                singleType = when {
                    income > 0 && expense == 0.0 && saving == 0.0 -> TransactionType.INCOME
                    expense > 0 && income == 0.0 && saving == 0.0 -> TransactionType.EXPENSE
                    saving > 0 && income == 0.0 && expense == 0.0 -> TransactionType.SAVING
                    else -> null
                },
                diffFromPrevious = null,
                diffPercent = null
            )
        }.sortedBy { it.timestamp }

        val withDiffs = sortedAsc.mapIndexed { index, data ->
            if (index == 0) data else {
                val prev = sortedAsc[index - 1]
                val diff = data.headlineValue - prev.headlineValue
                val percent = if (abs(prev.headlineValue) > 0.0) (diff / abs(prev.headlineValue)) * 100.0 else null
                data.copy(diffFromPrevious = diff, diffPercent = percent)
            }
        }
        withDiffs.sortedByDescending { it.timestamp }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 6.dp, bottom = 96.dp)
    ) {
        // 1. FILTER STRIP
        item(key = "trend_filters") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val selected = selectedCategory == cat
                        Surface(
                            onClick = { selectedCategory = cat },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.8.dp, if (selected) MaterialTheme.colorScheme.primary else borderColor)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                if (persons.size > 2) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(persons) { person ->
                            val selected = selectedPerson == person
                            Surface(
                                onClick = { selectedPerson = person },
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) sky.copy(alpha = if (isDark) 0.20f else 0.12f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(0.7.dp, if (selected) sky else borderColor)
                            ) {
                                Text(
                                    text = person,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) sky else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. BU AY VS ÖNCEKİ AY COMPARISON CARD
        if (monthlyData.isNotEmpty()) {
            item(key = "trend_mom_card") {
                Column {
                    SettingsSectionHeader("Bu Ay vs Önceki Ay")
                    SettingsGroupCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ComparisonDeltaColumn(
                                label = "Gelir",
                                current = thisMonthSummary.first,
                                previous = prevMonthSummary.first,
                                positiveIsGood = true,
                                format = format,
                                mint = mint,
                                coral = coral,
                                modifier = Modifier.weight(1f)
                            )
                            Box(modifier = Modifier.height(42.dp).width(0.8.dp).background(borderColor))
                            ComparisonDeltaColumn(
                                label = "Gider",
                                current = thisMonthSummary.second,
                                previous = prevMonthSummary.second,
                                positiveIsGood = false,
                                format = format,
                                mint = mint,
                                coral = coral,
                                modifier = Modifier.weight(1f)
                            )
                            Box(modifier = Modifier.height(42.dp).width(0.8.dp).background(borderColor))
                            ComparisonDeltaColumn(
                                label = "Birikim",
                                current = thisMonthSummary.third,
                                previous = prevMonthSummary.third,
                                positiveIsGood = true,
                                format = format,
                                mint = mint,
                                coral = coral,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 3. MONTHLY COMPARISON LIST INSIDE GROUPED CARD
        if (monthlyData.isEmpty()) {
            item(key = "trend_empty") {
                SettingsGroupCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(34.dp)
                        )
                        Text(
                            text = "Seçilen filtrelere uygun aylık veri bulunamadı.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            item(key = "trend_monthly_list") {
                Column {
                    SettingsSectionHeader("Aylık Geçmiş (${monthlyData.size} Ay)")
                    SettingsGroupCard {
                        monthlyData.forEachIndexed { idx, row ->
                            val accentColor = when {
                                row.singleType == TransactionType.INCOME -> mint
                                row.singleType == TransactionType.EXPENSE -> coral
                                row.singleType == TransactionType.SAVING -> sky
                                row.headlineValue >= 0 -> mint
                                else -> coral
                            }
                            val subtitleTag = when (row.singleType) {
                                TransactionType.INCOME -> "Gelir"
                                TransactionType.EXPENSE -> "Gider"
                                TransactionType.SAVING -> "Birikim"
                                null -> "Net Akış"
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 13.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = monthFormat.format(Date(row.timestamp)).replaceFirstChar { it.titlecase(Locale("tr", "TR")) },
                                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${row.txCount} işlem · $subtitleTag",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = format.format(row.headlineValue),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = JetBrainsMonoFamily,
                                                fontFeatureSettings = "tnum"
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                        if (row.diffFromPrevious != null) {
                                            val isPositiveDiff = row.diffFromPrevious >= 0
                                            val isGood = if (row.singleType == TransactionType.EXPENSE) !isPositiveDiff else isPositiveDiff
                                            val diffColor = if (isGood) mint else coral
                                            val arrow = if (isPositiveDiff) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward
                                            val pctText = row.diffPercent?.let { " (%${com.example.util.FormatUtil.getNumberFormat(1).format(abs(it))})" } ?: ""
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Surface(
                                                color = diffColor.copy(alpha = if (isDark) 0.16f else 0.11f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Icon(arrow, contentDescription = null, tint = diffColor, modifier = Modifier.size(11.dp))
                                                    Text(
                                                        text = "${format.format(abs(row.diffFromPrevious))}$pctText",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontFamily = JetBrainsMonoFamily,
                                                            fontFeatureSettings = "tnum"
                                                        ),
                                                        fontWeight = FontWeight.Bold,
                                                        color = diffColor
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (!row.isSingleType) {
                                    Spacer(modifier = Modifier.height(9.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (row.income > 0) TrendMiniPill("+${format.format(row.income)}", mint, isDark)
                                        if (row.expense > 0) TrendMiniPill("-${format.format(row.expense)}", coral, isDark)
                                        if (row.saving > 0) TrendMiniPill("Birikim ${format.format(row.saving)}", sky, isDark)
                                    }
                                }
                            }

                            if (idx != monthlyData.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 14.dp),
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

@Composable
private fun ComparisonDeltaColumn(
    label: String,
    current: Double,
    previous: Double,
    positiveIsGood: Boolean,
    format: NumberFormat,
    mint: Color,
    coral: Color,
    modifier: Modifier = Modifier
) {
    val pct = if (previous > 0.0) (((current - previous) / previous) * 100).toInt() else null
    val isGood = if (pct == null) true else if (positiveIsGood) pct >= 0 else pct <= 0
    val deltaColor = if (pct == null) MaterialTheme.colorScheme.onSurfaceVariant else if (isGood) mint else coral

    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = format.format(current),
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = JetBrainsMonoFamily,
                fontFeatureSettings = "tnum"
            ),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (pct == null) "Önc: ${format.format(previous)}" else "${if (pct >= 0) "+" else ""}%$pct",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMonoFamily,
                fontFeatureSettings = "tnum"
            ),
            fontWeight = FontWeight.Bold,
            color = deltaColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TrendMiniPill(text: String, color: Color, isDark: Boolean) {
    Surface(
        color = color.copy(alpha = if (isDark) 0.15f else 0.10f),
        shape = RoundedCornerShape(7.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMonoFamily,
                fontFeatureSettings = "tnum"
            ),
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

data class MonthlyComparisonRow(
    val timestamp: Long,
    val income: Double,
    val expense: Double,
    val saving: Double,
    val txCount: Int,
    val headlineValue: Double,
    val isSingleType: Boolean,
    val singleType: TransactionType?,
    val diffFromPrevious: Double?,
    val diffPercent: Double?
)
