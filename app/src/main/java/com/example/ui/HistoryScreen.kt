package com.example.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.util.PdfExporter
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.data.TransactionType
import com.example.ui.theme.LocalAppThemeColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    transactions: List<Transaction>,
    onEdit: ((Transaction) -> Unit)? = null,
    onDelete: ((Transaction) -> Unit)? = null,
    isDark: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val timeFilters = listOf("Tüm Zamanlar", "Son 7 Gün", "Son 15 Gün", "Bu Ay", "Geçen Ay", "Son 3 Ay", "Son 6 Ay", "Bu Yıl")
    var selectedTimeFilter by remember { mutableStateOf("Bu Ay") }
    var expandedTimeFilter by remember { mutableStateOf(false) }

    var sortColumn by remember { mutableStateOf("Tarih") }
    var sortAscending by remember { mutableStateOf(false) }

    val typeFilters = listOf("Tüm Türler", "Gelir", "Gider", "Birikim")
    var selectedTypeFilter by remember { mutableStateOf(typeFilters[0]) }
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

    val categoryFilters = listOf("Tüm Kategoriler") + transactions.mapNotNull { it.category }.distinct().sorted()
    var selectedCategoryFilter by remember { mutableStateOf("Tüm Kategoriler") }
    var expandedCategoryFilter by remember { mutableStateOf(false) }

    val filteredTransactions = remember(transactions, searchQuery, selectedTimeFilter, selectedTypeFilter, selectedCategoryFilter, sortColumn, sortAscending) {
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
                cal.add(Calendar.MONTH, -2)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "Son 6 Ay" -> {
                cal.add(Calendar.MONTH, -5)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "Bu Yıl" -> {
                cal.timeInMillis = System.currentTimeMillis()
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
                (tx.person?.contains(searchQuery, true) ?: false) ||
                (tx.category?.contains(searchQuery, true) ?: false)
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
    val netBalance = totalIncome - totalExpense - totalSaving

    val format = remember { com.example.util.FormatUtil.getCurrencyFormat() }
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale("tr")) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. HEADER & PDF EXPORT ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "İşlem Geçmişi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "TÜM HAREKETLER VE KAYITLAR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }

                Button(
                    onClick = { showPdfExportDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF Çıktı", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // --- 2. SUMMARY METRICS ROW ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dönem Özeti ($selectedTimeFilter)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            color = if (netBalance >= 0) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Net: ${format.format(netBalance)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (netBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModernSummaryBox(
                            title = "GELİR",
                            amount = format.format(totalIncome),
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        ModernSummaryBox(
                            title = "GİDER",
                            amount = format.format(totalExpense),
                            color = Color(0xFFEF4444),
                            modifier = Modifier.weight(1f)
                        )
                        ModernSummaryBox(
                            title = "BİRİKİM",
                            amount = format.format(totalSaving),
                            color = Color(0xFF0284C7),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- 3. SEARCH & TIME FILTER ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("İşlemlerde ara (başlık, kişi, kategori)...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Temizle", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                )

                // Time & Type chips
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

                    items(typeFilters) { typeName ->
                        val isSelected = selectedTypeFilter == typeName
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTypeFilter = typeName },
                            label = { Text(typeName, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
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
                                    label = { Text(if (selectedCategoryFilter == "Tüm Kategoriler") "Kategori ▼" else "$selectedCategoryFilter ▼", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
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
        }

        // --- 4. TRANSACTION ITEMS LIST ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kayıtlar (${filteredTransactions.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Sıralama: $sortColumn ${if (sortAscending) "↑" else "↓"}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            sortAscending = !sortAscending
                        }
                    )
                }
            }
        }

        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
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
                            "Filtreleri veya arama terimini değiştirerek tekrar deneyebilirsiniz.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { tx ->
                var expandedMenu by remember { mutableStateOf(false) }

                val typeColor = when (tx.type) {
                    TransactionType.INCOME -> Color(0xFF10B981)
                    TransactionType.EXPENSE -> Color(0xFFEF4444)
                    TransactionType.SAVING -> Color(0xFF0284C7)
                }

                val typeSign = when (tx.type) {
                    TransactionType.INCOME -> "+"
                    TransactionType.EXPENSE -> "-"
                    TransactionType.SAVING -> ""
                }

                val icon = when {
                    tx.type == TransactionType.INCOME -> Icons.Rounded.TrendingUp
                    tx.type == TransactionType.SAVING -> Icons.Rounded.Savings
                    tx.category?.lowercase()?.contains("kira") == true -> Icons.Rounded.Home
                    tx.category?.lowercase()?.contains("market") == true -> Icons.Rounded.ShoppingCart
                    tx.category?.lowercase()?.contains("fatura") == true -> Icons.Rounded.ReceiptLong
                    tx.category?.lowercase()?.contains("kredi") == true -> Icons.Rounded.CreditCard
                    tx.category?.lowercase()?.contains("ulaşım") == true || tx.category?.lowercase()?.contains("yakıt") == true -> Icons.Rounded.DirectionsCar
                    tx.category?.lowercase()?.contains("sağlık") == true -> Icons.Rounded.LocalHospital
                    else -> Icons.Rounded.Payments
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedMenu = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(typeColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = typeColor, modifier = Modifier.size(22.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (!tx.category.isNullOrBlank()) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = tx.category,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    if (!tx.person.isNullOrBlank()) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = tx.person,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = sdf.format(Date(tx.timestamp)),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "$typeSign${format.format(tx.amount)}",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = typeColor
                            )
                        }

                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false }
                        ) {
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

@Composable
fun ModernSummaryBox(
    title: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(10.dp)
    ) {
        Column {
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(amount, fontSize = 12.sp, fontWeight = FontWeight.Black, color = color, maxLines = 1)
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
