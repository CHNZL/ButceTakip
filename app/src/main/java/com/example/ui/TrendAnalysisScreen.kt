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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.data.TransactionType
import com.example.ui.theme.CyberNeonCoral
import com.example.ui.theme.CyberNeonCyan
import com.example.ui.theme.CyberNeonMint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun TrendAnalysisScreen(transactions: List<Transaction>, isDark: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f) {
    val persons = remember(transactions) {
        listOf("Tüm Kişiler") + transactions.mapNotNull { it.person }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val categories = remember(transactions) {
        listOf("Tümü", "Tüm Gelirler", "Tüm Giderler", "Tüm Birikimler") + transactions.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }

    var selectedPerson by remember { mutableStateOf("Tüm Kişiler") }
    var selectedCategory by remember { mutableStateOf("Tümü") }

    val format = remember { getCurrencyFormat() }
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("tr", "TR")) }

    val filteredTransactions = remember(transactions, selectedPerson, selectedCategory) {
        transactions.filter {
            val personMatch = (selectedPerson == "Tüm Kişiler" || it.person == selectedPerson)
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

    // Current Month vs Previous Month Comparison
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
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
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
            if (index == 0) {
                data
            } else {
                val prev = sortedAsc[index - 1]
                val diff = data.headlineValue - prev.headlineValue
                val percent = if (abs(prev.headlineValue) > 0.0) (diff / abs(prev.headlineValue)) * 100.0 else null
                data.copy(diffFromPrevious = diff, diffPercent = percent)
            }
        }

        withDiffs.sortedByDescending { it.timestamp }
    }

    val neonIncome = if (isDark) CyberNeonMint else Color(0xFF059669)
    val neonExpense = if (isDark) CyberNeonCoral else Color(0xFFDC2626)
    val neonSaving = if (isDark) CyberNeonCyan else Color(0xFF0284C7)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 84.dp)
    ) {
        // 1. 1-Tap Category / Type Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            if (persons.size > 2) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(persons) { person ->
                        FilterChip(
                            selected = selectedPerson == person,
                            onClick = { selectedPerson = person },
                            label = { Text(person, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // 2. Bu Ay vs Önceki Ay Quick Delta Card
        if (monthlyData.isNotEmpty()) {
            item {
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Bu Ay vs Önceki Ay",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ComparisonDeltaBox(
                            label = "GELİR",
                            current = thisMonthSummary.first,
                            previous = prevMonthSummary.first,
                            positiveIsGood = true,
                            format = format,
                            modifier = Modifier.weight(1f)
                        )
                        ComparisonDeltaBox(
                            label = "GİDER",
                            current = thisMonthSummary.second,
                            previous = prevMonthSummary.second,
                            positiveIsGood = false,
                            format = format,
                            modifier = Modifier.weight(1f)
                        )
                        ComparisonDeltaBox(
                            label = "BİRİKİM",
                            current = thisMonthSummary.third,
                            previous = prevMonthSummary.third,
                            positiveIsGood = true,
                            format = format,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (monthlyData.isEmpty()) {
            item {
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Seçilen filtrelere uygun aylık veri bulunamadı.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(monthlyData, key = { it.timestamp }) { row ->
                val accentColor = when {
                    row.singleType == TransactionType.INCOME -> neonIncome
                    row.singleType == TransactionType.EXPENSE -> neonExpense
                    row.singleType == TransactionType.SAVING -> neonSaving
                    row.headlineValue >= 0 -> neonIncome
                    else -> neonExpense
                }

                val subtitleTag = when (row.singleType) {
                    TransactionType.INCOME -> "GELİR"
                    TransactionType.EXPENSE -> "GİDER"
                    TransactionType.SAVING -> "BİRİKİM"
                    null -> "NET AKIŞ"
                }

                CyberCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = monthFormat.format(Date(row.timestamp)).replaceFirstChar { it.titlecase(Locale("tr")) },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${row.txCount} işlem • $subtitleTag",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = format.format(row.headlineValue),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = accentColor
                            )

                            if (row.diffFromPrevious != null) {
                                val isPositiveDiff = row.diffFromPrevious >= 0
                                // For pure EXPENSE filter, increase is bad (coral), decrease is good (mint)
                                val isGood = if (row.singleType == TransactionType.EXPENSE) !isPositiveDiff else isPositiveDiff
                                val diffColor = if (isGood) neonIncome else neonExpense
                                val arrow = if (isPositiveDiff) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward
                                val pctText = row.diffPercent?.let { " (%${com.example.util.FormatUtil.getNumberFormat(1).format(abs(it))})" } ?: ""

                                Surface(
                                    color = diffColor.copy(alpha = 0.12f),
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
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = diffColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (!row.isSingleType) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (row.income > 0) {
                                MiniBreakdownChip("+${format.format(row.income)}", neonIncome)
                            }
                            if (row.expense > 0) {
                                MiniBreakdownChip("-${format.format(row.expense)}", neonExpense)
                            }
                            if (row.saving > 0) {
                                MiniBreakdownChip("Birikim ${format.format(row.saving)}", neonSaving)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonDeltaBox(
    label: String,
    current: Double,
    previous: Double,
    positiveIsGood: Boolean,
    format: java.text.NumberFormat,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val pct = if (previous > 0.0) (((current - previous) / previous) * 100).toInt() else null
    val isGood = if (pct == null) true else if (positiveIsGood) pct >= 0 else pct <= 0
    val deltaColor = if (pct == null) MaterialTheme.colorScheme.onSurfaceVariant
    else if (isGood) (if (isDark) CyberNeonMint else Color(0xFF059669))
    else (if (isDark) CyberNeonCoral else Color(0xFFDC2626))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.45f else 0.55f))
            .padding(8.dp)
    ) {
        Column {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = format.format(current),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (pct == null) "Önceki: ${format.format(previous)}" else "${if (pct >= 0) "+" else ""}%$pct",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = deltaColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MiniBreakdownChip(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
