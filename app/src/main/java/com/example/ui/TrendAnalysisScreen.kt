package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.data.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendAnalysisScreen(transactions: List<Transaction>) {
    val persons = remember(transactions) {
        listOf("Tüm Kişiler") + transactions.mapNotNull { it.person }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val categories = remember(transactions) {
        listOf("Tüm Kategoriler") + transactions.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }

    var selectedPerson by remember { mutableStateOf("Tüm Kişiler") }
    var selectedCategory by remember { mutableStateOf("Tüm Kategoriler") }

    var expandedPerson by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    val format = remember { getCurrencyFormat() }
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("tr", "TR")) }

    val filteredTransactions = remember(transactions, selectedPerson, selectedCategory) {
        transactions.filter {
            (selectedPerson == "Tüm Kişiler" || it.person == selectedPerson) &&
            (selectedCategory == "Tüm Kategoriler" || it.category == selectedCategory)
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
        grouped.map { (time, txs) ->
            val income = txs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = txs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val saving = txs.filter { it.type == TransactionType.SAVING }.sumOf { it.amount }
            val savingQty = txs.filter { it.type == TransactionType.SAVING }.sumOf { it.quantity ?: 0.0 }
            MonthlySummary(time, income, expense, saving, savingQty)
        }.sortedByDescending { it.timestamp }
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
                    label = { Text("Kişi Seçimi") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPerson) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expandedPerson,
                    onDismissRequest = { expandedPerson = false }
                ) {
                    persons.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p) },
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
                    label = { Text("Kategori Seçimi") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c) },
                            onClick = {
                                selectedCategory = c
                                expandedCategory = false
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (monthlyData.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Seçilen filtrelere uygun veri bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(monthlyData) { data ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = monthFormat.format(Date(data.timestamp)).uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("GELİR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                    Text(format.format(data.income), fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("GİDER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                    Text(format.format(data.expense), fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text("BİRİKİM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                    Text(format.format(data.saving), fontWeight = FontWeight.Black, fontSize = 14.sp)
                                    if (data.savingQty > 0) {
                                        Text("${String.format(Locale("tr"), "%,.2f", data.savingQty)} Adet/Gr", fontSize = 10.sp, color = Color(0xFF2563EB))
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val net = data.income - data.expense - data.saving
                            val netColor = if (net >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                            Row(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("NET DURUM", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = (if (net > 0) "+" else "") + format.format(net),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = netColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class MonthlySummary(
    val timestamp: Long,
    val income: Double,
    val expense: Double,
    val saving: Double,
    val savingQty: Double
)
