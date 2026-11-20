package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppCategory
import com.example.data.Person
import com.example.data.Transaction
import com.example.data.TransactionType
import com.example.ui.theme.*
import com.example.util.AmountVisualTransformation
import com.example.util.FormatUtil
import com.example.util.formatDoubleForInput
import com.example.util.formatInputAmount
import com.example.util.parseFormattedAmount
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun isSameDayMillis(a: Long, b: Long): Boolean {
    val ca = Calendar.getInstance().apply { timeInMillis = a }
    val cb = Calendar.getInstance().apply { timeInMillis = b }
    return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
        ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
}

private fun categoryQuickIcon(name: String, type: TransactionType): ImageVector {
    val n = name.lowercase(Locale("tr", "TR"))
    return when (type) {
        TransactionType.INCOME -> when {
            n.contains("maaş") || n.contains("maas") -> Icons.Rounded.AccountBalanceWallet
            n.contains("kira") -> Icons.Rounded.Home
            n.contains("prim") || n.contains("bonus") -> Icons.Rounded.WorkspacePremium
            n.contains("yatırım") || n.contains("faiz") || n.contains("temettü") -> Icons.Rounded.ShowChart
            else -> Icons.Rounded.TrendingUp
        }
        TransactionType.EXPENSE -> when {
            n.contains("kira") || n.contains("aidat") || n.contains("ev") -> Icons.Rounded.Home
            n.contains("elektrik") -> Icons.Rounded.Bolt
            n.contains("su") -> Icons.Rounded.WaterDrop
            n.contains("doğalgaz") || n.contains("dogalgaz") || n.contains("gaz") -> Icons.Rounded.LocalFireDepartment
            n.contains("internet") || n.contains("telefon") || n.contains("fatura") -> Icons.Rounded.Wifi
            n.contains("market") || n.contains("gıda") || n.contains("mutfak") -> Icons.Rounded.ShoppingCart
            n.contains("kredi") || n.contains("kart") || n.contains("taksit") -> Icons.Rounded.CreditCard
            n.contains("okul") || n.contains("kurs") || n.contains("eğitim") -> Icons.Rounded.School
            n.contains("araç") || n.contains("yakıt") || n.contains("benzin") -> Icons.Rounded.DirectionsCar
            n.contains("sağlık") || n.contains("eczane") -> Icons.Rounded.HealthAndSafety
            else -> Icons.Rounded.ReceiptLong
        }
        TransactionType.SAVING -> when {
            n.contains("altın") || n.contains("altin") || n.contains("gram") || n.contains("çeyrek") -> Icons.Rounded.Diamond
            n.contains("dolar") || n.contains("usd") || n.contains("euro") || n.contains("eur") || n.contains("döviz") -> Icons.Rounded.CurrencyExchange
            n.contains("hisse") || n.contains("fon") || n.contains("borsa") -> Icons.Rounded.ShowChart
            n.contains("mevduat") || n.contains("faiz") -> Icons.Rounded.AccountBalance
            else -> Icons.Rounded.Savings
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialog(
    incomeCategories: List<AppCategory>,
    expenseCategories: List<AppCategory>,
    savingCategories: List<AppCategory>,
    persons: List<Person>,
    fixedType: TransactionType? = null,
    editingTransaction: Transaction? = null,
    initialCategoryName: String? = null,
    initialAmount: Double? = null,
    initialPersonName: String? = null,
    onDismiss: () -> Unit,
    onSave: (Int, Double, String, TransactionType, String, String?, Long, Double?, Double?, Int?, Boolean, Boolean) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)

    var type by remember {
        mutableStateOf(editingTransaction?.type ?: (fixedType ?: TransactionType.EXPENSE))
    }

    val activeColor = when (type) {
        TransactionType.INCOME -> mint
        TransactionType.EXPENSE -> coral
        TransactionType.SAVING -> sky
    }

    var amountText by remember {
        mutableStateOf(
            (editingTransaction?.amount ?: initialAmount)?.let { formatDoubleForInput(it) } ?: ""
        )
    }
    var quantityText by remember {
        mutableStateOf(editingTransaction?.quantity?.let { formatDoubleForInput(it) } ?: "")
    }
    var unitPriceText by remember {
        mutableStateOf(editingTransaction?.unitPrice?.let { formatDoubleForInput(it) } ?: "")
    }
    var installmentsText by remember {
        mutableStateOf(editingTransaction?.installments?.takeIf { it > 1 }?.toString() ?: "")
    }
    var showCustomInstallment by remember {
        mutableStateOf((editingTransaction?.installments ?: 0).let { it > 1 && it !in listOf(2, 3, 6, 9, 12) })
    }

    val categories = when (type) {
        TransactionType.INCOME -> incomeCategories
        TransactionType.EXPENSE -> expenseCategories
        TransactionType.SAVING -> savingCategories
    }

    var category by remember(type, initialCategoryName, editingTransaction, categories) {
        mutableStateOf<AppCategory?>(
            editingTransaction?.let { tx -> categories.firstOrNull { it.name == tx.category } }
                ?: initialCategoryName?.let { name ->
                    categories.firstOrNull {
                        it.name.equals(name, ignoreCase = true) || it.name.contains(name, ignoreCase = true)
                    }
                }
                ?: categories.firstOrNull()
        )
    }

    var person by remember {
        mutableStateOf<Person?>(
            editingTransaction?.let { tx -> persons.firstOrNull { it.name == tx.person } }
                ?: initialPersonName?.let { pName -> persons.firstOrNull { it.name.equals(pName, ignoreCase = true) } }
        )
    }

    var timestamp by remember { mutableLongStateOf(editingTransaction?.timestamp ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAllCategories by remember { mutableStateOf(false) }

    var isPaid by remember { mutableStateOf(editingTransaction?.isPaid ?: false) }
    var repeatUntilYearEnd by remember { mutableStateOf(false) }

    LaunchedEffect(type) {
        if (editingTransaction == null) {
            isPaid = false
            repeatUntilYearEnd = false
        }
    }

    val todayMillis = remember { System.currentTimeMillis() }
    val yesterdayMillis = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.timeInMillis
    }
    val shortDateSdf = remember { SimpleDateFormat("dd MMM yyyy", Locale("tr", "TR")) }
    val currencyFormat = remember { FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 2 } }
    val wholeCurrencyFormat = remember { FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 } }
    val amountVisual = remember { AmountVisualTransformation() }

    val parsedAmount = remember(amountText) { parseFormattedAmount(amountText) }
    val parsedQuantity = remember(quantityText) { parseFormattedAmount(quantityText) }
    val parsedUnitPrice = remember(unitPriceText) { parseFormattedAmount(unitPriceText) }
    val computedSavingTotal = parsedQuantity * parsedUnitPrice
    val parsedInstallments = installmentsText.toIntOrNull()?.takeIf { it > 1 }

    val canSave = category != null && if (type == TransactionType.SAVING) {
        parsedQuantity > 0.0 && parsedUnitPrice > 0.0
    } else {
        parsedAmount > 0.0
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = timestamp)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { timestamp = it }
                    showDatePicker = false
                }) { Text("Tamam", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("İptal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // 1. HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(activeColor.copy(alpha = if (isDark) 0.18f else 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (type) {
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
                            text = if (editingTransaction != null) "İşlemi Düzenle" else "Yeni İşlem",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = when (type) {
                                TransactionType.INCOME -> "Gelir kaydı oluşturun"
                                TransactionType.EXPENSE -> "Gider veya fatura kaydı oluşturun"
                                TransactionType.SAVING -> "Birikim veya yatırım alımı kaydedin"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = activeColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_add_transaction_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Kapat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SCROLLABLE FORM CONTENT
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 2. TYPE SEGMENTED CAPSULE (if unlocked)
                if (fixedType == null && editingTransaction == null) {
                    Surface(
                        shape = RoundedCornerShape(15.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.8.dp, borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val typeOptions = listOf(
                                Triple(TransactionType.INCOME, "Gelir", mint),
                                Triple(TransactionType.EXPENSE, "Gider", coral),
                                Triple(TransactionType.SAVING, "Birikim", sky)
                            )
                            typeOptions.forEach { (itemType, label, tint) ->
                                val selected = type == itemType
                                Surface(
                                    onClick = {
                                        if (type != itemType) {
                                            type = itemType
                                            category = null
                                            showAllCategories = false
                                        }
                                    },
                                    shape = RoundedCornerShape(11.dp),
                                    color = if (selected) tint.copy(alpha = if (isDark) 0.22f else 0.14f) else Color.Transparent,
                                    border = if (selected) BorderStroke(0.8.dp, tint.copy(alpha = 0.45f)) else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("tx_type_${itemType.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 9.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = when (itemType) {
                                                TransactionType.INCOME -> Icons.Rounded.TrendingUp
                                                TransactionType.EXPENSE -> Icons.Rounded.Payments
                                                TransactionType.SAVING -> Icons.Rounded.Savings
                                            },
                                            contentDescription = null,
                                            tint = if (selected) tint else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) tint else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. HERO AMOUNT COCKPIT OR SAVING CALCULATOR CARD
                if (type == TransactionType.SAVING) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.9.dp, activeColor.copy(alpha = if (isDark) 0.40f else 0.30f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            activeColor.copy(alpha = if (isDark) 0.10f else 0.06f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOPLAM YATIRIM TUTARI",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 0.8.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = currencyFormat.format(computedSavingTotal),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = activeColor
                                )
                            }

                            HorizontalDivider(thickness = 0.7.dp, color = borderColor.copy(alpha = 0.8f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = quantityText,
                                    onValueChange = { quantityText = formatInputAmount(it) },
                                    label = { Text("Adet / Gram") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    visualTransformation = amountVisual,
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    textStyle = TextStyle(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("saving_quantity_input")
                                )
                                OutlinedTextField(
                                    value = unitPriceText,
                                    onValueChange = { unitPriceText = formatInputAmount(it) },
                                    label = { Text("Birim Fiyat") },
                                    suffix = { Text("₺", fontWeight = FontWeight.Bold, color = activeColor) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    visualTransformation = amountVisual,
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    textStyle = TextStyle(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("saving_unit_price_input")
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.9.dp, activeColor.copy(alpha = if (isDark) 0.42f else 0.30f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            activeColor.copy(alpha = if (isDark) 0.10f else 0.06f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (type == TransactionType.INCOME) "GELİR TUTARI" else "ÖDEME TUTARI",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 0.8.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (amountText.isNotEmpty()) {
                                    Surface(
                                        onClick = { amountText = "" },
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                    ) {
                                        Text(
                                            text = "Temizle",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "₺",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        color = activeColor
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (amountText.isEmpty()) {
                                        Text(
                                            text = "0",
                                            style = TextStyle(
                                                fontFamily = JetBrainsMonoFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 32.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                            )
                                        )
                                    }
                                    BasicTextField(
                                        value = amountText,
                                        onValueChange = { amountText = formatInputAmount(it) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        visualTransformation = amountVisual,
                                        cursorBrush = SolidColor(activeColor),
                                        textStyle = TextStyle(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 32.sp,
                                            fontFeatureSettings = "tnum",
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("amount_input")
                                    )
                                }
                            }

                            if (type == TransactionType.EXPENSE && parsedAmount > 0.0 && parsedInstallments != null) {
                                val monthlyShare = parsedAmount / parsedInstallments
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(9.dp),
                                    color = amber.copy(alpha = if (isDark) 0.16f else 0.10f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CreditScore,
                                            contentDescription = null,
                                            tint = amber,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "$parsedInstallments taksit · Aylık ${wholeCurrencyFormat.format(monthlyShare)}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = JetBrainsMonoFamily,
                                                fontFeatureSettings = "tnum"
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            color = amber
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. CATEGORY SELECTOR (1-TAP PILLS + EXPANDABLE GRID)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "KATEGORİ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.8.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (categories.size > 5) {
                            Text(
                                text = if (showAllCategories) "Tek Satır" else "Tümü (${categories.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = activeColor,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showAllCategories = !showAllCategories }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (categories.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.8.dp, borderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Bu tür için henüz kategori bulunmuyor. Ayarlar'dan kategori ekleyebilirsiniz.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    } else if (showAllCategories) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { cat ->
                                TransactionCategoryPill(
                                    name = cat.name,
                                    icon = categoryQuickIcon(cat.name, type),
                                    selected = category?.id == cat.id,
                                    accent = activeColor,
                                    borderColor = borderColor,
                                    isDark = isDark,
                                    onClick = { category = cat }
                                )
                            }
                        }
                    } else {
                        // Place currently selected category first if it's far down the list so user sees it immediately
                        val orderedCategories = remember(categories, category) {
                            val sel = category
                            if (sel != null && categories.indexOfFirst { it.id == sel.id } > 3) {
                                listOf(sel) + categories.filterNot { it.id == sel.id }
                            } else {
                                categories
                            }
                        }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(orderedCategories, key = { it.id }) { cat ->
                                TransactionCategoryPill(
                                    name = cat.name,
                                    icon = categoryQuickIcon(cat.name, type),
                                    selected = category?.id == cat.id,
                                    accent = activeColor,
                                    borderColor = borderColor,
                                    isDark = isDark,
                                    onClick = { category = cat }
                                )
                            }
                        }
                    }
                }

                // 5. SMART 1-TAP DATE BAR
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "TARİH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val isToday = isSameDayMillis(timestamp, todayMillis)
                    val isYesterday = isSameDayMillis(timestamp, yesterdayMillis)
                    val isCustomDate = !isToday && !isYesterday

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DateQuickPill(
                            label = "Bugün",
                            selected = isToday,
                            accent = activeColor,
                            borderColor = borderColor,
                            isDark = isDark,
                            onClick = { timestamp = todayMillis },
                            modifier = Modifier.testTag("tx_date_today")
                        )
                        DateQuickPill(
                            label = "Dün",
                            selected = isYesterday,
                            accent = activeColor,
                            borderColor = borderColor,
                            isDark = isDark,
                            onClick = { timestamp = yesterdayMillis },
                            modifier = Modifier.testTag("tx_date_yesterday")
                        )
                        Surface(
                            onClick = { showDatePicker = true },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCustomDate) activeColor.copy(alpha = if (isDark) 0.20f else 0.12f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                0.8.dp,
                                if (isCustomDate) activeColor.copy(alpha = 0.55f) else borderColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tx_date_picker_btn")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = null,
                                    tint = if (isCustomDate) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = shortDateSdf.format(Date(timestamp)),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    fontWeight = if (isCustomDate) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCustomDate) activeColor else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // 6. PERSON SELECTOR (1-TAP PILLS)
                if (persons.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "İLGİLİ KİŞİ (OPSİYONEL)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.8.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item(key = "person_none") {
                                val selected = person == null
                                Surface(
                                    onClick = { person = null },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.20f else 0.12f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        0.8.dp,
                                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.50f) else borderColor
                                    )
                                ) {
                                    Text(
                                        text = "Kişisiz",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                                    )
                                }
                            }
                            items(persons, key = { it.id }) { p ->
                                val selected = person?.id == p.id
                                val initial = p.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                                Surface(
                                    onClick = { person = if (selected) null else p },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) activeColor.copy(alpha = if (isDark) 0.20f else 0.12f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        0.8.dp,
                                        if (selected) activeColor.copy(alpha = 0.55f) else borderColor
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 7.dp, bottom = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (selected) activeColor else MaterialTheme.colorScheme.surfaceVariant
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = initial,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = p.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) activeColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 7. EXPENSE PAYMENT STATUS, INSTALLMENTS & RECURRENCE CARD
                if (type == TransactionType.EXPENSE) {
                    SettingsGroupCard {
                        // Paid status switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isPaid = !isPaid }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background((if (isPaid) mint else amber).copy(alpha = if (isDark) 0.18f else 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPaid) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = if (isPaid) mint else amber,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ödeme Yapıldı (Peşin)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isPaid) "Tamamlanan ödeme olarak kaydedilir" else "Bekleyen ödeme listesine eklenir",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isPaid,
                                onCheckedChange = { isPaid = it },
                                modifier = Modifier.testTag("tx_switch_is_paid")
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            thickness = 0.6.dp,
                            color = borderColor.copy(alpha = 0.75f)
                        )

                        // Quick Installments selector
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Taksit Sayısı",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (parsedInstallments != null) "$parsedInstallments Taksit" else "Tek Çekim",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = if (parsedInstallments != null) amber else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val presetInst = listOf(1, 2, 3, 6, 9, 12)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(presetInst) { count ->
                                    val isSel = !showCustomInstallment &&
                                        ((count == 1 && parsedInstallments == null) || parsedInstallments == count)
                                    Surface(
                                        onClick = {
                                            showCustomInstallment = false
                                            installmentsText = if (count == 1) "" else count.toString()
                                            if (count > 1) repeatUntilYearEnd = false
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSel) amber.copy(alpha = if (isDark) 0.22f else 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                        border = BorderStroke(0.7.dp, if (isSel) amber else Color.Transparent)
                                    ) {
                                        Text(
                                            text = if (count == 1) "Tek" else "${count}T",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontFamily = JetBrainsMonoFamily,
                                                fontFeatureSettings = "tnum"
                                            ),
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSel) amber else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                                item {
                                    Surface(
                                        onClick = {
                                            showCustomInstallment = true
                                            repeatUntilYearEnd = false
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (showCustomInstallment) amber.copy(alpha = if (isDark) 0.22f else 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                        border = BorderStroke(0.7.dp, if (showCustomInstallment) amber else Color.Transparent)
                                    ) {
                                        Text(
                                            text = "Özel",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (showCustomInstallment) FontWeight.Bold else FontWeight.Medium,
                                            color = if (showCustomInstallment) amber else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(visible = showCustomInstallment) {
                                OutlinedTextField(
                                    value = installmentsText,
                                    onValueChange = { v ->
                                        if (v.all { it.isDigit() } && v.length <= 2) {
                                            installmentsText = v
                                            if ((v.toIntOrNull() ?: 0) > 1) repeatUntilYearEnd = false
                                        }
                                    },
                                    label = { Text("Taksit Sayısı (örn. 4, 18)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                )
                            }
                        }

                        if (editingTransaction == null) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                thickness = 0.6.dp,
                                color = borderColor.copy(alpha = 0.75f)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val next = !repeatUntilYearEnd
                                        repeatUntilYearEnd = next
                                        if (next) {
                                            installmentsText = ""
                                            showCustomInstallment = false
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(sky.copy(alpha = if (isDark) 0.18f else 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.EventRepeat,
                                        contentDescription = null,
                                        tint = sky,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Yıl Sonuna Kadar Tekrarla",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Kira veya sabit faturalar için her aya otomatik ekler",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = repeatUntilYearEnd,
                                    onCheckedChange = { checked ->
                                        repeatUntilYearEnd = checked
                                        if (checked) {
                                            installmentsText = ""
                                            showCustomInstallment = false
                                        }
                                    },
                                    modifier = Modifier.testTag("tx_switch_repeat_year")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // 8. STICKY BOTTOM ACTION BAR
            HorizontalDivider(thickness = 0.8.dp, color = borderColor)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.8.dp, borderColor)
                ) {
                    Text("İptal", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        val finalAmount: Double
                        var q: Double? = null
                        var up: Double? = null
                        var inst: Int? = null

                        if (type == TransactionType.SAVING) {
                            q = parsedQuantity
                            up = parsedUnitPrice
                            if (q <= 0.0 || up <= 0.0) return@Button
                            finalAmount = q * up
                        } else {
                            if (parsedAmount <= 0.0) return@Button
                            finalAmount = parsedAmount
                            if (type == TransactionType.EXPENSE) {
                                inst = parsedInstallments
                            }
                        }

                        val selectedCat = category ?: return@Button
                        val finalPaid = if (type == TransactionType.EXPENSE) isPaid else true
                        onSave(
                            editingTransaction?.id ?: 0,
                            finalAmount,
                            selectedCat.name,
                            type,
                            selectedCat.name,
                            person?.name,
                            timestamp,
                            q,
                            up,
                            inst,
                            finalPaid,
                            repeatUntilYearEnd
                        )
                    },
                    enabled = canSave,
                    modifier = Modifier
                        .weight(1.45f)
                        .height(50.dp)
                        .testTag("save_transaction_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = activeColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (editingTransaction != null) "Güncelle" else "Kaydet",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionCategoryPill(
    name: String,
    icon: ImageVector,
    selected: Boolean,
    accent: Color,
    borderColor: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) accent.copy(alpha = if (isDark) 0.22f else 0.14f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 1.dp else 0.8.dp,
            color = if (selected) accent.copy(alpha = 0.60f) else borderColor
        ),
        modifier = Modifier.testTag("tx_category_$name")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) accent else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun DateQuickPill(
    label: String,
    selected: Boolean,
    accent: Color,
    borderColor: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) accent.copy(alpha = if (isDark) 0.22f else 0.14f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.8.dp, if (selected) accent.copy(alpha = 0.55f) else borderColor),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}
