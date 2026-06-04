package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.TransactionType
import com.example.data.AppCategory
import com.example.data.Person
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    incomeCategories: List<AppCategory>,
    expenseCategories: List<AppCategory>,
    savingCategories: List<AppCategory>,
    persons: List<Person>,
    onDismiss: () -> Unit,
    onSave: (Double, String, TransactionType, String, String?, Long, Double?, Double?, Int?) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<AppCategory?>(null) }
    var person by remember { mutableStateOf<Person?>(null) }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    var showDatePicker by remember { mutableStateOf(false) }

    var quantityText by remember { mutableStateOf("") }
    var unitPriceText by remember { mutableStateOf("") }
    var installmentsText by remember { mutableStateOf("") }

    val categories = when (type) {
        TransactionType.INCOME -> incomeCategories
        TransactionType.EXPENSE -> expenseCategories
        TransactionType.SAVING -> savingCategories
    }

    var categoryExpanded by remember { mutableStateOf(false) }
    var personExpanded by remember { mutableStateOf(false) }
    
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("tr"))

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = timestamp)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { timestamp = it }
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
        title = { Text("Yeni İşlem Ekle") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SegmentedButton(
                        selected = type == TransactionType.INCOME,
                        onClick = {
                            type = TransactionType.INCOME
                            category = null
                        },
                        text = "Gelir",
                        selectedColor = androidx.compose.ui.graphics.Color(0xFF16A34A) // Green tones for Income
                    )
                    SegmentedButton(
                        selected = type == TransactionType.EXPENSE,
                        onClick = {
                            type = TransactionType.EXPENSE
                            category = null
                        },
                        text = "Gider",
                        selectedColor = androidx.compose.ui.graphics.Color(0xFFDC2626) // Red tones for Expense
                    )
                    SegmentedButton(
                        selected = type == TransactionType.SAVING,
                        onClick = {
                            type = TransactionType.SAVING
                            category = null
                        },
                        text = "Birikim",
                        selectedColor = androidx.compose.ui.graphics.Color(0xFFF97316) // Orange tones for Saving
                    )
                }
                
                OutlinedTextField(
                    value = sdf.format(Date(timestamp)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tarih (Zorunlu)") },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    enabled = false, // Use clickable instead
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                if (type == TransactionType.SAVING) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() || it == '.' || it == ',' }) {
                                    quantityText = newValue
                                }
                            },
                            label = { Text("Miktar") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = unitPriceText,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() || it == '.' || it == ',' }) {
                                    unitPriceText = newValue
                                }
                            },
                            label = { Text("Birim Fiyat") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    val q = quantityText.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
                    val up = unitPriceText.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
                    val computedAmount = q * up

                    OutlinedTextField(
                        value = "%,.2f".format(Locale("tr"), computedAmount),
                        onValueChange = {},
                        label = { Text("Toplam Tutar (₺)") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                } else {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() || it == '.' || it == ',' }) {
                                amountText = newValue
                            }
                        },
                        label = { Text("Tutar (₺) - Zorunlu") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("amount_input"),
                        singleLine = true,
                        placeholder = { Text("Örn: 1.250,50") }
                    )

                    if (type == TransactionType.EXPENSE) {
                        OutlinedTextField(
                            value = installmentsText,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() }) {
                                    installmentsText = newValue
                                }
                            },
                            label = { Text("Taksit Sayısı (İsteğe Bağlı)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Peşin için boş bırakın") }
                        )
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori (Zorunlu)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption.name) },
                                onClick = {
                                    category = selectionOption
                                    categoryExpanded = false
                                }
                            )
                        }
                        if (categories.isEmpty()) {
                            DropdownMenuItem(text = { Text("Hiç kategori yok") }, onClick = { categoryExpanded = false })
                        }
                    }
                }
                
                ExposedDropdownMenuBox(
                    expanded = personExpanded,
                    onExpandedChange = { personExpanded = !personExpanded }
                ) {
                    OutlinedTextField(
                        value = person?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kişi (İsteğe Bağlı)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = personExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = personExpanded,
                        onDismissRequest = { personExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Seçimi Temizle") },
                            onClick = {
                                person = null
                                personExpanded = false
                            }
                        )
                        persons.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption.name) },
                                onClick = {
                                    person = selectionOption
                                    personExpanded = false
                                }
                            )
                        }
                        if (persons.isEmpty()) {
                            DropdownMenuItem(text = { Text("Kayıtlı kişi yok") }, onClick = { personExpanded = false })
                        }
                    }
                }
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Not (İsteğe Bağlı)") },
                    modifier = Modifier.fillMaxWidth().testTag("title_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var finalAmount = 0.0
                    var q: Double? = null
                    var up: Double? = null
                    var inst: Int? = null
                    
                    if (type == TransactionType.SAVING) {
                        q = quantityText.replace(".", "").replace(",", ".").toDoubleOrNull()
                        up = unitPriceText.replace(".", "").replace(",", ".").toDoubleOrNull()
                        if (q != null && up != null) {
                            finalAmount = q * up
                        } else {
                            return@Button // Do not save if invalid Saving inputs
                        }
                    } else {
                        val parsedAmountStr = amountText.replace(".", "").replace(",", ".")
                        val parsedAmt = parsedAmountStr.toDoubleOrNull()
                        if (parsedAmt != null) {
                            finalAmount = parsedAmt
                        } else {
                            return@Button
                        }
                        if (type == TransactionType.EXPENSE) {
                            inst = installmentsText.toIntOrNull()
                        }
                    }

                    if (category != null) {
                        onSave(finalAmount, title.ifBlank { category!!.name }, type, category!!.name, person?.name, timestamp, q, up, inst)
                    }
                },
                modifier = Modifier.testTag("save_btn"),
                enabled = category != null && (if (type == TransactionType.SAVING) quantityText.isNotBlank() && unitPriceText.isNotBlank() else amountText.isNotBlank())
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

@Composable
fun SegmentedButton(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    selectedColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) selectedColor else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text)
    }
}
