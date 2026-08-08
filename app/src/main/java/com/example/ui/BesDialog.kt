package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
    var startDate by remember { mutableLongStateOf(besPortfolio?.startDate ?: System.currentTimeMillis()) }
    var investmentText by remember { mutableStateOf(besPortfolio?.investment?.let { com.example.util.formatDoubleForInput(it) } ?: "") }
    var investmentReturnText by remember { mutableStateOf(besPortfolio?.investmentReturn?.let { com.example.util.formatDoubleForInput(it) } ?: "") }
    var stateContributionText by remember { mutableStateOf(besPortfolio?.stateContribution?.let { com.example.util.formatDoubleForInput(it) } ?: "") }
    var stateContributionReturnText by remember { mutableStateOf(besPortfolio?.stateContributionReturn?.let { com.example.util.formatDoubleForInput(it) } ?: "") }
    var isRetired by remember { mutableStateOf(besPortfolio?.isRetired ?: false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale("tr")) }

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
        title = { Text("Bireysel Emeklilik (BES)") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = dateFormat.format(Date(startDate)),
                    onValueChange = {},
                    label = { Text("BES Başlangıç Tarihi") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                OutlinedTextField(
                    value = investmentText,
                    onValueChange = { investmentText = com.example.util.formatInputAmount(it) },
                    label = { Text("Yatırım Tutarı (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = investmentReturnText,
                    onValueChange = { investmentReturnText = com.example.util.formatInputAmount(it) },
                    label = { Text("Yatırım Getirisi (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stateContributionText,
                    onValueChange = { stateContributionText = com.example.util.formatInputAmount(it) },
                    label = { Text("Devlet Katkısı (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stateContributionReturnText,
                    onValueChange = { stateContributionReturnText = com.example.util.formatInputAmount(it) },
                    label = { Text("Devlet Katkısı Getirisi (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isRetired,
                        onCheckedChange = { isRetired = it }
                    )
                    Text("Emekliliği Hak Ettim")
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
                        id = 1,
                        startDate = startDate,
                        investment = investment,
                        investmentReturn = investmentReturn,
                        stateContribution = stateContribution,
                        stateContributionReturn = stateContributionReturn,
                        isRetired = isRetired
                    )
                    onSave(newBes)
                }
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
