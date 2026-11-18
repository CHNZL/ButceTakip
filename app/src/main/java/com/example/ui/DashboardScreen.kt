package com.example.ui

import com.example.updater.AppUpdater
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccountCircle

import android.widget.Toast
import kotlinx.coroutines.launch
import com.example.BuildConfig
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.LocalAppThemeColors
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
    val isFetchingGold by viewModel.isFetchingGold.collectAsStateWithLifecycle()
    val lastGoldUpdate by viewModel.lastGoldUpdate.collectAsStateWithLifecycle()
    val isFetchingBank by viewModel.isFetchingBankRates.collectAsStateWithLifecycle()
    val lastBankUpdate by viewModel.lastBankRateUpdate.collectAsStateWithLifecycle()
    val isFetchingZiraat by viewModel.isFetchingZiraatRates.collectAsStateWithLifecycle()
    val lastZiraatUpdate by viewModel.lastZiraatRateUpdate.collectAsStateWithLifecycle()
    val customPricesTrigger by viewModel.customPricesRefreshTrigger.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var initialSavingCategory by remember { mutableStateOf<String?>(null) }
    var initialQuickType by remember { mutableStateOf<TransactionType?>(null) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showProfileMenu by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }
    val appThemeColors = LocalAppThemeColors.current

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

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appUpdater = remember { AppUpdater(context) }
    var isCheckingUpdate by remember { mutableStateOf(false) }

    val isDark = viewModel.darkThemeEnabled.collectAsStateWithLifecycle().value
    val mainBgColor = MaterialTheme.colorScheme.background
    val primaryBtnColor = MaterialTheme.colorScheme.primary
    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer
    val onSecondaryContainerColor = MaterialTheme.colorScheme.onSecondaryContainer

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
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Uygulama Bilgisi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                                Text("Sürüm: ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        isCheckingUpdate = true
                                                        val updateInfo = appUpdater.checkForUpdate()
                                                        isCheckingUpdate = false
                                                        if (updateInfo != null) {
                                                            Toast.makeText(context, "Yeni sürüm (${updateInfo.versionName}) bulunuyor, indiriliyor...", Toast.LENGTH_LONG).show()
                                                            appUpdater.downloadAndInstallUpdate(updateInfo)
                                                        } else {
                                                            Toast.makeText(context, "Uygulamanız güncel.", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                enabled = !isCheckingUpdate,
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                if (isCheckingUpdate) {
                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                                } else {
                                                    Text("Güncelle", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        HorizontalDivider()
                                        Spacer(modifier = Modifier.height(12.dp))
                                        if (authState is com.example.viewmodel.AuthState.Authenticated) {
                                            val user = authState as com.example.viewmodel.AuthState.Authenticated
                                            Text(user.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            Text(user.userEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        } else if (currentUserId.isNotBlank()) {
                                            Text(savedName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            Text(currentUserId, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        } else {
                                            Text(savedName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        }
                                    }
                                }
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
                                if (authState is com.example.viewmodel.AuthState.Authenticated || currentUserId.isNotBlank()) {
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
                                        text = { Text("Giriş Yap") },
                                        onClick = {
                                            showProfileMenu = false
                                            onSignOut()
                                        },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            val greetingName = if (savedName != "Gizli Kullanici" && savedName.isNotBlank()) savedName.split(" ").first() else "Cihan"
                            Text(
                                text = if (selectedTab == 0) "Özet" else when(selectedTab) { 1 -> "Gelirler"; 2 -> "Ödeme Takibi"; 3 -> "Birikim & Portföy"; 4 -> "Geçmiş"; 5 -> "Gelir & Gider"; 7 -> "Hesaplayıcı"; 8 -> "Konut Kredisi"; else -> "Ayarlar" },
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (selectedTab == 0) {
                                val dateFormat = remember { SimpleDateFormat("d MMMM EEEE", Locale("tr", "TR")) }
                                Text(
                                    text = "$greetingName • ${dateFormat.format(Date())}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                },
                actions = {
                    val isDarkMode by viewModel.darkThemeEnabled.collectAsStateWithLifecycle()
                    IconButton(
                        onClick = { viewModel.toggleDarkTheme(!isDarkMode) },
                        modifier = Modifier.testTag("theme_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = if (isDarkMode) "Açık Temaya Geç" else "Koyu Temaya Geç",
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
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                color = appThemeColors.navBarContainer,
                border = BorderStroke(1.dp, if (isDark) Color(0xFF1E283E).copy(alpha = 0.7f) else Color(0xFFE2E8F0))
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    contentColor = appThemeColors.navBarUnselected,
                    tonalElevation = 0.dp
                ) {
                    val navColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = appThemeColors.navBarSelectedText,
                        selectedTextColor = if (isDark) appThemeColors.accentColor else MaterialTheme.colorScheme.primary,
                        indicatorColor = appThemeColors.navBarIndicator,
                        unselectedIconColor = appThemeColors.navBarUnselected,
                        unselectedTextColor = appThemeColors.navBarUnselected
                    )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Anasayfa") },
                    label = { Text("Anasayfa", maxLines = 1, softWrap = false, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    alwaysShowLabel = true,
                    colors = navColors
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.TrendingUp, contentDescription = "Gelirler") },
                    label = { Text("Gelirler", maxLines = 1, softWrap = false, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    alwaysShowLabel = true,
                    colors = navColors
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.CreditCard, contentDescription = "Ödemeler") },
                    label = { Text("Ödemeler", maxLines = 1, softWrap = false, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    alwaysShowLabel = true,
                    colors = navColors
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Birikim") },
                    label = { Text("Birikim", maxLines = 1, softWrap = false, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold) },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    alwaysShowLabel = true,
                    colors = navColors
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.History, contentDescription = "Geçmiş") },
                    label = { Text("Geçmiş", maxLines = 1, softWrap = false, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold) },
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    alwaysShowLabel = true,
                    colors = navColors
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Analiz") },
                    label = { Text("Analiz", maxLines = 1, softWrap = false, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold) },
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    alwaysShowLabel = true,
                    colors = navColors
                )
            }
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
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ekle", modifier = Modifier.size(32.dp))
                }
            }
        }
    ) { innerPadding ->
        if (selectedTab == 0) {
            var isBalanceHidden by remember { mutableStateOf(false) }

            val monthTitleSdf = remember { SimpleDateFormat("MMMM yyyy", Locale("tr", "TR")) }
            val currentMonthLabel = remember { monthTitleSdf.format(Date()).uppercase(Locale("tr", "TR")) }

            val timeAnchors = remember {
                val calStart = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startMs = calStart.timeInMillis

                val maxDay = calStart.getActualMaximum(Calendar.DAY_OF_MONTH)
                val todayCal = Calendar.getInstance()
                val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)
                val remDays = (maxDay - todayDay + 1).coerceAtLeast(1)

                todayCal.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val todayStartMs = todayCal.timeInMillis

                val calEnd = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val endMs = calEnd.timeInMillis
                listOf(startMs, todayStartMs, endMs, remDays.toLong())
            }
            val currentMonthStart = timeAnchors[0]
            val todayStart = timeAnchors[1]
            val currentMonthEnd = timeAnchors[2]
            val remainingDaysInMonth = timeAnchors[3].toInt()

            val thisMonthTxs = remember(uiState.transactions, currentMonthStart, currentMonthEnd) {
                uiState.transactions.filter { it.timestamp in currentMonthStart..currentMonthEnd }
            }
            val pastUnpaidTxs = remember(uiState.transactions, currentMonthStart) {
                uiState.transactions.filter {
                    it.timestamp < currentMonthStart && it.type == TransactionType.EXPENSE && !it.isPaid
                }
            }
            val overdueExpenses = remember(uiState.transactions, todayStart) {
                uiState.transactions.filter {
                    it.type == TransactionType.EXPENSE && !it.isPaid && it.timestamp < todayStart
                }
            }
            val upcomingPendingThisMonth = remember(uiState.transactions, todayStart, currentMonthEnd) {
                uiState.transactions.filter {
                    it.type == TransactionType.EXPENSE && !it.isPaid && it.timestamp in todayStart..currentMonthEnd
                }
            }
            val upcoming = remember(uiState.transactions, currentMonthEnd) {
                uiState.transactions
                    .filter { it.type == TransactionType.EXPENSE && !it.isPaid && it.timestamp <= currentMonthEnd }
                    .sortedBy { it.timestamp }
                    .take(4)
            }
            val recentActivity = remember(uiState.transactions) {
                uiState.transactions.sortedByDescending { it.timestamp }.take(3)
            }

            val cmIncome = remember(thisMonthTxs) {
                thisMonthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            }
            val cmExpense = remember(thisMonthTxs, pastUnpaidTxs) {
                thisMonthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount } +
                    pastUnpaidTxs.sumOf { it.amount }
            }
            val cmSaving = remember(thisMonthTxs) {
                thisMonthTxs.filter { it.type == TransactionType.SAVING }.sumOf { it.amount }
            }
            val cmBalance = cmIncome - cmExpense - cmSaving

            val mSdf = remember { SimpleDateFormat("MMM", Locale("tr", "TR")) }
            val (recentLabels, recentIncomes, recentExpenses) = remember(uiState.transactions) {
                val lbls = mutableListOf<String>()
                val incs = mutableListOf<Double>()
                val exps = mutableListOf<Double>()
                for (i in 5 downTo 0) {
                    val c = Calendar.getInstance().apply { add(Calendar.MONTH, -i) }
                    val st = c.apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val en = c.apply {
                        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis
                    val txs = uiState.transactions.filter { it.timestamp in st..en }
                    lbls.add(mSdf.format(Date(st)))
                    incs.add(txs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount })
                    exps.add(txs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount })
                }
                Triple(lbls, incs, exps)
            }

            val (portfolioValue, portfolioCost, besTotal) = remember(
                uiState.transactions,
                uiState.besPortfolios,
                goldPrices,
                bankRates,
                ziraatRates,
                customPricesTrigger
            ) {
                val savingTxs = uiState.transactions.filter { it.type == TransactionType.SAVING }
                val besSum = uiState.besPortfolios.sumOf {
                    it.investment + it.investmentReturn + it.stateContribution + it.stateContributionReturn
                }
                val totalCost = savingTxs.sumOf { it.amount }
                val groupedVal = savingTxs.groupBy { it.category }.entries.sumOf { (cat, list) ->
                    val qty = list.sumOf { it.quantity ?: 0.0 }
                    val livePrice = resolveCurrentUnitPrice(
                        category = cat,
                        goldPrices = goldPrices,
                        bankRates = bankRates,
                        preferenceManager = viewModel.preferenceManager,
                        ziraatRates = ziraatRates
                    )
                    if (livePrice != null && qty > 0.0) qty * livePrice else list.sumOf { it.amount }
                }
                Triple(groupedVal, totalCost, besSum)
            }
            val investRatio = if (cmIncome > 0) ((cmSaving / cmIncome) * 100).toInt().coerceIn(0, 100) else 0

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(top = 6.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Flagship Executive Net Budget Hero
                item {
                    HomeHeroCommandCard(
                        monthLabel = currentMonthLabel,
                        balance = cmBalance,
                        income = cmIncome,
                        expense = cmExpense,
                        saving = cmSaving,
                        isBalanceHidden = isBalanceHidden,
                        onToggleVisibility = { isBalanceHidden = !isBalanceHidden },
                        onOpenIncomes = { selectedTab = 1 },
                        onOpenPayments = { selectedTab = 2 },
                        onOpenSavings = { selectedTab = 3 },
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }

                // 2. Quick Command Dock
                item {
                    HomeQuickActionDock(
                        onAddIncome = {
                            editingTransaction = null
                            initialQuickType = TransactionType.INCOME
                            showAddDialog = true
                        },
                        onAddExpense = {
                            editingTransaction = null
                            initialQuickType = TransactionType.EXPENSE
                            showAddDialog = true
                        },
                        onAddSaving = {
                            editingTransaction = null
                            initialQuickType = TransactionType.SAVING
                            showAddDialog = true
                        },
                        onOpenAnalytics = { selectedTab = 5 },
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }

                // 3. Daily Budget Limit & Payment Radar
                item {
                    HomeDailyRadarRow(
                        remainingBalance = cmBalance,
                        remainingDaysInMonth = remainingDaysInMonth,
                        overdueCount = overdueExpenses.size,
                        overdueTotal = overdueExpenses.sumOf { it.amount },
                        pendingThisMonthCount = upcomingPendingThisMonth.size,
                        pendingThisMonthTotal = upcomingPendingThisMonth.sumOf { it.amount },
                        isBalanceHidden = isBalanceHidden,
                        onOpenPayments = { selectedTab = 2 },
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }

                // 4. Urgent & Upcoming Payments Section
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Yaklaşan Ödemeler",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (upcoming.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = if (isDark) Color(0xFF450A0A) else Color(0xFFFEE2E2),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${upcoming.size} Bekliyor",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        softWrap = false,
                                        maxLines = 1,
                                        color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        TextButton(
                            onClick = { selectedTab = 2 },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Tümünü Gör", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (upcoming.isNotEmpty()) {
                    items(upcoming, key = { "up_${it.id}" }) { transaction ->
                        ModernTransactionItem(
                            transaction = transaction,
                            onDelete = { viewModel.deleteTransaction(transaction.id) },
                            onTogglePaid = { isPaid -> viewModel.toggleTransactionPaid(transaction, isPaid) },
                            isDark = isDark
                        )
                    }
                } else {
                    item {
                        CyberCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF064E3B) else Color(0xFFD1FAE5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Bekleyen ödeme yok",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Bu ay için tüm faturalarınız güncel.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Tasarruf Oranı & Canlı Portföy
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CyberInvestmentGaugeCard(
                            percentage = investRatio,
                            title = "Tasarruf Oranı",
                            subtitle = if (cmIncome > 0) "Bu ayki gelirden" else "Gelir bekleniyor",
                            modifier = Modifier.weight(1f)
                        )
                        HomePortfolioSpotlightCard(
                            portfolioValue = portfolioValue,
                            portfolioCost = portfolioCost,
                            besTotal = besTotal,
                            isBalanceHidden = isBalanceHidden,
                            onOpenSavings = { selectedTab = 3 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 6. 6 Aylık Gelir & Gider Dengesi
                item {
                    CyberIncomeExpenseBalanceCard(
                        incomes = recentIncomes,
                        expenses = recentExpenses,
                        labels = recentLabels,
                        title = "Son 6 Ay Nakit Akışı",
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }

                // 7. Compact Collapsible Live Market Card
                item {
                    HomeCompactMarketCard(
                        goldPrices = goldPrices,
                        isFetchingGold = isFetchingGold,
                        lastGoldUpdate = lastGoldUpdate,
                        onRefreshGold = { viewModel.fetchGoldPrices() },
                        bankRates = bankRates,
                        isFetchingBank = isFetchingBank,
                        lastBankUpdate = lastBankUpdate,
                        onRefreshBank = { viewModel.fetchBankRates() },
                        ziraatRates = ziraatRates,
                        isFetchingZiraat = isFetchingZiraat,
                        lastZiraatUpdate = lastZiraatUpdate,
                        onRefreshZiraat = { viewModel.fetchZiraatRates() },
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }

                // 8. Son İşlemler
                if (recentActivity.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Son Hareketler",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = { selectedTab = 4 },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Tüm Geçmiş", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    items(recentActivity, key = { "rec_${it.id}" }) { transaction ->
                        ModernTransactionItem(
                            transaction = transaction,
                            onDelete = { viewModel.deleteTransaction(transaction.id) },
                            onTogglePaid = if (transaction.type == TransactionType.EXPENSE) {
                                { isPaid -> viewModel.toggleTransactionPaid(transaction, isPaid) }
                            } else null,
                            isDark = isDark
                        )
                    }
                }
            }
        } else if (selectedTab == 1) {
            Box(
                 modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                IncomesScreen(
                    transactions = uiState.transactions,
                    onAddIncome = {
                        editingTransaction = null
                        initialQuickType = TransactionType.INCOME
                        showAddDialog = true
                    },
                    onEditIncome = { tx ->
                        editingTransaction = tx
                        showAddDialog = true
                    },
                    onDeleteIncome = { tx ->
                        viewModel.deleteTransaction(tx.id)
                    },
                    onRepeatIncome = { tx ->
                        val repeated = tx.copy(
                            id = 0,
                            timestamp = System.currentTimeMillis(),
                            isPaid = true,
                            installments = 1
                        )
                        viewModel.addTransactionWithInstallments(repeated, repeatUntilYearEnd = false)
                        Toast.makeText(context, "\"${tx.title}\" bugüne tekrarlandı", Toast.LENGTH_SHORT).show()
                    }
                )
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
                    onAddSaving = { cat ->
                        initialSavingCategory = cat
                        showAddDialog = true
                    },
                    onDeleteSavingTransaction = { viewModel.deleteTransaction(it) },
                    preferenceManager = viewModel.preferenceManager,
                    customPricesTrigger = customPricesTrigger,
                    onUpdateCustomPrice = { category, price ->
                        viewModel.updateCustomPrice(category, price)
                    },
                    ziraatRates = ziraatRates,
                    besPortfolios = uiState.besPortfolios,
                    onUpdateBes = { viewModel.updateBesPortfolio(it) },
                    onDeleteBes = { viewModel.deleteBesPortfolio(it) }
                )
            }
        } else if (selectedTab == 4) {
            Box(
                 modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                HistoryScreen(
                    isDark = isDark,
                    transactions = uiState.transactions,
                    onEdit = { tx ->
                        editingTransaction = tx
                        showAddDialog = true
                    },
                    onDelete = { tx ->
                        viewModel.deleteTransaction(tx.id)
                    },
                    onTogglePaid = { tx, isPaid ->
                        viewModel.toggleTransactionPaid(tx, isPaid)
                    },
                    onRepeat = { tx ->
                        val repeated = tx.copy(
                            id = 0,
                            timestamp = System.currentTimeMillis(),
                            isPaid = tx.type != TransactionType.EXPENSE,
                            installments = 1
                        )
                        viewModel.addTransactionWithInstallments(repeated, repeatUntilYearEnd = false)
                        Toast.makeText(context, "\"${tx.title}\" bugüne tekrarlandı", Toast.LENGTH_SHORT).show()
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
            else -> initialQuickType
        }
        
        AddTransactionDialog(
            incomeCategories = viewModel.incomeCategories.collectAsStateWithLifecycle().value,
            expenseCategories = viewModel.expenseCategories.collectAsStateWithLifecycle().value,
            savingCategories = viewModel.savingCategories.collectAsStateWithLifecycle().value,
            persons = viewModel.persons.collectAsStateWithLifecycle().value,
            fixedType = fixedType,
            editingTransaction = editingTransaction,
            initialCategoryName = initialSavingCategory,
            onDismiss = {
                showAddDialog = false
                initialSavingCategory = null
                initialQuickType = null
                editingTransaction = null
            },
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
                initialSavingCategory = null
                initialQuickType = null
                editingTransaction = null
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
