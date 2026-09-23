package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
fun IncomesScreen(transactions: List<Transaction>) {
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

    val incomeTx = transactions.filter { it.type == TransactionType.INCOME }
    val thisMonthIncomes = incomeTx.filter { it.timestamp in startOfMonth until endOfMonth }.sortedByDescending { it.timestamp }
    val totalIncomeThisMonth = thisMonthIncomes.sumOf { it.amount }

    val format = remember { com.example.util.FormatUtil.getCurrencyFormat() }
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale("tr")) }

    val headerCardBg = if (isDark) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFFF0FDF4)
    val headerCardBorder = if (isDark) Color(0xFF059669).copy(alpha = 0.4f) else Color(0xFFBBF7D0)
    val headerTitleColor = if (isDark) Color(0xFF6EE7B7) else Color(0xFF166534)
    val headerAmountColor = if (isDark) Color(0xFF34D399) else Color(0xFF15803D)

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
            Text("Gelirlerim", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
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

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = headerCardBg),
            border = BorderStroke(1.dp, headerCardBorder),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Bu Ay Toplam Gelir", color = headerTitleColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(format.format(totalIncomeThisMonth), fontWeight = FontWeight.Black, fontSize = 20.sp, color = headerAmountColor)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            if (thisMonthIncomes.isNotEmpty()) {
                item {
                    Text("İşlemler (${thisMonthIncomes.size})", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
                }
                items(thisMonthIncomes, key = { it.id }) { tx ->
                    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else Color.White
                    val cardBorder = if (isDark) Color(0xFF334155).copy(alpha = 0.4f) else Color(0xFFF1F5F9)
                    val iconBg = if (isDark) Color(0xFF064E3B) else Color(0xFFDCFCE7)
                    val iconTint = if (isDark) Color(0xFF34D399) else Color(0xFF16A34A)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, cardBorder),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(iconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(sdf.format(Date(tx.timestamp)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("+${format.format(tx.amount)}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = iconTint)
                        }
                    }
                }
            } else {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Bu ay henüz gelir işlemi bulunmuyor.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

