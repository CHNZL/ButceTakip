package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BankRate
import com.example.data.GoldPrice
import com.example.data.Transaction
import com.example.data.TransactionType
import com.example.ui.theme.LocalAppThemeColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ModernHeroWalletCard(
    balance: Double,
    income: Double,
    expense: Double,
    saving: Double,
    isDark: Boolean,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onAddSaving: () -> Unit,
    onOpenTheme: () -> Unit
) {
    val appThemeColors = LocalAppThemeColors.current
    val balanceFormat = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }
    var isBalanceHidden by remember { mutableStateOf(false) }

    // Proportions for cashflow bar
    val totalInflow = if (income > 0) income else (expense + saving).coerceAtLeast(1.0)
    val expenseRatio = (expense / totalInflow).coerceIn(0.0, 1.0).toFloat()
    val savingRatio = (saving / totalInflow).coerceIn(0.0, 1.0 - expenseRatio.toDouble()).toFloat()
    val remainingRatio = (1.0f - expenseRatio - savingRatio).coerceAtLeast(0f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors = appThemeColors.heroGradient))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top row: Chip & Privacy toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4ADE80))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GÜNCEL NET KASA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = Color.White.copy(alpha = 0.95f)
                            )
                        }
                    }

                    IconButton(
                        onClick = { isBalanceHidden = !isBalanceHidden },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Bakiye Gizle/Göster",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Balance Big Text
                Text(
                    text = if (isBalanceHidden) "•••••••• ₺" else balanceFormat.format(balance),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Cashflow Ratio Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        if (expenseRatio > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(expenseRatio)
                                    .fillMaxHeight()
                                    .background(Color(0xFFF87171))
                            )
                        }
                        if (savingRatio > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(savingRatio)
                                    .fillMaxHeight()
                                    .background(Color(0xFF38BDF8))
                            )
                        }
                        if (remainingRatio > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(remainingRatio)
                                    .fillMaxHeight()
                                    .background(Color(0xFF4ADE80))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val expensePct = (expenseRatio * 100).toInt()
                        val savingPct = (savingRatio * 100).toInt()
                        Text(
                            text = "Gider: %$expensePct • Birikim: %$savingPct",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = if (balance >= 0) "Kasa Artıda ✓" else "Dikkat: Bütçe Ekside!",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (balance >= 0) Color(0xFF4ADE80) else Color(0xFFFCA5A5)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3 Micro-Cards for Income, Expense, Saving
                val subCardBg = Color.White.copy(alpha = if (isDark) 0.12f else 0.16f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Gelir
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = subCardBg,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFF4ADE80),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    "GELİR",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBalanceHidden) "••••" else balanceFormat.format(income),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4ADE80),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Gider
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = subCardBg,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.TrendingDown,
                                    contentDescription = null,
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    "GİDER",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBalanceHidden) "••••" else balanceFormat.format(expense),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF87171),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Birikim
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = subCardBg,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Savings,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    "BİRİKİM",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBalanceHidden) "••••" else balanceFormat.format(saving),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    // Quick Actions Dock
    QuickActionsRow(
        onAddExpense = onAddExpense,
        onAddIncome = onAddIncome,
        onAddSaving = onAddSaving,
        onOpenTheme = onOpenTheme,
        isDark = isDark
    )
}

@Composable
fun QuickActionsRow(
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onAddSaving: () -> Unit,
    onOpenTheme: () -> Unit,
    isDark: Boolean
) {
    val appThemeColors = LocalAppThemeColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(
            title = "Ödeme Ekle",
            icon = Icons.Rounded.CreditCard,
            iconTint = Color(0xFFEF4444),
            bgColor = if (isDark) Color(0xFF450A0A) else Color(0xFFFEE2E2),
            modifier = Modifier.weight(1f),
            onClick = onAddExpense
        )
        QuickActionButton(
            title = "Gelir Ekle",
            icon = Icons.Rounded.TrendingUp,
            iconTint = Color(0xFF10B981),
            bgColor = if (isDark) Color(0xFF064E3B) else Color(0xFFD1FAE5),
            modifier = Modifier.weight(1f),
            onClick = onAddIncome
        )
        QuickActionButton(
            title = "Birikim",
            icon = Icons.Rounded.Savings,
            iconTint = Color(0xFF0284C7),
            bgColor = if (isDark) Color(0xFF082F49) else Color(0xFFE0F2FE),
            modifier = Modifier.weight(1f),
            onClick = onAddSaving
        )
        QuickActionButton(
            title = "Görünüm",
            icon = Icons.Default.Palette,
            iconTint = appThemeColors.accentColor,
            bgColor = if (isDark) appThemeColors.cardBorder else appThemeColors.surfaceHighlight,
            modifier = Modifier.weight(1f),
            onClick = onOpenTheme
        )
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ModernMarketRatesHub(
    goldPrices: List<GoldPrice>,
    isFetchingGold: Boolean,
    lastGoldUpdate: String?,
    onRefreshGold: () -> Unit,
    bankRates: List<BankRate>,
    isFetchingBank: Boolean,
    lastBankUpdate: String?,
    onRefreshBank: () -> Unit,
    ziraatRates: List<BankRate>,
    isFetchingZiraat: Boolean,
    lastZiraatUpdate: String?,
    onRefreshZiraat: () -> Unit,
    isDark: Boolean
) {
    var selectedSource by remember { mutableIntStateOf(0) } // 0: Sivas Altın, 1: Yapı Kredi, 2: Ziraat

    val currentFetching = when (selectedSource) {
        0 -> isFetchingGold
        1 -> isFetchingBank
        else -> isFetchingZiraat
    }
    val currentLastUpdate = when (selectedSource) {
        0 -> lastGoldUpdate
        1 -> lastBankUpdate
        else -> lastZiraatUpdate
    }
    val onRefreshCurrent = {
        when (selectedSource) {
            0 -> onRefreshGold()
            1 -> onRefreshBank()
            else -> onRefreshZiraat()
        }
    }

    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val cardBorder = if (isDark) Color(0xFF334155).copy(alpha = 0.45f) else Color(0xFFE2E8F0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header with title and refresh button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.ShowChart,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Canlı Piyasa & Kurlar",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (currentLastUpdate != null) {
                            Text(
                                text = "Güncelleme: $currentLastUpdate",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onRefreshCurrent,
                    modifier = Modifier.size(28.dp)
                ) {
                    if (currentFetching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Segmented source selector tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MarketTabPill(
                    title = "Altın (Sivas)",
                    isSelected = selectedSource == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedSource = 0 }
                )
                MarketTabPill(
                    title = "Yapı Kredi",
                    isSelected = selectedSource == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedSource = 1 }
                )
                MarketTabPill(
                    title = "Ziraat Bank",
                    isSelected = selectedSource == 2,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedSource = 2 }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Content according to selected source
            when (selectedSource) {
                0 -> {
                    if (goldPrices.isEmpty() && !isFetchingGold) {
                        Text(
                            "Veriler yüklenemedi. Lütfen yenileyin.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            goldPrices.chunked(2).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    row.forEach { gp ->
                                        ModernRateItemCard(
                                            title = gp.name,
                                            subtitle = "ALTIN",
                                            buyPrice = gp.buy,
                                            sellPrice = gp.sell,
                                            isDark = isDark,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (row.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    if (bankRates.isEmpty() && !isFetchingBank) {
                        Text(
                            "Veriler yüklenemedi. Lütfen yenileyin.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            bankRates.chunked(2).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    row.forEach { rate ->
                                        ModernRateItemCard(
                                            title = rate.code,
                                            subtitle = rate.name,
                                            buyPrice = rate.buy,
                                            sellPrice = rate.sell,
                                            isDark = isDark,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (row.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    if (ziraatRates.isEmpty() && !isFetchingZiraat) {
                        Text(
                            "Veriler yüklenemedi. Lütfen yenileyin.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ziraatRates.chunked(2).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    row.forEach { rate ->
                                        ModernRateItemCard(
                                            title = rate.code,
                                            subtitle = rate.name,
                                            buyPrice = rate.buy,
                                            sellPrice = rate.sell,
                                            isDark = isDark,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (row.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarketTabPill(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ModernRateItemCard(
    title: String,
    subtitle: String,
    buyPrice: String,
    sellPrice: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.5f) else Color(0xFFF8FAFC)
    val border = if (isDark) Color(0xFF334155).copy(alpha = 0.35f) else Color(0xFFE2E8F0)

    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, border)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle.take(10),
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("ALIŞ", fontSize = 7.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Text(
                        text = buyPrice,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFEF4444)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("SATIŞ", fontSize = 7.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Text(
                        text = sellPrice,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernTransactionItem(
    transaction: Transaction,
    onDelete: () -> Unit,
    onTogglePaid: ((Boolean) -> Unit)? = null,
    isDark: Boolean = false
) {
    val format = remember { com.example.util.FormatUtil.getCurrencyFormat() }
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale("tr", "TR")) }

    val amountColor = when (transaction.type) {
        TransactionType.INCOME -> if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A)
        TransactionType.EXPENSE -> if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
        TransactionType.SAVING -> if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    }

    val prefix = when (transaction.type) {
        TransactionType.INCOME -> "+"
        TransactionType.EXPENSE -> "-"
        TransactionType.SAVING -> "💎 "
    }

    // Dynamic icon based on category keywords
    val categoryLower = transaction.category.lowercase(Locale.ROOT)
    val (categoryIcon, categoryIconColor, categoryBg) = when {
        categoryLower.contains("kira") || categoryLower.contains("ev") || categoryLower.contains("aidat") ->
            Triple(Icons.Rounded.Home, Color(0xFF3B82F6), if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE))
        categoryLower.contains("market") || categoryLower.contains("gıda") || categoryLower.contains("yemek") || categoryLower.contains("restoran") ->
            Triple(Icons.Rounded.ShoppingCart, Color(0xFFF59E0B), if (isDark) Color(0xFF78350F) else Color(0xFFFEF3C7))
        categoryLower.contains("fatura") || categoryLower.contains("elektrik") || categoryLower.contains("su") || categoryLower.contains("gaz") ->
            Triple(Icons.Rounded.Receipt, Color(0xFFEC4899), if (isDark) Color(0xFF831843) else Color(0xFFFCE7F3))
        categoryLower.contains("kredi") || categoryLower.contains("kart") || categoryLower.contains("banka") || categoryLower.contains("taksit") ->
            Triple(Icons.Rounded.CreditCard, Color(0xFF8B5CF6), if (isDark) Color(0xFF4C1D95) else Color(0xFFEDE9FE))
        categoryLower.contains("araç") || categoryLower.contains("araba") || categoryLower.contains("benzin") || categoryLower.contains("yakıt") || categoryLower.contains("ulaşım") ->
            Triple(Icons.Rounded.DirectionsCar, Color(0xFF14B8A6), if (isDark) Color(0xFF134E4A) else Color(0xFFCCFBF1))
        categoryLower.contains("maaş") || categoryLower.contains("gelir") || categoryLower.contains("prim") || categoryLower.contains("ek gelir") ->
            Triple(Icons.Rounded.Payments, Color(0xFF10B981), if (isDark) Color(0xFF064E3B) else Color(0xFFD1FAE5))
        categoryLower.contains("altın") || categoryLower.contains("döviz") || categoryLower.contains("birikim") || categoryLower.contains("bes") ->
            Triple(Icons.Rounded.Savings, Color(0xFFD97706), if (isDark) Color(0xFF78350F) else Color(0xFFFEF3C7))
        categoryLower.contains("sağlık") || categoryLower.contains("eczane") || categoryLower.contains("hastane") ->
            Triple(Icons.Rounded.MedicalServices, Color(0xFFEF4444), if (isDark) Color(0xFF7F1D1D) else Color(0xFFFEE2E2))
        categoryLower.contains("eğitim") || categoryLower.contains("okul") || categoryLower.contains("kurs") ->
            Triple(Icons.Rounded.School, Color(0xFF6366F1), if (isDark) Color(0xFF312E81) else Color(0xFFE0E7FF))
        else ->
            Triple(Icons.Rounded.AccountBalanceWallet, Color(0xFF64748B), if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))
    }

    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val cardBorder = if (isDark) Color(0xFF334155).copy(alpha = 0.45f) else Color(0xFFF1F5F9)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(categoryBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = transaction.category,
                    tint = categoryIconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (transaction.person != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = transaction.person,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = sdf.format(Date(transaction.timestamp)),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Text(
                        text = transaction.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Paid / Unpaid tag for expenses
                    if (transaction.type == TransactionType.EXPENSE) {
                        Surface(
                            color = if (transaction.isPaid) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (transaction.isPaid) "ÖDENDİ" else "BEKLİYOR",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (transaction.isPaid) Color(0xFF065F46) else Color(0xFF991B1B),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount and Action
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = prefix + format.format(transaction.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("delete_btn")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
