package com.example.ui

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
import java.util.Date
import java.util.Locale

import java.util.Calendar

@Composable
fun PaymentsScreen(transactions: List<Transaction>, onTogglePaid: (Transaction, Boolean) -> Unit = { _,_ -> }) {
    val currentMonthSdf = SimpleDateFormat("MMMM yyyy", Locale("tr"))
    val currentMonthName = currentMonthSdf.format(Date()).uppercase()

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

    val format = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("tr"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Ödemeler Özeti", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)

        // Summary Cards
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Toplam Borç
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Bu Ay Bekleyen", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(format.format(totalDebt), fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            // Gerçekleşen
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Bu Ay Ödenen", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(format.format(totalPaid), fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF10B981))
                }
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (pastDueRemaining > 0) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDD5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Geciken Ödemeler (Geçmiş Aylardan)", color = Color(0xFF9A3412), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(format.format(pastDueRemaining), fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFFC2410C))
                    }
                }
            }
        }

        // Smart Suggestion
        val totalCombined = totalDebt + totalPaid
        val pct = if (totalCombined > 0) ((totalPaid / totalCombined) * 100).toInt() else 0
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Info, contentDescription = null, tint = Color(0xFFD97706))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Akıllı Öneri: Bu ayki ödemelerinizin %$pct'ini tamamladınız.", color = Color(0xFF92400E), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Lists
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (pastDueTx.isNotEmpty()) {
                item {
                    Text("Geçmiş Aylardan Kalanlar", fontWeight = FontWeight.Black, color = Color(0xFFE11D48), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(pastDueTx) { tx ->
                    PaymentItem(tx, isPaid = false, sdf, isPastDue = true, onToggle = { onTogglePaid(tx, !tx.isPaid) })
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
            
            if (thisMonthUnpaidTx.isNotEmpty()) {
                item {
                    Text("Bu Ay Ödenecekler", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(thisMonthUnpaidTx) { tx ->
                    val isPastDueDay = tx.timestamp < today // overdue within this month
                    PaymentItem(tx, isPaid = false, sdf, isPastDue = isPastDueDay, onToggle = { onTogglePaid(tx, !tx.isPaid) })
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
            }

            if (thisMonthPaidTx.isNotEmpty()) {
                item {
                    Text("Bu Ay Ödenenler", fontWeight = FontWeight.Black, color = Color(0xFF10B981), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(thisMonthPaidTx) { tx ->
                    PaymentItem(tx, isPaid = true, sdf, onToggle = { onTogglePaid(tx, !tx.isPaid) })
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
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
                    Text("Gelecek Aylar", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                groupedFuture.forEach { (monthMs, txs) ->
                    val monthName = groupSdf.format(Date(monthMs))
                    val monthTotal = txs.sumOf { it.amount }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(monthName.uppercase(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            Text(format.format(monthTotal), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        }
                    }
                    items(txs) { tx ->
                        PaymentItem(tx, isPaid = false, sdf, isPastDue = false, onToggle = { onTogglePaid(tx, !tx.isPaid) })
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

@Composable
fun PaymentItem(tx: Transaction, isPaid: Boolean, sdf: SimpleDateFormat, isPastDue: Boolean = false, onToggle: () -> Unit = {}) {
    val alpha = if (isPaid) 0.6f else 1f
    val color = if (isPastDue) Color(0xFFE11D48) else if (isPaid) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().clickable { onToggle() }.clip(RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (isPaid) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = color.copy(alpha),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tx.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha), maxLines = 1)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(sdf.format(Date(tx.timestamp)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha))
                Text(NumberFormat.getCurrencyInstance(Locale("tr","TR")).format(tx.amount), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color.copy(alpha))
            }
        }
    }
}
