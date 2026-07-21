package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.data.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(transactions: List<Transaction>, onEdit: ((Transaction) -> Unit)? = null, onDelete: ((Transaction) -> Unit)? = null) {
    var searchQuery by remember { mutableStateOf("") }
    
    val timeFilters = listOf("Tüm Zamanlar", "Son 7 Gün", "Son 15 Gün", "Bu Ay", "Geçen Ay", "Son 3 Ay", "Son 6 Ay", "Bu Yıl")
    var selectedTimeFilter by remember { mutableStateOf(timeFilters[0]) }
    var expandedTimeFilter by remember { mutableStateOf(false) }

    var sortColumn by remember { mutableStateOf("Tarih") }
    var sortAscending by remember { mutableStateOf(false) }

    val typeFilters = listOf("Tüm Türler", "Gelir", "Gider", "Birikim")
    var selectedTypeFilter by remember { mutableStateOf(typeFilters[0]) }
    var expandedTypeFilter by remember { mutableStateOf(false) }

    val categoryFilters = listOf("Tüm Kategoriler") + transactions.mapNotNull { it.category }.distinct().sorted()
    var selectedCategoryFilter by remember { mutableStateOf("Tüm Kategoriler") }
    var expandedCategoryFilter by remember { mutableStateOf(false) }

    val filteredTransactions = remember(transactions, searchQuery, selectedTimeFilter, selectedTypeFilter, selectedCategoryFilter) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        
        val timeLimit = when (selectedTimeFilter) {
            "Son 7 Gün" -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                cal.timeInMillis
            }
            "Son 15 Gün" -> {
                cal.add(Calendar.DAY_OF_YEAR, -15)
                cal.timeInMillis
            }
            "Bu Ay" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "Geçen Ay" -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "Son 3 Ay" -> {
                cal.add(Calendar.MONTH, -2) // including current month
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "Son 6 Ay" -> {
                cal.add(Calendar.MONTH, -5) // including current month
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "Bu Yıl" -> {
                cal.timeInMillis = System.currentTimeMillis() // Reset to now
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            else -> 0L
        }

        val upperBound = when (selectedTimeFilter) {
            "Geçen Ay" -> {
                val calUpper = Calendar.getInstance()
                calUpper.set(Calendar.DAY_OF_MONTH, 1)
                calUpper.set(Calendar.HOUR_OF_DAY, 0)
                calUpper.set(Calendar.MINUTE, 0)
                calUpper.set(Calendar.SECOND, 0)
                calUpper.set(Calendar.MILLISECOND, 0)
                calUpper.timeInMillis
            }
            else -> Long.MAX_VALUE
        }

        val filtered = transactions.filter { tx ->
            val matchSearch = if (searchQuery.isNotBlank()) {
                tx.title.contains(searchQuery, true) ||
                (tx.person?.contains(searchQuery, true) ?: false)
            } else true

            val matchTime = tx.timestamp in timeLimit until upperBound
            
            val matchType = when (selectedTypeFilter) {
                "Gelir" -> tx.type == TransactionType.INCOME
                "Gider" -> tx.type == TransactionType.EXPENSE
                "Birikim" -> tx.type == TransactionType.SAVING
                else -> true
            }

            val matchCategory = if (selectedCategoryFilter != "Tüm Kategoriler") {
                tx.category == selectedCategoryFilter
            } else true

            matchSearch && matchTime && matchType && matchCategory
        }
        
        when (sortColumn) {
            "Tarih" -> if (sortAscending) filtered.sortedBy { it.timestamp } else filtered.sortedByDescending { it.timestamp }
            "Tür" -> if (sortAscending) filtered.sortedBy { it.type.name } else filtered.sortedByDescending { it.type.name }
            "Kategori" -> if (sortAscending) filtered.sortedBy { it.category ?: "" } else filtered.sortedByDescending { it.category ?: "" }
            "Tutar" -> if (sortAscending) filtered.sortedBy { it.amount } else filtered.sortedByDescending { it.amount }
            else -> filtered.sortedByDescending { it.timestamp }
        }
    }

    val totalIncome = filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val totalSaving = filteredTransactions.filter { it.type == TransactionType.SAVING }.sumOf { it.amount }

    val format = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("tr"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("İşlem Geçmişi", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Text("TÜM GELİR VE GİDER KAYITLARI", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Summary row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryBox(title = "TOPLAM GELİR", amount = format.format(totalIncome), bg = Color(0xFFF0FDF4), fg = Color(0xFF16A34A), modifier = Modifier.weight(1f))
            SummaryBox(title = "TOPLAM GİDER", amount = format.format(totalExpense), bg = Color(0xFFFFF1F2), fg = Color(0xFFE11D48), modifier = Modifier.weight(1f))
            SummaryBox(title = "TOPLAM BİRİKİM", amount = format.format(totalSaving), bg = Color(0xFFF0F9FF), fg = Color(0xFF0284C7), modifier = Modifier.weight(1f))
        }

        // Filters
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("İşlemlerde ara (Kişi, başlık)...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedTimeFilter,
                    onExpandedChange = { expandedTimeFilter = !expandedTimeFilter },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedTimeFilter,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTimeFilter) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTimeFilter,
                        onDismissRequest = { expandedTimeFilter = false }
                    ) {
                        timeFilters.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption, fontSize = 13.sp) },
                                onClick = {
                                    selectedTimeFilter = selectionOption
                                    expandedTimeFilter = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedTypeFilter,
                    onExpandedChange = { expandedTypeFilter = !expandedTypeFilter },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedTypeFilter,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeFilter) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTypeFilter,
                        onDismissRequest = { expandedTypeFilter = false }
                    ) {
                        typeFilters.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption, fontSize = 13.sp) },
                                onClick = {
                                    selectedTypeFilter = selectionOption
                                    expandedTypeFilter = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedCategoryFilter,
                    onExpandedChange = { expandedCategoryFilter = !expandedCategoryFilter },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedCategoryFilter,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryFilter) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategoryFilter,
                        onDismissRequest = { expandedCategoryFilter = false }
                    ) {
                        categoryFilters.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption, fontSize = 13.sp) },
                                onClick = {
                                    selectedCategoryFilter = selectionOption
                                    expandedCategoryFilter = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(0.dp))

        // Table Header
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            val updateSort: (String) -> Unit = { col ->
                if (sortColumn == col) sortAscending = !sortAscending
                else {
                    sortColumn = col
                    sortAscending = true
                }
            }
            Text("TARİH ${if(sortColumn=="Tarih") (if(sortAscending) "↑" else "↓") else ""}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f).clickable { updateSort("Tarih") })
            Text("TÜR ${if(sortColumn=="Tür") (if(sortAscending) "↑" else "↓") else ""}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.8f).clickable { updateSort("Tür") })
            Text("KATEGORİ ${if(sortColumn=="Kategori") (if(sortAscending) "↑" else "↓") else ""}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f).clickable { updateSort("Kategori") })
            Text("TUTAR ${if(sortColumn=="Tutar") (if(sortAscending) "↑" else "↓") else ""}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f).clickable { updateSort("Tutar") }, textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }
        HorizontalDivider()

        LazyColumn {
            items(filteredTransactions) { tx ->
                    var expandedMenu by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedMenu = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(sdf.format(Date(tx.timestamp)), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        
                        Box(modifier = Modifier.weight(0.8f)) {
                            val typeBg = when(tx.type) {
                                TransactionType.INCOME -> Color(0xFFDCFCE7)
                                TransactionType.EXPENSE -> Color(0xFFFFE4E6)
                                TransactionType.SAVING -> Color(0xFFE0F2FE)
                            }
                            val typeFg = when(tx.type) {
                                TransactionType.INCOME -> Color(0xFF16A34A)
                                TransactionType.EXPENSE -> Color(0xFFE11D48)
                                TransactionType.SAVING -> Color(0xFF0284C7)
                            }
                            val typeText = when(tx.type) {
                                TransactionType.INCOME -> "GELİR"
                                TransactionType.EXPENSE -> "GİDER"
                                TransactionType.SAVING -> "BİRİKİM"
                            }
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(typeBg).padding(horizontal=6.dp, vertical=2.dp)) {
                                Text(typeText, fontSize = 9.sp, fontWeight = FontWeight.Black, color = typeFg)
                            }
                        }

                        Text(tx.category ?: "-", fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.2f))
                        
                        Text(format.format(tx.amount), fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)

                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Düzenle") },
                                onClick = { 
                                    expandedMenu = false
                                    onEdit?.invoke(tx)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sil", color = MaterialTheme.colorScheme.error) },
                                onClick = { 
                                    expandedMenu = false
                                    onDelete?.invoke(tx)
                                }
                            )
                        }
                    }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
            }
            if (filteredTransactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Kriterlere uygun işlem bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryBox(title: String, amount: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Column {
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = fg.copy(alpha=0.8f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(amount, fontSize = 13.sp, fontWeight = FontWeight.Black, color = fg, maxLines = 1)
        }
    }
}
