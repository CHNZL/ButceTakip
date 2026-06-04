package com.example.ui

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
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Logout
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
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showProfileMenu by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is com.example.viewmodel.AuthState.Authenticated) {
            val user = authState as com.example.viewmodel.AuthState.Authenticated
            viewModel.login(user.userEmail)
        }
    }

    val mainBgColor = Color(0xFFFDFBFF)
    val primaryBtnColor = Color(0xFF0061A4)
    val secondaryContainerColor = Color(0xFFD1E4FF)
    val onSecondaryContainerColor = Color(0xFF001D36)

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
                                    AsyncImage(
                                        model = (authState as com.example.viewmodel.AuthState.Authenticated).profilePictureUrl,
                                        contentDescription = "Profil",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else if (authState is com.example.viewmodel.AuthState.Authenticated) {
                                    val name = (authState as com.example.viewmodel.AuthState.Authenticated).userName
                                    Text(name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = onSecondaryContainerColor)
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
                                        text = { Text("Çıkış Yap") },
                                        onClick = {
                                            showProfileMenu = false
                                            onSignOut()
                                        },
                                        leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null) }
                                    )
                                } else {
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
                        Text(if (selectedTab == 0) "Bütçem" else if (selectedTab == 1) "Birikimlerim" else "Ayarlar", fontWeight = FontWeight.Medium, color = onSecondaryContainerColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF3F4F9),
                contentColor = Color(0xFF475569)
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Ana Sayfa") },
                    label = { Text("Ana Sayfa") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = onSecondaryContainerColor,
                        selectedTextColor = onSecondaryContainerColor,
                        indicatorColor = secondaryContainerColor
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Savings, contentDescription = "Birikim") },
                    label = { Text("Birikim") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = onSecondaryContainerColor,
                        selectedTextColor = onSecondaryContainerColor,
                        indicatorColor = secondaryContainerColor
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ayarlar") },
                    label = { Text("Ayarlar") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = onSecondaryContainerColor,
                        selectedTextColor = onSecondaryContainerColor,
                        indicatorColor = secondaryContainerColor
                    )
                )
            }
        },
        floatingActionButton = {
            if (selectedTab != 2) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
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
                    item {
                        BudgetSummaryCards(uiState.totalBalance, uiState.totalIncome, uiState.totalExpense)
                        Text(
                            text = "Son İşlemler",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF001D36),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(uiState.transactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onDelete = { viewModel.deleteTransaction(transaction.id) }
                        )
                    }
                    if (uiState.transactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Henüz işlem bulunmuyor.",
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
                SavingsScreen(uiState.savings, onDelete = { viewModel.deleteSaving(it) })
            }
        } else if (selectedTab == 2) {
            Box(
                 modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                SettingsScreen(viewModel)
            }
        }
    }

    if (showAddDialog) {
        if (selectedTab == 0) {
            AddTransactionDialog(
                incomeCategories = viewModel.incomeCategories.collectAsStateWithLifecycle().value,
                expenseCategories = viewModel.expenseCategories.collectAsStateWithLifecycle().value,
                savingCategories = viewModel.savingCategories.collectAsStateWithLifecycle().value,
                persons = viewModel.persons.collectAsStateWithLifecycle().value,
                onDismiss = { showAddDialog = false },
                onSave = { amount, title, type, category, person, timestamp, q, up, inst ->
                    viewModel.addTransaction(
                        Transaction(
                            amount = amount,
                            title = title,
                            type = type,
                            category = category,
                            person = person,
                            timestamp = timestamp,
                            quantity = q,
                            unitPrice = up,
                            installments = inst
                        )
                    )
                    showAddDialog = false
                }
            )
        } else {
             AddSavingDialog(
                onDismiss = { showAddDialog = false },
                onSave = { title, target, saved ->
                    viewModel.addSaving(
                        com.example.data.SavingGoal(
                            title = title,
                            targetAmount = target,
                            savedAmount = saved
                        )
                    )
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun BudgetSummaryCards(balance: Double, income: Double, expense: Double) {
    val format = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
    
    val cardBg = Color(0xFFD1E4FF)
    val cardText = Color(0xFF001D36)
    val innerCardBg = Color.White.copy(alpha = 0.4f)
    val incomeText = Color(0xFF15803D)
    val incomeIconBg = Color(0xFF22C55E).copy(alpha = 0.2f)
    val expenseText = Color(0xFFB91C1C)
    val expenseIconBg = Color(0xFFEF4444).copy(alpha = 0.2f)

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
                    text = "TOPLAM BAKIYE",
                    style = MaterialTheme.typography.labelMedium,
                    color = cardText.copy(alpha = 0.7f),
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = format.format(balance),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = cardText
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Gelir Card
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .background(innerCardBg, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(incomeIconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Gelir",
                                tint = incomeText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "GELİR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569) // slate-600
                            )
                            Text(
                                text = format.format(income),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = incomeText
                            )
                        }
                    }

                    // Gider Card
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .background(innerCardBg, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(expenseIconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Gider",
                                tint = expenseText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "GİDER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                            Text(
                                text = format.format(expense),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = expenseText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, onDelete: () -> Unit) {
    val format = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("tr", "TR"))
    
    val amountColor = when (transaction.type) {
        TransactionType.INCOME -> Color(0xFF16A34A)
        TransactionType.EXPENSE -> Color(0xFFDC2626)
        TransactionType.SAVING -> Color(0xFFF97316)
    }
    
    val prefix = when (transaction.type) {
        TransactionType.INCOME -> "+"
        TransactionType.EXPENSE -> "-"
        TransactionType.SAVING -> "-"
    }

    val iconColor = when (transaction.type) {
        TransactionType.INCOME -> Color(0xFF2563EB)
        TransactionType.EXPENSE -> Color(0xFFEA580C)
        TransactionType.SAVING -> Color(0xFF9333EA)
    }
    
    val iconBg = when (transaction.type) {
        TransactionType.INCOME -> Color(0xFFDBEAFE)
        TransactionType.EXPENSE -> Color(0xFFFFEDD5)
        TransactionType.SAVING -> Color(0xFFF3E8FF)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)) // border-slate-100
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
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F172A) // slate-900
                )
                
                val subtitle = buildString {
                    append(transaction.category)
                    if (transaction.person != null) {
                        append(" • ${transaction.person}")
                    }
                    if (transaction.installments != null) {
                        append(" • ${transaction.installments} Taksit")
                    }
                    if (transaction.quantity != null && transaction.unitPrice != null && transaction.type == TransactionType.SAVING) {
                        append(" • ${transaction.quantity} x ${format.format(transaction.unitPrice)}")
                    }
                    append(" • ${sdf.format(Date(transaction.timestamp))}")
                }
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8), // slate-400
                    fontWeight = FontWeight.Medium
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = prefix + format.format(transaction.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_btn"),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Sil",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}
