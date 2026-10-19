package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppThemeColors
import kotlin.math.pow

@Composable
fun MortgageCalculatorScreen() {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val appThemeColors = LocalAppThemeColors.current

    var amountText by remember { mutableStateOf("1500000") }
    var rateText by remember { mutableStateOf("3.05") }
    var termMonthsText by remember { mutableStateOf("120") }

    val amount = com.example.util.parseFormattedAmount(amountText) ?: 0.0
    val monthlyRate = (rateText.replace(",", ".").toDoubleOrNull() ?: 0.0) / 100.0
    val inputMonths = termMonthsText.toIntOrNull() ?: 120

    val currentMonthlyPayment = if (monthlyRate > 0 && inputMonths > 0) {
        (amount * monthlyRate * (1 + monthlyRate).pow(inputMonths)) / ((1 + monthlyRate).pow(inputMonths) - 1)
    } else if (inputMonths > 0) {
        amount / inputMonths
    } else 0.0

    val currentTotalPayment = currentMonthlyPayment * inputMonths
    val currentTotalInterest = (currentTotalPayment - amount).coerceAtLeast(0.0)

    val tableTerms = remember(inputMonths) {
        val terms = mutableListOf<Int>()
        if (inputMonths > 0) {
            terms.add(inputMonths)
            var nextTerm = inputMonths + (12 - (inputMonths % 12))
            while (nextTerm <= 120) {
                if (nextTerm >= inputMonths) terms.add(nextTerm)
                nextTerm += 12
            }
        } else {
            var term = 12
            while (term <= 120) {
                terms.add(term)
                term += 12
            }
        }
        terms.distinct().sorted().filter { it <= 120 }
    }

    val format = remember { com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. HEADER ---
        item {
            Column {
                Text(
                    "Konut Kredisi Hesaplayıcı",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "BANKA KONUT VE İHTİYAÇ KREDİSİ ÖDEME PLANI",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
        }

        // --- 2. INPUT PARAMETERS CARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Kredi Bilgileri", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = com.example.util.formatInputAmount(it) },
                        label = { Text("Kredi Tutarı (TL)") },
                        leadingIcon = { Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                        label = { Text("Aylık Faiz Oranı (%)") },
                        leadingIcon = { Icon(Icons.Rounded.Percent, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = termMonthsText,
                        onValueChange = { termMonthsText = it.filter { c -> c.isDigit() } },
                        label = { Text("Vade Süresi (Ay)") },
                        leadingIcon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        }

        // --- 3. RESULT HERO CARD ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = appThemeColors.heroGradient
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "AYLIK TAKSİT TUTARI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 1.sp
                        )
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$inputMonths Ay Vade",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = format.format(currentMonthlyPayment.coerceAtLeast(0.0)),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Toplam Geri Ödeme", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                            Text(format.format(currentTotalPayment.coerceAtLeast(0.0)), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Toplam Faiz Tutarı", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                            Text(format.format(currentTotalInterest), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFEF08A))
                        }
                    }
                }
            }
        }

        // --- 4. COMPARISON SCHEDULE ---
        item {
            Text(
                text = "Farklı Vade Seçenekleri Karşılaştırması",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(tableTerms) { months ->
            val monthlyPay = if (monthlyRate > 0 && months > 0) {
                (amount * monthlyRate * (1 + monthlyRate).pow(months)) / ((1 + monthlyRate).pow(months) - 1)
            } else if (months > 0) {
                amount / months
            } else 0.0

            val totalPay = monthlyPay * months
            val isCurrentTerm = months == inputMonths

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentTerm) {
                        if (isDark) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    } else {
                        if (isDark) MaterialTheme.colorScheme.surface else Color.White
                    }
                ),
                border = BorderStroke(
                    1.dp,
                    if (isCurrentTerm) MaterialTheme.colorScheme.primary else if (isDark) Color(0xFF334155).copy(alpha = 0.4f) else Color(0xFFE2E8F0)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "$months Ay Vade",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isCurrentTerm) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        "Seçili",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            "Toplam: ${format.format(totalPay.coerceAtLeast(0.0))}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            format.format(monthlyPay.coerceAtLeast(0.0)),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "/ ay",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
