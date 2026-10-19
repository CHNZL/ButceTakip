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
import kotlin.math.ceil

@Composable
fun SavingsCalculatorScreen() {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val appThemeColors = LocalAppThemeColors.current

    var targetAmountText by remember { mutableStateOf("1000000") }
    var downPaymentText by remember { mutableStateOf("200000") }
    var monthlyPaymentText by remember { mutableStateOf("25000") }
    var organizationFeeRateText by remember { mutableStateOf("7") }
    val bddkMinRatio = 0.40

    val targetAmount = com.example.util.parseFormattedAmount(targetAmountText) ?: 0.0
    val downPayment = com.example.util.parseFormattedAmount(downPaymentText) ?: 0.0
    val monthlyPayment = com.example.util.parseFormattedAmount(monthlyPaymentText) ?: 0.0
    val organizationFeeRate = (organizationFeeRateText.toDoubleOrNull() ?: 0.0) / 100.0

    val remainingAmount = (targetAmount - downPayment).coerceAtLeast(0.0)
    val totalInstallments = if (monthlyPayment > 0) ceil(remainingAmount / monthlyPayment).toInt() else 0
    val deliveryMonth = ceil((totalInstallments * bddkMinRatio) - ((downPayment / targetAmount.coerceAtLeast(1.0)) * totalInstallments)).toInt().coerceAtLeast(1)
    val orgFee = targetAmount * organizationFeeRate
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
                    "Tasarruf Finansmanı Hesaplayıcı",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "BDDK UYUMLU FAİZSİZ EV / ARAÇ FİNANSMAN SİMÜLASYONU",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
        }

        // --- 2. INPUT CARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Finansman Parametreleri", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = targetAmountText,
                        onValueChange = { targetAmountText = com.example.util.formatInputAmount(it) },
                        label = { Text("Hedeflenen Finansman Tutarı (TL)") },
                        leadingIcon = { Icon(Icons.Rounded.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = downPaymentText,
                        onValueChange = { downPaymentText = com.example.util.formatInputAmount(it) },
                        label = { Text("Peşinat Tutarı (TL)") },
                        leadingIcon = { Icon(Icons.Rounded.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = monthlyPaymentText,
                        onValueChange = { monthlyPaymentText = com.example.util.formatInputAmount(it) },
                        label = { Text("Aylık Ödeme Gücü (TL)") },
                        leadingIcon = { Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = organizationFeeRateText,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) organizationFeeRateText = it },
                        label = { Text("Organizasyon Ücreti (%)") },
                        leadingIcon = { Icon(Icons.Rounded.Percent, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
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
                            "HESAPLAMA SONUÇLARI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 1.sp
                        )
                        Surface(
                            color = Color(0xFFF59E0B),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Teslimat: $deliveryMonth. Ay ⭐",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Kalan Finansman", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                            Text(format.format(remainingAmount), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Toplam Vade", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                            Text("$totalInstallments Ay", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Organizasyon Ücreti", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                            Text(format.format(orgFee), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFEF08A))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Aylık Taksit", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                            Text(format.format(monthlyPayment), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // --- 4. SCHEDULE TITLE ---
        if (totalInstallments > 0) {
            item {
                Text(
                    text = "Aylık Ödeme Planı ($totalInstallments Ay)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // --- 5. SCHEDULE ITEMS ---
        items(totalInstallments) { i ->
            val month = i + 1
            val installment = if (month == totalInstallments) (remainingAmount - (monthlyPayment * (totalInstallments - 1))).coerceAtLeast(0.0) else monthlyPayment
            val isDelivery = month == deliveryMonth

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isDelivery -> if (isDark) Color(0xFF78350F).copy(alpha = 0.5f) else Color(0xFFFEF9C3)
                        month < deliveryMonth -> if (isDark) MaterialTheme.colorScheme.surface else Color.White
                        else -> if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else Color(0xFFF8FAFC)
                    }
                ),
                border = BorderStroke(
                    1.dp,
                    if (isDelivery) Color(0xFFF59E0B) else if (isDark) Color(0xFF334155).copy(alpha = 0.4f) else Color(0xFFE2E8F0)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isDelivery) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$month",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDelivery) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Text(
                                "$month. Ay Ödemesi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (isDelivery) "TESLİMAT AYI ⭐" else if (month < deliveryMonth) "Tasarruf Dönemi" else "Geri Ödeme Dönemi",
                                fontSize = 10.sp,
                                fontWeight = if (isDelivery) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDelivery) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        format.format(installment),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = if (isDelivery) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
