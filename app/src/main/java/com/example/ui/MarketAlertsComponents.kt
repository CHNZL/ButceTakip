package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.BankRate
import com.example.data.GoldPrice
import com.example.data.MarketAssetOption
import com.example.data.MarketPriceAlarm
import com.example.data.MarketPriceParser
import com.example.data.MarketScheduleConfig
import com.example.ui.theme.*
import java.util.Locale
import java.util.UUID

/**
 * Unified Market Notifications & Price Limit Alarms panel.
 * Embeds cleanly inside both HomeMarketBottomSheet ("Bildirimler" tab) and SettingsScreen modal.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarketAlertsControlPanel(
    scheduleConfig: MarketScheduleConfig,
    priceAlarms: List<MarketPriceAlarm>,
    goldPrices: List<GoldPrice>,
    bankRates: List<BankRate>,
    ziraatRates: List<BankRate>,
    onUpdateSchedule: (MarketScheduleConfig) -> Unit,
    onSaveAlarm: (MarketPriceAlarm) -> Unit,
    onToggleAlarm: (String, Boolean) -> Unit,
    onDeleteAlarm: (String) -> Unit,
    onSendTestSummary: ((Boolean, String) -> Unit) -> Unit,
    preselectedAssetKeyForNewAlarm: String? = null,
    onConsumePreselectedAsset: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)
    val dividerColor = if (isDark) Color(0xFF1F293D) else Color(0xFFE2E8F0)

    val catalog = remember(goldPrices, bankRates, ziraatRates) {
        MarketPriceParser.buildCatalog(goldPrices, bankRates, ziraatRates)
    }
    val catalogByKey = remember(catalog) { catalog.associateBy { it.key } }

    var hasNotifPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPermission = granted
        if (!granted) {
            Toast.makeText(context, "Bildirimler için sistem izni gerekiyor.", Toast.LENGTH_LONG).show()
        }
    }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var isSendingTest by remember { mutableStateOf(false) }
    var showAlarmDialog by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<MarketPriceAlarm?>(null) }
    var preselectedAssetKey by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(preselectedAssetKeyForNewAlarm) {
        if (!preselectedAssetKeyForNewAlarm.isNullOrBlank()) {
            editingAlarm = null
            preselectedAssetKey = preselectedAssetKeyForNewAlarm
            showAlarmDialog = true
            onConsumePreselectedAsset()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Permission banner on Android 13+ if denied
        if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Surface(
                color = amber.copy(alpha = 0.14f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.8.dp, amber.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsOff,
                        contentDescription = null,
                        tint = amber,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bildirim izni kapalı",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Piyasa özeti ve fiyat alarmlarının ulaşması için izin verin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(
                        onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                        modifier = Modifier.testTag("request_notif_permission_btn")
                    ) {
                        Text("İzin Ver", fontWeight = FontWeight.Bold, color = amber)
                    }
                }
            }
        }

        // --- SECTION 1: SCHEDULED MARKET SUMMARY ---
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(sky.copy(alpha = if (isDark) 0.16f else 0.11f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = sky,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Zamanlı Piyasa Özeti",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (scheduleConfig.enabled) {
                                "${scheduleConfig.startTime}–${scheduleConfig.endTime} • ${formatIntervalLabel(scheduleConfig.intervalMinutes)}"
                            } else {
                                "Seçili saatlerde ve sıklıkta kur bildirimi"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Switch(
                    checked = scheduleConfig.enabled,
                    onCheckedChange = { enabled ->
                        if (enabled && !hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        onUpdateSchedule(scheduleConfig.copy(enabled = enabled))
                    },
                    modifier = Modifier.testTag("market_schedule_switch")
                )
            }

            AnimatedVisibility(visible = scheduleConfig.enabled) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(thickness = 0.6.dp, color = dividerColor)

                    // Time Window Pickers
                    Text(
                        text = "AKTİF SAAT ARALIĞI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) CyberDarkSurfaceVariant.copy(alpha = 0.65f) else CyberLightSurfaceVariant)
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showStartPicker = true }
                                .padding(vertical = 6.dp)
                                .testTag("market_start_time_btn"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Başlangıç", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = scheduleConfig.startTime,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(34.dp)
                                .background(dividerColor)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showEndPicker = true }
                                .padding(vertical = 6.dp)
                                .testTag("market_end_time_btn"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Bitiş", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = scheduleConfig.endTime,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Frequency Selector Chips
                    Text(
                        text = "BİLDİRİM SIKLIĞI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val intervals = listOf(15, 30, 60, 120, 240)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        intervals.forEach { mins ->
                            val selected = scheduleConfig.intervalMinutes == mins
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    onUpdateSchedule(scheduleConfig.copy(intervalMinutes = mins))
                                },
                                label = {
                                    Text(
                                        text = formatIntervalLabel(mins),
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        softWrap = false,
                                        maxLines = 1
                                    )
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Weekdays Only Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sadece Hafta İçi (Pzt–Cum)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Hafta sonu piyasa kapalıyken bildirim gönderme",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = scheduleConfig.weekdaysOnly,
                            onCheckedChange = { onUpdateSchedule(scheduleConfig.copy(weekdaysOnly = it)) }
                        )
                    }

                    // Instrument Multi-Select
                    Text(
                        text = "ÖZETTE GÖSTERİLECEK KURLAR (${scheduleConfig.selectedAssetKeys.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        catalog.forEach { asset ->
                            val isSelected = asset.key in scheduleConfig.selectedAssetKeys
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val next = scheduleConfig.selectedAssetKeys.toMutableSet()
                                    if (isSelected) {
                                        if (next.size > 1) next.remove(asset.key)
                                    } else {
                                        next.add(asset.key)
                                    }
                                    onUpdateSchedule(scheduleConfig.copy(selectedAssetKeys = next))
                                },
                                label = {
                                    Text(
                                        text = asset.shortLabel,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        softWrap = false,
                                        maxLines = 1
                                    )
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Test Notification Button
                    OutlinedButton(
                        onClick = {
                            if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                return@OutlinedButton
                            }
                            isSendingTest = true
                            onSendTestSummary { _, message ->
                                isSendingTest = false
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isSendingTest,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("market_test_notify_btn")
                    ) {
                        if (isSendingTest) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Rounded.NotificationsActive, contentDescription = null, modifier = Modifier.size(17.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Şimdi Test Bildirimi Gönder", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- SECTION 2: PRICE LIMIT ALARMS ---
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(amber.copy(alpha = if (isDark) 0.16f else 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationImportant,
                            contentDescription = null,
                            tint = amber,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fiyat Limit Alarmları (${priceAlarms.count { it.isEnabled }})",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Hedef fiyata ulaşıldığında anlık uyarı al",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                FilledTonalButton(
                    onClick = {
                        if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        editingAlarm = null
                        preselectedAssetKey = null
                        showAlarmDialog = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("market_add_alarm_btn")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limit Ekle", fontSize = 12.sp, fontWeight = FontWeight.Bold, softWrap = false)
                }
            }

            if (priceAlarms.isEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) CyberDarkSurfaceVariant.copy(alpha = 0.5f) else CyberLightSurfaceVariant)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz fiyat limit alarmı yok. Altın veya döviz belirlediğiniz sınıra geldiğinde bildirim almak için \"Limit Ekle\"ye dokunun.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(thickness = 0.6.dp, color = dividerColor)

                priceAlarms.forEachIndexed { idx, alarm ->
                    val liveAsset = catalogByKey[alarm.assetKey]
                    val currentVal = if (alarm.isSell) liveAsset?.sellValue else liveAsset?.buyValue
                    val accent = if (alarm.isAbove) mint else coral

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                editingAlarm = alarm
                                preselectedAssetKey = null
                                showAlarmDialog = true
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(accent.copy(alpha = if (alarm.isEnabled) 0.15f else 0.06f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (alarm.isAbove) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                                contentDescription = null,
                                tint = if (alarm.isEnabled) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = alarm.assetLabel,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Surface(
                                    color = accent.copy(alpha = 0.13f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    val dirSym = if (alarm.isAbove) "≥" else "≤"
                                    val fieldTxt = if (alarm.isSell) "Satış" else "Alış"
                                    Text(
                                        text = "$fieldTxt $dirSym ${MarketPriceParser.formatPrice(alarm.targetPrice)}",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accent,
                                        softWrap = false,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (currentVal != null && currentVal > 0.0) {
                                    val diffPct = ((alarm.targetPrice - currentVal) / currentVal) * 100.0
                                    Text(
                                        text = String.format(
                                            Locale("tr"),
                                            "Güncel %s (%s%.1f%%)",
                                            MarketPriceParser.formatPrice(currentVal),
                                            if (diffPct >= 0) "+" else "",
                                            diffPct
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        softWrap = false,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = if (alarm.oneTime) "• Tek sefer" else "• Sürekli",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    softWrap = false,
                                    maxLines = 1
                                )
                            }
                        }

                        Switch(
                            checked = alarm.isEnabled,
                            onCheckedChange = { onToggleAlarm(alarm.id, it) }
                        )

                        IconButton(
                            onClick = { onDeleteAlarm(alarm.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Alarmı Sil",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (idx < priceAlarms.lastIndex) {
                        HorizontalDivider(thickness = 0.6.dp, color = dividerColor)
                    }
                }
            }
        }
    }

    if (showStartPicker) {
        MinimalTimePickerDialog(
            title = "Bildirim Başlangıç Saati",
            currentValue = scheduleConfig.startTime,
            onConfirm = {
                onUpdateSchedule(scheduleConfig.copy(startTime = it))
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        MinimalTimePickerDialog(
            title = "Bildirim Bitiş Saati",
            currentValue = scheduleConfig.endTime,
            onConfirm = {
                onUpdateSchedule(scheduleConfig.copy(endTime = it))
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }

    if (showAlarmDialog) {
        MarketPriceAlarmDialog(
            catalog = catalog,
            existingAlarm = editingAlarm,
            initialAssetKey = editingAlarm?.assetKey ?: preselectedAssetKey,
            onConfirm = { saved ->
                onSaveAlarm(saved)
                showAlarmDialog = false
                editingAlarm = null
                preselectedAssetKey = null
            },
            onDismiss = {
                showAlarmDialog = false
                editingAlarm = null
                preselectedAssetKey = null
            }
        )
    }
}

/**
 * Dialog for creating or editing a live market price threshold alarm with quick % offset chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarketPriceAlarmDialog(
    catalog: List<MarketAssetOption>,
    existingAlarm: MarketPriceAlarm?,
    initialAssetKey: String?,
    onConfirm: (MarketPriceAlarm) -> Unit,
    onDismiss: () -> Unit
) {
    val defaultOption = remember(catalog, initialAssetKey) {
        catalog.firstOrNull { it.key == initialAssetKey } ?: catalog.firstOrNull()
    }
    var selectedOption by remember(defaultOption) { mutableStateOf(defaultOption) }
    var assetDropdownExpanded by remember { mutableStateOf(false) }

    var priceField by remember { mutableStateOf(existingAlarm?.priceField ?: MarketPriceAlarm.FIELD_SELL) }
    var direction by remember { mutableStateOf(existingAlarm?.direction ?: MarketPriceAlarm.DIR_ABOVE) }
    var oneTime by remember { mutableStateOf(existingAlarm?.oneTime ?: true) }

    val initialLivePrice = remember(selectedOption, priceField) {
        if (priceField == MarketPriceAlarm.FIELD_SELL) {
            selectedOption?.sellValue ?: selectedOption?.buyValue
        } else {
            selectedOption?.buyValue ?: selectedOption?.sellValue
        }
    }

    var targetPriceText by remember {
        mutableStateOf(
            if (existingAlarm != null) {
                if (existingAlarm.targetPrice >= 1000.0) {
                    String.format(Locale.US, "%.0f", existingAlarm.targetPrice)
                } else {
                    String.format(Locale.US, "%.2f", existingAlarm.targetPrice)
                }
            } else {
                initialLivePrice?.let {
                    if (it >= 1000.0) String.format(Locale.US, "%.0f", it) else String.format(Locale.US, "%.2f", it)
                } ?: ""
            }
        )
    }
    var errorText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingAlarm == null) "Fiyat Limit Alarmı Kur" else "Alarmı Düzenle",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Asset Picker
                Text("Enstrüman", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box {
                    OutlinedCard(
                        onClick = { assetDropdownExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedOption?.label ?: "Seçiniz",
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val liveText = selectedOption?.let {
                                    "Alış: ${it.buyText}  •  Satış: ${it.sellText}"
                                } ?: ""
                                if (liveText.isNotBlank()) {
                                    Text(
                                        text = liveText,
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = assetDropdownExpanded,
                        onDismissRequest = { assetDropdownExpanded = false },
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        catalog.forEach { opt ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(opt.label, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = "Alış: ${opt.buyText} • Satış: ${opt.sellText}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedOption = opt
                                    assetDropdownExpanded = false
                                    val live = if (priceField == MarketPriceAlarm.FIELD_SELL) opt.sellValue ?: opt.buyValue else opt.buyValue ?: opt.sellValue
                                    if (live != null) {
                                        targetPriceText = if (live >= 1000.0) String.format(Locale.US, "%.0f", live) else String.format(Locale.US, "%.2f", live)
                                    }
                                }
                            )
                        }
                    }
                }

                // Price Field: Satış vs Alış
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = priceField == MarketPriceAlarm.FIELD_SELL,
                        onClick = { priceField = MarketPriceAlarm.FIELD_SELL },
                        label = { Text("Satış Fiyatı", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = priceField == MarketPriceAlarm.FIELD_BUY,
                        onClick = { priceField = MarketPriceAlarm.FIELD_BUY },
                        label = { Text("Alış Fiyatı", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Condition: Above vs Below
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = direction == MarketPriceAlarm.DIR_ABOVE,
                        onClick = { direction = MarketPriceAlarm.DIR_ABOVE },
                        label = { Text("Üzerine (≥)", fontWeight = FontWeight.SemiBold, softWrap = false) },
                        leadingIcon = { Icon(Icons.Rounded.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = direction == MarketPriceAlarm.DIR_BELOW,
                        onClick = { direction = MarketPriceAlarm.DIR_BELOW },
                        label = { Text("Altına (≤)", fontWeight = FontWeight.SemiBold, softWrap = false) },
                        leadingIcon = { Icon(Icons.Rounded.TrendingDown, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Target Price Input
                OutlinedTextField(
                    value = targetPriceText,
                    onValueChange = {
                        targetPriceText = it
                        errorText = ""
                    },
                    label = { Text("Hedef Limit (₺)") },
                    singleLine = true,
                    isError = errorText.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alarm_target_price_input")
                )

                // Quick Offset Helper Chips based on live price
                val baseVal = if (priceField == MarketPriceAlarm.FIELD_SELL) {
                    selectedOption?.sellValue ?: selectedOption?.buyValue
                } else {
                    selectedOption?.buyValue ?: selectedOption?.sellValue
                }
                if (baseVal != null && baseVal > 0.0) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(-3.0 to "-%3", -1.0 to "-%1", 1.0 to "+%1", 3.0 to "+%3").forEach { (pct, label) ->
                            AssistChip(
                                onClick = {
                                    val computed = baseVal * (1.0 + pct / 100.0)
                                    targetPriceText = if (computed >= 1000.0) {
                                        String.format(Locale.US, "%.0f", computed)
                                    } else {
                                        String.format(Locale.US, "%.2f", computed)
                                    }
                                    direction = if (pct >= 0) MarketPriceAlarm.DIR_ABOVE else MarketPriceAlarm.DIR_BELOW
                                    errorText = ""
                                },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // One-time vs Recurring toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tek Seferlik Alarm", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(
                            text = if (oneTime) "Limit tetiklenince otomatik durur" else "Saatlik aralıkla tekrar uyarır",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = oneTime, onCheckedChange = { oneTime = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val opt = selectedOption
                    val parsed = MarketPriceParser.parseDoubleOrNull(targetPriceText)
                    if (opt == null) {
                        errorText = "Lütfen bir enstrüman seçin."
                        return@Button
                    }
                    if (parsed == null || parsed <= 0.0) {
                        errorText = "Geçerli bir hedef fiyat girin."
                        return@Button
                    }
                    val saved = MarketPriceAlarm(
                        id = existingAlarm?.id ?: UUID.randomUUID().toString(),
                        assetKey = opt.key,
                        assetLabel = opt.label,
                        priceField = priceField,
                        direction = direction,
                        targetPrice = parsed,
                        isEnabled = true,
                        oneTime = oneTime,
                        lastTriggeredAt = 0L
                    )
                    onConfirm(saved)
                },
                modifier = Modifier.testTag("save_market_alarm_btn")
            ) {
                Text("Alarmı Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

/**
 * Standalone ModalBottomSheet wrapper used from SettingsScreen to open Market Alerts cleanly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketAlertsBottomSheet(
    scheduleConfig: MarketScheduleConfig,
    priceAlarms: List<MarketPriceAlarm>,
    goldPrices: List<GoldPrice>,
    bankRates: List<BankRate>,
    ziraatRates: List<BankRate>,
    onUpdateSchedule: (MarketScheduleConfig) -> Unit,
    onSaveAlarm: (MarketPriceAlarm) -> Unit,
    onToggleAlarm: (String, Boolean) -> Unit,
    onDeleteAlarm: (String) -> Unit,
    onSendTestSummary: ((Boolean, String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Piyasa Bildirimleri & Alarmlar",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            MarketAlertsControlPanel(
                scheduleConfig = scheduleConfig,
                priceAlarms = priceAlarms,
                goldPrices = goldPrices,
                bankRates = bankRates,
                ziraatRates = ziraatRates,
                onUpdateSchedule = onUpdateSchedule,
                onSaveAlarm = onSaveAlarm,
                onToggleAlarm = onToggleAlarm,
                onDeleteAlarm = onDeleteAlarm,
                onSendTestSummary = onSendTestSummary
            )
        }
    }
}

private fun formatIntervalLabel(minutes: Int): String = when {
    minutes < 60 -> "$minutes dk"
    minutes % 60 == 0 -> "${minutes / 60} saat"
    else -> "$minutes dk"
}
