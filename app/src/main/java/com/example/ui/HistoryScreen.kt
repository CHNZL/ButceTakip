package com.example.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.data.Transaction
import com.example.data.TransactionType
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberNeonCoral
import com.example.ui.theme.CyberNeonCyan
import com.example.ui.theme.CyberNeonMint
import com.example.util.PdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class HistorySortOption(val label: String) {
    DATE_DESC("En Yeni"),
    DATE_ASC("En Eski"),
    AMOUNT_DESC("En Yüksek Tutar"),
    AMOUNT_ASC("En Düşük Tutar"),
    CATEGORY_ASC("Kategori (A-Z)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    transactions: List<Transaction>,
    onEdit: ((Transaction) -> Unit)? = null,
    onDelete: ((Transaction) -> Unit)? = null,
    onTogglePaid: ((Transaction, Boolean) -> Unit)? = null,
    onRepeat: ((Transaction) -> Unit)? = null,
    isDark: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f
) {
    var searchQuery by remember { mutableStateOf("") }

    val timeFilters = listOf("Tüm Zamanlar", "Son 7 Gün", "Son 15 Gün", "Bu Ay", "Geçen Ay", "Son 3 Ay", "Son 6 Ay", "Bu Yıl")
    var selectedTimeFilter by remember { mutableStateOf("Bu Ay") }
    var expandedTimeFilter by remember { mutableStateOf(false) }

    var sortOption by remember { mutableStateOf(HistorySortOption.DATE_DESC) }
    var expandedSortMenu by remember { mutableStateOf(false) }

    val typeFilters = listOf("Tümü", "Bekleyen", "Gelir", "Gider", "Birikim")
    var selectedTypeFilter by remember { mutableStateOf("Tümü") }

    var showPdfExportDialog by remember { mutableStateOf(false) }
    var pdfTransactionsToExport by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var pdfTitleToExport by remember { mutableStateOf("") }
    val context = LocalContext.current
    var pdfFileNameToExport by remember { mutableStateOf("") }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    val coroutineScope = rememberCoroutineScope()
    val pdfExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        PdfExporter.exportToPdf(pdfTransactionsToExport, pdfTitleToExport, out)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "PDF başarıyla kaydedildi.", Toast.LENGTH_LONG).show()
                        showPdfNotification(context, uri, pdfFileNameToExport)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "PDF kaydedilirken hata oluştu.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    val categoryFilters = remember(transactions) {
        listOf("Tüm Kategoriler") + transactions.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }
    var selectedCategoryFilter by remember { mutableStateOf("Tüm Kategoriler") }
    var expandedCategoryFilter by remember { mutableStateOf(false) }

    val filteredTransactions = remember(
        transactions,
        searchQuery,
        selectedTimeFilter,
        selectedTypeFilter,
        selectedCategoryFilter,
        sortOption
    ) {
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
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "Geçen Ay" -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "Son 3 Ay" -> {
                cal.add(Calendar.MONTH, -2)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "Son 6 Ay" -> {
                cal.add(Calendar.MONTH, -5)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "Bu Yıl" -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            else -> 0L
        }

        val upperBound = when (selectedTimeFilter) {
            "Geçen Ay" -> {
                Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            else -> Long.MAX_VALUE
        }

        val filtered = transactions.filter { tx ->
            val matchSearch = if (searchQuery.isNotBlank()) {
                tx.title.contains(searchQuery, true) ||
                    (tx.person?.contains(searchQuery, true) ?: false) ||
                    tx.category.contains(searchQuery, true)
            } else true

            val matchTime = tx.timestamp in timeLimit until upperBound

            val matchType = when (selectedTypeFilter) {
                "Bekleyen" -> tx.type == TransactionType.EXPENSE && !tx.isPaid
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

        when (sortOption) {
            HistorySortOption.DATE_DESC -> filtered.sortedByDescending { it.timestamp }
            HistorySortOption.DATE_ASC -> filtered.sortedBy { it.timestamp }
            HistorySortOption.AMOUNT_DESC -> filtered.sortedByDescending { it.amount }
            HistorySortOption.AMOUNT_ASC -> filtered.sortedBy { it.amount }
            HistorySortOption.CATEGORY_ASC -> filtered.sortedBy { it.category.lowercase(Locale("tr")) }
        }
    }

    val totalIncome = remember(filteredTransactions) { filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount } }
    val totalExpense = remember(filteredTransactions) { filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount } }
    val totalSaving = remember(filteredTransactions) { filteredTransactions.filter { it.type == TransactionType.SAVING }.sumOf { it.amount } }
    val netBalance = totalIncome - totalExpense - totalSaving

    val format = remember { com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 } }
    val dayGroupSdf = remember { SimpleDateFormat("dd MMMM yyyy, EEEE", Locale("tr", "TR")) }
    val itemDateSdf = remember { SimpleDateFormat("dd MMM", Locale("tr", "TR")) }

    val neonIncome = if (isDark) CyberNeonMint else Color(0xFF059669)
    val neonExpense = if (isDark) CyberNeonCoral else Color(0xFFDC2626)
    val neonSaving = if (isDark) CyberNeonCyan else Color(0xFF0284C7)

    // Group by calendar day when sorted chronologically
    val isChronological = sortOption == HistorySortOption.DATE_DESC || sortOption == HistorySortOption.DATE_ASC
    val groupedByDay = remember(filteredTransactions, isChronological) {
        if (!isChronological) emptyList() else {
            val todayCal = Calendar.getInstance()
            val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            filteredTransactions.groupBy { tx ->
                val c = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                Triple(c.get(Calendar.YEAR), c.get(Calendar.DAY_OF_YEAR), tx.timestamp)
            }.entries.groupBy { (k, _) ->
                k.first to k.second
            }.map { (dayPair, entries) ->
                val list = entries.flatMap { it.value }
                val sampleTs = list.first().timestamp
                val label = when {
                    dayPair.first == todayCal.get(Calendar.YEAR) && dayPair.second == todayCal.get(Calendar.DAY_OF_YEAR) -> "Bugün"
                    dayPair.first == yesterdayCal.get(Calendar.YEAR) && dayPair.second == yesterdayCal.get(Calendar.DAY_OF_YEAR) -> "Dün"
                    else -> dayGroupSdf.format(Date(sampleTs))
                }
                Triple(label, sampleTs, list)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 6.dp, bottom = 84.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- 1. SLEEK SEARCH BAR + PDF EXPORT BUTTON ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("İşlem, kişi veya kategori ara...", fontSize = 13.sp, maxLines = 1) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(19.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Aramayı Temizle", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) CyberDarkSurface else Color.White,
                        unfocusedContainerColor = if (isDark) CyberDarkSurface else Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                )

                FilledTonalIconButton(
                    onClick = { showPdfExportDialog = true },
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("pdf_export_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = "PDF Dışa Aktar", modifier = Modifier.size(22.dp))
                }
            }
        }

        // --- 2. SMART INSTANT SEARCH / FILTER SUMMARY BANNER ---
        if (searchQuery.isNotBlank() || selectedTypeFilter != "Tümü" || selectedCategoryFilter != "Tüm Kategoriler") {
            item {
                val filterLabel = buildList {
                    if (searchQuery.isNotBlank()) add("\"$searchQuery\"")
                    if (selectedTypeFilter != "Tümü") add(selectedTypeFilter)
                    if (selectedCategoryFilter != "Tüm Kategoriler") add(selectedCategoryFilter)
                }.joinToString(" • ")

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.35f else 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.FilterAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "$filterLabel (${filteredTransactions.size} kayıt)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        TextButton(
                            onClick = {
                                searchQuery = ""
                                selectedTypeFilter = "Tümü"
                                selectedCategoryFilter = "Tüm Kategoriler"
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Sıfırla", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 3. COMPACT CYBER PERIOD SUMMARY STRIP ---
        item {
            CyberCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$selectedTimeFilter Özeti (${filteredTransactions.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = (if (netBalance >= 0) neonIncome else neonExpense).copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Net: ${format.format(netBalance)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (netBalance >= 0) neonIncome else neonExpense,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModernSummaryBox(
                        title = "GELİR",
                        amount = format.format(totalIncome),
                        color = neonIncome,
                        modifier = Modifier.weight(1f)
                    )
                    ModernSummaryBox(
                        title = "GİDER",
                        amount = format.format(totalExpense),
                        color = neonExpense,
                        modifier = Modifier.weight(1f)
                    )
                    ModernSummaryBox(
                        title = "BİRİKİM",
                        amount = format.format(totalSaving),
                        color = neonSaving,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- 4. SMART FILTER & WORKING SORT BAR ---
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Box {
                        FilterChip(
                            selected = true,
                            onClick = { expandedTimeFilter = true },
                            label = { Text("📅 $selectedTimeFilter ▼", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = expandedTimeFilter,
                            onDismissRequest = { expandedTimeFilter = false }
                        ) {
                            timeFilters.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t, fontWeight = if (selectedTimeFilter == t) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        selectedTimeFilter = t
                                        expandedTimeFilter = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Box {
                        FilterChip(
                            selected = sortOption != HistorySortOption.DATE_DESC,
                            onClick = { expandedSortMenu = true },
                            label = { Text("↕ ${sortOption.label} ▼", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = expandedSortMenu,
                            onDismissRequest = { expandedSortMenu = false }
                        ) {
                            HistorySortOption.entries.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt.label, fontWeight = if (sortOption == opt) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        sortOption = opt
                                        expandedSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                items(typeFilters) { typeName ->
                    val isSelected = selectedTypeFilter == typeName
                    val label = if (typeName == "Bekleyen") "⏳ Bekleyen" else typeName
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTypeFilter = typeName },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                if (categoryFilters.size > 1) {
                    item {
                        Box {
                            FilterChip(
                                selected = selectedCategoryFilter != "Tüm Kategoriler",
                                onClick = { expandedCategoryFilter = true },
                                label = {
                                    Text(
                                        text = if (selectedCategoryFilter == "Tüm Kategoriler") "Kategori ▼" else "$selectedCategoryFilter ▼",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            DropdownMenu(
                                expanded = expandedCategoryFilter,
                                onDismissRequest = { expandedCategoryFilter = false }
                            ) {
                                categoryFilters.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c, fontWeight = if (selectedCategoryFilter == c) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            selectedCategoryFilter = c
                                            expandedCategoryFilter = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 5. TRANSACTION LIST (GROUPED BY DAY WHEN CHRONOLOGICAL) ---
        if (filteredTransactions.isEmpty()) {
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
                            Icons.Rounded.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            "Kriterlere uygun işlem bulunamadı.",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Zaman aralığını veya filtreleri değiştirerek tekrar deneyebilirsiniz.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else if (isChronological) {
            groupedByDay.forEach { (dayLabel, _, dayTxs) ->
                val dayIncome = dayTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val dayExpense = dayTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                val dayNet = dayIncome - dayExpense

                item(key = "header_${dayLabel}_${dayTxs.first().id}") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dayLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (dayIncome > 0 || dayExpense > 0) {
                            Text(
                                text = "${if (dayNet >= 0) "+" else ""}${format.format(dayNet)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dayNet >= 0) neonIncome else neonExpense
                            )
                        }
                    }
                }

                items(dayTxs, key = { it.id }) { tx ->
                    HistoryTransactionRowCard(
                        tx = tx,
                        dateText = itemDateSdf.format(Date(tx.timestamp)),
                        format = format,
                        neonIncome = neonIncome,
                        neonExpense = neonExpense,
                        neonSaving = neonSaving,
                        onEdit = onEdit,
                        onDelete = onDelete,
                        onTogglePaid = onTogglePaid,
                        onRepeat = onRepeat
                    )
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { tx ->
                HistoryTransactionRowCard(
                    tx = tx,
                    dateText = itemDateSdf.format(Date(tx.timestamp)),
                    format = format,
                    neonIncome = neonIncome,
                    neonExpense = neonExpense,
                    neonSaving = neonSaving,
                    onEdit = onEdit,
                    onDelete = onDelete,
                    onTogglePaid = onTogglePaid,
                    onRepeat = onRepeat
                )
            }
        }
    }

    if (showPdfExportDialog) {
        PdfExportDialog(
            onDismiss = { showPdfExportDialog = false },
            onExport = { year, month ->
                showPdfExportDialog = false
                val filtered = if (year == null || month == null) {
                    pdfTitleToExport = "Tüm Zamanlar"
                    transactions
                } else {
                    pdfTitleToExport = "${month + 1}.$year"
                    transactions.filter { tx ->
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = tx.timestamp
                        cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month
                    }
                }
                pdfTransactionsToExport = filtered.sortedByDescending { it.timestamp }
                val fileName = if (year == null) "Islem_Gecmisi_TumZamanlar.pdf" else "Islem_Gecmisi_${month?.plus(1)}_$year.pdf"
                pdfFileNameToExport = fileName
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                pdfExportLauncher.launch(fileName)
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryTransactionRowCard(
    tx: Transaction,
    dateText: String,
    format: java.text.NumberFormat,
    neonIncome: Color,
    neonExpense: Color,
    neonSaving: Color,
    onEdit: ((Transaction) -> Unit)?,
    onDelete: ((Transaction) -> Unit)?,
    onTogglePaid: ((Transaction, Boolean) -> Unit)?,
    onRepeat: ((Transaction) -> Unit)?
) {
    var expandedMenu by remember { mutableStateOf(false) }

    val typeColor = when (tx.type) {
        TransactionType.INCOME -> neonIncome
        TransactionType.EXPENSE -> neonExpense
        TransactionType.SAVING -> neonSaving
    }
    val typeSign = when (tx.type) {
        TransactionType.INCOME -> "+"
        TransactionType.EXPENSE -> "-"
        TransactionType.SAVING -> ""
    }
    val icon = when {
        tx.type == TransactionType.INCOME -> Icons.Rounded.TrendingUp
        tx.type == TransactionType.SAVING -> Icons.Rounded.Savings
        tx.category.lowercase().contains("kira") -> Icons.Rounded.Home
        tx.category.lowercase().contains("market") -> Icons.Rounded.ShoppingCart
        tx.category.lowercase().contains("fatura") -> Icons.Rounded.ReceiptLong
        tx.category.lowercase().contains("kredi") -> Icons.Rounded.CreditCard
        tx.category.lowercase().contains("ulaşım") || tx.category.lowercase().contains("yakıt") -> Icons.Rounded.DirectionsCar
        tx.category.lowercase().contains("sağlık") -> Icons.Rounded.LocalHospital
        else -> Icons.Rounded.Payments
    }

    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandedMenu = true },
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(13.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(typeColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = typeColor, modifier = Modifier.size(21.dp))
                }

                Spacer(modifier = Modifier.width(11.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tx.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (tx.category.isNotBlank() && !tx.category.equals(tx.title, ignoreCase = true)) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = tx.category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    softWrap = false,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (!tx.person.isNullOrBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = tx.person,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    softWrap = false,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = dateText,
                            fontSize = 10.sp,
                            softWrap = false,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$typeSign${format.format(tx.amount)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        softWrap = false,
                        maxLines = 1,
                        color = typeColor
                    )
                    if (tx.type == TransactionType.EXPENSE) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Surface(
                            color = if (tx.isPaid) neonIncome.copy(alpha = 0.14f) else neonExpense.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable(enabled = onTogglePaid != null) {
                                onTogglePaid?.invoke(tx, !tx.isPaid)
                            }
                        ) {
                            Text(
                                text = if (tx.isPaid) "Ödendi ✓" else "Bekliyor",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                softWrap = false,
                                maxLines = 1,
                                color = if (tx.isPaid) neonIncome else neonExpense,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            DropdownMenu(
                expanded = expandedMenu,
                onDismissRequest = { expandedMenu = false }
            ) {
                if (tx.type == TransactionType.EXPENSE && onTogglePaid != null) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                if (tx.isPaid) Icons.Rounded.RadioButtonUnchecked else Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = neonIncome,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        text = { Text(if (tx.isPaid) "Ödenmedi İşaretle" else "Ödendi İşaretle") },
                        onClick = {
                            expandedMenu = false
                            onTogglePaid(tx, !tx.isPaid)
                        }
                    )
                }
                if (onRepeat != null) {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        text = { Text("Bugüne Tekrarla") },
                        onClick = {
                            expandedMenu = false
                            onRepeat(tx)
                        }
                    )
                }
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text("Düzenle") },
                    onClick = {
                        expandedMenu = false
                        onEdit?.invoke(tx)
                    }
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                    text = { Text("Sil", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        expandedMenu = false
                        onDelete?.invoke(tx)
                    }
                )
            }
        }
    }
}

@Composable
fun ModernSummaryBox(
    title: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = if (isDark) 0.12f else 0.08f))
            .padding(10.dp)
    ) {
        Column {
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(amount, fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

fun showPdfNotification(context: Context, uri: Uri, fileName: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "pdf_exports"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "PDF Çıktıları", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_menu_save)
        .setContentTitle("PDF İndirildi")
        .setContentText("$fileName dosyası hazır. Açmak için dokunun.")
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    try {
        notificationManager.notify(uri.hashCode(), notification)
    } catch (e: SecurityException) {
        // Permission not granted
    }
}
