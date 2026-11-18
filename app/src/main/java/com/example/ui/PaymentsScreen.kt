package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PaymentsScreen(
    transactions: List<Transaction>,
    onTogglePaid: (Transaction, Boolean) -> Unit = { _, _ -> }
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val appThemeColors = LocalAppThemeColors.current

    val currentMonthSdf = remember { SimpleDateFormat("MMMM yyyy", Locale("tr")) }
    val currentMonthName = remember { currentMonthSdf.format(Date()).uppercase() }

    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    val startOfMonth = cal.timeInMillis

    cal.add(Calendar.MONTH, 1)
    val endOfMonth = cal.timeInMillis

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val expenseTx = transactions.filter { it.type == TransactionType.EXPENSE }

    val pastDueTx = expenseTx.filter { !it.isPaid && it.timestamp < startOfMonth }.sortedBy { it.timestamp }
    val thisMonthUnpaidTx = expenseTx.filter { !it.isPaid && it.timestamp in startOfMonth until endOfMonth }.sortedBy { it.timestamp }
    val futureUnpaidTx = expenseTx.filter { !it.isPaid && it.timestamp >= endOfMonth }.sortedBy { it.timestamp }
    val thisMonthPaidTx = expenseTx.filter { it.isPaid && it.timestamp in startOfMonth until endOfMonth }.sortedByDescending { it.timestamp }

    val totalDebt = thisMonthUnpaidTx.sumOf { it.amount }
    val totalPaid = thisMonthPaidTx.sumOf { it.amount }
    val pastDueRemaining = pastDueTx.sumOf { it.amount }
    val totalCombinedMonth = totalDebt + totalPaid

    val progressPct = if (totalCombinedMonth > 0) ((totalPaid / totalCombinedMonth) * 100).toInt() else if (totalDebt == 0.0 && totalPaid > 0.0) 100 else 0
    val progressFloat = if (totalCombinedMonth > 0) (totalPaid / totalCombinedMonth).toFloat().coerceIn(0f, 1f) else 0f

    val format = remember { com.example.util.FormatUtil.getCurrencyFormat() }
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale("tr")) }

    var selectedFilter by remember { mutableStateOf("Tümü") } // "Tümü", "Bekleyenler", "Ödenenler", "Gecikenler", "Gelecek"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. HERO PROGRESS CARD ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = appThemeColors.heroGradient
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.CreditCard,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "ÖDEME TAKİP MERKEZİ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.85f),
                                letterSpacing = 1.sp
                            )
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = currentMonthName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Kalan Ödeme Tutarı",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = format.format(totalDebt),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Surface(
                            color = if (progressPct >= 100) Color(0xFF10B981) else Color.White.copy(alpha = 0.25f),
                            shape = CircleShape
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    if (progressPct >= 100) Icons.Rounded.CheckCircle else Icons.Rounded.PieChart,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "%$progressPct Ödendi",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { progressFloat },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = Color(0xFF34D399),
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Bu Ay Ödenen", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                            Text(format.format(totalPaid), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6EE7B7))
                        }
                        if (pastDueRemaining > 0) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Gecikmiş Borç", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                                Text(format.format(pastDueRemaining), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFCA5A5))
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Durum", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                                Text(if (totalDebt == 0.0) "Tüm Borçlar Ödendi ✨" else "${thisMonthUnpaidTx.size} Bekleyen", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // --- 2. PAST DUE ALERT BANNER (IF ANY) ---
        if (pastDueRemaining > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF450A0A) else Color(0xFFFFF1F2)),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF991B1B) else Color(0xFFFECDD3))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF7F1D1D) else Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE11D48),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Geçmiş Aylardan Kalan Borçlar!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isDark) Color(0xFFFCA5A5) else Color(0xFF9F1239)
                            )
                            Text(
                                "${pastDueTx.size} adet gecikmiş ödemeniz bulunuyor.",
                                fontSize = 11.sp,
                                color = if (isDark) Color(0xFFFECDD3) else Color(0xFFBE123C)
                            )
                        }
                        Text(
                            format.format(pastDueRemaining),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = if (isDark) Color(0xFFF87171) else Color(0xFFE11D48)
                        )
                    }
                }
            }
        }

        // --- 3. FILTER PILLS ---
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "Tümü",
                    "Bekleyenler (${thisMonthUnpaidTx.size})",
                    "Ödenenler (${thisMonthPaidTx.size})",
                    if (pastDueTx.isNotEmpty()) "Gecikenler (${pastDueTx.size})" else null,
                    if (futureUnpaidTx.isNotEmpty()) "Gelecek Aylar (${futureUnpaidTx.size})" else null
                ).filterNotNull()

                items(filters) { f ->
                    val isSelected = selectedFilter.startsWith(f.substringBefore(" ("))
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = f.substringBefore(" (") },
                        label = { Text(f, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        // --- 4. LIST SECTIONS ---
        val showPastDue = selectedFilter == "Tümü" || selectedFilter == "Gecikenler"
        val showUnpaid = selectedFilter == "Tümü" || selectedFilter == "Bekleyenler"
        val showPaid = selectedFilter == "Tümü" || selectedFilter == "Ödenenler"
        val showFuture = selectedFilter == "Tümü" || selectedFilter == "Gelecek Aylar"

        if (showPastDue && pastDueTx.isNotEmpty()) {
            item {
                PaymentSectionHeader(
                    title = "Geciken Ödemeler (${pastDueTx.size})",
                    totalText = format.format(pastDueRemaining),
                    accentColor = Color(0xFFE11D48)
                )
            }
            items(pastDueTx, key = { it.id }) { tx ->
                ModernPaymentCard(
                    tx = tx,
                    isPaid = false,
                    isPastDue = true,
                    sdf = sdf,
                    isDark = isDark,
                    onToggle = { onTogglePaid(tx, !tx.isPaid) }
                )
            }
        }

        if (showUnpaid && thisMonthUnpaidTx.isNotEmpty()) {
            item {
                PaymentSectionHeader(
                    title = "Bu Ay Ödenecekler (${thisMonthUnpaidTx.size})",
                    totalText = format.format(totalDebt),
                    accentColor = MaterialTheme.colorScheme.onSurface
                )
            }
            items(thisMonthUnpaidTx, key = { it.id }) { tx ->
                val isPastDueDay = tx.timestamp < today
                ModernPaymentCard(
                    tx = tx,
                    isPaid = false,
                    isPastDue = isPastDueDay,
                    sdf = sdf,
                    isDark = isDark,
                    onToggle = { onTogglePaid(tx, !tx.isPaid) }
                )
            }
        }

        if (showPaid && thisMonthPaidTx.isNotEmpty()) {
            item {
                PaymentSectionHeader(
                    title = "Bu Ay Tamamlananlar (${thisMonthPaidTx.size})",
                    totalText = format.format(totalPaid),
                    accentColor = Color(0xFF10B981)
                )
            }
            items(thisMonthPaidTx, key = { it.id }) { tx ->
                ModernPaymentCard(
                    tx = tx,
                    isPaid = true,
                    isPastDue = false,
                    sdf = sdf,
                    isDark = isDark,
                    onToggle = { onTogglePaid(tx, !tx.isPaid) }
                )
            }
        }

        if (showFuture && futureUnpaidTx.isNotEmpty()) {
            val groupSdf = SimpleDateFormat("MMMM yyyy", Locale("tr", "TR"))
            val groupedFuture = futureUnpaidTx.groupBy {
                val c = Calendar.getInstance()
                c.timeInMillis = it.timestamp
                c.set(Calendar.DAY_OF_MONTH, 1)
                c.set(Calendar.HOUR_OF_DAY, 0)
                c.set(Calendar.MINUTE, 0)
                c.set(Calendar.SECOND, 0)
                c.set(Calendar.MILLISECOND, 0)
                c.timeInMillis
            }.toSortedMap()

            groupedFuture.forEach { (monthMs, txs) ->
                val monthName = groupSdf.format(Date(monthMs))
                val monthTotal = txs.sumOf { it.amount }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            monthName.uppercase(),
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                        Text(
                            format.format(monthTotal),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                    }
                }
                items(txs, key = { it.id }) { tx ->
                    ModernPaymentCard(
                        tx = tx,
                        isPaid = false,
                        isPastDue = false,
                        sdf = sdf,
                        isDark = isDark,
                        onToggle = { onTogglePaid(tx, !tx.isPaid) }
                    )
                }
            }
        }

        if (expenseTx.isEmpty()) {
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            "Harika! Ödeme bulunmuyor.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Bekleyen veya geçmiş ödemeniz bulunmuyor. Yeni bir gider eklemek için '+' butonuna basabilirsiniz.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentSectionHeader(
    title: String,
    totalText: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            color = accentColor.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = totalText,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                softWrap = false,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModernPaymentCard(
    tx: Transaction,
    isPaid: Boolean,
    isPastDue: Boolean,
    sdf: SimpleDateFormat,
    isDark: Boolean,
    onToggle: () -> Unit
) {
    val format = remember { com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 } }
    val paidAccent = if (isDark) com.example.ui.theme.CyberNeonMint else Color(0xFF059669)
    val overdueAccent = if (isDark) com.example.ui.theme.CyberNeonCoral else Color(0xFFE11D48)

    val cardBg = if (isDark) com.example.ui.theme.CyberDarkSurface else Color.White
    val cardBorder = if (isPastDue) {
        overdueAccent.copy(alpha = 0.55f)
    } else if (isPaid) {
        paidAccent.copy(alpha = 0.40f)
    } else {
        if (isDark) com.example.ui.theme.CyberBorderStroke else Color(0xFFE2E8F0)
    }

    val icon = when {
        tx.category.lowercase().contains("kira") -> Icons.Rounded.Home
        tx.category.lowercase().contains("market") -> Icons.Rounded.ShoppingCart
        tx.category.lowercase().contains("fatura") -> Icons.Rounded.ReceiptLong
        tx.category.lowercase().contains("kredi") -> Icons.Rounded.CreditCard
        tx.category.lowercase().contains("ulaşım") || tx.category.lowercase().contains("yakıt") -> Icons.Rounded.DirectionsCar
        tx.category.lowercase().contains("sağlık") -> Icons.Rounded.LocalHospital
        else -> Icons.Rounded.Payments
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isPaid -> paidAccent.copy(alpha = 0.14f)
                            isPastDue -> overdueAccent.copy(alpha = 0.14f)
                            else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = when {
                        isPaid -> paidAccent
                        isPastDue -> overdueAccent
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(23.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isPaid) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (isPastDue) {
                        Surface(
                            color = overdueAccent.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Gecikti",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = overdueAccent,
                                softWrap = false,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (tx.category.isNotBlank() && !tx.category.equals(tx.title, ignoreCase = true)) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = tx.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                softWrap = false,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
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
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            Icons.Rounded.CalendarToday,
                            contentDescription = null,
                            tint = if (isPastDue) overdueAccent.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = sdf.format(Date(tx.timestamp)),
                            fontSize = 10.sp,
                            fontWeight = if (isPastDue) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isPastDue) overdueAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            softWrap = false,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = format.format(tx.amount),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    softWrap = false,
                    maxLines = 1,
                    color = when {
                        isPaid -> paidAccent
                        isPastDue -> overdueAccent
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = if (isPaid) paidAccent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onToggle() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            if (isPaid) Icons.Rounded.Check else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = if (isPaid) "Ödendi" else "Bekliyor",
                            tint = if (isPaid) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isPaid) "Ödendi" else "Bekliyor",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            softWrap = false,
                            maxLines = 1,
                            color = if (isPaid) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
