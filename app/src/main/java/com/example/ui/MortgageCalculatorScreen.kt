package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

@Composable
fun MortgageCalculatorScreen() {
    var amountText by remember { mutableStateOf("") }
    var rateText by remember { mutableStateOf("") }
    var termMonthsText by remember { mutableStateOf("") }
    
    // Improved cleaning function
    fun cleanNumber(text: String): String {
        return text.replace(".", "").replace(",", ".")
    }

    // Re-parse with cleaned input
    val amount = cleanNumber(amountText).toDoubleOrNull() ?: 0.0
    val monthlyRate = (cleanNumber(rateText).toDoubleOrNull() ?: 0.0) / 100.0
    val inputMonths = termMonthsText.toIntOrNull() ?: 0
    
    // Calculate table terms
    val tableTerms = remember(inputMonths) {
        val terms = mutableListOf<Int>()
        if (inputMonths > 0) {
            terms.add(inputMonths)
            var nextTerm = inputMonths + (6 - (inputMonths % 6))
            while (nextTerm <= 120) {
                if (nextTerm >= inputMonths) terms.add(nextTerm)
                nextTerm += 6
            }
        } else {
            var term = 12
            while (term <= 120) {
                terms.add(term)
                term += 6
            }
        }
        terms.distinct().sorted().filter { it <= 120 }
    }
    
    val numberFormat = NumberFormat.getNumberInstance(Locale("tr", "TR")).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }
    
    val format = NumberFormat.getCurrencyInstance(Locale("tr", "TR")).apply { maximumFractionDigits = 0 }
    
    // Helper to format input for display
    fun formatInput(text: String): String {
        // Only allow digits and one comma
        var cleaned = text.replace(".", "").replace(",", "")
        
        // This is complex to do while typing, let's keep it simple for now as requested
        // "yazarken böllüklere ayır" - let's implement a simplified formatter
        val parts = text.split(",")
        val integerPart = parts[0].filter { it.isDigit() }.reversed().chunked(3).joinToString(".").reversed()
        val decimalPart = if (parts.size > 1) "," + parts[1].filter { it.isDigit() }.take(2) else ""
        return integerPart + decimalPart
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Konut Kredisi Hesaplayıcı", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(value = amountText, onValueChange = { amountText = formatInput(it) }, label = { Text("Kredi Tutarı (TL)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = rateText, onValueChange = { rateText = it.filter { c -> c.isDigit() || c == ',' } }, label = { Text("Aylık Faiz Oranı (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = termMonthsText, onValueChange = { termMonthsText = it.filter { c -> c.isDigit() } }, label = { Text("Vade (Ay)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Ödeme Tablosu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        items(tableTerms) { months ->
            val monthlyPayment = if (monthlyRate > 0 && months > 0) {
                (amount * monthlyRate * (1 + monthlyRate).pow(months)) / ((1 + monthlyRate).pow(months) - 1)
            } else if (months > 0) {
                amount / months
            } else 0.0
            
            val totalPayment = monthlyPayment * months
            
            Card(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("$months Ay Vade", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Aylık Taksit: ${format.format(monthlyPayment.coerceAtLeast(0.0))}")
                    Text("Toplam Geri Ödeme: ${format.format(totalPayment.coerceAtLeast(0.0))}")
                }
            }
        }
    }
}
