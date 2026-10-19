package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BesPortfolio
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BesDialog(
    besPortfolio: BesPortfolio?,
    onDismiss: () -> Unit,
    onSave: (BesPortfolio) -> Unit
) {
    var holderNameText by remember { mutableStateOf(besPortfolio?.holderName ?: "BES") }
    var startDate by remember { mutableLongStateOf(besPortfolio?.startDate ?: System.currentTimeMillis()) }
    var investmentText by remember { mutableStateOf(besPortfolio?.investment?.let { com.example.util.formatDoubleForInput(it) } ?: "") }
    var investmentReturnText by remember { mutableStateOf(besPortfolio?.investmentReturn?.let { com.example.util.formatDoubleForInput(it) } ?: "") }
    var stateContributionText by remember { mutableStateOf(besPortfolio?.stateContribution?.let { com.example.util.formatDoubleForInput(it) } ?: "") }
    var stateContributionReturnText by remember { mutableStateOf(besPortfolio?.stateContributionReturn?.let { com.example.util.formatDoubleForInput(it) } ?: "") }
    var isRetired by remember { mutableStateOf(besPortfolio?.isRetired ?: false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale("tr")) }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDate = it }
                    showDatePicker = false
                }) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("İptal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Bireysel Emeklilik (BES)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = holderNameText,
                    onValueChange = { holderNameText = it },
                    label = { Text("Hesap / Kişi Adı") },
                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dateFormat.format(Date(startDate)),
                    onValueChange = {},
                    label = { Text("BES Başlangıç Tarihi") },
                    leadingIcon = { Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    enabled = false,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                OutlinedTextField(
                    value = investmentText,
                    onValueChange = { investmentText = com.example.util.formatInputAmount(it) },
                    label = { Text("Yatırım Tutarı (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = investmentReturnText,
                    onValueChange = { investmentReturnText = com.example.util.formatInputAmount(it) },
                    label = { Text("Yatırım Getirisi (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stateContributionText,
                    onValueChange = { stateContributionText = com.example.util.formatInputAmount(it) },
                    label = { Text("Devlet Katkısı (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stateContributionReturnText,
                    onValueChange = { stateContributionReturnText = com.example.util.formatInputAmount(it) },
                    label = { Text("Devlet Katkısı Getirisi (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isRetired,
                        onCheckedChange = { isRetired = it }
                    )
                    Text("Emekliliği Hak Ettim (%100 Devlet Katkısı)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val investment = com.example.util.parseFormattedAmount(investmentText)
                    val investmentReturn = com.example.util.parseFormattedAmount(investmentReturnText)
                    val stateContribution = com.example.util.parseFormattedAmount(stateContributionText)
                    val stateContributionReturn = com.example.util.parseFormattedAmount(stateContributionReturnText)
                    
                    val newBes = BesPortfolio(
                        id = besPortfolio?.id ?: 0,
                        holderName = holderNameText.ifBlank { "BES" },
                        startDate = startDate,
                        investment = investment,
                        investmentReturn = investmentReturn,
                        stateContribution = stateContribution,
                        stateContributionReturn = stateContributionReturn,
                        isRetired = isRetired
                    )
                    onSave(newBes)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Kaydet", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("İptal", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
