package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppCategory
import com.example.data.MonthlyExpectedPaymentRule
import com.example.data.MonthlyPaymentEvaluator
import com.example.data.MonthlyRuleChecklistItem
import com.example.data.MonthlyRuleStatus
import com.example.data.Person
import com.example.data.Transaction
import com.example.ui.theme.*
import java.util.Locale

private fun monthlyCategoryIcon(name: String): ImageVector {
    val n = name.lowercase(Locale("tr", "TR"))
    return when {
        n.contains("kira") || n.contains("aidat") || n.contains("ev") -> Icons.Rounded.Home
        n.contains("elektrik") -> Icons.Rounded.Bolt
        n.contains("su") -> Icons.Rounded.WaterDrop
        n.contains("doğalgaz") || n.contains("dogalgaz") || n.contains("gaz") -> Icons.Rounded.LocalFireDepartment
        n.contains("internet") || n.contains("telefon") || n.contains("gsm") || n.contains("fatura") -> Icons.Rounded.Wifi
        n.contains("kredi") || n.contains("kart") || n.contains("taksit") || n.contains("banka") -> Icons.Rounded.CreditCard
        n.contains("market") || n.contains("gıda") || n.contains("mutfak") -> Icons.Rounded.ShoppingCart
        n.contains("okul") || n.contains("kurs") || n.contains("eğitim") -> Icons.Rounded.School
        n.contains("araç") || n.contains("yakıt") || n.contains("benzin") || n.contains("otopark") -> Icons.Rounded.DirectionsCar
        n.contains("sağlık") || n.contains("sigorta") -> Icons.Rounded.HealthAndSafety
        n.contains("abonelik") || n.contains("dijital") -> Icons.Rounded.Subscriptions
        else -> Icons.Rounded.ReceiptLong
    }
}

/**
 * Compact executive alert banner shown on Anasayfa only when tracked monthly
 * payments are still missing past their reminder day.
 */
@Composable
fun HomeMissingPaymentsBanner(
    missingItems: List<MonthlyRuleChecklistItem>,
    onOpenPayments: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (missingItems.isEmpty()) return

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)
    val preview = remember(missingItems) {
        val names = missingItems.take(3).joinToString(" · ") { it.displayTitle }
        if (missingItems.size > 3) "$names +${missingItems.size - 3}" else names
    }

    Surface(
        onClick = onOpenPayments,
        shape = RoundedCornerShape(18.dp),
        color = amber.copy(alpha = if (isDark) 0.13f else 0.08f),
        border = BorderStroke(0.9.dp, amber.copy(alpha = 0.36f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_missing_payments_banner")
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
                    .background(amber.copy(alpha = if (isDark) 0.22f else 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.EventBusy,
                    contentDescription = null,
                    tint = amber,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bu Ay Girilmemiş ${missingItems.size} Düzenli Ödeme",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = amber,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Ödemelere Git",
                tint = amber,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Actionable Missing Monthly Payments block shown inside PaymentsScreen.
 * Allows 1-tap prefilled entry ("+ Gir") or skipping just for this month ("Atla").
 */
@Composable
fun MissingMonthlyPaymentsCard(
    missingItems: List<MonthlyRuleChecklistItem>,
    trackedCount: Int,
    isHidden: Boolean,
    onQuickEnter: (MonthlyRuleChecklistItem) -> Unit,
    onSkipThisMonth: (MonthlyRuleChecklistItem) -> Unit,
    onManageRules: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)
    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }

    if (missingItems.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(amber)
                )
                Text(
                    text = "BU AY GİRİLMEMİŞ ÖDEMELER (${missingItems.size})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 0.7.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = amber,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                onClick = onManageRules,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.testTag("missing_card_manage_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Yönet ($trackedCount)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("missing_monthly_payments_card"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.9.dp, amber.copy(alpha = if (isDark) 0.42f else 0.35f)),
            shadowElevation = if (isDark) 0.dp else 1.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                missingItems.forEachIndexed { idx, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 64.dp)
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(amber.copy(alpha = if (isDark) 0.16f else 0.11f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = monthlyCategoryIcon(item.categoryName),
                                contentDescription = null,
                                tint = amber,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.displayTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val subtitle = buildString {
                                append("Ayın ${item.remindAfterDay}. günü")
                                item.suggestedAmount?.let { amt ->
                                    append(" · ")
                                    append(if (isHidden) "••••" else "~${format.format(amt)}")
                                }
                            }
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        TextButton(
                            onClick = { onSkipThisMonth(item) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("skip_missing_${item.categoryName}")
                        ) {
                            Text(
                                text = "Atla",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        Surface(
                            onClick = { onQuickEnter(item) },
                            shape = RoundedCornerShape(10.dp),
                            color = amber.copy(alpha = if (isDark) 0.22f else 0.14f),
                            border = BorderStroke(0.8.dp, amber.copy(alpha = 0.50f)),
                            modifier = Modifier.testTag("quick_enter_missing_${item.categoryName}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = amber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Gir",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = amber
                                )
                            }
                        }
                    }

                    if (idx < missingItems.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 64.dp, end = 14.dp),
                            thickness = 0.6.dp,
                            color = borderColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Bottom Sheet to choose WHICH monthly payment items should be reminded
 * when not entered, configure reminder day (1..28), optional estimated amount & person,
 * skip for current month, or add a custom monthly payment item.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MonthlyPaymentRemindersBottomSheet(
    rules: List<MonthlyExpectedPaymentRule>,
    expenseCategories: List<AppCategory>,
    transactions: List<Transaction>,
    persons: List<Person>,
    remindMissingEnabled: Boolean,
    onToggleMasterNotification: (Boolean) -> Unit,
    onToggleItem: (categoryName: String, enabled: Boolean, suggestedAmount: Double?, suggestedPerson: String?) -> Unit,
    onSaveRule: (MonthlyExpectedPaymentRule) -> Unit,
    onDeleteRule: (String) -> Unit,
    onSkipForCurrentMonth: (categoryName: String, skip: Boolean) -> Unit,
    onQuickEnterItem: ((MonthlyRuleChecklistItem) -> Unit)? = null,
    onSendTestNotification: (((Boolean, String) -> Unit) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)

    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }

    val checklist = remember(rules, expenseCategories, transactions) {
        MonthlyPaymentEvaluator.buildUnifiedChecklist(
            rules = rules,
            expenseCategories = expenseCategories,
            transactions = transactions
        )
    }

    val trackedCount = remember(checklist) { checklist.count { it.isTracked } }
    val missingCount = remember(checklist) { checklist.count { it.status == MonthlyRuleStatus.MISSING_NOW } }

    var selectedTab by remember { mutableStateOf("Tümü") }
    var editingItem by remember { mutableStateOf<MonthlyRuleChecklistItem?>(null) }
    var showCreateCustomDialog by remember { mutableStateOf(false) }
    var testFeedback by remember { mutableStateOf<String?>(null) }

    val visibleItems = remember(checklist, selectedTab) {
        when (selectedTab) {
            "Takip Edilen" -> checklist.filter { it.isTracked }
            "Eksik" -> checklist.filter { it.status == MonthlyRuleStatus.MISSING_NOW }
            else -> checklist
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
                .padding(horizontal = 18.dp)
                .padding(bottom = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aylık Ödeme Hatırlatıcı",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Girilmeyen düzenli ödeme kalemlerini seçip takip edin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_monthly_reminders_sheet")
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Kapat")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Master notification card
            SettingsGroupCard {
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
                            .background(amber.copy(alpha = if (isDark) 0.18f else 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = amber,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Eksik Ödeme Bildirimi",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (remindMissingEnabled) {
                                "$trackedCount kalem takip ediliyor · Günlük kontrol açık"
                            } else {
                                "Arka plan bildirimi kapalı (liste görünür)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (onSendTestNotification != null) {
                        TextButton(
                            onClick = {
                                onSendTestNotification { _, msg -> testFeedback = msg }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("test_missing_payment_notif_btn")
                        ) {
                            Text("Test", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Switch(
                        checked = remindMissingEnabled,
                        onCheckedChange = onToggleMasterNotification,
                        modifier = Modifier.testTag("switch_remind_missing_monthly")
                    )
                }
            }

            if (!testFeedback.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = testFeedback!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = mint,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Segmented Capsule
            val tabs = listOf(
                Triple("Tümü", checklist.size, MaterialTheme.colorScheme.primary),
                Triple("Takip Edilen", trackedCount, mint),
                Triple("Eksik", missingCount, if (missingCount > 0) coral else MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Surface(
                shape = RoundedCornerShape(14.dp),
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
                    tabs.forEach { (tabName, count, badgeTint) ->
                        val isSelected = selectedTab == tabName
                        Surface(
                            onClick = { selectedTab = tabName },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.22f else 0.12f)
                            } else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("monthly_rule_tab_$tabName")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tabName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else badgeTint
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Checklist
            if (visibleItems.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (selectedTab == "Eksik") Icons.Rounded.CheckCircleOutline else Icons.Rounded.EventRepeat,
                            contentDescription = null,
                            tint = if (selectedTab == "Eksik") mint else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (selectedTab) {
                                "Eksik" -> "Bu ay eksik düzenli ödeme bulunmuyor"
                                "Takip Edilen" -> "Henüz hatırlatma için kalem seçilmedi"
                                else -> "Gider kategorisi bulunamadı"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 390.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        itemsIndexed(
                            items = visibleItems,
                            key = { _, item -> item.categoryName }
                        ) { idx, item ->
                            val (statusText, statusColor) = when (item.status) {
                                MonthlyRuleStatus.ENTERED_THIS_MONTH -> "Bu ay girildi" to mint
                                MonthlyRuleStatus.MISSING_NOW -> "Eksik" to coral
                                MonthlyRuleStatus.UPCOMING_THIS_MONTH -> "Ayın ${item.remindAfterDay}'inde" to sky
                                MonthlyRuleStatus.SKIPPED_THIS_MONTH -> "Bu ay atlandı" to MaterialTheme.colorScheme.onSurfaceVariant
                                MonthlyRuleStatus.DISABLED -> if (item.isRecommended) "Önerilen" to amber else null to MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            val iconTint = when (item.status) {
                                MonthlyRuleStatus.MISSING_NOW -> coral
                                MonthlyRuleStatus.ENTERED_THIS_MONTH -> mint
                                MonthlyRuleStatus.UPCOMING_THIS_MONTH -> sky
                                MonthlyRuleStatus.SKIPPED_THIS_MONTH -> MaterialTheme.colorScheme.onSurfaceVariant
                                MonthlyRuleStatus.DISABLED -> if (item.isRecommended) amber else MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (item.isTracked) {
                                            editingItem = item
                                        } else {
                                            onToggleItem(
                                                item.categoryName,
                                                true,
                                                item.lastKnownAmount,
                                                item.lastKnownPerson
                                            )
                                        }
                                    }
                                    .defaultMinSize(minHeight = 64.dp)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(iconTint.copy(alpha = if (isDark) 0.16f else 0.11f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = monthlyCategoryIcon(item.categoryName),
                                        contentDescription = null,
                                        tint = iconTint,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = item.displayTitle,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (statusText != null) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = statusColor.copy(alpha = if (isDark) 0.18f else 0.12f)
                                            ) {
                                                Text(
                                                    text = statusText,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = statusColor,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    val subtitleText = if (item.isTracked) {
                                        buildString {
                                            append("Her ayın ${item.remindAfterDay}. günü")
                                            item.suggestedAmount?.let { amt ->
                                                append(" · ~${format.format(amt)}")
                                            }
                                        }
                                    } else if (item.lastKnownAmount != null) {
                                        "Son tutar: ${format.format(item.lastKnownAmount)} · Hatırlatma kapalı"
                                    } else {
                                        "Hatırlatma kapalı"
                                    }
                                    Text(
                                        text = subtitleText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (item.status == MonthlyRuleStatus.MISSING_NOW && onQuickEnterItem != null) {
                                    IconButton(
                                        onClick = {
                                            onDismiss()
                                            onQuickEnterItem(item)
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("sheet_enter_${item.categoryName}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.AddCircleOutline,
                                            contentDescription = "${item.displayTitle} gir",
                                            tint = amber,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                if (item.isTracked) {
                                    IconButton(
                                        onClick = { editingItem = item },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("tune_rule_${item.categoryName}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Tune,
                                            contentDescription = "${item.displayTitle} kuralını düzenle",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }

                                Switch(
                                    checked = item.isTracked,
                                    onCheckedChange = { checked ->
                                        onToggleItem(
                                            item.categoryName,
                                            checked,
                                            item.lastKnownAmount,
                                            item.lastKnownPerson
                                        )
                                    },
                                    modifier = Modifier.testTag("switch_rule_${item.categoryName}")
                                )
                            }

                            if (idx < visibleItems.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 64.dp, end = 14.dp),
                                    thickness = 0.6.dp,
                                    color = borderColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { showCreateCustomDialog = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("add_custom_monthly_rule_btn")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Yeni Aylık Ödeme Kalemi Ekle",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Rule configuration dialog for an existing item
    editingItem?.let { item ->
        MonthlyRuleConfigDialog(
            initialRule = item.rule ?: MonthlyExpectedPaymentRule(
                categoryName = item.categoryName,
                label = item.displayTitle,
                isEnabled = true,
                remindAfterDay = 1,
                defaultAmount = item.lastKnownAmount,
                defaultPerson = item.lastKnownPerson
            ),
            isNewCustom = false,
            canDelete = item.isCustomRule,
            isSkippedThisMonth = item.status == MonthlyRuleStatus.SKIPPED_THIS_MONTH,
            persons = persons,
            onSave = { updated ->
                onSaveRule(updated)
                editingItem = null
            },
            onToggleSkipThisMonth = { skip ->
                onSkipForCurrentMonth(item.categoryName, skip)
                editingItem = null
            },
            onDelete = if (item.isCustomRule && item.rule != null) {
                {
                    onDeleteRule(item.rule.id)
                    editingItem = null
                }
            } else null,
            onDismiss = { editingItem = null }
        )
    }

    // Add brand-new custom recurring payment item
    if (showCreateCustomDialog) {
        MonthlyRuleConfigDialog(
            initialRule = MonthlyExpectedPaymentRule(
                categoryName = "",
                label = "",
                isEnabled = true,
                remindAfterDay = 5
            ),
            isNewCustom = true,
            canDelete = false,
            isSkippedThisMonth = false,
            persons = persons,
            onSave = { created ->
                onSaveRule(created)
                showCreateCustomDialog = false
            },
            onToggleSkipThisMonth = {},
            onDelete = null,
            onDismiss = { showCreateCustomDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonthlyRuleConfigDialog(
    initialRule: MonthlyExpectedPaymentRule,
    isNewCustom: Boolean,
    canDelete: Boolean,
    isSkippedThisMonth: Boolean,
    persons: List<Person>,
    onSave: (MonthlyExpectedPaymentRule) -> Unit,
    onToggleSkipThisMonth: (Boolean) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)

    var nameText by remember { mutableStateOf(initialRule.displayTitle) }
    var remindDay by remember { mutableIntStateOf(initialRule.remindAfterDay.coerceIn(1, 28)) }
    var amountText by remember {
        mutableStateOf(
            initialRule.defaultAmount?.let { com.example.util.formatDoubleForInput(it) } ?: ""
        )
    }
    var selectedPerson by remember { mutableStateOf(initialRule.defaultPerson) }
    var nameError by remember { mutableStateOf<String?>(null) }

    val quickDays = listOf(1, 5, 10, 15, 20, 25)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (isNewCustom) "Yeni Hatırlatma Kalemi" else initialRule.displayTitle,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isNewCustom) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = {
                            nameText = it
                            nameError = null
                        },
                        label = { Text("Ödeme Kalemi Adı (örn. Doğalgaz)") },
                        singleLine = true,
                        isError = nameError != null,
                        supportingText = nameError?.let { err -> { Text(err) } },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_monthly_rule_name_input")
                    )
                }

                // Day of month picker
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hatırlatma Günü",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                onClick = { if (remindDay > 1) remindDay-- },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Remove, contentDescription = "Günü Azalt", modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = "Ayın $remindDay. günü",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontFeatureSettings = "tnum"
                                ),
                                fontWeight = FontWeight.Bold,
                                color = amber
                            )
                            Surface(
                                onClick = { if (remindDay < 28) remindDay++ },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Add, contentDescription = "Günü Artır", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Text(
                        text = "Bu güne kadar girilmemişse eksik ödeme olarak hatırlatılır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickDays.forEach { d ->
                            val selected = remindDay == d
                            FilterChip(
                                selected = selected,
                                onClick = { remindDay = d },
                                label = {
                                    Text(
                                        text = "$d.",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontFeatureSettings = "tnum"
                                        )
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = amber.copy(alpha = if (isDark) 0.22f else 0.14f),
                                    selectedLabelColor = amber
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = borderColor,
                                    selectedBorderColor = amber
                                )
                            )
                        }
                    }
                }

                // Optional estimated amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Tahmini Tutar (Opsiyonel)") },
                    suffix = { Text("₺") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Optional default person
                if (persons.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Varsayılan Kişi (Opsiyonel)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedPerson == null,
                                onClick = { selectedPerson = null },
                                label = { Text("Seçilmedi", fontSize = 11.sp) }
                            )
                            persons.forEach { person ->
                                FilterChip(
                                    selected = selectedPerson.equals(person.name, ignoreCase = true),
                                    onClick = { selectedPerson = person.name },
                                    label = { Text(person.name, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                if (!isNewCustom) {
                    HorizontalDivider(thickness = 0.7.dp, color = borderColor)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onToggleSkipThisMonth(!isSkippedThisMonth) }
                        ) {
                            Icon(
                                imageVector = if (isSkippedThisMonth) Icons.Rounded.Restore else Icons.Rounded.EventBusy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isSkippedThisMonth) "Bu Ay Atlamayı Kaldır" else "Sadece Bu Ay Atla")
                        }

                        if (canDelete && onDelete != null) {
                            TextButton(
                                onClick = onDelete,
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Kaldır")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = nameText.trim()
                    if (isNewCustom && trimmed.isBlank()) {
                        nameError = "Ödeme kalemi adı gerekli"
                        return@Button
                    }
                    val parsedAmount = com.example.util.parseFormattedAmount(amountText)
                        .takeIf { it > 0.0 }
                    val finalCat = if (isNewCustom) trimmed else initialRule.categoryName
                    onSave(
                        initialRule.copy(
                            categoryName = finalCat,
                            label = if (isNewCustom) trimmed else initialRule.displayTitle,
                            isEnabled = true,
                            remindAfterDay = remindDay,
                            defaultAmount = parsedAmount,
                            defaultPerson = selectedPerson
                        )
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_monthly_rule_btn")
            ) {
                Text("Kaydet", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
