package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppCategory
import com.example.data.Person
import com.example.data.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    incomeCategories: List<AppCategory>,
    expenseCategories: List<AppCategory>,
    savingCategories: List<AppCategory>,
    persons: List<Person>,
    fixedType: TransactionType? = null,
    editingTransaction: com.example.data.Transaction? = null,
    onDismiss: () -> Unit,
    onSave: (Int, Double, String, TransactionType, String, String?, Long, Double?, Double?, Int?, Boolean) -> Unit
) {
    var amountText by remember { mutableStateOf(editingTransaction?.amount?.let { if (it > 0) it.toString() else "" } ?: "") }
    var type by remember { mutableStateOf(editingTransaction?.type ?: (fixedType ?: TransactionType.EXPENSE)) }

    val categories = when (type) {
        TransactionType.INCOME -> incomeCategories
        TransactionType.EXPENSE -> expenseCategories
        TransactionType.SAVING -> savingCategories
    }

    var category by remember { mutableStateOf<AppCategory?>(editingTransaction?.let { tx -> categories.firstOrNull { it.name == tx.category } }) }
    var person by remember { mutableStateOf<Person?>(editingTransaction?.let { tx -> persons.firstOrNull { it.name == tx.person } }) }
    var timestamp by remember { mutableLongStateOf(editingTransaction?.timestamp ?: System.currentTimeMillis()) }

    var showDatePicker by remember { mutableStateOf(false) }

    var quantityText by remember { mutableStateOf(editingTransaction?.quantity?.toString() ?: "") }
    var unitPriceText by remember { mutableStateOf(editingTransaction?.unitPrice?.toString() ?: "") }
    var installmentsText by remember { mutableStateOf(editingTransaction?.installments?.toString() ?: "") }
    
    var isPaid by remember { mutableStateOf(editingTransaction?.isPaid ?: false) }

    LaunchedEffect(type) {
        if (editingTransaction == null) {
            isPaid = false
        }
    }

    var categoryExpanded by remember { mutableStateOf(false) }
    var personExpanded by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("tr"))

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

    // --- Premium Colors ---
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val activeColor = when(type) {
        TransactionType.INCOME -> Color(0xFF10B981) // Emerald
        TransactionType.EXPENSE -> Color(0xFFEF4444) // Red
        TransactionType.SAVING -> Color(0xFFF97316) // Orange
    }
    val dialogBgColor = activeColor

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = dialogBgColor,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Area
                Text(
                    text = "Yeni İşlem",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (fixedType == null) {
                    // Premium Segmented Control
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha=0.2f), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CompactTab(
                            text = "Gelir",
                            selected = type == TransactionType.INCOME,
                            onClick = { type = TransactionType.INCOME; category = null },
                            modifier = Modifier.weight(1f)
                        )
                        CompactTab(
                            text = "Gider",
                            selected = type == TransactionType.EXPENSE,
                            onClick = { type = TransactionType.EXPENSE; category = null },
                            modifier = Modifier.weight(1f)
                        )
                        CompactTab(
                            text = "Birikim",
                            selected = type == TransactionType.SAVING,
                            onClick = { type = TransactionType.SAVING; category = null },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Form Body - Super Compact
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing
                ) {
                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedContainerColor = Color.White.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.1f),
                        disabledTextColor = Color.White,
                        disabledBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.7f)
                    )
                    val fieldShape = RoundedCornerShape(10.dp)

                    // ROW 1: DATE & CATEGORY
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sdf.format(Date(timestamp)),
                            onValueChange = {},
                            placeholder = { Text("Tarih", color = Color.White.copy(0.7f)) },
                            readOnly = true,
                            enabled = false,
                            singleLine = true,
                            modifier = Modifier.weight(1f).clickable { showDatePicker = true }.height(50.dp),
                            colors = fieldColors,
                            textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp),
                            shape = fieldShape,
                            leadingIcon = { Icon(Icons.Rounded.EditCalendar, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                        )

                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded },
                            modifier = Modifier.weight(1.3f)
                        ) {
                            OutlinedTextField(
                                value = category?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                placeholder = { Text("Kategori", color = Color.White.copy(0.7f)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth().height(50.dp),
                                shape = fieldShape,
                                textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp),
                                colors = fieldColors
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                categories.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption.name) },
                                        onClick = { category = selectionOption; categoryExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    // ROW 2: PERSON
                    ExposedDropdownMenuBox(
                        expanded = personExpanded,
                        onExpandedChange = { personExpanded = !personExpanded }
                    ) {
                        OutlinedTextField(
                            value = person?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            placeholder = { Text("İlgili Kişi (İsteğe Bağlı)", color = Color.White.copy(0.7f)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = personExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth().height(50.dp),
                            shape = fieldShape,
                            textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp),
                            colors = fieldColors
                        )
                        ExposedDropdownMenu(
                            expanded = personExpanded,
                            onDismissRequest = { personExpanded = false }
                        ) {
                            DropdownMenuItem(text = { Text("Temizle", color = MaterialTheme.colorScheme.error) }, onClick = { person = null; personExpanded = false })
                            persons.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption.name) },
                                    onClick = { person = selectionOption; personExpanded = false }
                                )
                            }
                        }
                    }

                    // ROW 3: AMOUNT & OPTIONS
                    if (type == TransactionType.SAVING) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = quantityText,
                                onValueChange = { newValue -> if (newValue.all { it.isDigit() || it == '.' || it == ',' }) quantityText = newValue },
                                placeholder = { Text("Adet", color = Color.White.copy(0.7f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).height(50.dp),
                                singleLine = true,
                                shape = fieldShape,
                                colors = fieldColors,
                                textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            )
                            OutlinedTextField(
                                value = unitPriceText,
                                onValueChange = { newValue -> if (newValue.all { it.isDigit() || it == '.' || it == ',' }) unitPriceText = newValue },
                                placeholder = { Text("Birim Fiyat", color = Color.White.copy(0.7f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).height(50.dp),
                                singleLine = true,
                                shape = fieldShape,
                                colors = fieldColors,
                                textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            )
                        }

                        val q = quantityText.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
                        val up = unitPriceText.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
                        val computedAmount = q * up

                        Text(
                            text = "Toplam: %,.2f ₺".format(Locale("tr"), computedAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.align(Alignment.End).padding(vertical = 4.dp)
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { newValue -> if (newValue.all { it.isDigit() || it == '.' || it == ',' }) amountText = newValue },
                                placeholder = { Text("İşlem Tutarı (₺)", color = Color.White.copy(0.7f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1.5f).height(50.dp).testTag("amount_input"),
                                singleLine = true,
                                shape = fieldShape,
                                colors = fieldColors,
                                textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Black, fontSize = 16.sp),
                                leadingIcon = { Text("₺", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(start=12.dp)) }
                            )

                            if (type == TransactionType.EXPENSE) {
                                OutlinedTextField(
                                    value = installmentsText,
                                    onValueChange = { newValue -> if (newValue.all { it.isDigit() }) installmentsText = newValue },
                                    placeholder = { Text("Taksit", color = Color.White.copy(0.7f)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    singleLine = true,
                                    shape = fieldShape,
                                    colors = fieldColors,
                                    textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                )
                            }
                        }
                    }
                    
                    if (type == TransactionType.EXPENSE) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isPaid,
                                onCheckedChange = { isPaid = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color.White, uncheckedColor = Color.White.copy(alpha=0.7f), checkmarkColor = activeColor)
                            )
                            Text("Bu ödeme yapıldı", color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                    } 

                    Spacer(modifier = Modifier.height(4.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("İptal", fontWeight = FontWeight.Bold)
                        }
                        
                        val isEnabled = category != null && (if (type == TransactionType.SAVING) quantityText.isNotBlank() && unitPriceText.isNotBlank() else amountText.isNotBlank())
                        
                        Button(
                            onClick = {
                                var finalAmount = 0.0
                                var q: Double? = null
                                var up: Double? = null
                                var inst: Int? = null
                                
                                if (type == TransactionType.SAVING) {
                                    q = quantityText.replace(".", "").replace(",", ".").toDoubleOrNull()
                                    up = unitPriceText.replace(".", "").replace(",", ".").toDoubleOrNull()
                                    if (q != null && up != null) { finalAmount = q * up } else { return@Button }
                                } else {
                                    val parsedAmt = amountText.replace(".", "").replace(",", ".").toDoubleOrNull()
                                    if (parsedAmt != null) { finalAmount = parsedAmt } else { return@Button }
                                    if (type == TransactionType.EXPENSE) { inst = installmentsText.toIntOrNull() }
                                }

                                if (category != null) {
                                    val finalPaid = if (type == TransactionType.EXPENSE) isPaid else true
                                    onSave(editingTransaction?.id ?: 0, finalAmount, category!!.name, type, category!!.name, person?.name, timestamp, q, up, inst, finalPaid)
                                }
                            },
                            enabled = isEnabled,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = activeColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Onayla", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
