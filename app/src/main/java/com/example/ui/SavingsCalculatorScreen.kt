package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.ceil

@Composable
fun SavingsCalculatorScreen() {
    var targetAmountText by remember { mutableStateOf("") }
    var downPaymentText by remember { mutableStateOf("") }
    var monthlyPaymentText by remember { mutableStateOf("") }
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
    val format = NumberFormat.getCurrencyInstance(Locale("tr", "TR")).apply { maximumFractionDigits = 0 }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        item {
            Text("Tasarruf Finansmanı Hesaplayıcı", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(value = targetAmountText, onValueChange = { targetAmountText = com.example.util.formatInputAmount(it) }, label = { Text("Hedeflenen Finansman (TL)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = downPaymentText, onValueChange = { downPaymentText = com.example.util.formatInputAmount(it) }, label = { Text("Peşinat Tutarı (TL)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = monthlyPaymentText, onValueChange = { monthlyPaymentText = com.example.util.formatInputAmount(it) }, label = { Text("Aylık Ödeme Gücü (TL)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = organizationFeeRateText, onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) organizationFeeRateText = it }, label = { Text("Organizasyon Ücreti (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Sonuçlar", style = MaterialTheme.typography.titleMedium)
                    Text("Kalan Finansman: ${format.format(remainingAmount)}")
                    Text("Toplam Vade: $totalInstallments ay")
                    Text("Teslimat Ayı: $deliveryMonth. Ay", fontWeight = FontWeight.Bold, color = Color(0xFFE53E3E))
                    Text("Organizasyon Ücreti: ${format.format(orgFee)}")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Ödeme Tablosu", style = MaterialTheme.typography.titleMedium)
        }
        
        items(totalInstallments) { i ->
            val month = i + 1
            val installment = if (month == totalInstallments) (remainingAmount - (monthlyPayment * (totalInstallments - 1))).coerceAtLeast(0.0) else monthlyPayment
            
            Card(modifier = Modifier.padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = if (month == deliveryMonth) Color(0xFFFEF08A) else MaterialTheme.colorScheme.surfaceVariant)) {
                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val textColor = if (month == deliveryMonth) Color.Black else MaterialTheme.colorScheme.onSurface
                    Text("$month. Ay", color = textColor)
                    Text(format.format(installment), color = textColor)
                    Text(if (month == deliveryMonth) "TESLİMAT" else if (month < deliveryMonth) "Tasarruf" else "Geri Ödeme", color = textColor)
                }
            }
        }
    }
}
