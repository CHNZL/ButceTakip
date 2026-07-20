package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GoldPrice
import com.example.data.BankRate
import com.example.data.Transaction
import com.example.data.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class SavingAssetSummary(
    val category: String,
    val totalQuantity: Double,
    val totalPaid: Double,
    val currentUnitPrice: Double,
    val currentValue: Double,
    val profitLoss: Double,
    val profitLossPercent: Double
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SavingsScreen(
    transactions: List<Transaction>,
    goldPrices: List<GoldPrice>,
    bankRates: List<BankRate>,
    onAddSaving: () -> Unit,
    onDeleteSavingTransaction: (Int) -> Unit,
    preferenceManager: com.example.data.PreferenceManager? = null,
    customPricesTrigger: Long = 0L,
    onUpdateCustomPrice: ((String, Double) -> Unit)? = null,
    ziraatRates: List<BankRate> = emptyList()
) {
    var editingCategoryPrice by remember { mutableStateOf<String?>(null) }
    var newPriceText by remember { mutableStateOf("") }

    val savingTransactions = remember(transactions) {
        transactions.filter { it.type == TransactionType.SAVING }
            .sortedByDescending { it.timestamp }
    }

    val assetSummaries = remember(savingTransactions, goldPrices, bankRates, ziraatRates, customPricesTrigger) {
        val groups = savingTransactions.groupBy { it.category }
        groups.map { (category, txList) ->
            val totalQuantity = txList.sumOf { it.quantity ?: 0.0 }
            val totalPaid = txList.sumOf { it.amount }
            
            val livePrice = resolveCurrentUnitPrice(category, goldPrices, bankRates, preferenceManager, ziraatRates)
            val latestPurchasePrice = txList.maxByOrNull { it.timestamp }?.unitPrice ?: 0.0
            val currentUnitPrice = livePrice ?: latestPurchasePrice
            
            val currentValue = totalQuantity * currentUnitPrice
            val profitLoss = currentValue - totalPaid
            val profitLossPercent = if (totalPaid > 0.0) (profitLoss / totalPaid) * 100.0 else 0.0
            
            SavingAssetSummary(
                category = category,
                totalQuantity = totalQuantity,
                totalPaid = totalPaid,
                currentUnitPrice = currentUnitPrice,
                currentValue = currentValue,
                profitLoss = profitLoss,
                profitLossPercent = profitLossPercent
            )
        }.sortedByDescending { it.currentValue }
    }

    val totalCurrentValue = assetSummaries.sumOf { it.currentValue }
    val totalPaidAll = assetSummaries.sumOf { it.totalPaid }
    val totalProfitLoss = totalCurrentValue - totalPaidAll
    val totalProfitLossPercent = if (totalPaidAll > 0.0) (totalProfitLoss / totalPaidAll) * 100.0 else 0.0

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("tr", "TR")) }
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale("tr")) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("savings_screen_root"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. PORTFOLIO KEY METRICS HEADER CARD ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF001D36), Color(0xFF00335A))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOPLAM BİRİKİM DEĞERİ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA5C8E1),
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFFA5C8E1).copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = currencyFormat.format(totalCurrentValue),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = 36.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Divider(color = Color(0xFF1E354B), thickness = 1.dp)
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Maliyet (Toplam Ödeme)",
                                fontSize = 11.sp,
                                color = Color(0xFFA5C8E1).copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currencyFormat.format(totalPaidAll),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Profit-Loss Trend Badge
                        Surface(
                            color = when {
                                totalProfitLoss > 0.0 -> Color(0xFF10B981).copy(alpha = 0.15f)
                                totalProfitLoss < 0.0 -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                else -> Color.White.copy(alpha = 0.1f)
                            },
                            shape = CircleShape,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = when {
                                        totalProfitLoss > 0.0 -> Icons.Rounded.TrendingUp
                                        totalProfitLoss < 0.0 -> Icons.Rounded.TrendingDown
                                        else -> Icons.Rounded.AccountBalanceWallet
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        totalProfitLoss > 0.0 -> Color(0xFF34D399)
                                        totalProfitLoss < 0.0 -> Color(0xFFF87171)
                                        else -> Color.LightGray
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "%s%,.2f (%s%,.2f%%)".format(
                                        if (totalProfitLoss > 0) "+" else "",
                                        totalProfitLoss,
                                        if (totalProfitLoss > 0) "+" else "",
                                        totalProfitLossPercent
                                    ),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        totalProfitLoss > 0.0 -> Color(0xFF34D399)
                                        totalProfitLoss < 0.0 -> Color(0xFFF87171)
                                        else -> Color.LightGray
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. ASSETS ROW/GRID SUMMARY CARDS ---
        if (assetSummaries.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Text(
                        text = "Varlık Özetlerim",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        maxItemsInEachRow = 2
                    ) {
                        assetSummaries.forEach { summary ->
                            Box(modifier = Modifier.weight(1f).minimumInteractiveComponentSize()) {
                                AssetSummaryCard(
                                    summary = summary,
                                    currencyFormat = currencyFormat,
                                    preferenceManager = preferenceManager,
                                    onEditClick = {
                                        editingCategoryPrice = summary.category
                                        newPriceText = if (summary.currentUnitPrice > 0.0) summary.currentUnitPrice.toString() else ""
                                    }
                                )
                            }
                        }
                        if (assetSummaries.size % 2 != 0) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // --- 3. LEDGER HISTORY HEADER & BUTTON ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Birikim Kayıtları",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Button(
                    onClick = onAddSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0061A4),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp).testTag("add_saving_transaction_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Birikim Ekle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 4. DETAILED LEDGER ENTRIES LIST ---
        if (savingTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Henüz birikim kaydınız bulunmuyor.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                        Button(
                            onClick = onAddSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("İlk Birikimi Ekle", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(savingTransactions, key = { it.id }) { tx ->
                val livePrice = resolveCurrentUnitPrice(tx.category, goldPrices, bankRates, preferenceManager, ziraatRates)
                val rate = livePrice ?: tx.unitPrice ?: 0.0
                val qty = tx.quantity ?: 0.0
                val currentValue = qty * rate
                val profitLoss = currentValue - tx.amount

                LedgerRowItem(
                    tx = tx,
                    currentValue = currentValue,
                    profitLoss = profitLoss,
                    dateFormat = dateFormat,
                    currencyFormat = currencyFormat,
                    onDelete = { onDeleteSavingTransaction(tx.id) }
                )
            }
        }
    }

    if (editingCategoryPrice != null) {
        val catName = editingCategoryPrice!!
        AlertDialog(
            onDismissRequest = { editingCategoryPrice = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = com.example.util.parseFormattedAmount(newPriceText)
                        if (parsed != null && parsed >= 0.0) {
                            onUpdateCustomPrice?.invoke(catName, parsed)
                        }
                        editingCategoryPrice = null
                    }
                ) {
                    Text("Güncelle", color = Color(0xFF0061A4), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCategoryPrice = null }) {
                    Text("İptal", color = Color(0xFF64748B))
                }
            },
            title = {
                Text(
                    text = "$catName Fiyatını Güncelle",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Lütfen 1 gram veya 1 adet için güncel birim fiyatını giriniz (₺):",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                    OutlinedTextField(
                        value = newPriceText,
                        onValueChange = { newPriceText = com.example.util.formatInputAmount(it) },
                        placeholder = { Text("Örn: 2450.5") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("custom_price_input")
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

fun isEditableCategory(category: String, preferenceManager: com.example.data.PreferenceManager? = null): Boolean {
    val clean = category.trim().lowercase()
    val isDefault = clean.contains("bireysel") || clean.contains("emeklilik") || clean == "set" || clean.startsWith("set") || clean.endsWith("set")
    if (isDefault) return true
    if (preferenceManager != null) {
        val match = preferenceManager.getMarketMatch(category)
        if (match == "manual") return true
    }
    return false
}

@Composable
fun AssetSummaryCard(
    summary: SavingAssetSummary,
    currencyFormat: NumberFormat,
    preferenceManager: com.example.data.PreferenceManager?,
    onEditClick: (() -> Unit)? = null
) {
    val isProfit = summary.profitLoss >= 0.0
    val dynamicColor = when {
        summary.category.contains("24", ignoreCase = true) || summary.category.contains("altın", ignoreCase = true) || summary.category.contains("altin", ignoreCase = true) || summary.category.contains("xau", ignoreCase = true) -> Color(0xFFD4AF37) // Bright Gold
        summary.category.contains("22", ignoreCase = true) -> Color(0xFFCD7F32) // Bronze/Dark Gold
        summary.category.contains("dolar", ignoreCase = true) || summary.category.contains("usd", ignoreCase = true) -> Color(0xFF10B981) // Emerald
        else -> Color(0xFF3B82F6) // Saving Blue
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFFECEFF3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored Asset Badge Indicator
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dynamicColor)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val rateUnitStr = if (summary.category.lowercase().contains("bilezik") || summary.category.lowercase().contains("altın") || summary.category.lowercase().contains("altin")) "₺/g" else "₺"
                    // Asset Unit Price Badge
                    Text(
                        text = "%,.1f %s".format(Locale("tr"), summary.currentUnitPrice, rateUnitStr),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    if (onEditClick != null && isEditableCategory(summary.category, preferenceManager)) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(24.dp).testTag("custom_price_edit_btn_${summary.category}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Fiyat Düzenle",
                                tint = Color(0xFF0061A4),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Asset Category Name
            Text(
                text = summary.category,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Physical Quantity
            Text(
                text = "%,.2f %s".format(
                    Locale("tr"), 
                    summary.totalQuantity, 
                    if (summary.category.lowercase().contains("bilezik") || summary.category.lowercase().contains("altın") || summary.category.lowercase().contains("altin")) "gr" else "adet"
                ),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Current Portfolio Value for this asset
            Text(
                text = currencyFormat.format(summary.currentValue),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )

            // Dynamic Profit Loss Display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = if (isProfit) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                    contentDescription = null,
                    tint = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444),
                    modifier = Modifier.size(10.dp)
                )
                Text(
                    text = "%s%,.1f ₺ (%s%,.1f%%)".format(
                        if (summary.profitLoss > 0) "+" else "",
                        summary.profitLoss,
                        if (summary.profitLoss > 0) "+" else "",
                        summary.profitLossPercent
                    ),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isEditableCategory(summary.category, preferenceManager)) {
                val updateTime = preferenceManager?.getCustomPriceTime(summary.category) ?: 0L
                val timeStr = if (updateTime > 0L) {
                    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr"))
                    sdf.format(Date(updateTime))
                } else {
                    "Güncellenmedi"
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Son Güncelleme: $timeStr",
                    fontSize = 8.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LedgerRowItem(
    tx: Transaction,
    currentValue: Double,
    profitLoss: Double,
    dateFormat: SimpleDateFormat,
    currencyFormat: NumberFormat,
    onDelete: () -> Unit
) {
    val isProfit = profitLoss >= 0.0
    val formattedDate = remember(tx.timestamp) { dateFormat.format(Date(tx.timestamp)) }
    
    val dynamicColor = when {
        tx.category.contains("24", ignoreCase = true) -> Color(0xFFD4AF37)
        tx.category.contains("22", ignoreCase = true) -> Color(0xFFCD7F32)
        tx.category.contains("dolar", ignoreCase = true) || tx.category.contains("usd", ignoreCase = true) -> Color(0xFF10B981)
        else -> Color(0xFF3B82F6)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // First Row: Header/Title + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(dynamicColor)
                    )
                    Text(
                        text = tx.category,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp).testTag("delete_saving_tx_btn_${tx.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Kayıt Sil",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle: Date
            Text(
                text = formattedDate,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Quantity & Base Purchase Unit price
                Column {
                    Text(
                        text = "Miktar & Maliyet",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "%,.2f %s @ %,.1f ₺".format(
                            Locale("tr"),
                            tx.quantity ?: 0.0,
                            if (tx.category.lowercase().contains("bilezik") || tx.category.lowercase().contains("altın") || tx.category.lowercase().contains("altin")) "gr" else "ad",
                            tx.unitPrice ?: 0.0
                        ),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569),
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Total Purchase Paid
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Ödeme",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currencyFormat.format(tx.amount),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Value / Profit-Loss Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Güncel Değer",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = currencyFormat.format(currentValue),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E293B)
                    )
                }

                // Row Profit Loss pill
                Surface(
                    color = if (isProfit) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = if (isProfit) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "%s%,.1f ₺".format(if (profitLoss > 0) "+" else "", profitLoss),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Resolves current market price for well-known categories from Sivas Kuyumder or BankRates.
 */
fun resolveCurrentUnitPrice(
    category: String,
    goldPrices: List<GoldPrice>,
    bankRates: List<BankRate>,
    preferenceManager: com.example.data.PreferenceManager? = null,
    ziraatRates: List<BankRate> = emptyList()
): Double? {
    val cleanCatLower = category.trim().lowercase(java.util.Locale("tr", "TR"))
    val cleanCatRoot = category.trim().lowercase(java.util.Locale.ROOT)

    fun parseVal(s: String?): Double? {
        if (s == null || s.isBlank() || s == "-") return null
        return s.replace(".", "").replace(",", ".").trim().toDoubleOrNull()
    }

    val marketMatch = preferenceManager?.getMarketMatch(category)
    if (marketMatch == "manual") {
        val customPrice = preferenceManager?.getCustomPrice(category)
        if (customPrice != null) {
            return customPrice
        }
    } else if (marketMatch != null) {
        if (marketMatch.startsWith("yk_")) {
            val code = marketMatch.removePrefix("yk_")
            val br = bankRates.find { it.code == code }
            br?.let { parseVal(it.buy) ?: parseVal(it.sell) }?.let { return it }
        } else if (marketMatch.startsWith("zr_")) {
            val code = marketMatch.removePrefix("zr_")
            val br = ziraatRates.find { it.code == code }
            br?.let { parseVal(it.buy) ?: parseVal(it.sell) }?.let { return it }
        } else if (marketMatch.startsWith("gp_")) {
            val name = marketMatch.removePrefix("gp_")
            val gp = goldPrices.find { it.name.trim().lowercase() == name.lowercase() || it.name.trim() == name }
            gp?.let { parseVal(it.buy) ?: parseVal(it.sell) }?.let { return it }
        }
    } else {
        if (isEditableCategory(category, preferenceManager)) {
            val customPrice = preferenceManager?.getCustomPrice(category)
            if (customPrice != null) {
                return customPrice
            }
        }
    }

    fun matches(vararg keywords: String): Boolean {
        return keywords.any { kw ->
            cleanCatLower.contains(kw) || cleanCatRoot.contains(kw)
        }
    }

    when {
        matches("bilezik", "bılezık") -> {
            val gp = goldPrices.find { it.name.contains("BİLEZİK", ignoreCase = true) }
            gp?.let { parseVal(it.buy) ?: parseVal(it.sell) }?.let { return it }
        }
        matches("22 ayar gram", "22 ayar") || (matches("22") && matches("gram")) -> {
            val gp = goldPrices.find { it.name.trim() == "22 AYAR GRAM ALTIN" }
            gp?.let { parseVal(it.buy) ?: parseVal(it.sell) }?.let { return it }
        }
        matches("24 ayar gram", "24 ayar") || (matches("24") && matches("gram")) -> {
            val gp = goldPrices.find { it.name.trim() == "24 AYAR GRAM ALTIN" }
            gp?.let { parseVal(it.buy) ?: parseVal(it.sell) }?.let { return it }
        }
        matches("yapı", "yapi", "yk", "kredi", "xau") -> {
            val br = bankRates.find { it.code == "XAU" }
            br?.let { parseVal(it.buy) ?: parseVal(it.sell) }?.let { return it }
        }
        matches("altın", "altin") -> {
            val gp24 = goldPrices.find { it.name.contains("24 AYAR", ignoreCase = true) }
            val xau = bankRates.find { it.code == "XAU" }
            val val24 = gp24?.let { parseVal(it.buy) ?: parseVal(it.sell) }
            val valXau = xau?.let { parseVal(it.buy) ?: parseVal(it.sell) }
            val24?.let { return it } ?: valXau?.let { return it }
        }
        matches("dolar", "usd", "$") -> {
            val br = bankRates.find { it.code == "USD" }
            br?.let { parseVal(it.buy) ?: parseVal(it.sell) }?.let { return it }
        }
        matches("euro", "eur", "€") -> {
            val br = bankRates.find { it.code == "EUR" }
            br?.let { parseVal(it.buy) ?: parseVal(it.sell) }?.let { return it }
        }
    }
    return null
}
