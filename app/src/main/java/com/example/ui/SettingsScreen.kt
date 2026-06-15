package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Link
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppCategory
import com.example.data.Person
import com.example.viewmodel.BudgetViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.worker.PaymentReminderWorker
import android.widget.Toast

@Composable
fun SettingsScreen(viewModel: BudgetViewModel) {
    val incomeCategories by viewModel.incomeCategories.collectAsStateWithLifecycle()
    val expenseCategories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val savingCategories by viewModel.savingCategories.collectAsStateWithLifecycle()
    val persons by viewModel.persons.collectAsStateWithLifecycle()

    var showCategoryDialog by remember { mutableStateOf(false) }
    var categoryDialogType by remember { mutableStateOf("INCOME") }
    var editingCategory by remember { mutableStateOf<AppCategory?>(null) }

    var showPersonDialog by remember { mutableStateOf(false) }
    var editingPerson by remember { mutableStateOf<Person?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    var isIncomeExpanded by remember { mutableStateOf(false) }
    var isExpenseExpanded by remember { mutableStateOf(false) }
    var isSavingExpanded by remember { mutableStateOf(false) }
    var isPersonExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.ms-excel")) { uri ->
        if (uri != null) {
            viewModel.exportTransactionsXls(context, uri) { success, msg ->
                Toast.makeText(context, if (success) msg else "Hata: $msg", Toast.LENGTH_LONG).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.importTransactionsXls(context, uri) { success, msg ->
                Toast.makeText(context, if (success) msg else "Hata: $msg", Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Uygulama Ayarları",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Kategorileri, kişileri, bildirimleri ve yedekleri yönetin",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isIncomeExpanded = !isIncomeExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Gelir Kategorileri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Icon(if (isIncomeExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                    if (isIncomeExpanded) {
                        HorizontalDivider()
                        Column(modifier = Modifier.padding(12.dp)) {
                            incomeCategories.forEach { cat ->
                                SettingsItemRow(
                                    name = cat.name,
                                    onEdit = {
                                        editingCategory = cat
                                        categoryDialogType = "INCOME"
                                        errorMessage = ""
                                        showCategoryDialog = true
                                    },
                                    onDelete = { viewModel.deleteCategory(cat) }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { 
                                    editingCategory = null
                                    categoryDialogType = "INCOME"
                                    errorMessage = ""
                                    showCategoryDialog = true 
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Yeni Gelir Kategorisi Ekle")
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpenseExpanded = !isExpenseExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Gider Kategorileri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Icon(if (isExpenseExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                    if (isExpenseExpanded) {
                        HorizontalDivider()
                        Column(modifier = Modifier.padding(12.dp)) {
                            expenseCategories.forEach { cat ->
                                SettingsItemRow(
                                    name = cat.name,
                                    onEdit = {
                                        editingCategory = cat
                                        categoryDialogType = "EXPENSE"
                                        errorMessage = ""
                                        showCategoryDialog = true
                                    },
                                    onDelete = { viewModel.deleteCategory(cat) }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { 
                                    editingCategory = null
                                    categoryDialogType = "EXPENSE"
                                    errorMessage = ""
                                    showCategoryDialog = true 
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Yeni Gider Kategorisi Ekle")
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSavingExpanded = !isSavingExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Birikim Kategorileri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Icon(if (isSavingExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                    if (isSavingExpanded) {
                        HorizontalDivider()
                        Column(modifier = Modifier.padding(12.dp)) {
                            savingCategories.forEach { cat ->
                                SettingsItemRow(
                                    name = cat.name,
                                    onEdit = {
                                        editingCategory = cat
                                        categoryDialogType = "SAVING"
                                        errorMessage = ""
                                        showCategoryDialog = true
                                    },
                                    onDelete = { viewModel.deleteCategory(cat) }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { 
                                    editingCategory = null
                                    categoryDialogType = "SAVING"
                                    errorMessage = ""
                                    showCategoryDialog = true 
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Yeni Birikim Kategorisi Ekle")
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isPersonExpanded = !isPersonExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Kişiler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Icon(if (isPersonExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                    if (isPersonExpanded) {
                        HorizontalDivider()
                        Column(modifier = Modifier.padding(12.dp)) {
                            persons.forEach { p ->
                                SettingsItemRow(
                                    name = p.name,
                                    onEdit = {
                                        editingPerson = p
                                        errorMessage = ""
                                        showPersonDialog = true
                                    },
                                    onDelete = { viewModel.deletePerson(p) }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { 
                                    editingPerson = null
                                    errorMessage = ""
                                    showPersonDialog = true 
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Yeni Kişi Ekle")
                            }
                        }
                    }
                }
            }
        }
        
        item {
            var isNotificationExpanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isNotificationExpanded = !isNotificationExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Bildirim Ayarları", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Icon(if (isNotificationExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                    
                    if (isNotificationExpanded) {
                        HorizontalDivider()
                        Column(modifier = Modifier.padding(16.dp)) {
                            var remindDueDay by remember { mutableStateOf(viewModel.preferenceManager.remindDueDay) }
                            var remindOverdue by remember { mutableStateOf(viewModel.preferenceManager.remindOverdue) }
                            var remindUpcomingDays by remember { mutableStateOf(viewModel.preferenceManager.remindUpcomingDays) }
                            var upcomingExpanded by remember { mutableStateOf(false) }
                            
                            var updateIntervalHours by remember { mutableStateOf(viewModel.preferenceManager.updateIntervalHours) }
                            var updateIntervalExpanded by remember { mutableStateOf(false) }
                            
                            var silentHoursEnabled by remember { mutableStateOf(viewModel.preferenceManager.silentHoursEnabled) }
                            var silentHoursStart by remember { mutableStateOf(viewModel.preferenceManager.silentHoursStart) }
                            var silentHoursEnd by remember { mutableStateOf(viewModel.preferenceManager.silentHoursEnd) }
                            var showStartPicker by remember { mutableStateOf(false) }
                            var showEndPicker by remember { mutableStateOf(false) }
                            
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Son Gün Hatırlatması")
                                Switch(checked = remindDueDay, onCheckedChange = { 
                                    remindDueDay = it
                                    viewModel.preferenceManager.remindDueDay = it
                                })
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Gecikmiş Ödeme Hatırlatması")
                                Switch(checked = remindOverdue, onCheckedChange = { 
                                    remindOverdue = it
                                    viewModel.preferenceManager.remindOverdue = it
                                })
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Yaklaşan Ödeme Ayarı")
                                Box {
                                    TextButton(onClick = { upcomingExpanded = true }) {
                                        Text("$remindUpcomingDays Gün Önce")
                                    }
                                    DropdownMenu(expanded = upcomingExpanded, onDismissRequest = { upcomingExpanded = false }) {
                                        (1..7).forEach { d ->
                                            DropdownMenuItem(
                                                text = { Text("$d Gün Önce") },
                                                onClick = { 
                                                    remindUpcomingDays = d
                                                    viewModel.preferenceManager.remindUpcomingDays = d
                                                    upcomingExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Güncelleme Sıklığı")
                                Box {
                                    TextButton(onClick = { updateIntervalExpanded = true }) {
                                        Text("$updateIntervalHours Saatte Bir")
                                    }
                                    DropdownMenu(expanded = updateIntervalExpanded, onDismissRequest = { updateIntervalExpanded = false }) {
                                        listOf(1, 3, 6).forEach { h ->
                                            DropdownMenuItem(
                                                text = { Text("$h Saatte Bir") },
                                                onClick = { 
                                                    updateIntervalHours = h
                                                    viewModel.preferenceManager.updateIntervalHours = h
                                                    updateIntervalExpanded = false
                                                    
                                                    val workRequest = PeriodicWorkRequestBuilder<PaymentReminderWorker>(h.toLong(), TimeUnit.HOURS).build()
                                                    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                                                        "PaymentReminderWork",
                                                        ExistingPeriodicWorkPolicy.UPDATE,
                                                        workRequest
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Sessiz Saatler (Uykuda)", fontWeight = FontWeight.Bold)
                                    Text("Belirtilen saatlerde bildirim gelmez", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = silentHoursEnabled, onCheckedChange = { 
                                    silentHoursEnabled = it
                                    viewModel.preferenceManager.silentHoursEnabled = it
                                })
                            }
                            
                            if (silentHoursEnabled) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showStartPicker = true }
                                            .padding(vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Başlangıç", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(silentHoursStart, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .height(30.dp)
                                            .width(1.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant)
                                    )
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showEndPicker = true }
                                            .padding(vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Bitiş", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(silentHoursEnd, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            
                            if (showStartPicker) {
                                MinimalTimePickerDialog(
                                    title = "Sessiz Saatler Başlangıcı",
                                    currentValue = silentHoursStart,
                                    onConfirm = {
                                        silentHoursStart = it
                                        viewModel.preferenceManager.silentHoursStart = it
                                        showStartPicker = false
                                    },
                                    onDismiss = { showStartPicker = false }
                                )
                            }
                            
                            if (showEndPicker) {
                                MinimalTimePickerDialog(
                                    title = "Sessiz Saatler Bitişi",
                                    currentValue = silentHoursEnd,
                                    onConfirm = {
                                        silentHoursEnd = it
                                        viewModel.preferenceManager.silentHoursEnd = it
                                        showEndPicker = false
                                    },
                                    onDismiss = { showEndPicker = false }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            var isMarketMatchExpanded by remember { mutableStateOf(false) }
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val transactions = uiState.transactions
            val goldPrices by viewModel.goldPrices.collectAsStateWithLifecycle()
            val savingCategoriesFromTx = remember(transactions) {
                transactions.filter { it.type == com.example.data.TransactionType.SAVING }
                    .map { it.category }
                    .distinct()
                    .sorted()
            }
            var expandedDropdownCategory by remember { mutableStateOf<String?>(null) }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isMarketMatchExpanded = !isMarketMatchExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Canlı Piyasa Eşleşmeleri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Icon(if (isMarketMatchExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                    
                    if (isMarketMatchExpanded) {
                        HorizontalDivider()
                        
                        if (savingCategoriesFromTx.isEmpty()) {
                            Text(
                                text = "Henüz birikim kalemi bulunamadı. Birikimlerinizin canlı piyasa fiyatlarına bağlanabilmesi için lütfen önce Birikimler ekranından birikim işlemi ekleyin.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(
                                    text = "Birikim yaptığınız kalemlerin güncel fiyatlarını hangi canlı piyasa verisinden çekeceğini dilediğiniz gibi seçin:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                savingCategoriesFromTx.forEach { category ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = category,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        Box {
                                            var currentMatchCode by remember(category) { mutableStateOf(viewModel.preferenceManager.getMarketMatch(category)) }
                                            val displayLabel = when {
                                                currentMatchCode == null -> "Otomatik (Akıllı Eşleşme)"
                                                 currentMatchCode == "manual" -> "✍️ Manuel Giriş"
                                                currentMatchCode == "yk_USD" -> "Yapı Kredi - USD (Dolar)"
                                                currentMatchCode == "yk_EUR" -> "Yapı Kredi - EUR (Euro)"
                                                currentMatchCode == "yk_XAU" -> "Yapı Kredi - XAU (gr Altın)"
                                                currentMatchCode == "yk_XAG" -> "Yapı Kredi - XAG (gr Gümüş)"
                                                currentMatchCode?.startsWith("gp_") == true -> {
                                                    val name = currentMatchCode?.removePrefix("gp_") ?: ""
                                                    "Sivas - $name"
                                                }
                                                else -> "Otomatik (Akıllı Eşleşme)"
                                            }
                                            
                                            Button(
                                                onClick = { expandedDropdownCategory = category },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                ),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.heightIn(max = 38.dp)
                                            ) {
                                                Text(displayLabel, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                            
                                            DropdownMenu(
                                                expanded = expandedDropdownCategory == category,
                                                onDismissRequest = { expandedDropdownCategory = null }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Otomatik (Akıllı Eşleşme)", fontWeight = FontWeight.Bold) },
                                                    onClick = {
                                                        viewModel.preferenceManager.setMarketMatch(category, null); currentMatchCode = null
                                                        viewModel.triggerCustomPricesRefresh()
                                                        expandedDropdownCategory = null
                                                    }
                                                )
                                                
                                                HorizontalDivider()
                                                
                                                DropdownMenuItem(
                                                    text = { Text("✍️ Manuel Giriş (Kendim Belirleyeceğim)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }, onClick = { viewModel.preferenceManager.setMarketMatch(category, "manual"); currentMatchCode = "manual"; viewModel.triggerCustomPricesRefresh(); expandedDropdownCategory = null })
                                                 HorizontalDivider()
                                                 DropdownMenuItem(
                                                     text = { Text("Yapı Kredi (Banka Gişesi)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) },
                                                    onClick = {},
                                                    enabled = false
                                                )
                                                
                                                DropdownMenuItem(
                                                    text = { Text("   XAU - Altın (gram)") },
                                                    onClick = {
                                                        viewModel.preferenceManager.setMarketMatch(category, "yk_XAU"); currentMatchCode = "yk_XAU"
                                                        viewModel.triggerCustomPricesRefresh()
                                                        expandedDropdownCategory = null
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("   XAG - Gümüş (gram)") },
                                                    onClick = {
                                                        viewModel.preferenceManager.setMarketMatch(category, "yk_XAG"); currentMatchCode = "yk_XAG"
                                                        viewModel.triggerCustomPricesRefresh()
                                                        expandedDropdownCategory = null
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("   USD - Amerikan Doları") },
                                                    onClick = {
                                                        viewModel.preferenceManager.setMarketMatch(category, "yk_USD"); currentMatchCode = "yk_USD"
                                                        viewModel.triggerCustomPricesRefresh()
                                                        expandedDropdownCategory = null
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("   EUR - Euro") },
                                                    onClick = {
                                                        viewModel.preferenceManager.setMarketMatch(category, "yk_EUR"); currentMatchCode = "yk_EUR"
                                                        viewModel.triggerCustomPricesRefresh()
                                                        expandedDropdownCategory = null
                                                    }
                                                )
                                                
                                                if (goldPrices.isNotEmpty()) {
                                                    HorizontalDivider()
                                                    DropdownMenuItem(
                                                        text = { Text("Sivas Sarraf Fiyatları", color = Color(0xFFD97706), fontWeight = FontWeight.SemiBold) },
                                                        onClick = {},
                                                        enabled = false
                                                    )
                                                    goldPrices.forEach { gp ->
                                                        DropdownMenuItem(
                                                            text = { Text("   ${gp.name}") },
                                                            onClick = {
                                                                viewModel.preferenceManager.setMarketMatch(category, "gp_${gp.name}"); currentMatchCode = "gp_${gp.name}"
                                                                viewModel.triggerCustomPricesRefresh()
                                                                expandedDropdownCategory = null
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Veri Yedekleme (Excel XLS)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Dosya formatı (Dışa aktarılan Excel .xls şablonunu kullanabilirsiniz): Sütunlar sırasıyla Id,Tarih,Tür,Kategori,Kisi,Tutar,Miktar,Birim Fiyat,Odendi.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { exportLauncher.launch("islemler_yedek.xls") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Dışa Aktar")
                        }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/vnd.ms-excel", "application/excel", "application/x-excel", "application/x-msexcel", "*/*")) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("İçe Aktar")
                        }
                    }
                }
            }
        }

        item {
            var showClearDialog by remember { mutableStateOf(false) }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Tehlikeli Bölge", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tüm uygulama verilerini, işlemleri, kişileri ve kategorileri sil. Bu işlem geri alınamaz.", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showClearDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                    ) {
                        Text("Tüm Verileri Sil")
                    }
                }
            }

            if (showClearDialog) {
                AlertDialog(
                    onDismissRequest = { showClearDialog = false },
                    title = { Text("Tüm Veriler Silinsin Mi?") },
                    text = { Text("Tüm bütçe işlemleri, kişiler ve kategoriler kalıcı olarak silinecektir. Bu işlem GERİ ALINAMAZ. Emin misiniz?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteAllData()
                                showClearDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Evet, Sil")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearDialog = false }) { Text("İptal") }
                    }
                )
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showCategoryDialog) {
        var inputName by remember { mutableStateOf(editingCategory?.name ?: "") }
        
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text(if (editingCategory == null) "Kategori Ekle" else "Kategoriyi Düzenle") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { 
                            inputName = it
                            errorMessage = ""
                        },
                        label = { Text("Kategori Adı") },
                        singleLine = true,
                        isError = errorMessage.isNotEmpty()
                    )
                    if (errorMessage.isNotEmpty()) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nameStr = inputName.trim()
                        if (nameStr.isBlank()) {
                            errorMessage = "Kategori adı boş olamaz"
                            return@Button
                        }
                        val currentList = when (categoryDialogType) {
                            "INCOME" -> incomeCategories
                            "EXPENSE" -> expenseCategories
                            else -> savingCategories
                        }
                        val exists = currentList.any { it.name.equals(nameStr, ignoreCase = true) && it.id != (editingCategory?.id ?: -1) }
                        if (exists) {
                            errorMessage = "Bu isimde bir kategori zaten var"
                            return@Button
                        }

                        if (editingCategory != null) {
                            viewModel.updateCategory(editingCategory!!.copy(name = nameStr))
                        } else {
                            viewModel.addCategory(AppCategory(name = nameStr, type = categoryDialogType))
                        }
                        showCategoryDialog = false
                    }
                ) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { showCategoryDialog = false }) { Text("İptal") }
            }
        )
    }

    if (showPersonDialog) {
        var inputName by remember { mutableStateOf(editingPerson?.name ?: "") }
        AlertDialog(
            onDismissRequest = { showPersonDialog = false },
            title = { Text(if (editingPerson == null) "Kişi Ekle" else "Kişiyi Düzenle") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { 
                            inputName = it 
                            errorMessage = ""
                        },
                        label = { Text("Kişi Adı/Soyadı") },
                        singleLine = true,
                        isError = errorMessage.isNotEmpty()
                    )
                    if (errorMessage.isNotEmpty()) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nameStr = inputName.trim()
                        if (nameStr.isBlank()) {
                            errorMessage = "Kişi adı boş olamaz"
                            return@Button
                        }
                        val exists = persons.any { it.name.equals(nameStr, ignoreCase = true) && it.id != (editingPerson?.id ?: -1) }
                        if (exists) {
                            errorMessage = "Bu isimde bir kişi zaten var"
                            return@Button
                        }

                        if (editingPerson != null) {
                            viewModel.updatePerson(editingPerson!!.copy(name = nameStr))
                        } else {
                            viewModel.addPerson(Person(name = nameStr))
                        }
                        showPersonDialog = false
                    }
                ) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { showPersonDialog = false }) { Text("İptal") }
            }
        )
    }
}

@Composable
fun SettingsItemRow(
    name: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Düzenle",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MinimalTimePickerDialog(
    title: String,
    currentValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val parts = currentValue.split(":")
    val curHour = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val curMin = parts.getOrNull(1)?.toIntOrNull() ?: 0
    
    var selectedHour by remember { mutableStateOf(curHour) }
    var selectedMin by remember { mutableStateOf(curMin) }
    
    var hourExpanded by remember { mutableStateOf(false) }
    var minExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clickable { hourExpanded = true }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(String.format("%02d", selectedHour), style = MaterialTheme.typography.titleLarge)
                    DropdownMenu(expanded = hourExpanded, onDismissRequest = { hourExpanded = false }) {
                        (0..23).forEach { h ->
                            DropdownMenuItem(
                                text = { Text(String.format("%02d", h)) },
                                onClick = {
                                    selectedHour = h
                                    hourExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Text(":", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 12.dp))
                
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clickable { minExpanded = true }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(String.format("%02d", selectedMin), style = MaterialTheme.typography.titleLarge)
                    DropdownMenu(expanded = minExpanded, onDismissRequest = { minExpanded = false }) {
                        (0..59 step 5).forEach { m ->
                            DropdownMenuItem(
                                text = { Text(String.format("%02d", m)) },
                                onClick = {
                                    selectedMin = m
                                    minExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(String.format("%02d:%02d", selectedHour, selectedMin))
            }) {
                Text("Tamam")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
