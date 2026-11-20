package com.example.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.BuildConfig
import com.example.data.AppCategory
import com.example.data.Person
import com.example.data.TransactionType
import com.example.ui.theme.*
import com.example.updater.AppUpdater
import com.example.viewmodel.BudgetViewModel
import com.example.worker.PaymentReminderWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun SettingsScreen(viewModel: BudgetViewModel) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)
    val indigo = if (isDark) CyberNeonPurple else Color(0xFF6366F1)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)

    val incomeCategories by viewModel.incomeCategories.collectAsStateWithLifecycle()
    val expenseCategories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val savingCategories by viewModel.savingCategories.collectAsStateWithLifecycle()
    val persons by viewModel.persons.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.darkThemeEnabled.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val marketSchedule by viewModel.marketScheduleConfig.collectAsStateWithLifecycle()
    val marketAlarms by viewModel.marketPriceAlarms.collectAsStateWithLifecycle()
    val monthlyPaymentRules by viewModel.monthlyPaymentRules.collectAsStateWithLifecycle()
    val remindMissingMonthlyPayments by viewModel.remindMissingMonthlyPayments.collectAsStateWithLifecycle()
    val goldPrices by viewModel.goldPrices.collectAsStateWithLifecycle()
    val bankRates by viewModel.bankRates.collectAsStateWithLifecycle()
    val ziraatRates by viewModel.ziraatRates.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appUpdater = remember { AppUpdater(context) }

    // Reminder preferences state (mirrored to PreferenceManager immediately)
    var remindDueDay by remember { mutableStateOf(viewModel.preferenceManager.remindDueDay) }
    var remindOverdue by remember { mutableStateOf(viewModel.preferenceManager.remindOverdue) }
    var remindUpcomingDays by remember { mutableIntStateOf(viewModel.preferenceManager.remindUpcomingDays) }
    var updateIntervalHours by remember { mutableIntStateOf(viewModel.preferenceManager.updateIntervalHours) }
    var silentHoursEnabled by remember { mutableStateOf(viewModel.preferenceManager.silentHoursEnabled) }
    var silentHoursStart by remember { mutableStateOf(viewModel.preferenceManager.silentHoursStart) }
    var silentHoursEnd by remember { mutableStateOf(viewModel.preferenceManager.silentHoursEnd) }

    // Modal sheets & dialogs
    var showCategorySheet by remember { mutableStateOf(false) }
    var showPersonSheet by remember { mutableStateOf(false) }
    var showPaymentRemindersSheet by remember { mutableStateOf(false) }
    var showMonthlyPaymentRulesSheet by remember { mutableStateOf(false) }
    var showMarketAlertsSheet by remember { mutableStateOf(false) }
    var showMarketMatchSheet by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var updateDialogState by remember { mutableStateOf<UpdateDialogState?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.ms-excel")
    ) { uri ->
        if (uri != null) {
            viewModel.exportTransactionsXls(context, uri) { success, msg ->
                Toast.makeText(context, if (success) msg else "Hata: $msg", Toast.LENGTH_LONG).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importTransactionsXls(context, uri) { success, msg ->
                Toast.makeText(context, if (success) msg else "Hata: $msg", Toast.LENGTH_LONG).show()
            }
        }
    }

    val totalCategories = incomeCategories.size + expenseCategories.size + savingCategories.size
    val activeAlarmCount = marketAlarms.count { it.isEnabled }
    val marketAlertsActiveCount = activeAlarmCount + if (marketSchedule.enabled) 1 else 0

    val savingCategoriesFromTx = remember(uiState.transactions) {
        uiState.transactions
            .filter { it.type == TransactionType.SAVING }
            .map { it.category }
            .distinct()
            .sorted()
    }

    val savedUserName = remember {
        viewModel.preferenceManager.userName
            .takeIf { it.isNotBlank() && it != "Gizli Kullanici" }
            ?: "Aile Bütçesi"
    }
    val userInitial = savedUserName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "A"

    val personPreviewSubtitle = remember(persons) {
        if (persons.isEmpty()) "Henüz kişi eklenmedi"
        else {
            val preview = persons.take(3).joinToString(" · ") { it.name }
            if (persons.size > 3) "$preview +${persons.size - 3}" else preview
        }
    }

    val paymentReminderSubtitle = remember(
        remindDueDay, remindOverdue, remindUpcomingDays, silentHoursEnabled, silentHoursStart, silentHoursEnd
    ) {
        val status = if (remindDueDay || remindOverdue) "Aktif" else "Kapalı"
        val silent = if (silentHoursEnabled) "Sessiz $silentHoursStart–$silentHoursEnd" else "Sessiz kapalı"
        "$status · ${remindUpcomingDays}g önce · $silent"
    }

    val marketAlertSubtitle = remember(marketSchedule, activeAlarmCount) {
        buildString {
            if (marketSchedule.enabled) {
                val freq = if (marketSchedule.intervalMinutes < 60) {
                    "${marketSchedule.intervalMinutes} dk"
                } else {
                    "${marketSchedule.intervalMinutes / 60} sa"
                }
                append("Özet ${marketSchedule.startTime}–${marketSchedule.endTime} ($freq)")
            } else {
                append("Zamanlı özet kapalı")
            }
            if (activeAlarmCount > 0) {
                append(" · $activeAlarmCount limit alarmı")
            }
        }
    }

    val trackedMonthlyCount = remember(monthlyPaymentRules) {
        monthlyPaymentRules.count { it.isEnabled }
    }
    val missingMonthlyCount = remember(monthlyPaymentRules, uiState.transactions) {
        com.example.data.MonthlyPaymentEvaluator.getMissingDueNow(
            rules = monthlyPaymentRules,
            transactions = uiState.transactions
        ).size
    }
    val monthlyMissingSubtitle = remember(trackedMonthlyCount, missingMonthlyCount, remindMissingMonthlyPayments) {
        when {
            trackedMonthlyCount == 0 -> "Hangi aylık kalemlerin hatırlatılacağını seçin"
            missingMonthlyCount > 0 -> "$trackedMonthlyCount kalem takipte · $missingMonthlyCount bu ay eksik"
            remindMissingMonthlyPayments -> "$trackedMonthlyCount kalem takipte · Bildirim açık"
            else -> "$trackedMonthlyCount kalem takipte · Bildirim kapalı"
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
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 6.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. EXECUTIVE OVERVIEW CARD
            item(key = "settings_hero") {
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
                                        listOf(
                                            Color(0xFF131C2B),
                                            MaterialTheme.colorScheme.surface
                                        )
                                    } else {
                                        listOf(
                                            mint.copy(alpha = 0.06f),
                                            Color.White
                                        )
                                    }
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(mint.copy(alpha = if (isDark) 0.18f else 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userInitial,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = mint
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = savedUserName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Yönetim ve Tercih Merkezi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                border = BorderStroke(0.6.dp, borderColor)
                            ) {
                                Text(
                                    text = "v${BuildConfig.VERSION_NAME}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(thickness = 0.7.dp, color = borderColor.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SettingsHeroStatCell(
                                value = totalCategories.toString(),
                                label = "Kategori",
                                accent = mint,
                                onClick = { showCategorySheet = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_stat_categories")
                            )
                            Box(
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(0.8.dp)
                                    .background(borderColor)
                            )
                            SettingsHeroStatCell(
                                value = persons.size.toString(),
                                label = "Kişi",
                                accent = indigo,
                                onClick = { showPersonSheet = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_stat_persons")
                            )
                            Box(
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(0.8.dp)
                                    .background(borderColor)
                            )
                            SettingsHeroStatCell(
                                value = if (marketAlertsActiveCount > 0) "$marketAlertsActiveCount Aktif" else "Kapalı",
                                label = "Piyasa Alarmı",
                                accent = if (marketAlertsActiveCount > 0) amber else MaterialTheme.colorScheme.onSurfaceVariant,
                                onClick = { showMarketAlertsSheet = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_stat_market_alerts")
                            )
                        }
                    }
                }
            }

            // 2. GÖRÜNÜM
            item(key = "section_appearance") {
                Column {
                    SettingsSectionHeader("Görünüm")
                    SettingsGroupCard {
                        SettingsRowItem(
                            icon = if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                            iconTint = if (isDarkTheme) amber else sky,
                            title = "Tema Modu",
                            subtitle = if (isDarkTheme) "Koyu görünüm aktif" else "Açık görünüm aktif",
                            showChevron = false,
                            testTag = "settings_theme_row",
                            trailing = {
                                ThemeSegmentedToggle(
                                    isDarkTheme = isDarkTheme,
                                    onSelectDark = { viewModel.toggleDarkTheme(it) }
                                )
                            }
                        )
                    }
                }
            }

            // 3. BÜTÇE YAPISI
            item(key = "section_structure") {
                Column {
                    SettingsSectionHeader("Bütçe Yapısı")
                    SettingsGroupCard {
                        SettingsRowItem(
                            icon = Icons.Rounded.Category,
                            iconTint = mint,
                            title = "Kategoriler",
                            subtitle = "${incomeCategories.size} Gelir · ${expenseCategories.size} Gider · ${savingCategories.size} Birikim",
                            badgeText = totalCategories.toString(),
                            testTag = "settings_categories_row",
                            onClick = { showCategorySheet = true }
                        )
                        SettingsInsetDivider()
                        SettingsRowItem(
                            icon = Icons.Rounded.Groups,
                            iconTint = indigo,
                            title = "Aile & Kişiler",
                            subtitle = personPreviewSubtitle,
                            badgeText = persons.size.toString(),
                            testTag = "settings_persons_row",
                            onClick = { showPersonSheet = true }
                        )
                    }
                }
            }

            // 4. BİLDİRİM & CANLI PİYASA
            item(key = "section_notifications_market") {
                Column {
                    SettingsSectionHeader("Bildirim & Canlı Piyasa")
                    SettingsGroupCard {
                        SettingsRowItem(
                            icon = Icons.Rounded.NotificationsActive,
                            iconTint = amber,
                            title = "Ödeme Hatırlatmaları",
                            subtitle = paymentReminderSubtitle,
                            testTag = "settings_payment_reminders_row",
                            onClick = { showPaymentRemindersSheet = true }
                        )
                        SettingsInsetDivider()
                        SettingsRowItem(
                            icon = Icons.Rounded.EventRepeat,
                            iconTint = if (missingMonthlyCount > 0) coral else sky,
                            title = "Girilmemiş Aylık Ödemeler",
                            subtitle = monthlyMissingSubtitle,
                            badgeText = when {
                                missingMonthlyCount > 0 -> "$missingMonthlyCount Eksik"
                                trackedMonthlyCount > 0 -> "$trackedMonthlyCount"
                                else -> null
                            },
                            badgeColor = if (missingMonthlyCount > 0) coral else sky,
                            testTag = "settings_monthly_missing_rules_row",
                            onClick = { showMonthlyPaymentRulesSheet = true }
                        )
                        SettingsInsetDivider()
                        SettingsRowItem(
                            icon = Icons.Rounded.ShowChart,
                            iconTint = mint,
                            title = "Piyasa Bildirimleri & Alarmlar",
                            subtitle = marketAlertSubtitle,
                            badgeText = if (marketAlertsActiveCount > 0) "$marketAlertsActiveCount" else null,
                            badgeColor = amber,
                            testTag = "settings_market_alerts_row",
                            onClick = { showMarketAlertsSheet = true }
                        )
                        SettingsInsetDivider()
                        SettingsRowItem(
                            icon = Icons.Rounded.CurrencyExchange,
                            iconTint = sky,
                            title = "Birikim Kur Eşleşmeleri",
                            subtitle = if (savingCategoriesFromTx.isEmpty()) {
                                "Birikim kalemi eklendiğinde canlı kura bağlayın"
                            } else {
                                "${savingCategoriesFromTx.size} birikim kalemi yapılandırılabilir"
                            },
                            badgeText = if (savingCategoriesFromTx.isNotEmpty()) "${savingCategoriesFromTx.size}" else null,
                            testTag = "settings_market_match_row",
                            onClick = { showMarketMatchSheet = true }
                        )
                    }
                }
            }

            // 5. VERİ & YEDEKLEME
            item(key = "section_data") {
                Column {
                    SettingsSectionHeader("Veri & Yedekleme")
                    SettingsGroupCard {
                        SettingsRowItem(
                            icon = Icons.Rounded.Download,
                            iconTint = sky,
                            title = "Excel Olarak Dışa Aktar",
                            subtitle = "${uiState.transactions.size} işlem kaydını .xls yedek dosyasına aktar",
                            testTag = "settings_export_xls_row",
                            onClick = { exportLauncher.launch("islemler_yedek.xls") }
                        )
                        SettingsInsetDivider()
                        SettingsRowItem(
                            icon = Icons.Rounded.UploadFile,
                            iconTint = mint,
                            title = "Excel Dosyasından İçe Aktar",
                            subtitle = "Kayıtlı .xls yedeğinden işlemleri geri yükle",
                            testTag = "settings_import_xls_row",
                            onClick = {
                                importLauncher.launch(
                                    arrayOf(
                                        "application/vnd.ms-excel",
                                        "application/excel",
                                        "application/x-excel",
                                        "application/x-msexcel",
                                        "*/*"
                                    )
                                )
                            }
                        )
                    }
                    Text(
                        text = "Şablon sütunları: Id, Tarih, Tür, Kategori, Kişi, Tutar, Miktar, Birim Fiyat, Ödendi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                        modifier = Modifier.padding(start = 8.dp, top = 6.dp, end = 8.dp)
                    )
                }
            }

            // 6. UYGULAMA & GÜVENLİK
            item(key = "section_app_security") {
                Column {
                    SettingsSectionHeader("Uygulama & Güvenlik")
                    SettingsGroupCard {
                        SettingsRowItem(
                            icon = Icons.Rounded.SystemUpdate,
                            iconTint = MaterialTheme.colorScheme.primary,
                            title = "Güncellemeleri Denetle",
                            subtitle = "Mevcut sürüm: v${BuildConfig.VERSION_NAME}",
                            testTag = "settings_check_update_row",
                            onClick = {
                                updateDialogState = UpdateDialogState.Checking
                                coroutineScope.launch {
                                    val info = appUpdater.checkForUpdate()
                                    updateDialogState = if (info != null) {
                                        UpdateDialogState.Available(info)
                                    } else {
                                        UpdateDialogState.UpToDate(BuildConfig.VERSION_NAME)
                                    }
                                }
                            }
                        )
                        SettingsInsetDivider()
                        SettingsRowItem(
                            icon = Icons.Rounded.DeleteForever,
                            iconTint = coral,
                            title = "Tüm Verileri Sıfırla",
                            subtitle = "İşlemler, kategoriler ve kişiler kalıcı olarak silinir",
                            titleColor = coral,
                            testTag = "settings_clear_all_row",
                            onClick = { showClearDialog = true }
                        )
                    }
                }
            }
        }
    }

    // --- DEDICATED MODALS & DIALOGS ---

    if (showCategorySheet) {
        CategoryManagerBottomSheet(
            incomeCategories = incomeCategories,
            expenseCategories = expenseCategories,
            savingCategories = savingCategories,
            onAddCategory = { name, type -> viewModel.addCategory(AppCategory(name = name, type = type)) },
            onUpdateCategory = { cat, newName -> viewModel.updateCategory(cat.copy(name = newName)) },
            onDeleteCategory = { cat -> viewModel.deleteCategory(cat) },
            onDismiss = { showCategorySheet = false }
        )
    }

    if (showPersonSheet) {
        PersonManagerBottomSheet(
            persons = persons,
            onAddPerson = { name -> viewModel.addPerson(Person(name = name)) },
            onUpdatePerson = { person, newName -> viewModel.updatePerson(person.copy(name = newName)) },
            onDeletePerson = { person -> viewModel.deletePerson(person) },
            onDismiss = { showPersonSheet = false }
        )
    }

    if (showPaymentRemindersSheet) {
        PaymentRemindersBottomSheet(
            remindDueDay = remindDueDay,
            remindOverdue = remindOverdue,
            remindUpcomingDays = remindUpcomingDays,
            updateIntervalHours = updateIntervalHours,
            silentHoursEnabled = silentHoursEnabled,
            silentHoursStart = silentHoursStart,
            silentHoursEnd = silentHoursEnd,
            onRemindDueDayChange = {
                remindDueDay = it
                viewModel.preferenceManager.remindDueDay = it
            },
            onRemindOverdueChange = {
                remindOverdue = it
                viewModel.preferenceManager.remindOverdue = it
            },
            onRemindUpcomingDaysChange = {
                remindUpcomingDays = it
                viewModel.preferenceManager.remindUpcomingDays = it
            },
            onUpdateIntervalHoursChange = { hours ->
                updateIntervalHours = hours
                viewModel.preferenceManager.updateIntervalHours = hours
                val workRequest = PeriodicWorkRequestBuilder<PaymentReminderWorker>(
                    hours.toLong(),
                    TimeUnit.HOURS
                ).build()
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "PaymentReminderWork",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
            },
            onSilentHoursEnabledChange = {
                silentHoursEnabled = it
                viewModel.preferenceManager.silentHoursEnabled = it
            },
            onSilentHoursStartChange = {
                silentHoursStart = it
                viewModel.preferenceManager.silentHoursStart = it
            },
            onSilentHoursEndChange = {
                silentHoursEnd = it
                viewModel.preferenceManager.silentHoursEnd = it
            },
            onDismiss = { showPaymentRemindersSheet = false }
        )
    }

    if (showMonthlyPaymentRulesSheet) {
        MonthlyPaymentRemindersBottomSheet(
            rules = monthlyPaymentRules,
            expenseCategories = expenseCategories,
            transactions = uiState.transactions,
            persons = persons,
            remindMissingEnabled = remindMissingMonthlyPayments,
            onToggleMasterNotification = { viewModel.setRemindMissingMonthlyPayments(it) },
            onToggleItem = { cat, enabled, amt, person ->
                viewModel.toggleMonthlyPaymentRule(cat, enabled, amt, person)
            },
            onSaveRule = { viewModel.saveMonthlyPaymentRule(it) },
            onDeleteRule = { viewModel.deleteMonthlyPaymentRule(it) },
            onSkipForCurrentMonth = { cat, skip ->
                viewModel.skipMonthlyPaymentForCurrentMonth(cat, skip)
            },
            onSendTestNotification = { cb ->
                viewModel.sendTestMissingPaymentNotification(cb)
            },
            onDismiss = { showMonthlyPaymentRulesSheet = false }
        )
    }

    if (showMarketAlertsSheet) {
        MarketAlertsBottomSheet(
            scheduleConfig = marketSchedule,
            priceAlarms = marketAlarms,
            goldPrices = goldPrices,
            bankRates = bankRates,
            ziraatRates = ziraatRates,
            onUpdateSchedule = { viewModel.updateMarketScheduleConfig(it) },
            onSaveAlarm = { viewModel.addOrUpdateMarketAlarm(it) },
            onToggleAlarm = { id, enabled -> viewModel.toggleMarketAlarm(id, enabled) },
            onDeleteAlarm = { id -> viewModel.deleteMarketAlarm(id) },
            onSendTestSummary = { cb -> viewModel.sendTestMarketSummary(cb) },
            onDismiss = { showMarketAlertsSheet = false }
        )
    }

    if (showMarketMatchSheet) {
        MarketMatchingBottomSheet(
            savingCategories = savingCategoriesFromTx,
            goldPrices = goldPrices,
            getMatchCode = { cat -> viewModel.preferenceManager.getMarketMatch(cat) },
            onSelectMatch = { cat, code ->
                viewModel.preferenceManager.setMarketMatch(cat, code)
                viewModel.triggerCustomPricesRefresh()
            },
            onDismiss = { showMarketMatchSheet = false }
        )
    }

    updateDialogState?.let { dialogState ->
        AppUpdateDialog(
            state = dialogState,
            onInstall = { info ->
                appUpdater.downloadAndInstallUpdate(info)
                Toast.makeText(context, "Güncelleme indiriliyor...", Toast.LENGTH_SHORT).show()
                updateDialogState = null
            },
            onDismiss = { updateDialogState = null }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Tüm Veriler Silinsin Mi?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Tüm gelir, gider ve birikim işlemleri ile kişiler ve kategoriler kalıcı olarak silinecektir. Bu işlem geri alınamaz.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllData()
                        showClearDialog = false
                        Toast.makeText(context, "Tüm veriler temizlendi", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Text("Evet, Tümünü Sil") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("İptal") }
            }
        )
    }
}

@Composable
private fun SettingsHeroStatCell(
    value: String,
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ThemeSegmentedToggle(
    isDarkTheme: Boolean,
    onSelectDark: (Boolean) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)
    val trackBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.7f else 0.9f)
    val selectedBg = MaterialTheme.colorScheme.surface

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = trackBg,
        border = BorderStroke(0.7.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ThemeToggleChip(
                icon = Icons.Rounded.LightMode,
                label = "Açık",
                selected = !isDarkTheme,
                selectedBg = selectedBg,
                onClick = { onSelectDark(false) },
                testTag = "theme_toggle_light"
            )
            ThemeToggleChip(
                icon = Icons.Rounded.DarkMode,
                label = "Koyu",
                selected = isDarkTheme,
                selectedBg = selectedBg,
                onClick = { onSelectDark(true) },
                testTag = "theme_toggle_dark"
            )
        }
    }
}

@Composable
private fun ThemeToggleChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    selectedBg: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) selectedBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
