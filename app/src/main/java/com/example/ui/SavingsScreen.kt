package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BankRate
import com.example.data.BesPortfolio
import com.example.data.GoldPrice
import com.example.data.PreferenceManager
import com.example.data.Transaction
import com.example.data.TransactionType
import com.example.ui.theme.*
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

fun getAssetIcon(category: String): ImageVector {
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

private fun getAssetAccentColor(category: String, isDark: Boolean): Color {
    val clean = category.lowercase(Locale("tr"))
    return when {
        clean.contains("altın") || clean.contains("altin") || clean.contains("24") || clean.contains("22") || clean.contains("bilezik") || clean.contains("xau") ->
            if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
        clean.contains("dolar") || clean.contains("usd") || clean.contains("$") ->
            if (isDark) CyberNeonMint else Color(0xFF059669)
        clean.contains("euro") || clean.contains("eur") || clean.contains("€") ->
            if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)
        clean.contains("borsa") || clean.contains("hisse") || clean.contains("stock") ->
            if (isDark) CyberNeonCyan else Color(0xFF0284C7)
        clean.contains("fon") ->
            if (isDark) CyberNeonPurple else Color(0xFF7E22CE)
        clean.contains("gümüş") || clean.contains("gumus") || clean.contains("xag") ->
            if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
        clean.contains("emeklilik") || clean.contains("bes") ->
            if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488)
        else -> if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    transactions: List<Transaction>,
    goldPrices: List<GoldPrice>,
    bankRates: List<BankRate>,
    onAddSaving: (categoryName: String?) -> Unit,
    onDeleteSavingTransaction: (Int) -> Unit,
    preferenceManager: PreferenceManager? = null,
    customPricesTrigger: Long = 0L,
    onUpdateCustomPrice: ((String, Double) -> Unit)? = null,
    ziraatRates: List<BankRate> = emptyList(),
    besPortfolios: List<BesPortfolio> = emptyList(),
    onUpdateBes: ((BesPortfolio) -> Unit)? = null,
    onDeleteBes: ((BesPortfolio) -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val teal = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488)
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)

    var isPortfolioHidden by remember { mutableStateOf(false) }
    var editingCategoryPrice by remember { mutableStateOf<String?>(null) }
    var newPriceText by remember { mutableStateOf("") }
    var showBesDialog by remember { mutableStateOf(false) }
    var editingBes by remember { mutableStateOf<BesPortfolio?>(null) }
    var pendingDeleteBes by remember { mutableStateOf<BesPortfolio?>(null) }
    var selectedCategoryForDetails by remember { mutableStateOf<String?>(null) }

    val savingTransactions = remember(transactions) {
        transactions.filter { it.type == TransactionType.SAVING }
            .sortedByDescending { it.timestamp }
    }

    val assetSummaries = remember(savingTransactions, goldPrices, bankRates, ziraatRates, customPricesTrigger) {
        savingTransactions.groupBy { it.category }.map { (category, txList) ->
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
    val isTotalProfit = totalProfitLoss >= 0.0

    val currencyFormat = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("tr", "TR")) }

    // Real proportional allocation bar segments
    val allocationSegments = remember(assetSummaries, besTotalValue, isDark) {
        buildList {
            assetSummaries.filter { it.currentValue > 0.0 }.forEach {
                add(Triple(it.category, it.currentValue, getAssetAccentColor(it.category, isDark)))
            }
            if (besTotalValue > 0.0) {
                add(Triple("BES", besTotalValue, teal))
            }
        }.sortedByDescending { it.second }.take(5)
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
                .testTag("savings_screen_root"),
            contentPadding = PaddingValues(top = 6.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. EXECUTIVE PORTFOLIO HERO CARD (100% REAL ALLOCATION BAR)
            item(key = "portfolio_hero") {
                val pnlColor = if (isTotalProfit) mint else coral
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, borderColor),
                    shadowElevation = if (isDark) 0.dp else 1.5.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = if (isDark) {
                                        listOf(Color(0xFF101D24), MaterialTheme.colorScheme.surface)
                                    } else {
                                        listOf(mint.copy(alpha = 0.06f), Color.White)
                                    }
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "TOPLAM PORTFÖY DEĞERİ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 0.8.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(
                                    onClick = { isPortfolioHidden = !isPortfolioHidden },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("portfolio_visibility_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (isPortfolioHidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = "Portföy Tutarını Gizle veya Göster",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (totalPaidAll > 0.0) {
                                Surface(
                                    shape = RoundedCornerShape(9.dp),
                                    color = pnlColor.copy(alpha = if (isDark) 0.16f else 0.11f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isTotalProfit) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                                            contentDescription = null,
                                            tint = pnlColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "${if (isTotalProfit) "+" else ""}${String.format(Locale.US, "%.1f", totalProfitLossPercent)}%",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = JetBrainsMonoFamily,
                                                fontFeatureSettings = "tnum"
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            color = pnlColor
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isPortfolioHidden) "•••••• ₺" else currencyFormat.format(totalCurrentValue),
                            style = TabularHeroStyle,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Real Asset Allocation Proportion Bar
                        if (allocationSegments.isNotEmpty() && totalCurrentValue > 0.0) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                val segSum = allocationSegments.sumOf { it.second }.coerceAtLeast(1.0)
                                allocationSegments.forEach { (_, value, color) ->
                                    val weight = (value / segSum).toFloat().coerceAtLeast(0.03f)
                                    Box(
                                        modifier = Modifier
                                            .weight(weight)
                                            .fillMaxHeight()
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                allocationSegments.take(3).forEach { (name, value, color) ->
                                    val share = ((value / totalCurrentValue) * 100).toInt()
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Text(
                                            text = "$name %$share",
                                            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(thickness = 0.7.dp, color = borderColor.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PortfolioStatCell(
                                label = "Maliyet",
                                value = if (isPortfolioHidden) "••••" else currencyFormat.format(totalPaidAll),
                                accent = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(0.8.dp)
                                    .background(borderColor)
                            )
                            PortfolioStatCell(
                                label = "Net Kâr / Zarar",
                                value = if (isPortfolioHidden) "••••" else "${if (isTotalProfit) "+" else ""}${currencyFormat.format(totalProfitLoss)}",
                                accent = pnlColor,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(0.8.dp)
                                    .background(borderColor)
                            )
                            PortfolioStatCell(
                                label = "Varlık",
                                value = "${assetSummaries.size + besPortfolios.size} kalem",
                                accent = sky,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. VARLIK PORTFÖYÜ SECTION
            item(key = "section_assets") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp, end = 2.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VARLIK PORTFÖYÜ (${assetSummaries.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.9.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            onClick = { onAddSaving(null) },
                            shape = RoundedCornerShape(9.dp),
                            color = mint.copy(alpha = if (isDark) 0.16f else 0.11f),
                            modifier = Modifier.testTag("add_new_asset_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = mint, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "Birikim Ekle",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = mint
                                )
                            }
                        }
                    }

                    if (assetSummaries.isEmpty()) {
                        SettingsGroupCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(mint.copy(alpha = if (isDark) 0.16f else 0.11f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Savings,
                                        contentDescription = null,
                                        tint = mint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = "Henüz birikim varlığı eklenmedi",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Altın, döviz, hisse veya fon birikimlerinizi ekleyerek canlı piyasa değerini anlık takip edin.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        SettingsGroupCard {
                            assetSummaries.forEachIndexed { index, summary ->
                                val weightPct = if (totalCurrentValue > 0.0) {
                                    ((summary.currentValue / totalCurrentValue) * 100).toInt()
                                } else 0
                                AssetVaultRow(
                                    summary = summary,
                                    weightPct = weightPct,
                                    currencyFormat = currencyFormat,
                                    preferenceManager = preferenceManager,
                                    isHidden = isPortfolioHidden,
                                    onQuickAdd = { onAddSaving(summary.category) },
                                    onEditPrice = {
                                        editingCategoryPrice = summary.category
                                        newPriceText = if (summary.currentUnitPrice > 0.0) {
                                            com.example.util.formatDoubleForInput(summary.currentUnitPrice)
                                        } else ""
                                    },
                                    onClick = { selectedCategoryForDetails = summary.category }
                                )
                                if (index != assetSummaries.lastIndex) {
                                    SettingsInsetDivider()
                                }
                            }
                        }
                    }
                }
            }

            // 3. BİREYSEL EMEKLİLİK (BES) SECTION
            item(key = "section_bes") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp, end = 2.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BİREYSEL EMEKLİLİK (BES)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.9.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            onClick = {
                                editingBes = null
                                showBesDialog = true
                            },
                            shape = RoundedCornerShape(9.dp),
                            color = teal.copy(alpha = if (isDark) 0.16f else 0.11f),
                            modifier = Modifier.testTag("add_bes_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = teal, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "BES Ekle",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = teal
                                )
                            }
                        }
                    }

                    if (besCalculatedValues.isEmpty()) {
                        SettingsGroupCard {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        editingBes = null
                                        showBesDialog = true
                                    }
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(teal.copy(alpha = if (isDark) 0.16f else 0.11f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Shield, contentDescription = null, tint = teal, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "BES Portföyü Takip Et",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Devlet katkısı hak ediş oranıyla net emeklilik değerini hesapla",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                )
                            }
                        }
                    } else {
                        SettingsGroupCard {
                            besCalculatedValues.forEachIndexed { index, (bes, totalVal, paid) ->
                                BesVaultRow(
                                    besPortfolio = bes,
                                    besTotalValue = totalVal,
                                    besPaid = paid,
                                    currencyFormat = currencyFormat,
                                    isHidden = isPortfolioHidden,
                                    onEdit = {
                                        editingBes = bes
                                        showBesDialog = true
                                    },
                                    onDelete = { pendingDeleteBes = bes }
                                )
                                if (index != besCalculatedValues.lastIndex) {
                                    SettingsInsetDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 1. Asset Transactions Detail Sheet
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
            onDeleteTransaction = { id -> onDeleteSavingTransaction(id) }
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

    // 3. BES Delete Confirmation
    if (pendingDeleteBes != null && onDeleteBes != null) {
        val besToDelete = pendingDeleteBes!!
        AlertDialog(
            onDismissRequest = { pendingDeleteBes = null },
            title = { Text("BES Kaydı Silinsin Mi?", fontWeight = FontWeight.Bold) },
            text = { Text("\"${besToDelete.holderName.ifBlank { "Bireysel Emeklilik" }}\" portföy kaydı silinecek.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteBes(besToDelete)
                        pendingDeleteBes = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sil") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteBes = null }) { Text("İptal") }
            }
        )
    }

    // 4. Custom Unit Price Dialog
    if (editingCategoryPrice != null) {
        val catName = editingCategoryPrice!!
        AlertDialog(
            onDismissRequest = { editingCategoryPrice = null },
            title = { Text("$catName Birim Fiyatı", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1 gram veya 1 adet için güncel piyasa fiyatını girin (₺):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newPriceText,
                        onValueChange = { newPriceText = com.example.util.formatInputAmount(it) },
                        placeholder = { Text("Örn: 2450,50") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_price_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = com.example.util.parseFormattedAmount(newPriceText)
                        if (parsed != null && parsed >= 0.0) {
                            onUpdateCustomPrice?.invoke(catName, parsed)
                        }
                        editingCategoryPrice = null
                    }
                ) { Text("Güncelle") }
            },
            dismissButton = {
                TextButton(onClick = { editingCategoryPrice = null }) { Text("İptal") }
            }
        )
    }
}

@Composable
private fun PortfolioStatCell(
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
private fun AssetVaultRow(
    summary: SavingAssetSummary,
    weightPct: Int,
    currencyFormat: NumberFormat,
    preferenceManager: PreferenceManager?,
    isHidden: Boolean,
    onQuickAdd: () -> Unit,
    onEditPrice: () -> Unit,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val accent = getAssetAccentColor(summary.category, isDark)
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val isProfit = summary.profitLoss >= 0.0
    val pnlColor = if (isProfit) mint else coral

    val isGram = summary.category.lowercase().let {
        it.contains("bilezik") || it.contains("altın") || it.contains("altin") || it.contains("gümüş") || it.contains("gumus")
    }
    val unitLabel = if (isGram) "gr" else "adet"
    val rateUnitStr = if (isGram) "₺/g" else "₺"
    val editable = isEditableCategory(summary.category, preferenceManager)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp)
            .testTag("asset_summary_card_${summary.category}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = if (isDark) 0.16f else 0.11f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getAssetIcon(summary.category),
                    contentDescription = summary.category,
                    tint = accent,
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = summary.category,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (weightPct > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "%$weightPct",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${com.example.util.FormatUtil.getNumberFormat(2).format(summary.totalQuantity)} $unitLabel · ${com.example.util.FormatUtil.getNumberFormat(1).format(summary.currentUnitPrice)} $rateUnitStr",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isHidden) "••••" else currencyFormat.format(summary.currentValue),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontFeatureSettings = "tnum"
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    softWrap = false,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Surface(
                    shape = RoundedCornerShape(7.dp),
                    color = pnlColor.copy(alpha = if (isDark) 0.16f else 0.11f)
                ) {
                    Text(
                        text = if (isHidden) "••%" else "${if (isProfit) "+" else ""}${String.format(Locale.US, "%.1f", summary.profitLossPercent)}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontFeatureSettings = "tnum"
                        ),
                        fontWeight = FontWeight.Bold,
                        color = pnlColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(9.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isHidden) "Kâr/Zarar: ••••" else "Kâr/Zarar: ${if (isProfit) "+" else ""}${currencyFormat.format(summary.profitLoss)}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontFeatureSettings = "tnum"
                ),
                color = pnlColor
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (editable) {
                    Surface(
                        onClick = onEditPrice,
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.testTag("custom_price_edit_btn_${summary.category}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Fiyat Güncelle", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Fiyat", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Surface(
                    onClick = onQuickAdd,
                    shape = RoundedCornerShape(8.dp),
                    color = accent.copy(alpha = if (isDark) 0.16f else 0.11f),
                    modifier = Modifier.testTag("quick_add_btn_${summary.category}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "${summary.category} Ekle", modifier = Modifier.size(13.dp), tint = accent)
                        Text("Alım", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun BesVaultRow(
    besPortfolio: BesPortfolio,
    besTotalValue: Double,
    besPaid: Double,
    currencyFormat: NumberFormat,
    isHidden: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val teal = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488)
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)

    val profit = besTotalValue - besPaid
    val profitPct = if (besPaid > 0) (profit / besPaid) * 100.0 else 0.0
    val isProfit = profit >= 0.0
    val pnlColor = if (isProfit) mint else coral

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(teal.copy(alpha = if (isDark) 0.16f else 0.11f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Shield, contentDescription = "BES", tint = teal, modifier = Modifier.size(21.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = besPortfolio.holderName.ifBlank { "Bireysel Emeklilik (BES)" },
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Yatırılan: ${if (isHidden) "••••" else currencyFormat.format(besPaid)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isHidden) "••••" else currencyFormat.format(besTotalValue),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontFeatureSettings = "tnum"
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(7.dp),
                    color = pnlColor.copy(alpha = if (isDark) 0.16f else 0.11f)
                ) {
                    Text(
                        text = if (isHidden) "••%" else "${if (isProfit) "+" else ""}${String.format(Locale.US, "%.1f", profitPct)}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontFeatureSettings = "tnum"
                        ),
                        fontWeight = FontWeight.Bold,
                        color = pnlColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Devlet Katkısı Dahil · ${if (isHidden) "••••" else "${if (isProfit) "+" else ""}${currencyFormat.format(profit)}"}",
                style = MaterialTheme.typography.labelSmall,
                color = pnlColor
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.Edit, contentDescription = "BES Düzenle", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "BES Sil", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
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
    preferenceManager: PreferenceManager?,
    dateFormat: SimpleDateFormat,
    currencyFormat: NumberFormat,
    onDismiss: () -> Unit,
    onAddTransaction: (String) -> Unit,
    onDeleteTransaction: (Int) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val accent = getAssetAccentColor(category, isDark)
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)

    var pendingDeleteId by remember { mutableStateOf<Int?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(accent.copy(alpha = if (isDark) 0.16f else 0.11f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(getAssetIcon(category), contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${transactions.size} alım kaydı",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(
                    onClick = { onAddTransaction(category) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = if (isDark) Color(0xFF041310) else Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ekle", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            if (summary != null) {
                val isProfit = summary.profitLoss >= 0.0
                val pnlColor = if (isProfit) mint else coral
                val unit = if (category.lowercase().let { it.contains("bilezik") || it.contains("altın") || it.contains("altin") || it.contains("gümüş") }) "gr" else "adet"
                SettingsGroupCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PortfolioStatCell("Güncel Değer", currencyFormat.format(summary.currentValue), MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
                        Box(modifier = Modifier.height(26.dp).width(0.8.dp).background(borderColor))
                        PortfolioStatCell("Toplam Miktar", "${com.example.util.FormatUtil.getNumberFormat(2).format(summary.totalQuantity)} $unit", accent, Modifier.weight(1f))
                        Box(modifier = Modifier.height(26.dp).width(0.8.dp).background(borderColor))
                        PortfolioStatCell("Kâr / Zarar", "${if (isProfit) "+" else ""}${currencyFormat.format(summary.profitLoss)}", pnlColor, Modifier.weight(1f))
                    }
                }
            }

            if (transactions.isEmpty()) {
                SettingsGroupCard {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Bu varlığa ait kayıt bulunmuyor.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(transactions, key = { it.id }) { tx ->
                            val livePrice = resolveCurrentUnitPrice(tx.category, goldPrices, bankRates, preferenceManager, ziraatRates)
                            val rate = livePrice ?: tx.unitPrice ?: 0.0
                            val qty = tx.quantity ?: 0.0
                            val currentVal = qty * rate
                            val pnl = currentVal - tx.amount
                            val isProfit = pnl >= 0.0
                            val pnlColor = if (isProfit) mint else coral
                            val unit = if (tx.category.lowercase().let { it.contains("bilezik") || it.contains("altın") || it.contains("altin") || it.contains("gümüş") }) "gr" else "ad"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${com.example.util.FormatUtil.getNumberFormat(2).format(qty)} $unit @ ${com.example.util.FormatUtil.getNumberFormat(1).format(tx.unitPrice ?: 0.0)} ₺",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontFeatureSettings = "tnum"
                                        ),
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${dateFormat.format(Date(tx.timestamp))} · Maliyet ${currencyFormat.format(tx.amount)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = currencyFormat.format(currentVal),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontFeatureSettings = "tnum"
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${if (isProfit) "+" else ""}${currencyFormat.format(pnl)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontFeatureSettings = "tnum"
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = pnlColor
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { pendingDeleteId = tx.id },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("delete_saving_tx_btn_${tx.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Kayıt Sil",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            if (tx.id != transactions.lastOrNull()?.id) {
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

    if (pendingDeleteId != null) {
        val idToDelete = pendingDeleteId!!
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Birikim Kaydı Silinsin Mi?", fontWeight = FontWeight.Bold) },
            text = { Text("Bu birikim alım kaydı portföyden kalıcı olarak kaldırılacak.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(idToDelete)
                        pendingDeleteId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sil") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) { Text("İptal") }
            }
        )
    }
}

fun isEditableCategory(category: String, preferenceManager: PreferenceManager? = null): Boolean {
    val clean = category.trim().lowercase()
    val isDefault = clean.contains("bireysel") || clean.contains("emeklilik") || clean == "set" || clean.startsWith("set") || clean.endsWith("set")
    if (isDefault) return true
    if (preferenceManager != null) {
        val match = preferenceManager.getMarketMatch(category)
        if (match == "manual") return true
    }
    return false
}

/**
 * Resolves current market price for well-known categories from Sivas Kuyumder or BankRates.
 */
fun resolveCurrentUnitPrice(
    category: String,
    goldPrices: List<GoldPrice>,
    bankRates: List<BankRate>,
    preferenceManager: PreferenceManager? = null,
    ziraatRates: List<BankRate> = emptyList(),
    besPortfolio: BesPortfolio? = null,
    onUpdateBes: ((BesPortfolio) -> Unit)? = null
): Double? {
    val cleanCatLower = category.trim().lowercase(Locale("tr", "TR"))
    val cleanCatRoot = category.trim().lowercase(Locale.ROOT)

    fun parseVal(s: String?): Double? {
        if (s == null || s.isBlank() || s == "-") return null
        return com.example.util.parseFormattedAmount(s)
    }

    val marketMatch = preferenceManager?.getMarketMatch(category)
    if (marketMatch == "manual") {
        val customPrice = preferenceManager.getCustomPrice(category)
        if (customPrice != null) return customPrice
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
            if (customPrice != null) return customPrice
        }
    }

    fun matches(vararg keywords: String): Boolean {
        return keywords.any { kw -> cleanCatLower.contains(kw) || cleanCatRoot.contains(kw) }
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
