package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppCategory
import com.example.data.MonthlyExpectedPaymentRule
import com.example.data.MonthlyPaymentEvaluator
import com.example.data.MonthlyRuleChecklistItem
import com.example.data.Person
import com.example.data.Transaction
import com.example.data.TransactionType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PaymentsScreen(
    transactions: List<Transaction>,
    onTogglePaid: (Transaction, Boolean) -> Unit = { _, _ -> },
    onAddExpense: (() -> Unit)? = null,
    onEditExpense: ((Transaction) -> Unit)? = null,
    onDeleteExpense: ((Transaction) -> Unit)? = null,
    monthlyRules: List<MonthlyExpectedPaymentRule> = emptyList(),
    expenseCategories: List<AppCategory> = emptyList(),
    persons: List<Person> = emptyList(),
    remindMissingEnabled: Boolean = true,
    onQuickEnterMissing: ((MonthlyRuleChecklistItem) -> Unit)? = null,
    onToggleMasterMissingNotif: ((Boolean) -> Unit)? = null,
    onToggleMonthlyRule: ((String, Boolean, Double?, String?) -> Unit)? = null,
    onSaveMonthlyRule: ((MonthlyExpectedPaymentRule) -> Unit)? = null,
    onDeleteMonthlyRule: ((String) -> Unit)? = null,
    onSkipMonthlyForMonth: ((String, Boolean) -> Unit)? = null,
    onSendTestMissingNotif: (((Boolean, String) -> Unit) -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)

    val currentMonthSdf = remember { SimpleDateFormat("MMMM yyyy", Locale("tr", "TR")) }
    val currentMonthName = remember { currentMonthSdf.format(Date()).uppercase(Locale("tr", "TR")) }

    val timeAnchors = remember {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val end = (cal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }.timeInMillis
        val todayMs = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        Triple(start, end, todayMs)
    }
    val startOfMonth = timeAnchors.first
    val endOfMonth = timeAnchors.second
    val today = timeAnchors.third

    val expenseTx = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE }
    }

    val pastDueTx = remember(expenseTx, startOfMonth) {
        expenseTx.filter { !it.isPaid && it.timestamp < startOfMonth }.sortedBy { it.timestamp }
    }
    val thisMonthUnpaidTx = remember(expenseTx, startOfMonth, endOfMonth) {
        expenseTx.filter { !it.isPaid && it.timestamp in startOfMonth until endOfMonth }.sortedBy { it.timestamp }
    }
    val futureUnpaidTx = remember(expenseTx, endOfMonth) {
        expenseTx.filter { !it.isPaid && it.timestamp >= endOfMonth }.sortedBy { it.timestamp }
    }
    val thisMonthPaidTx = remember(expenseTx, startOfMonth, endOfMonth) {
        expenseTx.filter { it.isPaid && it.timestamp in startOfMonth until endOfMonth }.sortedByDescending { it.timestamp }
    }

    val totalDebt = remember(thisMonthUnpaidTx) { thisMonthUnpaidTx.sumOf { it.amount } }
    val totalPaid = remember(thisMonthPaidTx) { thisMonthPaidTx.sumOf { it.amount } }
    val pastDueRemaining = remember(pastDueTx) { pastDueTx.sumOf { it.amount } }
    val totalCombinedMonth = totalDebt + totalPaid

    val progressPct = if (totalCombinedMonth > 0) {
        ((totalPaid / totalCombinedMonth) * 100).toInt().coerceIn(0, 100)
    } else if (totalDebt == 0.0 && totalPaid > 0.0) {
        100
    } else {
        0
    }
    val progressFloat = if (totalCombinedMonth > 0) {
        (totalPaid / totalCombinedMonth).toFloat().coerceIn(0f, 1f)
    } else 0f

    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }
    val sdf = remember { SimpleDateFormat("dd MMM", Locale("tr", "TR")) }
    val groupSdf = remember { SimpleDateFormat("MMMM yyyy", Locale("tr", "TR")) }

    var selectedFilter by remember { mutableStateOf("Tümü") }
    var isHidden by remember { mutableStateOf(false) }
    var pendingDeleteTx by remember { mutableStateOf<Transaction?>(null) }
    var showMonthlyRulesSheet by remember { mutableStateOf(false) }

    val missingMonthlyItems = remember(monthlyRules, transactions) {
        MonthlyPaymentEvaluator.getMissingDueNow(
            rules = monthlyRules,
            transactions = transactions
        )
    }
    val trackedRulesCount = remember(monthlyRules) {
        monthlyRules.count { it.isEnabled }
    }

    val filterOptions = remember(
        thisMonthUnpaidTx.size,
        thisMonthPaidTx.size,
        pastDueTx.size,
        futureUnpaidTx.size
    ) {
        buildList {
            add("Tümü" to (thisMonthUnpaidTx.size + thisMonthPaidTx.size + pastDueTx.size))
            add("Bekleyenler" to thisMonthUnpaidTx.size)
            add("Ödenenler" to thisMonthPaidTx.size)
            if (pastDueTx.isNotEmpty()) add("Gecikenler" to pastDueTx.size)
            if (futureUnpaidTx.isNotEmpty()) add("Gelecek" to futureUnpaidTx.size)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(horizontal = 14.dp)
                .testTag("payments_screen_list"),
            contentPadding = PaddingValues(top = 6.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. EXECUTIVE PAYMENT COCKPIT CARD
            item(key = "payments_hero") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, borderColor),
                    shadowElevation = if (isDark) 0.dp else 1.5.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = if (isDark) {
                                        listOf(Color(0xFF141B2B), MaterialTheme.colorScheme.surface)
                                    } else {
                                        listOf(
                                            (if (totalDebt == 0.0 && pastDueRemaining == 0.0) mint else coral).copy(alpha = 0.05f),
                                            Color.White
                                        )
                                    }
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "ÖDEME PLANI · $currentMonthName",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 0.8.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(
                                    onClick = { isHidden = !isHidden },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("payments_visibility_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (isHidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = "Tutarları Gizle veya Göster",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            val badgeAccent = if (progressPct >= 100 || (totalDebt == 0.0 && pastDueRemaining == 0.0)) mint else amber
                            Surface(
                                shape = RoundedCornerShape(9.dp),
                                color = badgeAccent.copy(alpha = if (isDark) 0.16f else 0.11f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (progressPct >= 100) Icons.Rounded.CheckCircle else Icons.Rounded.DonutLarge,
                                        contentDescription = null,
                                        tint = badgeAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "%$progressPct Ödendi",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontFeatureSettings = "tnum"
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = badgeAccent
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Bu Ay Kalan Ödeme",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isHidden) "•••••• ₺" else format.format(totalDebt),
                            style = TabularHeroStyle,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { progressFloat },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = mint,
                            trackColor = if (isDark) Color(0xFF1D273B) else Color(0xFFE2E8F0)
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(thickness = 0.7.dp, color = borderColor.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PaymentHeroStatColumn(
                                label = "Ödenen",
                                value = if (isHidden) "••••" else format.format(totalPaid),
                                accent = mint,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(0.8.dp)
                                    .background(borderColor)
                            )
                            PaymentHeroStatColumn(
                                label = "Bekleyen",
                                value = "${thisMonthUnpaidTx.size} kalem",
                                accent = if (thisMonthUnpaidTx.isEmpty()) mint else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(0.8.dp)
                                    .background(borderColor)
                            )
                            PaymentHeroStatColumn(
                                label = "Geciken",
                                value = if (isHidden) "••••" else if (pastDueRemaining > 0) format.format(pastDueRemaining) else "Yok",
                                accent = if (pastDueRemaining > 0) coral else mint,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (onToggleMonthlyRule != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(thickness = 0.7.dp, color = borderColor.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Surface(
                                onClick = { showMonthlyRulesSheet = true },
                                shape = RoundedCornerShape(12.dp),
                                color = if (missingMonthlyItems.isNotEmpty()) {
                                    amber.copy(alpha = if (isDark) 0.14f else 0.09f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                },
                                border = BorderStroke(
                                    0.7.dp,
                                    if (missingMonthlyItems.isNotEmpty()) amber.copy(alpha = 0.35f) else borderColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("payments_manage_monthly_rules_btn")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.EventRepeat,
                                        contentDescription = null,
                                        tint = if (missingMonthlyItems.isNotEmpty()) amber else sky,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when {
                                            trackedRulesCount == 0 -> "Girilmeyen aylık ödemeleri hatırlatmak için kalem seçin"
                                            missingMonthlyItems.isNotEmpty() -> "$trackedRulesCount aylık kalem takipte · ${missingMonthlyItems.size} eksik"
                                            else -> "$trackedRulesCount aylık ödeme kalemi takipte · Tümü tamam"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (missingMonthlyItems.isNotEmpty()) amber else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Rounded.ChevronRight,
                                        contentDescription = "Aylık Hatırlatma Kurallarını Aç",
                                        tint = if (missingMonthlyItems.isNotEmpty()) amber else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1B. MISSING EXPECTED MONTHLY PAYMENTS CARD
            if (missingMonthlyItems.isNotEmpty()) {
                item(key = "missing_monthly_payments") {
                    MissingMonthlyPaymentsCard(
                        missingItems = missingMonthlyItems,
                        trackedCount = trackedRulesCount,
                        isHidden = isHidden,
                        onQuickEnter = { item ->
                            onQuickEnterMissing?.invoke(item) ?: onAddExpense?.invoke()
                        },
                        onSkipThisMonth = { item ->
                            onSkipMonthlyForMonth?.invoke(item.categoryName, true)
                        },
                        onManageRules = { showMonthlyRulesSheet = true }
                    )
                }
            }

            // 2. ACTIONABLE PAST-DUE BANNER (TAPPING FILTERS TO GECIKENLER)
            if (pastDueRemaining > 0) {
                item(key = "past_due_banner") {
                    Surface(
                        onClick = { selectedFilter = "Gecikenler" },
                        shape = RoundedCornerShape(18.dp),
                        color = coral.copy(alpha = if (isDark) 0.13f else 0.08f),
                        border = BorderStroke(0.9.dp, coral.copy(alpha = 0.38f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("past_due_alert_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(coral.copy(alpha = if (isDark) 0.22f else 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PriorityHigh,
                                    contentDescription = null,
                                    tint = coral,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Geçmiş Aylardan Kalan Borç",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = coral
                                )
                                Text(
                                    text = "${pastDueTx.size} gecikmiş ödeme · Görmek için dokunun",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = if (isHidden) "••••" else format.format(pastDueRemaining),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontFeatureSettings = "tnum"
                                ),
                                fontWeight = FontWeight.ExtraBold,
                                color = coral
                            )
                        }
                    }
                }
            }

            // 3. FILTER PILL STRIP
            item(key = "payments_filters") {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterOptions, key = { it.first }) { (key, count) ->
                        val isSelected = selectedFilter == key
                        Surface(
                            onClick = { selectedFilter = key },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                0.8.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else borderColor
                            ),
                            modifier = Modifier.testTag("payment_filter_$key")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                    }
                                ) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontFeatureSettings = "tnum"
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. GROUPED PAYMENT SECTIONS
            val showPastDue = selectedFilter == "Tümü" || selectedFilter == "Gecikenler"
            val showUnpaid = selectedFilter == "Tümü" || selectedFilter == "Bekleyenler"
            val showPaid = selectedFilter == "Tümü" || selectedFilter == "Ödenenler"
            val showFuture = selectedFilter == "Tümü" || selectedFilter == "Gelecek"

            if (showPastDue && pastDueTx.isNotEmpty()) {
                item(key = "section_past_due") {
                    PaymentSectionBlock(
                        title = "Geciken Ödemeler (${pastDueTx.size})",
                        totalText = if (isHidden) "••••" else format.format(pastDueRemaining),
                        accentColor = coral,
                        transactions = pastDueTx,
                        isPaidSection = false,
                        forcePastDue = true,
                        todayStart = today,
                        sdf = sdf,
                        format = format,
                        isHidden = isHidden,
                        onTogglePaid = onTogglePaid,
                        onEdit = onEditExpense,
                        onRequestDelete = { pendingDeleteTx = it }
                    )
                }
            }

            if (showUnpaid && thisMonthUnpaidTx.isNotEmpty()) {
                item(key = "section_this_month_unpaid") {
                    PaymentSectionBlock(
                        title = "Bu Ay Ödenecekler (${thisMonthUnpaidTx.size})",
                        totalText = if (isHidden) "••••" else format.format(totalDebt),
                        accentColor = amber,
                        transactions = thisMonthUnpaidTx,
                        isPaidSection = false,
                        forcePastDue = false,
                        todayStart = today,
                        sdf = sdf,
                        format = format,
                        isHidden = isHidden,
                        onTogglePaid = onTogglePaid,
                        onEdit = onEditExpense,
                        onRequestDelete = { pendingDeleteTx = it }
                    )
                }
            }

            if (showPaid && thisMonthPaidTx.isNotEmpty()) {
                item(key = "section_this_month_paid") {
                    PaymentSectionBlock(
                        title = "Bu Ay Tamamlananlar (${thisMonthPaidTx.size})",
                        totalText = if (isHidden) "••••" else format.format(totalPaid),
                        accentColor = mint,
                        transactions = thisMonthPaidTx,
                        isPaidSection = true,
                        forcePastDue = false,
                        todayStart = today,
                        sdf = sdf,
                        format = format,
                        isHidden = isHidden,
                        onTogglePaid = onTogglePaid,
                        onEdit = onEditExpense,
                        onRequestDelete = { pendingDeleteTx = it }
                    )
                }
            }

            if (showFuture && futureUnpaidTx.isNotEmpty()) {
                val groupedFuture = futureUnpaidTx.groupBy {
                    Calendar.getInstance().apply {
                        timeInMillis = it.timestamp
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }.toSortedMap()

                groupedFuture.forEach { (monthMs, txs) ->
                    val monthTitle = groupSdf.format(Date(monthMs)).replaceFirstChar { it.titlecase(Locale("tr", "TR")) }
                    val monthTotal = txs.sumOf { it.amount }
                    item(key = "section_future_$monthMs") {
                        PaymentSectionBlock(
                            title = "$monthTitle (${txs.size})",
                            totalText = if (isHidden) "••••" else format.format(monthTotal),
                            accentColor = sky,
                            transactions = txs,
                            isPaidSection = false,
                            forcePastDue = false,
                            todayStart = today,
                            sdf = sdf,
                            format = format,
                            isHidden = isHidden,
                            onTogglePaid = onTogglePaid,
                            onEdit = onEditExpense,
                            onRequestDelete = { pendingDeleteTx = it }
                        )
                    }
                }
            }

            val hasVisibleItems = (showPastDue && pastDueTx.isNotEmpty()) ||
                (showUnpaid && thisMonthUnpaidTx.isNotEmpty()) ||
                (showPaid && thisMonthPaidTx.isNotEmpty()) ||
                (showFuture && futureUnpaidTx.isNotEmpty())

            if (!hasVisibleItems) {
                item(key = "payments_empty") {
                    SettingsGroupCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(mint.copy(alpha = if (isDark) 0.16f else 0.11f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = mint,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = if (expenseTx.isEmpty()) "Henüz ödeme kaydı yok" else "Bu filtrede ödeme bulunmuyor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (expenseTx.isEmpty()) {
                                    "Kira, fatura, taksit ve düzenli giderlerinizi ekleyerek tek ekrandan takip edebilirsiniz."
                                } else {
                                    "Tüm ödemeler temiz görünüyor veya farklı bir filtre seçebilirsiniz."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            if (onAddExpense != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = onAddExpense,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("empty_add_payment_button")
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Yeni Ödeme Ekle", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (pendingDeleteTx != null && onDeleteExpense != null) {
        val txToDelete = pendingDeleteTx!!
        AlertDialog(
            onDismissRequest = { pendingDeleteTx = null },
            title = { Text("Ödeme Silinsin Mi?", fontWeight = FontWeight.Bold) },
            text = { Text("\"${txToDelete.title}\" (${format.format(txToDelete.amount)}) kaydı kalıcı olarak silinecek.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExpense(txToDelete)
                        pendingDeleteTx = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sil") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteTx = null }) { Text("İptal") }
            }
        )
    }

    if (showMonthlyRulesSheet && onToggleMonthlyRule != null && onSaveMonthlyRule != null && onDeleteMonthlyRule != null && onSkipMonthlyForMonth != null) {
        MonthlyPaymentRemindersBottomSheet(
            rules = monthlyRules,
            expenseCategories = expenseCategories,
            transactions = transactions,
            persons = persons,
            remindMissingEnabled = remindMissingEnabled,
            onToggleMasterNotification = { onToggleMasterMissingNotif?.invoke(it) },
            onToggleItem = onToggleMonthlyRule,
            onSaveRule = onSaveMonthlyRule,
            onDeleteRule = onDeleteMonthlyRule,
            onSkipForCurrentMonth = onSkipMonthlyForMonth,
            onQuickEnterItem = onQuickEnterMissing,
            onSendTestNotification = onSendTestMissingNotif,
            onDismiss = { showMonthlyRulesSheet = false }
        )
    }
}

@Composable
private fun PaymentHeroStatColumn(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = JetBrainsMonoFamily,
                fontFeatureSettings = "tnum"
            ),
            fontWeight = FontWeight.Bold,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun PaymentSectionBlock(
    title: String,
    totalText: String,
    accentColor: Color,
    transactions: List<Transaction>,
    isPaidSection: Boolean,
    forcePastDue: Boolean,
    todayStart: Long,
    sdf: SimpleDateFormat,
    format: java.text.NumberFormat,
    isHidden: Boolean,
    onTogglePaid: (Transaction, Boolean) -> Unit,
    onEdit: ((Transaction) -> Unit)?,
    onRequestDelete: (Transaction) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 4.dp, bottom = 6.dp, top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(Locale("tr", "TR")),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = if (isDark) 0.16f else 0.11f)
            ) {
                Text(
                    text = totalText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontFeatureSettings = "tnum"
                    ),
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        SettingsGroupCard {
            transactions.forEachIndexed { index, tx ->
                val isPastDue = !isPaidSection && (forcePastDue || tx.timestamp < todayStart)
                PaymentInsetRow(
                    tx = tx,
                    isPaid = isPaidSection || tx.isPaid,
                    isPastDue = isPastDue,
                    sdf = sdf,
                    format = format,
                    isHidden = isHidden,
                    onToggle = { onTogglePaid(tx, !tx.isPaid) },
                    onEdit = onEdit?.let { cb -> { cb(tx) } },
                    onDelete = { onRequestDelete(tx) }
                )
                if (index != transactions.lastIndex) {
                    SettingsInsetDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PaymentInsetRow(
    tx: Transaction,
    isPaid: Boolean,
    isPastDue: Boolean,
    sdf: SimpleDateFormat,
    format: java.text.NumberFormat,
    isHidden: Boolean,
    onToggle: () -> Unit,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)

    var showMenu by remember { mutableStateOf(false) }

    val icon = when {
        tx.category.lowercase().contains("kira") -> Icons.Rounded.Home
        tx.category.lowercase().contains("market") -> Icons.Rounded.ShoppingCart
        tx.category.lowercase().contains("fatura") -> Icons.Rounded.ReceiptLong
        tx.category.lowercase().contains("kredi") -> Icons.Rounded.CreditCard
        tx.category.lowercase().contains("ulaşım") || tx.category.lowercase().contains("yakıt") -> Icons.Rounded.DirectionsCar
        tx.category.lowercase().contains("sağlık") -> Icons.Rounded.LocalHospital
        tx.category.lowercase().contains("kurs") || tx.category.lowercase().contains("eğitim") -> Icons.Rounded.School
        else -> Icons.Rounded.Payments
    }

    val iconTint = when {
        isPaid -> mint
        isPastDue -> coral
        else -> amber
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (onEdit != null) showMenu = true else onToggle()
                }
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag("payment_row_${tx.id}"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = if (isDark) 0.16f else 0.11f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPaid) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isPastDue) {
                        Surface(
                            color = coral.copy(alpha = if (isDark) 0.18f else 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Gecikti",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = coral,
                                softWrap = false,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (tx.category.isNotBlank() && !tx.category.equals(tx.title, ignoreCase = true)) {
                        Text(
                            text = tx.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!tx.person.isNullOrBlank()) {
                        Text(
                            text = "· ${tx.person}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "· ${sdf.format(Date(tx.timestamp))}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                        color = if (isPastDue) coral else MaterialTheme.colorScheme.onSurfaceVariant,
                        softWrap = false,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isHidden) "••••" else format.format(tx.amount),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontFeatureSettings = "tnum"
                    ),
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isPaid -> mint
                        isPastDue -> coral
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    softWrap = false,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(5.dp))
                Surface(
                    onClick = onToggle,
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPaid) mint.copy(alpha = if (isDark) 0.20f else 0.13f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    border = BorderStroke(
                        0.7.dp,
                        if (isPaid) mint.copy(alpha = 0.45f) else borderColor
                    ),
                    modifier = Modifier.testTag("payment_toggle_${tx.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPaid) Icons.Rounded.Check else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = if (isPaid) "Ödendi" else "Öde",
                            tint = if (isPaid) mint else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isPaid) "Ödendi" else "Bekliyor",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            softWrap = false,
                            maxLines = 1,
                            color = if (isPaid) mint else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = if (isPaid) Icons.Rounded.RadioButtonUnchecked else Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = mint,
                        modifier = Modifier.size(18.dp)
                    )
                },
                text = { Text(if (isPaid) "Ödenmedi İşaretle" else "Ödendi İşaretle") },
                onClick = {
                    showMenu = false
                    onToggle()
                }
            )
            if (onEdit != null) {
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text("Düzenle") },
                    onClick = {
                        showMenu = false
                        onEdit()
                    }
                )
            }
            if (onDelete != null) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    text = { Text("Sil", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}
