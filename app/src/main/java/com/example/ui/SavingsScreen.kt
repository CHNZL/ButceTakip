package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.luminance
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

data class AssetCardPalette(
    val containerColor: Color,
    val borderColor: Color,
    val iconContainerColor: Color,
    val iconColor: Color,
    val onContainerColor: Color,
    val secondaryTextColor: Color,
    val badgeContainerColor: Color,
    val badgeContentColor: Color,
    val accentColor: Color
)

fun getAssetIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    val clean = category.lowercase(Locale("tr"))
    return when {
        clean.contains("altın") || clean.contains("altin") || clean.contains("24") || clean.contains("22") || clean.contains("bilezik") || clean.contains("xau") -> Icons.Rounded.Savings
        clean.contains("dolar") || clean.contains("usd") || clean.contains("$") -> Icons.Rounded.AttachMoney
        clean.contains("euro") || clean.contains("eur") || clean.contains("€") -> Icons.Rounded.Euro
        clean.contains("borsa") || clean.contains("hisse") || clean.contains("stock") -> Icons.Rounded.ShowChart
        clean.contains("fon") -> Icons.Rounded.PieChart
        clean.contains("gümüş") || clean.contains("gumus") || clean.contains("xag") -> Icons.Rounded.Layers
        clean.contains("emeklilik") || clean.contains("bes") -> Icons.Rounded.Shield
        else -> Icons.Rounded.AccountBalance
    }
}

@Composable
fun getAssetCardPalette(category: String): AssetCardPalette {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val clean = category.lowercase(Locale("tr"))

    return when {
        clean.contains("altın") || clean.contains("altin") || clean.contains("24") || clean.contains("22") || clean.contains("bilezik") || clean.contains("xau") -> {
            if (isDark) {
                AssetCardPalette(
                    containerColor = Color(0xFF1E1C16),
                    borderColor = Color(0xFFEAB308).copy(alpha = 0.22f),
                    iconContainerColor = Color(0xFFEAB308).copy(alpha = 0.16f),
                    iconColor = Color(0xFFFACC15),
                    onContainerColor = Color(0xFFFEF08A),
                    secondaryTextColor = Color(0xFFEAB308).copy(alpha = 0.85f),
                    badgeContainerColor = Color(0xFFCA8A04).copy(alpha = 0.22f),
                    badgeContentColor = Color(0xFFFDE047),
                    accentColor = Color(0xFFEAB308)
                )
            } else {
                AssetCardPalette(
                    containerColor = Color(0xFFFFFFFF),
                    borderColor = Color(0xFFFDE047).copy(alpha = 0.6f),
                    iconContainerColor = Color(0xFFFEF9C3),
                    iconColor = Color(0xFFCA8A04),
                    onContainerColor = Color(0xFF713F12),
                    secondaryTextColor = Color(0xFF854D0E),
                    badgeContainerColor = Color(0xFFFEF08A),
                    badgeContentColor = Color(0xFF713F12),
                    accentColor = Color(0xFFD97706)
                )
            }
        }
        clean.contains("dolar") || clean.contains("usd") || clean.contains("$") -> {
            if (isDark) {
                AssetCardPalette(
                    containerColor = Color(0xFF14201A),
                    borderColor = Color(0xFF10B981).copy(alpha = 0.22f),
                    iconContainerColor = Color(0xFF10B981).copy(alpha = 0.16f),
                    iconColor = Color(0xFF34D399),
                    onContainerColor = Color(0xFFECFDF5),
                    secondaryTextColor = Color(0xFF6EE7B7).copy(alpha = 0.85f),
                    badgeContainerColor = Color(0xFF059669).copy(alpha = 0.22f),
                    badgeContentColor = Color(0xFFA7F3D0),
                    accentColor = Color(0xFF10B981)
                )
            } else {
                AssetCardPalette(
                    containerColor = Color(0xFFFFFFFF),
                    borderColor = Color(0xFF6EE7B7).copy(alpha = 0.6f),
                    iconContainerColor = Color(0xFFDCFCE7),
                    iconColor = Color(0xFF059669),
                    onContainerColor = Color(0xFF064E3B),
                    secondaryTextColor = Color(0xFF047857),
                    badgeContainerColor = Color(0xFFA7F3D0),
                    badgeContentColor = Color(0xFF064E3B),
                    accentColor = Color(0xFF059669)
                )
            }
        }
        clean.contains("euro") || clean.contains("eur") || clean.contains("€") -> {
            if (isDark) {
                AssetCardPalette(
                    containerColor = Color(0xFF171A26),
                    borderColor = Color(0xFF6366F1).copy(alpha = 0.22f),
                    iconContainerColor = Color(0xFF6366F1).copy(alpha = 0.16f),
                    iconColor = Color(0xFF818CF8),
                    onContainerColor = Color(0xFFEEF2FF),
                    secondaryTextColor = Color(0xFFA5B4FC).copy(alpha = 0.85f),
                    badgeContainerColor = Color(0xFF4F46E5).copy(alpha = 0.22f),
                    badgeContentColor = Color(0xFFC7D2FE),
                    accentColor = Color(0xFF6366F1)
                )
            } else {
                AssetCardPalette(
                    containerColor = Color(0xFFFFFFFF),
                    borderColor = Color(0xFFA5B4FC).copy(alpha = 0.6f),
                    iconContainerColor = Color(0xFFE0E7FF),
                    iconColor = Color(0xFF4F46E5),
                    onContainerColor = Color(0xFF312E81),
                    secondaryTextColor = Color(0xFF4338CA),
                    badgeContainerColor = Color(0xFFC7D2FE),
                    badgeContentColor = Color(0xFF312E81),
                    accentColor = Color(0xFF4F46E5)
                )
            }
        }
        clean.contains("borsa") || clean.contains("hisse") || clean.contains("stock") -> {
            if (isDark) {
                AssetCardPalette(
                    containerColor = Color(0xFF141C26),
                    borderColor = Color(0xFF0284C7).copy(alpha = 0.22f),
                    iconContainerColor = Color(0xFF0284C7).copy(alpha = 0.16f),
                    iconColor = Color(0xFF38BDF8),
                    onContainerColor = Color(0xFFF0F9FF),
                    secondaryTextColor = Color(0xFF7DD3FC).copy(alpha = 0.85f),
                    badgeContainerColor = Color(0xFF0284C7).copy(alpha = 0.22f),
                    badgeContentColor = Color(0xFFBAE6FD),
                    accentColor = Color(0xFF0284C7)
                )
            } else {
                AssetCardPalette(
                    containerColor = Color(0xFFFFFFFF),
                    borderColor = Color(0xFF7DD3FC).copy(alpha = 0.6f),
                    iconContainerColor = Color(0xFFE0F2FE),
                    iconColor = Color(0xFF0284C7),
                    onContainerColor = Color(0xFF0C4A6E),
                    secondaryTextColor = Color(0xFF0369A1),
                    badgeContainerColor = Color(0xFFBAE6FD),
                    badgeContentColor = Color(0xFF0C4A6E),
                    accentColor = Color(0xFF0284C7)
                )
            }
        }
        clean.contains("fon") -> {
            if (isDark) {
                AssetCardPalette(
                    containerColor = Color(0xFF1C1626),
                    borderColor = Color(0xFF9333EA).copy(alpha = 0.22f),
                    iconContainerColor = Color(0xFF9333EA).copy(alpha = 0.16f),
                    iconColor = Color(0xFFC084FC),
                    onContainerColor = Color(0xFFFAF5FF),
                    secondaryTextColor = Color(0xFFD8B4FE).copy(alpha = 0.85f),
                    badgeContainerColor = Color(0xFF7E22CE).copy(alpha = 0.22f),
                    badgeContentColor = Color(0xFFE9D5FF),
                    accentColor = Color(0xFF9333EA)
                )
            } else {
                AssetCardPalette(
                    containerColor = Color(0xFFFFFFFF),
                    borderColor = Color(0xFFD8B4FE).copy(alpha = 0.6f),
                    iconContainerColor = Color(0xFFF3E8FF),
                    iconColor = Color(0xFF7E22CE),
                    onContainerColor = Color(0xFF3B0764),
                    secondaryTextColor = Color(0xFF6B21A8),
                    badgeContainerColor = Color(0xFFE9D5FF),
                    badgeContentColor = Color(0xFF3B0764),
                    accentColor = Color(0xFF9333EA)
                )
            }
        }
        clean.contains("gümüş") || clean.contains("gumus") || clean.contains("xag") -> {
            if (isDark) {
                AssetCardPalette(
                    containerColor = Color(0xFF1B1E22),
                    borderColor = Color(0xFF94A3B8).copy(alpha = 0.22f),
                    iconContainerColor = Color(0xFF94A3B8).copy(alpha = 0.16f),
                    iconColor = Color(0xFFCBD5E1),
                    onContainerColor = Color(0xFFF8FAFC),
                    secondaryTextColor = Color(0xFF94A3B8).copy(alpha = 0.85f),
                    badgeContainerColor = Color(0xFF64748B).copy(alpha = 0.22f),
                    badgeContentColor = Color(0xFFE2E8F0),
                    accentColor = Color(0xFF94A3B8)
                )
            } else {
                AssetCardPalette(
                    containerColor = Color(0xFFFFFFFF),
                    borderColor = Color(0xFFCBD5E1).copy(alpha = 0.6f),
                    iconContainerColor = Color(0xFFF1F5F9),
                    iconColor = Color(0xFF475569),
                    onContainerColor = Color(0xFF0F172A),
                    secondaryTextColor = Color(0xFF334155),
                    badgeContainerColor = Color(0xFFE2E8F0),
                    badgeContentColor = Color(0xFF0F172A),
                    accentColor = Color(0xFF64748B)
                )
            }
        }
        clean.contains("emeklilik") || clean.contains("bes") -> {
            if (isDark) {
                AssetCardPalette(
                    containerColor = Color(0xFF131F21),
                    borderColor = Color(0xFF0D9488).copy(alpha = 0.22f),
                    iconContainerColor = Color(0xFF0D9488).copy(alpha = 0.16f),
                    iconColor = Color(0xFF2DD4BF),
                    onContainerColor = Color(0xFFF0FDFA),
                    secondaryTextColor = Color(0xFF5EEAD4).copy(alpha = 0.85f),
                    badgeContainerColor = Color(0xFF0D9488).copy(alpha = 0.22f),
                    badgeContentColor = Color(0xFF99F6E4),
                    accentColor = Color(0xFF0D9488)
                )
            } else {
                AssetCardPalette(
                    containerColor = Color(0xFFFFFFFF),
                    borderColor = Color(0xFF5EEAD4).copy(alpha = 0.6f),
                    iconContainerColor = Color(0xFFCCFBF1),
                    iconColor = Color(0xFF0D9488),
                    onContainerColor = Color(0xFF134E4A),
                    secondaryTextColor = Color(0xFF115E59),
                    badgeContainerColor = Color(0xFF99F6E4),
                    badgeContentColor = Color(0xFF134E4A),
                    accentColor = Color(0xFF0D9488)
                )
            }
        }
        else -> {
            if (isDark) {
                AssetCardPalette(
                    containerColor = Color(0xFF171B24),
                    borderColor = Color(0xFF3B82F6).copy(alpha = 0.22f),
                    iconContainerColor = Color(0xFF3B82F6).copy(alpha = 0.16f),
                    iconColor = Color(0xFF60A5FA),
                    onContainerColor = Color(0xFFEFF6FF),
                    secondaryTextColor = Color(0xFF93C5FD).copy(alpha = 0.85f),
                    badgeContainerColor = Color(0xFF2563EB).copy(alpha = 0.22f),
                    badgeContentColor = Color(0xFFBFDBFE),
                    accentColor = Color(0xFF3B82F6)
                )
            } else {
                AssetCardPalette(
                    containerColor = Color(0xFFFFFFFF),
                    borderColor = Color(0xFF93C5FD).copy(alpha = 0.6f),
                    iconContainerColor = Color(0xFFDBEAFE),
                    iconColor = Color(0xFF2563EB),
                    onContainerColor = Color(0xFF1E3A8A),
                    secondaryTextColor = Color(0xFF1D4ED8),
                    badgeContainerColor = Color(0xFFBFDBFE),
                    badgeContentColor = Color(0xFF1E3A8A),
                    accentColor = Color(0xFF2563EB)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    transactions: List<Transaction>,
    goldPrices: List<GoldPrice>,
    bankRates: List<BankRate>,
    onAddSaving: (categoryName: String?) -> Unit,
    onDeleteSavingTransaction: (Int) -> Unit,
    preferenceManager: com.example.data.PreferenceManager? = null,
    customPricesTrigger: Long = 0L,
    onUpdateCustomPrice: ((String, Double) -> Unit)? = null,
    ziraatRates: List<BankRate> = emptyList(),
    besPortfolios: List<com.example.data.BesPortfolio> = emptyList(),
    onUpdateBes: ((com.example.data.BesPortfolio) -> Unit)? = null,
    onDeleteBes: ((com.example.data.BesPortfolio) -> Unit)? = null
) {
    var editingCategoryPrice by remember { mutableStateOf<String?>(null) }
    var newPriceText by remember { mutableStateOf("") }
    var showBesDialog by remember { mutableStateOf(false) }
    var editingBes by remember { mutableStateOf<com.example.data.BesPortfolio?>(null) }
    var selectedCategoryForDetails by remember { mutableStateOf<String?>(null) }

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

    val besCalculatedValues = remember(besPortfolios) {
        besPortfolios.map { bes ->
            val years = (System.currentTimeMillis() - bes.startDate) / (1000L * 60 * 60 * 24 * 365)
            val stateVesting = if (bes.isRetired) 1.0 else {
                when {
                    years < 3 -> 0.0
                    years < 6 -> 0.15
                    years < 10 -> 0.35
                    else -> 0.60
                }
            }
            val totalVal = bes.investment + bes.investmentReturn + (bes.stateContribution + bes.stateContributionReturn) * stateVesting
            Triple(bes, totalVal, bes.investment)
        }
    }
    val besTotalValue = remember(besCalculatedValues) { besCalculatedValues.sumOf { it.second } }
    val besPaid = remember(besCalculatedValues) { besCalculatedValues.sumOf { it.third } }
    val totalCurrentValue = assetSummaries.sumOf { it.currentValue } + besTotalValue
    val totalPaidAll = assetSummaries.sumOf { it.totalPaid } + besPaid
    val totalProfitLoss = totalCurrentValue - totalPaidAll
    val totalProfitLossPercent = if (totalPaidAll > 0.0) (totalProfitLoss / totalPaidAll) * 100.0 else 0.0

    val currencyFormat = remember { com.example.util.FormatUtil.getCurrencyFormat() }
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

        // --- 2. ASSETS SUMMARY CARDS ---
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Varlık Özetlerim",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = { onAddSaving(null) },
                        modifier = Modifier.size(32.dp).testTag("add_new_asset_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddCircle,
                            contentDescription = "Yeni Birikim Ekle",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                
                if (assetSummaries.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Savings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Henüz birikim varlığı eklenmedi.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { onAddSaving(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Birikim Ekle", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        assetSummaries.forEach { summary ->
                            AssetSummaryCard(
                                summary = summary,
                                currencyFormat = currencyFormat,
                                preferenceManager = preferenceManager,
                                onQuickAdd = {
                                    onAddSaving(summary.category)
                                },
                                onEditClick = {
                                    editingCategoryPrice = summary.category
                                    newPriceText = if (summary.currentUnitPrice > 0.0) com.example.util.formatDoubleForInput(summary.currentUnitPrice) else ""
                                },
                                onCardClick = {
                                    selectedCategoryForDetails = summary.category
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- 3. BIREYSEL EMEKLILIK (BES) SECTION ---
        item { 
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                Spacer(modifier = Modifier.height(8.dp))
                if (besPortfolios.isNotEmpty()) { 
                    besCalculatedValues.forEachIndexed { index, (bes, totalVal, paid) ->
                        BesSummaryCard(
                            besPortfolio = bes,
                            besTotalValue = totalVal,
                            besPaid = paid,
                            currencyFormat = currencyFormat,
                            isFirstCard = index == 0,
                            onAddClick = {
                                editingBes = null
                                showBesDialog = true
                            },
                            onEditClick = {
                                editingBes = bes
                                showBesDialog = true
                            },
                            onDeleteClick = {
                                onDeleteBes?.invoke(bes)
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                } else { 
                    Button(
                        onClick = { 
                            editingBes = null
                            showBesDialog = true 
                        }, 
                        modifier = Modifier.fillMaxWidth()
                    ) { 
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bireysel Emeklilik (BES) Ekle") 
                    } 
                } 
            }
        }

        // --- 4. BOTTOM QUICK ADD BUTTON ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = { onAddSaving(null) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Yeni Birikim Kaydı Ekle", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }

    // --- DIALOGS & MODAL SHEETS ---

    // 1. Asset Category Transactions Detail Sheet
    if (selectedCategoryForDetails != null) {
        val cat = selectedCategoryForDetails!!
        val catTransactions = savingTransactions.filter { it.category == cat }
        val catSummary = assetSummaries.find { it.category == cat }

        AssetTransactionsSheet(
            category = cat,
            summary = catSummary,
            transactions = catTransactions,
            goldPrices = goldPrices,
            bankRates = bankRates,
            ziraatRates = ziraatRates,
            preferenceManager = preferenceManager,
            dateFormat = dateFormat,
            currencyFormat = currencyFormat,
            onDismiss = { selectedCategoryForDetails = null },
            onAddTransaction = { categoryName ->
                selectedCategoryForDetails = null
                onAddSaving(categoryName)
            },
            onDeleteTransaction = { id ->
                onDeleteSavingTransaction(id)
            }
        )
    }

    // 2. BES Dialog
    if (showBesDialog) { 
        BesDialog( 
            besPortfolio = editingBes, 
            onDismiss = { showBesDialog = false }, 
            onSave = { 
                onUpdateBes?.invoke(it) 
                showBesDialog = false 
            } 
        ) 
    }

    // 3. Custom Price Dialog
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
                    Text("Güncelle", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCategoryPrice = null }) {
                    Text("İptal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            title = {
                Text(
                    text = "$catName Fiyatını Güncelle",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Lütfen 1 gram veya 1 adet için güncel birim fiyatını giriniz (₺):",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            containerColor = MaterialTheme.colorScheme.surface
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
    onQuickAdd: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    onCardClick: () -> Unit
) {
    val palette = getAssetCardPalette(summary.category)
    val isProfit = summary.profitLoss >= 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("asset_summary_card_${summary.category}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.containerColor),
        border = BorderStroke(1.dp, palette.borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Top Row: Icon + Category Name & Quantity + Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon + Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.iconContainerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getAssetIcon(summary.category),
                            contentDescription = summary.category,
                            tint = palette.iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = summary.category,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.onContainerColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val unitLabel = if (summary.category.lowercase().contains("bilezik") || summary.category.lowercase().contains("altın") || summary.category.lowercase().contains("altin") || summary.category.lowercase().contains("gümüş")) "gr" else "adet"
                        Text(
                            text = "${com.example.util.FormatUtil.getNumberFormat(2).format(summary.totalQuantity)} $unitLabel",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = palette.secondaryTextColor
                        )
                    }
                }

                // Quick Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Quick Add (+) Button
                    FilledTonalIconButton(
                        onClick = onQuickAdd,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("quick_add_btn_${summary.category}"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = palette.accentColor,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "${summary.category} Ekle",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (onEditClick != null && isEditableCategory(summary.category, preferenceManager)) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("custom_price_edit_btn_${summary.category}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Fiyat Düzenle",
                                tint = palette.onContainerColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onCardClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Detaylar",
                            tint = palette.onContainerColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Portfolio Value & Profit/Loss Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Portföy Değeri",
                        fontSize = 11.sp,
                        color = palette.secondaryTextColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currencyFormat.format(summary.currentValue),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = palette.onContainerColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val rateUnitStr = if (summary.category.lowercase().contains("bilezik") || summary.category.lowercase().contains("altın") || summary.category.lowercase().contains("altin") || summary.category.lowercase().contains("gümüş")) "₺/g" else "₺"
                    Text(
                        text = "Birim Fiyat: ${com.example.util.FormatUtil.getNumberFormat(1).format(summary.currentUnitPrice)} $rateUnitStr",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.secondaryTextColor,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Profit/Loss Badge Pill
                val profitBgColor = if (isProfit) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                val profitTextColor = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)

                Surface(
                    color = profitBgColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isProfit) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = profitTextColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${if (summary.profitLoss > 0) "+" else ""}${currencyFormat.format(summary.profitLoss)} (${if (summary.profitLoss > 0) "+" else ""}${com.example.util.FormatUtil.getNumberFormat(2).format(summary.profitLossPercent)}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = profitTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
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
                    fontSize = 9.sp,
                    color = palette.secondaryTextColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetTransactionsSheet(
    category: String,
    summary: SavingAssetSummary?,
    transactions: List<Transaction>,
    goldPrices: List<GoldPrice>,
    bankRates: List<BankRate>,
    ziraatRates: List<BankRate>,
    preferenceManager: com.example.data.PreferenceManager?,
    dateFormat: SimpleDateFormat,
    currencyFormat: NumberFormat,
    onDismiss: () -> Unit,
    onAddTransaction: (String) -> Unit,
    onDeleteTransaction: (Int) -> Unit
) {
    val palette = getAssetCardPalette(category)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(palette.accentColor)
                    )
                    Text(
                        text = "$category Kayıtları",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = { onAddTransaction(category) },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accentColor, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ekle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (summary != null) {
                // Overview Summary Card inside Sheet
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = palette.containerColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Toplam Portföy Değeri", fontSize = 11.sp, color = palette.secondaryTextColor)
                            Text(
                                currencyFormat.format(summary.currentValue),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = palette.onContainerColor
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Toplam Miktar", fontSize = 11.sp, color = palette.secondaryTextColor)
                            Text(
                                "${com.example.util.FormatUtil.getNumberFormat(2).format(summary.totalQuantity)} ${if (category.lowercase().contains("bilezik") || category.lowercase().contains("altın") || category.lowercase().contains("altin")) "gr" else "adet"}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.onContainerColor
                            )
                        }
                    }
                }
            }

            Text(
                text = "İşlem Geçmişi (${transactions.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Bu varlığa ait kayıt bulunmuyor.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false).heightIn(max = 380.dp)
                ) {
                    items(transactions, key = { it.id }) { tx ->
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
                            onDelete = { onDeleteTransaction(tx.id) }
                        )
                    }
                }
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // First Row: Category Title + Date & Delete Button
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp).testTag("delete_saving_tx_btn_${tx.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Kayıt Sil",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle: Date
            Text(
                text = formattedDate,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(10.dp))

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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${com.example.util.FormatUtil.getNumberFormat(2).format(tx.quantity ?: 0.0)} ${if (tx.category.lowercase().contains("bilezik") || tx.category.lowercase().contains("altın") || tx.category.lowercase().contains("altin")) "gr" else "ad"} @ ${com.example.util.FormatUtil.getNumberFormat(1).format(tx.unitPrice ?: 0.0)} ₺",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Total Purchase Paid
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Ödeme",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currencyFormat.format(tx.amount),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = currencyFormat.format(currentValue),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Row Profit Loss pill
                Surface(
                    color = if (isProfit) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f),
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
    ziraatRates: List<BankRate> = emptyList(),
    besPortfolio: com.example.data.BesPortfolio? = null,
    onUpdateBes: ((com.example.data.BesPortfolio) -> Unit)? = null
): Double? {
    val cleanCatLower = category.trim().lowercase(java.util.Locale("tr", "TR"))
    val cleanCatRoot = category.trim().lowercase(java.util.Locale.ROOT)

    fun parseVal(s: String?): Double? {
        if (s == null || s.isBlank() || s == "-") return null
        return com.example.util.parseFormattedAmount(s)
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

@Composable
fun BesSummaryCard(
    besPortfolio: com.example.data.BesPortfolio,
    besTotalValue: Double,
    besPaid: Double,
    currencyFormat: java.text.NumberFormat,
    isFirstCard: Boolean = false,
    onAddClick: (() -> Unit)? = null,
    onEditClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    val palette = getAssetCardPalette("bes")
    val profit = besTotalValue - besPaid
    val profitPercent = if (besPaid > 0) (profit / besPaid) * 100 else 0.0
    val isProfit = profit >= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.containerColor),
        border = BorderStroke(1.dp, palette.borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.iconContainerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = "BES",
                            tint = palette.iconColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = besPortfolio.holderName.ifBlank { "Bireysel Emeklilik (BES)" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = palette.onContainerColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Devlet Katkısı Dahil Emeklilik",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = palette.secondaryTextColor
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isFirstCard && onAddClick != null) {
                        FilledTonalIconButton(
                            onClick = onAddClick,
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = palette.accentColor,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Yeni BES Ekle",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Düzenle",
                            tint = palette.onContainerColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (onDeleteClick != null) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Sil",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Toplam BES Değeri",
                        fontSize = 11.sp,
                        color = palette.secondaryTextColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currencyFormat.format(besTotalValue),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = palette.onContainerColor
                    )
                }

                val profitBgColor = if (isProfit) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                val profitTextColor = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)

                Surface(
                    color = profitBgColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isProfit) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = profitTextColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${if (isProfit) "+" else ""}${currencyFormat.format(profit)} (${if (isProfit) "+" else ""}${String.format(Locale.US, "%.2f", profitPercent)}%)",
                            color = profitTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
