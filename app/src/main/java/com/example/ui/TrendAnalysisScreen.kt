package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.data.TransactionType
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendAnalysisScreen(transactions: List<Transaction>) {
    val persons = remember(transactions) {
        listOf("Tüm Kişiler") + transactions.mapNotNull { it.person }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val categories = remember(transactions) {
        listOf("Tüm Kategoriler", "Tüm Gelirler", "Tüm Giderler", "Tüm Birikimler") + transactions.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }

    var selectedPerson by remember { mutableStateOf("Tüm Kişiler") }
    var selectedCategory by remember { mutableStateOf("Tüm Kategoriler") }

    var expandedPerson by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    val format = remember { getCurrencyFormat() }
    val monthFormat = remember { SimpleDateFormat("MMM yyyy", Locale("tr", "TR")) }

    val filteredTransactions = remember(transactions, selectedPerson, selectedCategory) {
        transactions.filter {
            val personMatch = (selectedPerson == "Tüm Kişiler" || it.person == selectedPerson)
            val categoryMatch = when (selectedCategory) {
                "Tüm Kategoriler" -> true
                "Tüm Gelirler" -> it.type == TransactionType.INCOME
                "Tüm Giderler" -> it.type == TransactionType.EXPENSE
                "Tüm Birikimler" -> it.type == TransactionType.SAVING
                else -> it.category == selectedCategory
            }
            personMatch && categoryMatch
        }
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

            val displayAmount: Double
            val displayColor: Color
            val displayTypeStr: String

            if (income > 0 && expense == 0.0 && saving == 0.0) {
                displayAmount = income
                displayColor = Color(0xFF16A34A) // Green
                displayTypeStr = "GELİR"
            } else if (expense > 0 && income == 0.0 && saving == 0.0) {
                displayAmount = expense
                displayColor = Color(0xFFDC2626) // Red
                displayTypeStr = "GİDER"
            } else if (saving > 0 && income == 0.0 && expense == 0.0) {
                displayAmount = saving
                displayColor = Color(0xFF2563EB) // Blue
                displayTypeStr = "BİRİKİM"
            } else {
                // Mixed
                val net = income - expense - saving
                displayAmount = abs(net)
                if (net >= 0) {
                    displayColor = Color(0xFF16A34A)
                    displayTypeStr = "NET GELİR"
                } else {
                    displayColor = Color(0xFFDC2626)
                    displayTypeStr = "NET GİDER"
                }
            }

            MonthlyTrendData(
                timestamp = time,
                displayAmount = displayAmount,
                displayColor = displayColor,
                displayTypeStr = displayTypeStr,
                diffFromPrevious = null,
                diffPercent = null
            )
        }.sortedBy { it.timestamp }

        // Calculate differences
        val withDiffs = sortedAsc.mapIndexed { index, data ->
            if (index == 0) {
                data
            } else {
                val prev = sortedAsc[index - 1]
                val diff = data.displayAmount - prev.displayAmount
                val percent = if (prev.displayAmount > 0) (diff / prev.displayAmount) * 100 else null
                data.copy(diffFromPrevious = diff, diffPercent = percent)
            }
        }

        withDiffs.sortedByDescending { it.timestamp }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExposedDropdownMenuBox(
                expanded = expandedPerson,
                onExpandedChange = { expandedPerson = !expandedPerson },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedPerson,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kişi") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPerson) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )
                ExposedDropdownMenu(
                    expanded = expandedPerson,
                    onDismissRequest = { expandedPerson = false }
                ) {
                    persons.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p, fontSize = 14.sp) },
                            onClick = {
                                selectedPerson = p
                                expandedPerson = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c, fontSize = 14.sp) },
                            onClick = {
                                selectedCategory = c
                                expandedCategory = false
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        if (monthlyData.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Seçilen filtrelere uygun veri bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TARİH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Text("DEĞER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text("DEĞİŞİM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(monthlyData) { data ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Date
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = monthFormat.format(Date(data.timestamp)).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // Amount
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = format.format(data.displayAmount),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = data.displayColor
                            )
                            Text(
                                text = data.displayTypeStr,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = data.displayColor.copy(alpha = 0.7f)
                            )
                        }

                        // Trend
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            if (data.diffFromPrevious != null) {
                                val diffColor = when {
                                    data.diffFromPrevious > 0 -> Color(0xFF16A34A)
                                    data.diffFromPrevious < 0 -> Color(0xFFDC2626)
                                    else -> Color.Gray
                                }
                                val icon = when {
                                    data.diffFromPrevious > 0 -> Icons.Default.ArrowUpward
                                    data.diffFromPrevious < 0 -> Icons.Default.ArrowDownward
                                    else -> Icons.Default.Remove
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = diffColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = format.format(abs(data.diffFromPrevious)),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = diffColor
                                    )
                                }
                                if (data.diffPercent != null) {
                                    Text(
                                        text = "%${String.format(Locale("tr"), "%.1f", abs(data.diffPercent))}",
                                        fontSize = 10.sp,
                                        color = diffColor.copy(alpha = 0.8f)
                                    )
                                }
                            } else {
                                Text(
                                    text = "-",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
                }
            }
        }
    }
}

data class MonthlyTrendData(
    val timestamp: Long,
    val displayAmount: Double,
    val displayColor: Color,
    val displayTypeStr: String,
    val diffFromPrevious: Double?,
    val diffPercent: Double?
)
