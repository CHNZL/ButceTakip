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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.*
import com.example.util.PdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class HistorySortOption(val label: String) {
    DATE_DESC("En Yeni"),
    DATE_ASC("En Eski"),
    AMOUNT_DESC("En Yüksek"),
    AMOUNT_ASC("En Düşük"),
    CATEGORY_ASC("Kategori A–Z")
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
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)

    var searchQuery by remember { mutableStateOf("") }
    val timeFilters = listOf("Bu Ay", "Geçen Ay", "Son 7 Gün", "Son 15 Gün", "Son 3 Ay", "Son 6 Ay", "Bu Yıl", "Tüm Zamanlar")
    var selectedTimeFilter by remember { mutableStateOf("Bu Ay") }
    var expandedTimeFilter by remember { mutableStateOf(false) }

    var sortOption by remember { mutableStateOf(HistorySortOption.DATE_DESC) }
    var expandedSortMenu by remember { mutableStateOf(false) }

    val typeFilters = listOf("Tümü", "Bekleyen", "Gelir", "Gider", "Birikim")
    var selectedTypeFilter by remember { mutableStateOf("Tümü") }

    val categoryFilters = remember(transactions) {
        listOf("Tüm Kategoriler") + transactions.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }
    var selectedCategoryFilter by remember { mutableStateOf("Tüm Kategoriler") }
    var expandedCategoryFilter by remember { mutableStateOf(false) }

    var pendingDeleteTx by remember { mutableStateOf<Transaction?>(null) }
    var showPdfExportDialog by remember { mutableStateOf(false) }
    var pdfTransactionsToExport by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var pdfTitleToExport by remember { mutableStateOf("") }
    var pdfFileNameToExport by remember { mutableStateOf("") }

    val context = LocalContext.current
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
            "Son 7 Gün" -> { cal.add(Calendar.DAY_OF_YEAR, -7); cal.timeInMillis }
            "Son 15 Gün" -> { cal.add(Calendar.DAY_OF_YEAR, -15); cal.timeInMillis }
            "Bu Ay" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "Geçen Ay" -> {
                cal.add(Calendar.MONTH, -1); cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "Son 3 Ay" -> {
                cal.add(Calendar.MONTH, -2); cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "Son 6 Ay" -> {
                cal.add(Calendar.MONTH, -5); cal.set(Calendar.DAY_OF_MONTH, 1)
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
            "Geçen Ay" -> Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
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
            val matchCategory = selectedCategoryFilter == "Tüm Kategoriler" || tx.category == selectedCategoryFilter
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

    val isChronological = sortOption == HistorySortOption.DATE_DESC || sortOption == HistorySortOption.DATE_ASC
    val groupedByDay = remember(filteredTransactions, isChronological) {
        if (!isChronological) emptyList() else {
            val todayCal = Calendar.getInstance()
            val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            filteredTransactions.groupBy { tx ->
                val c = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                c.get(Calendar.YEAR) to c.get(Calendar.DAY_OF_YEAR)
            }.map { (dayPair, list) ->
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

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(horizontal = 14.dp)
                .testTag("history_screen_list"),
            contentPadding = PaddingValues(top = 6.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. SEARCH BAR + PDF EXPORT ACTION
            item(key = "history_search") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("history_search_input"),
                        placeholder = {
                            Text(
                                "İşlem, kişi veya kategori ara...",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Aramayı Temizle", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = borderColor
                        )
                    )

                    Surface(
                        onClick = { showPdfExportDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.8.dp, borderColor),
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("pdf_export_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.PictureAsPdf,
                                contentDescription = "PDF Rapor İndir",
                                tint = coral,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // 2. ACTIVE FILTER RESET BANNER
            if (searchQuery.isNotBlank() || selectedTypeFilter != "Tümü" || selectedCategoryFilter != "Tüm Kategoriler") {
                item(key = "history_active_filters") {
                    val filterLabel = buildList {
                        if (searchQuery.isNotBlank()) add("\"$searchQuery\"")
                        if (selectedTypeFilter != "Tümü") add(selectedTypeFilter)
                        if (selectedCategoryFilter != "Tüm Kategoriler") add(selectedCategoryFilter)
                    }.joinToString(" · ")

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = mint.copy(alpha = if (isDark) 0.14f else 0.09f),
                        border = BorderStroke(0.8.dp, mint.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
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
                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.FilterList, contentDescription = null, tint = mint, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "$filterLabel (${filteredTransactions.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
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
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Sıfırla", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 3. EXECUTIVE PERIOD SUMMARY CARD
            item(key = "history_summary") {
                SettingsGroupCard {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$selectedTimeFilter · ${filteredTransactions.size} kayıt",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val netAccent = if (netBalance >= 0) mint else coral
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = netAccent.copy(alpha = if (isDark) 0.16f else 0.11f)
                            ) {
                                Text(
                                    text = "Net ${format.format(netBalance)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = netAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(thickness = 0.7.dp, color = borderColor.copy(alpha = 0.75f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HistoryStatColumn("Gelir", format.format(totalIncome), mint, Modifier.weight(1f))
                            Box(modifier = Modifier.height(26.dp).width(0.8.dp).background(borderColor))
                            HistoryStatColumn("Gider", format.format(totalExpense), coral, Modifier.weight(1f))
                            Box(modifier = Modifier.height(26.dp).width(0.8.dp).background(borderColor))
                            HistoryStatColumn("Birikim", format.format(totalSaving), sky, Modifier.weight(1f))
                        }
                    }
                }
            }

            // 4. CLEAN ICONIC FILTER & SORT STRIP (ZERO EMOJI CLUTTER)
            item(key = "history_filter_bar") {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Box {
                            HistoryPillButton(
                                icon = Icons.Rounded.CalendarMonth,
                                label = selectedTimeFilter,
                                selected = true,
                                trailingChevron = true,
                                onClick = { expandedTimeFilter = true },
                                testTag = "history_time_filter"
                            )
                            DropdownMenu(expanded = expandedTimeFilter, onDismissRequest = { expandedTimeFilter = false }) {
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
                            HistoryPillButton(
                                icon = Icons.Rounded.SwapVert,
                                label = sortOption.label,
                                selected = sortOption != HistorySortOption.DATE_DESC,
                                trailingChevron = true,
                                onClick = { expandedSortMenu = true },
                                testTag = "history_sort_filter"
                            )
                            DropdownMenu(expanded = expandedSortMenu, onDismissRequest = { expandedSortMenu = false }) {
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
                        HistoryPillButton(
                            icon = when (typeName) {
                                "Bekleyen" -> Icons.Rounded.Schedule
                                "Gelir" -> Icons.Rounded.TrendingUp
                                "Gider" -> Icons.Rounded.TrendingDown
                                "Birikim" -> Icons.Rounded.Savings
                                else -> null
                            },
                            label = typeName,
                            selected = selectedTypeFilter == typeName,
                            trailingChevron = false,
                            onClick = { selectedTypeFilter = typeName },
                            testTag = "history_type_$typeName"
                        )
                    }

                    if (categoryFilters.size > 1) {
                        item {
                            Box {
                                HistoryPillButton(
                                    icon = Icons.Rounded.Category,
                                    label = if (selectedCategoryFilter == "Tüm Kategoriler") "Kategori" else selectedCategoryFilter,
                                    selected = selectedCategoryFilter != "Tüm Kategoriler",
                                    trailingChevron = true,
                                    onClick = { expandedCategoryFilter = true },
                                    testTag = "history_category_filter"
                                )
                                DropdownMenu(expanded = expandedCategoryFilter, onDismissRequest = { expandedCategoryFilter = false }) {
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

            // 5. GROUPED LEDGER LIST
            if (filteredTransactions.isEmpty()) {
                item(key = "history_empty") {
                    SettingsGroupCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                            }
                            Text(
                                text = "Kriterlere uygun işlem bulunamadı",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Zaman aralığını veya filtreleri değiştirerek diğer kayıtları görüntüleyebilirsiniz.",
                                style = MaterialTheme.typography.bodySmall,
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

                    item(key = "day_group_${dayLabel}_${dayTxs.first().id}") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 6.dp, end = 4.dp, top = 4.dp, bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dayLabel.uppercase(Locale("tr", "TR")),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.7.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (dayIncome > 0 || dayExpense > 0) {
                                    Text(
                                        text = "${if (dayNet >= 0) "+" else ""}${format.format(dayNet)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontFeatureSettings = "tnum"
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = if (dayNet >= 0) mint else coral
                                    )
                                }
                            }

                            SettingsGroupCard {
                                dayTxs.forEachIndexed { idx, tx ->
                                    HistoryInsetRow(
                                        tx = tx,
                                        dateText = itemDateSdf.format(Date(tx.timestamp)),
                                        format = format,
                                        mint = mint,
                                        coral = coral,
                                        sky = sky,
                                        amber = amber,
                                        onEdit = onEdit,
                                        onRequestDelete = { pendingDeleteTx = tx },
                                        onTogglePaid = onTogglePaid,
                                        onRepeat = onRepeat
                                    )
                                    if (idx != dayTxs.lastIndex) {
                                        SettingsInsetDivider()
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                item(key = "history_sorted_flat_group") {
                    SettingsGroupCard {
                        filteredTransactions.forEachIndexed { idx, tx ->
                            HistoryInsetRow(
                                tx = tx,
                                dateText = itemDateSdf.format(Date(tx.timestamp)),
                                format = format,
                                mint = mint,
                                coral = coral,
                                sky = sky,
                                amber = amber,
                                onEdit = onEdit,
                                onRequestDelete = { pendingDeleteTx = tx },
                                onTogglePaid = onTogglePaid,
                                onRepeat = onRepeat
                            )
                            if (idx != filteredTransactions.lastIndex) {
                                SettingsInsetDivider()
                            }
                        }
                    }
                }
            }
        }
    }

    if (pendingDeleteTx != null && onDelete != null) {
        val txToDelete = pendingDeleteTx!!
        AlertDialog(
            onDismissRequest = { pendingDeleteTx = null },
            title = { Text("İşlem Silinsin Mi?", fontWeight = FontWeight.Bold) },
            text = { Text("\"${txToDelete.title}\" (${format.format(txToDelete.amount)}) kaydı kalıcı olarak silinecek.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(txToDelete)
                        pendingDeleteTx = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sil") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteTx = null }) { Text("İptal") }
            }
        )
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

@Composable
private fun HistoryStatColumn(
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
            maxLines = 1
        )
    }
}

@Composable
private fun HistoryPillButton(
    icon: ImageVector?,
    label: String,
    selected: Boolean,
    trailingChevron: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.8.dp, if (selected) MaterialTheme.colorScheme.primary else borderColor),
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1
            )
            if (trailingChevron) {
                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = contentColor, modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
private fun HistoryInsetRow(
    tx: Transaction,
    dateText: String,
    format: NumberFormat,
    mint: Color,
    coral: Color,
    sky: Color,
    amber: Color,
    onEdit: ((Transaction) -> Unit)?,
    onRequestDelete: () -> Unit,
    onTogglePaid: ((Transaction, Boolean) -> Unit)?,
    onRepeat: ((Transaction) -> Unit)?
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var expandedMenu by remember { mutableStateOf(false) }

    val typeColor = when (tx.type) {
        TransactionType.INCOME -> mint
        TransactionType.EXPENSE -> coral
        TransactionType.SAVING -> sky
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

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandedMenu = true }
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag("history_row_${tx.id}"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(typeColor.copy(alpha = if (isDark) 0.16f else 0.11f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = typeColor, modifier = Modifier.size(20.dp))
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
                Spacer(modifier = Modifier.height(2.dp))
                val metaText = buildList {
                    if (tx.category.isNotBlank() && !tx.category.equals(tx.title, ignoreCase = true)) add(tx.category)
                    if (!tx.person.isNullOrBlank()) add(tx.person)
                    add(dateText)
                }.joinToString(" · ")
                Text(
                    text = metaText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$typeSign${format.format(tx.amount)}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontFeatureSettings = "tnum"
                    ),
                    fontWeight = FontWeight.Bold,
                    color = typeColor,
                    softWrap = false,
                    maxLines = 1
                )
                if (tx.type == TransactionType.EXPENSE) {
                    Spacer(modifier = Modifier.height(3.dp))
                    val statusAccent = if (tx.isPaid) mint else amber
                    Surface(
                        onClick = { onTogglePaid?.invoke(tx, !tx.isPaid) },
                        enabled = onTogglePaid != null,
                        shape = RoundedCornerShape(6.dp),
                        color = statusAccent.copy(alpha = if (isDark) 0.16f else 0.11f)
                    ) {
                        Text(
                            text = if (tx.isPaid) "Ödendi" else "Bekliyor",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        DropdownMenu(expanded = expandedMenu, onDismissRequest = { expandedMenu = false }) {
            if (tx.type == TransactionType.EXPENSE && onTogglePaid != null) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            if (tx.isPaid) Icons.Rounded.RadioButtonUnchecked else Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = mint,
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
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                text = { Text("Sil", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    expandedMenu = false
                    onRequestDelete()
                }
            )
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
    } catch (_: SecurityException) {
    }
}
