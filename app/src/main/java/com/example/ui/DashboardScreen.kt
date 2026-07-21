package com.example.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Transaction
import com.example.data.TransactionType
import com.example.viewmodel.BudgetViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Calculate
import coil.compose.AsyncImage
import com.example.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: BudgetViewModel,
    authViewModel: AuthViewModel? = null,
    onSignOut: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authState by (authViewModel?.authState ?: kotlinx.coroutines.flow.MutableStateFlow(com.example.viewmodel.AuthState.Idle)).collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val goldPrices by viewModel.goldPrices.collectAsStateWithLifecycle()
    val bankRates by viewModel.bankRates.collectAsStateWithLifecycle()
    val ziraatRates by viewModel.ziraatRates.collectAsStateWithLifecycle()
    val customPricesTrigger by viewModel.customPricesRefreshTrigger.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showProfileMenu by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is com.example.viewmodel.AuthState.Authenticated) {
            val user = authState as com.example.viewmodel.AuthState.Authenticated
            viewModel.preferenceManager.userName = user.userName
            if (user.profilePictureUrl != null) {
                viewModel.preferenceManager.profilePicUrl = user.profilePictureUrl
            }
            viewModel.login(user.userEmail)
        }
    }

    val isDark = viewModel.darkThemeEnabled.collectAsStateWithLifecycle().value
    val mainBgColor = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFFDFBFF)
    val primaryBtnColor = if (isDark) MaterialTheme.colorScheme.primary else Color(0xFF0061A4)
    val secondaryContainerColor = if (isDark) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFD1E4FF)
    val onSecondaryContainerColor = if (isDark) MaterialTheme.colorScheme.onSecondaryContainer else Color(0xFF001D36)

    val savedPicUrl = viewModel.preferenceManager.profilePicUrl
    val savedName = viewModel.preferenceManager.userName.takeIf { it.isNotBlank() } ?: "Gizli Kullanici"

    Scaffold(
        containerColor = mainBgColor,
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(secondaryContainerColor)
                                    .clickable { showProfileMenu = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (authState is com.example.viewmodel.AuthState.Authenticated && (authState as com.example.viewmodel.AuthState.Authenticated).profilePictureUrl != null) {
                                    val name = (authState as com.example.viewmodel.AuthState.Authenticated).userName
                                    AsyncImage(
                                        model = (authState as com.example.viewmodel.AuthState.Authenticated).profilePictureUrl,
                                        contentDescription = "Profil",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else if (savedPicUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = savedPicUrl,
                                        contentDescription = "Profil",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else if (authState is com.example.viewmodel.AuthState.Authenticated) {
                                    val name = (authState as com.example.viewmodel.AuthState.Authenticated).userName
                                    Text(name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = onSecondaryContainerColor)
                                } else if (savedName != "Gizli Kullanici") {
                                    Text(savedName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = onSecondaryContainerColor)
                                } else if (currentUserId.isNotBlank()) {
                                    Text(currentUserId.take(1).uppercase(), fontWeight = FontWeight.Bold, color = onSecondaryContainerColor)
                                } else {
                                    Text("M", fontWeight = FontWeight.Bold, color = onSecondaryContainerColor)
                                }
                            }
                            
                            DropdownMenu(
                                expanded = showProfileMenu,
                                onDismissRequest = { showProfileMenu = false }
                            ) {
                                if (authState is com.example.viewmodel.AuthState.Authenticated) {
                                    val user = authState as com.example.viewmodel.AuthState.Authenticated
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text(user.userName, fontWeight = FontWeight.Bold)
                                                Text(user.userEmail, style = MaterialTheme.typography.bodySmall)
                                            }
                                        },
                                        onClick = { }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Tasarruf Finansmanı") },
                                        onClick = {
                                            selectedTab = 7
                                            showProfileMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Konut Kredisi Hesaplama") },
                                        onClick = {
                                            selectedTab = 8
                                            showProfileMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Ayarlar") },
                                        onClick = {
                                            selectedTab = 6
                                            showProfileMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Çıkış Yap") },
                                        onClick = {
                                            showProfileMenu = false
                                            onSignOut()
                                        },
                                        leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null) }
                                    )
                                } else if (currentUserId.isNotBlank()) {
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text(savedName, fontWeight = FontWeight.Bold)
                                                Text(currentUserId, style = MaterialTheme.typography.bodySmall)
                                            }
                                        },
                                        onClick = { }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Tasarruf Finansmanı") },
                                        onClick = {
                                            selectedTab = 7
                                            showProfileMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Konut Kredisi Hesaplama") },
                                        onClick = {
                                            selectedTab = 8
                                            showProfileMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Ayarlar") },
                                        onClick = {
                                            selectedTab = 6
                                            showProfileMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Çıkış Yap") },
                                        onClick = {
                                            showProfileMenu = false
                                            onSignOut()
                                        },
                                        leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null) }
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = { Text("Tasarruf Finansmanı") },
                                        onClick = {
                                            selectedTab = 7
                                            showProfileMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Konut Kredisi Hesaplama") },
                                        onClick = {
                                            selectedTab = 8
                                            showProfileMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Ayarlar") },
                                        onClick = {
                                            selectedTab = 6
                                            showProfileMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Çıkış Yap") },
                                        onClick = {
                                            showProfileMenu = false
                                            onSignOut()
                                        },
                                        leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null) }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(when(selectedTab) { 0 -> "Bütçem"; 1 -> "Gelirler"; 2 -> "Ödeme Takibi"; 3 -> "Birikimlerim"; 4 -> "Geçmiş"; 5 -> "Analiz"; 7 -> "Tasarruf Finansmanı"; 8 -> "Konut Kredisi"; else -> "Ayarlar" }, fontWeight = FontWeight.Medium, color = onSecondaryContainerColor)
                    }
                },
                actions = {
                    val isDark by viewModel.darkThemeEnabled.collectAsStateWithLifecycle()
                    IconButton(onClick = { viewModel.toggleDarkTheme(!isDark) }) {
                        Icon(
                            imageVector = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = "Tema Değiştir",
                            tint = onSecondaryContainerColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF061226),
                contentColor = Color(0xFF94A3B8),
                tonalElevation = 8.dp
            ) {
                val navColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF061226),
                    selectedTextColor = Color(0xFFD4AF37),
                    indicatorColor = Color(0xFFD4AF37),
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFF94A3B8)
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Anasayfa") },
                    label = { Text("Anasayfa", maxLines=1, fontSize=10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    alwaysShowLabel = true,
                    colors = navColors
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.TrendingUp, contentDescription = "Gelirler") },
                    label = { Text("Gelirler", maxLines=1, fontSize=10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    alwaysShowLabel = true,
                    colors = navColors
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.CreditCard, contentDescription = "Ödemeler") },
                    label = { Text("Ödemeler", maxLines=1, fontSize=10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    alwaysShowLabel = true,
                    colors = navColors
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Birikim") },
                    label = { Text("Birikim", maxLines=1, fontSize=10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    alwaysShowLabel = true,
                    colors = navColors
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.History, contentDescription = "Geçmiş") },
                    label = { Text("Geçmiş", maxLines=1, fontSize=10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    alwaysShowLabel = true,
                    colors = navColors
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Analiz") },
                    label = { Text("Analiz", maxLines=1, fontSize=10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    alwaysShowLabel = true,
                    colors = navColors
                )
            }
        },
        floatingActionButton = {
            if (selectedTab != 4 && selectedTab != 5 && selectedTab != 6) {
                FloatingActionButton(
                    onClick = { 
                        editingTransaction = null
                        showAddDialog = true 
                    },
                    modifier = Modifier.testTag("add_transaction_fab"),
                    containerColor = primaryBtnColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ekle", modifier = Modifier.size(32.dp))
                }
            }
        }
    ) { innerPadding ->
        if (selectedTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    val upcoming = uiState.transactions.filter { it.type == TransactionType.EXPENSE && !it.isPaid && it.timestamp <= Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis }.sortedBy { it.timestamp }.take(5)
                    
                    val currentMonthStart = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    
                    val currentMonthEnd = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis

                    val thisMonthTxs = uiState.transactions.filter { it.timestamp in currentMonthStart..currentMonthEnd }
                    val pastUnpaidTxs = uiState.transactions.filter { it.timestamp < currentMonthStart && it.type == TransactionType.EXPENSE && !it.isPaid }

                    val cmIncome = thisMonthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val cmExpense = thisMonthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount } + pastUnpaidTxs.sumOf { it.amount }
                    val cmSaving = thisMonthTxs.filter { it.type == TransactionType.SAVING }.sumOf { it.amount }
                    val cmBalance = cmIncome - cmExpense - cmSaving
                    
                    item {
                        BudgetSummaryCards(cmBalance, cmIncome, cmExpense, cmSaving, isDark)
                        
                        // ALtin fiyatlari
                        GoldPricesSection(
                            goldPrices = viewModel.goldPrices.collectAsStateWithLifecycle().value,
                            isFetching = viewModel.isFetchingGold.collectAsStateWithLifecycle().value,
                            lastUpdate = viewModel.lastGoldUpdate.collectAsStateWithLifecycle().value,
                            onRefresh = { viewModel.fetchGoldPrices() }
                        )

                        // Banka kurları
                        BankRatesSection(
                            bankRates = viewModel.bankRates.collectAsStateWithLifecycle().value,
                            isFetching = viewModel.isFetchingBankRates.collectAsStateWithLifecycle().value,
                            lastUpdate = viewModel.lastBankRateUpdate.collectAsStateWithLifecycle().value,
                            onRefresh = { viewModel.fetchBankRates() }
                        )

                        // Ziraat kurları
                        ZiraatRatesSection(
                            ziraatRates = viewModel.ziraatRates.collectAsStateWithLifecycle().value,
                            isFetching = viewModel.isFetchingZiraatRates.collectAsStateWithLifecycle().value,
                            lastUpdate = viewModel.lastZiraatRateUpdate.collectAsStateWithLifecycle().value,
                            onRefresh = { viewModel.fetchZiraatRates() }
                        )
                        
                        Text(
                            text = "Yaklaşan Ödemeler",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF001D36),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    items(upcoming) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onDelete = { viewModel.deleteTransaction(transaction.id) },
                            isDark = isDark
                        )
                    }
                    if (upcoming.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Yaklaşan ödeme bulunmuyor.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            Box(
                 modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                IncomesScreen(uiState.transactions)
            }
        } else if (selectedTab == 2) {
            Box(
                 modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                PaymentsScreen(
                    transactions = uiState.transactions,
                    onTogglePaid = { tx, isPaid -> viewModel.toggleTransactionPaid(tx, isPaid) }
                )
            }
        } else if (selectedTab == 3) {
            Box(
                 modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                SavingsScreen(
                    transactions = uiState.transactions,
                    goldPrices = goldPrices,
                    bankRates = bankRates,
                    onAddSaving = { showAddDialog = true },
                    onDeleteSavingTransaction = { viewModel.deleteTransaction(it) },
                    preferenceManager = viewModel.preferenceManager,
                    customPricesTrigger = customPricesTrigger,
                    onUpdateCustomPrice = { category, price ->
                        viewModel.updateCustomPrice(category, price)
                    },
                    ziraatRates = ziraatRates
                )
            }
        } else if (selectedTab == 4) {
            Box(
                 modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                HistoryScreen(
                    transactions = uiState.transactions,
                    onEdit = { tx ->
                        editingTransaction = tx
                        showAddDialog = true
                    },
                    onDelete = { tx ->
                        viewModel.deleteTransaction(tx.id)
                    }
                )
            }
        } else if (selectedTab == 5) {
            Box(
                 modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnalyticsScreen(
                    transactions = uiState.transactions,
                    goldPrices = goldPrices,
                    bankRates = bankRates,
                    preferenceManager = viewModel.preferenceManager,
                    ziraatRates = ziraatRates
                )
            }
        } else if (selectedTab == 6) {
            Box(
                 modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                SettingsScreen(viewModel)
            }
        } else if (selectedTab == 7) {
            Box(
                 modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                SavingsCalculatorScreen()
            }
        } else if (selectedTab == 8) {
            Box(
                 modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                MortgageCalculatorScreen()
            }
        }
    }

    if (showAddDialog) {
        val fixedType = when (selectedTab) {
            1 -> TransactionType.INCOME
            2 -> TransactionType.EXPENSE
            3 -> TransactionType.SAVING
            else -> null
        }
        
        AddTransactionDialog(
            incomeCategories = viewModel.incomeCategories.collectAsStateWithLifecycle().value,
            expenseCategories = viewModel.expenseCategories.collectAsStateWithLifecycle().value,
            savingCategories = viewModel.savingCategories.collectAsStateWithLifecycle().value,
            persons = viewModel.persons.collectAsStateWithLifecycle().value,
            fixedType = fixedType,
            editingTransaction = editingTransaction,
            onDismiss = { showAddDialog = false },
            onSave = { id, amount, title, type, category, person, timestamp, q, up, inst, isPaid, repeatUntilYearEnd ->
                val newTx = Transaction(
                    id = id,
                    amount = amount,
                    title = title,
                    type = type,
                    category = category,
                    person = person,
                    timestamp = timestamp,
                    quantity = q,
                    unitPrice = up,
                    installments = inst,
                    isPaid = isPaid
                )
                viewModel.addTransactionWithInstallments(newTx, repeatUntilYearEnd)
                showAddDialog = false
            }
        )
    }

    if (showAddGoalDialog) {
        AddSavingDialog(
            onDismiss = { showAddGoalDialog = false },
            onSave = { title, target, saved ->
                viewModel.addSaving(
                    com.example.data.SavingGoal(
                        title = title,
                        targetAmount = target,
                        savedAmount = saved
                    )
                )
                showAddGoalDialog = false
            }
        )
    }
}

@Composable
fun BudgetSummaryCards(balance: Double, income: Double, expense: Double, saving: Double, isDark: Boolean = false) {
    val format = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
    
    val cardBg = if (isDark) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFD1E4FF)
    val cardText = if (isDark) MaterialTheme.colorScheme.onSecondaryContainer else Color(0xFF001D36)
    val innerCardBg = if (isDark) Color.Black.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.4f)
    val incomeText = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)
    val expenseText = if (isDark) Color(0xFFF87171) else Color(0xFFB91C1C)
    val labelText = if (isDark) Color.White.copy(alpha=0.7f) else Color(0xFF475569)
    val savingText = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "MEVCUT AY BAKİYESİ",
                    style = MaterialTheme.typography.labelMedium,
                    color = cardText.copy(alpha = 0.7f),
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                val balanceFormat = NumberFormat.getCurrencyInstance(Locale("tr", "TR")).apply { maximumFractionDigits = 0 }
                Text(
                    text = balanceFormat.format(balance),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = cardText
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Gelir Card
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .background(innerCardBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "GELİR",
                                style = LocalTextStyle.current.copy(fontSize=8.sp, fontWeight=FontWeight.Bold, color=labelText)
                            )
                            Text(
                                text = balanceFormat.format(income),
                                style = LocalTextStyle.current.copy(fontSize=14.sp, fontWeight=FontWeight.Bold, color=incomeText)
                            )
                        }
                    }

                    // Gider Card
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .background(innerCardBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "GİDER",
                                style = LocalTextStyle.current.copy(fontSize=8.sp, fontWeight=FontWeight.Bold, color=labelText)
                            )
                            Text(
                                text = balanceFormat.format(expense),
                                style = LocalTextStyle.current.copy(fontSize=14.sp, fontWeight=FontWeight.Bold, color=expenseText)
                            )
                        }
                    }
                    
                    // Birikim Card
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .background(innerCardBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "BİRİKİM",
                                style = LocalTextStyle.current.copy(fontSize=8.sp, fontWeight=FontWeight.Bold, color=labelText)
                            )
                            Text(
                                text = balanceFormat.format(saving),
                                style = LocalTextStyle.current.copy(fontSize=14.sp, fontWeight=FontWeight.Bold, color=savingText)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, onDelete: () -> Unit, isDark: Boolean = false) {
    val format = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("tr", "TR"))
    
    val amountColor = when (transaction.type) {
        TransactionType.INCOME -> if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A)
        TransactionType.EXPENSE -> if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
        TransactionType.SAVING -> if (isDark) Color(0xFFFB923C) else Color(0xFFF97316)
    }
    
    val prefix = when (transaction.type) {
        TransactionType.INCOME -> "+"
        TransactionType.EXPENSE -> "-"
        TransactionType.SAVING -> "-"
    }

    val iconColor = when (transaction.type) {
        TransactionType.INCOME -> if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
        TransactionType.EXPENSE -> if (isDark) Color(0xFFFB923C) else Color(0xFFEA580C)
        TransactionType.SAVING -> if (isDark) Color(0xFFC084FC) else Color(0xFF9333EA)
    }
    
    val iconBg = when (transaction.type) {
        TransactionType.INCOME -> if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE)
        TransactionType.EXPENSE -> if (isDark) Color(0xFF7C2D12) else Color(0xFFFFEDD5)
        TransactionType.SAVING -> if (isDark) Color(0xFF581C87) else Color(0xFFF3E8FF)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.6f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)) // border-slate-700 or slate-100
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.category.firstOrNull()?.toString()?.uppercase() ?: "D",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sdf.format(Date(transaction.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                val subtitle = buildString {
                    append(transaction.title)
                    if (transaction.person != null) {
                        append(" • ${transaction.person}")
                    }
                    if (transaction.installments != null) {
                        append(" (${transaction.installments} Taksit)")
                    }
                    if (transaction.quantity != null && transaction.unitPrice != null && transaction.type == TransactionType.SAVING) {
                        append(" • ${transaction.quantity} x ${format.format(transaction.unitPrice)}")
                    }
                }
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = prefix + format.format(transaction.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = amountColor
                )
            }
            
            IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_btn")) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = Color(0xFFCBD5E1)
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun GoldPricesSection(goldPrices: List<com.example.data.GoldPrice>, isFetching: Boolean, lastUpdate: String?, onRefresh: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(6.dp).fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = Color(0xFFD4AF37))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Sivas Altın Fiyatları", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text("CANLI PİYASA VERİLERİ", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (lastUpdate != null && !isFetching) {
                        Text("SON GÜNCELLEME: $lastUpdate", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
                        if (isFetching) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Yenile", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            
            if (goldPrices.isEmpty() && !isFetching) {
                Text("Veriler şu an alınamıyor, lütfen Yenile butonuna basın.", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    goldPrices.chunked(2).forEach { rowPrices ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            rowPrices.forEach { gp ->
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(modifier = Modifier.padding(2.dp)) {
                                        Text(gp.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("ALIŞ", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(gp.buy, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444))
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("SATIŞ", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(gp.sell, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                                            }
                                        }
                                    }
                                }
                            }
                            if (rowPrices.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun BankRatesSection(bankRates: List<com.example.data.BankRate>, isFetching: Boolean, lastUpdate: String?, onRefresh: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(6.dp).fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Yapı Kredi Altın ve Döviz", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text("BANKA GİŞE FİYATLARI", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (lastUpdate != null && !isFetching) {
                        Text("SON GÜNCELLEME: $lastUpdate", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
                        if (isFetching) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Yenile", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            
            if (bankRates.isEmpty() && !isFetching) {
                Text("Veriler şu an alınamıyor, lütfen Yenile butonuna basın.", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    bankRates.chunked(2).forEach { rowRates ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            rowRates.forEach { rate ->
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(modifier = Modifier.padding(2.dp)) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                                            Text(rate.code, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                            Text(rate.name, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("ALIŞ", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(rate.buy, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444))
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("SATIŞ", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(rate.sell, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                                            }
                                        }
                                    }
                                }
                            }
                            if (rowRates.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ZiraatRatesSection(ziraatRates: List<com.example.data.BankRate>, isFetching: Boolean, lastUpdate: String?, onRefresh: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(6.dp).fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEC)), // light red background
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null, 
                                tint = Color(0xFFE10514), // Ziraat Red
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Ziraat Bankası Altın ve Döviz", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text("ZİRAAT GİŞE FİYATLARI", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (lastUpdate != null && !isFetching) {
                        Text("SON GÜNCELLEME: $lastUpdate", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
                        if (isFetching) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color(0xFFE10514))
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Yenile", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            
            if (ziraatRates.isEmpty() && !isFetching) {
                Text("Veriler şu an alınamıyor, lütfen Yenile butonuna basın.", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ziraatRates.chunked(2).forEach { rowRates ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            rowRates.forEach { rate ->
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(modifier = Modifier.padding(2.dp)) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                                            Text(rate.code, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                            Text(rate.name, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("ALIŞ", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(rate.buy, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444))
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("SATIŞ", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(rate.sell, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                                            }
                                        }
                                    }
                                }
                            }
                            if (rowRates.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
