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
import androidx.compose.ui.graphics.luminance
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
    initialCategoryName: String? = null,
    onDismiss: () -> Unit,
    onSave: (Int, Double, String, TransactionType, String, String?, Long, Double?, Double?, Int?, Boolean, Boolean) -> Unit
) {
    var amountText by remember { mutableStateOf(editingTransaction?.amount?.let { com.example.util.formatDoubleForInput(it) } ?: "") }
    var type by remember { mutableStateOf(editingTransaction?.type ?: (fixedType ?: TransactionType.EXPENSE)) }

    val categories = when (type) {
        TransactionType.INCOME -> incomeCategories
        TransactionType.EXPENSE -> expenseCategories
        TransactionType.SAVING -> savingCategories
    }

    var category by remember(type, initialCategoryName, editingTransaction, categories) {
        mutableStateOf<AppCategory?>(
            editingTransaction?.let { tx -> categories.firstOrNull { it.name == tx.category } }
                ?: initialCategoryName?.let { name -> categories.firstOrNull { it.name.equals(name, ignoreCase = true) || it.name.contains(name, ignoreCase = true) } }
                ?: categories.firstOrNull()
        )
    }
    var person by remember { mutableStateOf<Person?>(editingTransaction?.let { tx -> persons.firstOrNull { it.name == tx.person } }) }
    var timestamp by remember { mutableLongStateOf(editingTransaction?.timestamp ?: System.currentTimeMillis()) }

    var showDatePicker by remember { mutableStateOf(false) }

    var quantityText by remember { mutableStateOf(editingTransaction?.quantity?.let { com.example.util.formatDoubleForInput(it) } ?: "") }
    var unitPriceText by remember { mutableStateOf(editingTransaction?.unitPrice?.let { com.example.util.formatDoubleForInput(it) } ?: "") }
    var installmentsText by remember { mutableStateOf(editingTransaction?.installments?.toString() ?: "") }
    
    var isPaid by remember { mutableStateOf(editingTransaction?.isPaid ?: false) }
    var repeatUntilYearEnd by remember { mutableStateOf(false) }

    LaunchedEffect(type) {
        if (editingTransaction == null) {
            isPaid = false
        }
    }

    var categoryExpanded by remember { mutableStateOf(false) }
    var personExpanded by remember { mutableStateOf(false) }

    val sdf = remember { SimpleDateFormat("dd MMMM yyyy", Locale("tr")) }

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

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val activeColor = when (type) {
        TransactionType.INCOME -> Color(0xFF10B981)
        TransactionType.EXPENSE -> Color(0xFFEF4444)
        TransactionType.SAVING -> Color(0xFF0284C7)
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White),
            border = BorderStroke(1.dp, activeColor.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(activeColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                when (type) {
                                    TransactionType.INCOME -> Icons.Rounded.TrendingUp
                                    TransactionType.EXPENSE -> Icons.Rounded.Payments
                                    TransactionType.SAVING -> Icons.Rounded.Savings
                                },
                                contentDescription = null,
                                tint = activeColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (editingTransaction != null) "İşlemi Düzenle" else "Yeni İşlem Ekle",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (type) {
                                    TransactionType.INCOME -> "Gelir Kaydı"
                                    TransactionType.EXPENSE -> "Gider / Ödeme Kaydı"
                                    TransactionType.SAVING -> "Birikim / Varlık Kaydı"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = activeColor
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Kapat", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Segmented Control if not fixed
                if (fixedType == null && editingTransaction == null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val types = listOf(
                            Triple(TransactionType.INCOME, "Gelir", Color(0xFF10B981)),
                            Triple(TransactionType.EXPENSE, "Gider", Color(0xFFEF4444)),
                            Triple(TransactionType.SAVING, "Birikim", Color(0xFF0284C7))
                        )
                        types.forEach { (t, label, col) ->
                            val isSelected = type == t
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) col else Color.Transparent)
                                    .clickable {
                                        type = t
                                        category = null
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Form Fields
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Date & Category in one row or two
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = sdf.format(Date(timestamp)),
                            onValueChange = {},
                            label = { Text("Tarih") },
                            readOnly = true,
                            enabled = false,
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePicker = true },
                            shape = RoundedCornerShape(14.dp),
                            leadingIcon = { Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = activeColor, modifier = Modifier.size(18.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            OutlinedTextField(
                                value = category?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Kategori") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                categories.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption.name, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            category = selectionOption
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Person Selector
                    ExposedDropdownMenuBox(
                        expanded = personExpanded,
                        onExpandedChange = { personExpanded = !personExpanded }
                    ) {
                        OutlinedTextField(
                            value = person?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("İlgili Kişi (İsteğe Bağlı)") },
                            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = activeColor, modifier = Modifier.size(18.dp)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = personExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = personExpanded,
                            onDismissRequest = { personExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Temizle (Kişisiz)", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    person = null
                                    personExpanded = false
                                }
                            )
                            persons.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.name, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        person = p
                                        personExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Amount input or Quantity/UnitPrice
                    if (type == TransactionType.SAVING) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = quantityText,
                                onValueChange = { quantityText = com.example.util.formatInputAmount(it) },
                                label = { Text("Adet / Gram") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp)
                            )
                            OutlinedTextField(
                                value = unitPriceText,
                                onValueChange = { unitPriceText = com.example.util.formatInputAmount(it) },
                                label = { Text("Birim Fiyat (₺)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp)
                            )
                        }

                        val q = com.example.util.parseFormattedAmount(quantityText) ?: 0.0
                        val up = com.example.util.parseFormattedAmount(unitPriceText) ?: 0.0
                        val computedAmount = q * up

                        Surface(
                            color = activeColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Toplam Hesaplanan Tutar", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    com.example.util.FormatUtil.getCurrencyFormat().format(computedAmount),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = activeColor
                                )
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { amountText = com.example.util.formatInputAmount(it) },
                                label = { Text("Tutar (₺)") },
                                leadingIcon = { Text("₺", fontWeight = FontWeight.Black, fontSize = 16.sp, color = activeColor, modifier = Modifier.padding(start = 12.dp)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1.4f)
                                    .testTag("amount_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Black, fontSize = 16.sp)
                            )

                            if (type == TransactionType.EXPENSE) {
                                OutlinedTextField(
                                    value = installmentsText,
                                    onValueChange = { newValue -> if (newValue.all { it.isDigit() }) installmentsText = newValue },
                                    label = { Text("Taksit") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(0.9f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }
                        }
                    }

                    if (type == TransactionType.EXPENSE) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isPaid,
                                    onCheckedChange = { isPaid = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981))
                                )
                                Text("Bu ödeme peşin / şimdiden yapıldı", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = repeatUntilYearEnd,
                                    onCheckedChange = { repeatUntilYearEnd = it },
                                    colors = CheckboxDefaults.colors(checkedColor = activeColor)
                                )
                                Text("Yıl sonuna kadar her ay tekrarla", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Dialog Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("İptal", fontWeight = FontWeight.SemiBold)
                        }

                        val isEnabled = category != null && (if (type == TransactionType.SAVING) quantityText.isNotBlank() && unitPriceText.isNotBlank() else amountText.isNotBlank())

                        Button(
                            onClick = {
                                var finalAmount = 0.0
                                var q: Double? = null
                                var up: Double? = null
                                var inst: Int? = null

                                if (type == TransactionType.SAVING) {
                                    q = com.example.util.parseFormattedAmount(quantityText)
                                    up = com.example.util.parseFormattedAmount(unitPriceText)
                                    if (q != null && up != null) {
                                        finalAmount = q * up
                                    } else {
                                        return@Button
                                    }
                                } else {
                                    val parsedAmt = com.example.util.parseFormattedAmount(amountText)
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
                                    val finalPaid = if (type == TransactionType.EXPENSE) isPaid else true
                                    onSave(
                                        editingTransaction?.id ?: 0,
                                        finalAmount,
                                        category!!.name,
                                        type,
                                        category!!.name,
                                        person?.name,
                                        timestamp,
                                        q,
                                        up,
                                        inst,
                                        finalPaid,
                                        repeatUntilYearEnd
                                    )
                                }
                            },
                            enabled = isEnabled,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (editingTransaction != null) "Güncelle" else "Kaydet", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
