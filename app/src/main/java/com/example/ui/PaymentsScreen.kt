package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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

@Composable
fun PaymentsScreen(transactions: List<Transaction>, onTogglePaid: (Transaction, Boolean) -> Unit = { _,_ -> }) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

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

    val format = remember { com.example.util.FormatUtil.getCurrencyFormat() }
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale("tr")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ödemeler Özeti", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = currentMonthName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Summary Cards
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Bu Ay Bekleyen
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155).copy(alpha = 0.6f) else Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Bu Ay Bekleyen", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(format.format(totalDebt), fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            // Bu Ay Ödenen
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF065F46).copy(alpha = 0.5f) else Color(0xFF86EFAC).copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Bu Ay Ödenen", color = if (isDark) Color(0xFF34D399) else Color(0xFF16A34A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(format.format(totalPaid), fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isDark) Color(0xFF34D399) else Color(0xFF15803D))
                }
            }
        }
        
        if (pastDueRemaining > 0) {
            val pastDueBg = if (isDark) Color(0xFF450A0A).copy(alpha = 0.6f) else Color(0xFFFFF1F2)
            val pastDueBorder = if (isDark) Color(0xFF991B1B).copy(alpha = 0.5f) else Color(0xFFFECDD3)
            val pastDueText = if (isDark) Color(0xFFFCA5A5) else Color(0xFF9F1239)
            val pastDueAmountColor = if (isDark) Color(0xFFF87171) else Color(0xFFE11D48)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = pastDueBg),
                border = BorderStroke(1.dp, pastDueBorder),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Geciken Ödemeler (Geçmiş Aylardan)", color = pastDueText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(format.format(pastDueRemaining), fontWeight = FontWeight.Black, fontSize = 16.sp, color = pastDueAmountColor)
                    }
                    Icon(
                        Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = pastDueAmountColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Smart Suggestion
        val totalCombined = totalDebt + totalPaid
        val pct = if (totalCombined > 0) ((totalPaid / totalCombined) * 100).toInt() else 0
        val tipBg = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFBEB)
        val tipBorder = if (isDark) Color(0xFF78350F).copy(alpha = 0.5f) else Color(0xFFFDE68A)
        val tipText = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E)

        Card(
            colors = CardDefaults.cardColors(containerColor = tipBg),
            border = BorderStroke(1.dp, tipBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Info, contentDescription = null, tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Akıllı Öneri: Bu ayki ödemelerinizin %$pct'ini tamamladınız.", color = tipText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Lists
        LazyColumn(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            if (pastDueTx.isNotEmpty()) {
                item {
                    Text("Geçmiş Aylardan Kalanlar", fontWeight = FontWeight.Black, color = Color(0xFFE11D48), fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
                }
                items(pastDueTx, key = { it.id }) { tx ->
                    PaymentItem(tx, isPaid = false, sdf = sdf, isPastDue = true, isDark = isDark, onToggle = { onTogglePaid(tx, !tx.isPaid) })
                }
            }
            
            if (thisMonthUnpaidTx.isNotEmpty()) {
                item {
                    Text("Bu Ay Ödenecekler", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
                }
                items(thisMonthUnpaidTx, key = { it.id }) { tx ->
                    val isPastDueDay = tx.timestamp < today // overdue within this month
                    PaymentItem(tx, isPaid = false, sdf = sdf, isPastDue = isPastDueDay, isDark = isDark, onToggle = { onTogglePaid(tx, !tx.isPaid) })
                }
            }

            if (thisMonthPaidTx.isNotEmpty()) {
                item {
                    Text("Bu Ay Ödenenler", fontWeight = FontWeight.Black, color = if (isDark) Color(0xFF34D399) else Color(0xFF10B981), fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
                }
                items(thisMonthPaidTx, key = { it.id }) { tx ->
                    PaymentItem(tx, isPaid = true, sdf = sdf, isDark = isDark, onToggle = { onTogglePaid(tx, !tx.isPaid) })
                }
            }

            if (futureUnpaidTx.isNotEmpty()) {
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

                item {
                    Text("Gelecek Aylar", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp, bottom = 2.dp))
                }

                groupedFuture.forEach { (monthMs, txs) ->
                    val monthName = groupSdf.format(Date(monthMs))
                    val monthTotal = txs.sumOf { it.amount }
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(monthName.uppercase(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                            Text(format.format(monthTotal), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                        }
                    }
                    items(txs, key = { it.id }) { tx ->
                        PaymentItem(tx, isPaid = false, sdf = sdf, isPastDue = false, isDark = isDark, onToggle = { onTogglePaid(tx, !tx.isPaid) })
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

@Composable
fun PaymentItem(
    tx: Transaction, 
    isPaid: Boolean, 
    sdf: SimpleDateFormat, 
    isPastDue: Boolean = false, 
    isDark: Boolean = false,
    onToggle: () -> Unit = {}
) {
    val alpha = if (isPaid) 0.6f else 1f
    val color = when {
        isPastDue -> if (isDark) Color(0xFFF87171) else Color(0xFFE11D48)
        isPaid -> if (isDark) Color(0xFF34D399) else Color(0xFF10B981)
        else -> MaterialTheme.colorScheme.onSurface
    }
    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val cardBorder = if (isDark) Color(0xFF334155).copy(alpha = 0.4f) else Color(0xFFF1F5F9)

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPaid) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = if (isPaid) "Ödendi" else "Ödenmedi",
                tint = color.copy(alpha),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = sdf.format(Date(tx.timestamp)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = com.example.util.FormatUtil.getCurrencyFormat().format(tx.amount),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = color.copy(alpha)
            )
        }
    }
}

