package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.BuildConfig
import com.example.ui.theme.*
import com.example.updater.UpdateInfo

sealed class UpdateDialogState {
    data object Checking : UpdateDialogState()
    data class Available(val info: UpdateInfo) : UpdateDialogState()
    data class UpToDate(val currentVersion: String) : UpdateDialogState()
}

@Composable
fun AppUpdateDialog(
    state: UpdateDialogState,
    onInstall: (UpdateInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)

    val heroAccent = when (state) {
        is UpdateDialogState.Checking -> sky
        is UpdateDialogState.Available -> mint
        is UpdateDialogState.UpToDate -> mint
    }

    Dialog(
        onDismissRequest = { if (state !is UpdateDialogState.Checking) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .widthIn(max = 420.dp)
                .testTag("app_update_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.9.dp, borderColor),
            shadowElevation = if (isDark) 6.dp else 10.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Hero Header Band
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = if (isDark) {
                                    listOf(
                                        heroAccent.copy(alpha = 0.18f),
                                        Color(0xFF131C2B),
                                        MaterialTheme.colorScheme.surface
                                    )
                                } else {
                                    listOf(
                                        heroAccent.copy(alpha = 0.12f),
                                        Color(0xFFF8FAFC),
                                        Color.White
                                    )
                                }
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    if (state !is UpdateDialogState.Checking) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(34.dp)
                                .testTag("update_dialog_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Kapat",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(heroAccent.copy(alpha = if (isDark) 0.20f else 0.14f))
                                .border(
                                    width = 1.dp,
                                    color = heroAccent.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(18.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when (state) {
                                is UpdateDialogState.Checking -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(26.dp),
                                        strokeWidth = 2.6.dp,
                                        color = heroAccent
                                    )
                                }
                                is UpdateDialogState.Available -> {
                                    Icon(
                                        imageVector = Icons.Rounded.SystemUpdate,
                                        contentDescription = null,
                                        tint = heroAccent,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                is UpdateDialogState.UpToDate -> {
                                    Icon(
                                        imageVector = Icons.Rounded.Verified,
                                        contentDescription = null,
                                        tint = heroAccent,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val statusChipText = when (state) {
                            is UpdateDialogState.Checking -> "DENETLENİYOR"
                            is UpdateDialogState.Available -> "YENİ SÜRÜM HAZIR"
                            is UpdateDialogState.UpToDate -> "GÜNCEL SÜRÜM"
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = heroAccent.copy(alpha = if (isDark) 0.18f else 0.12f)
                        ) {
                            Text(
                                text = statusChipText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 0.8.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = heroAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val headlineText = when (state) {
                            is UpdateDialogState.Checking -> "Güncelleme Kontrol Ediliyor"
                            is UpdateDialogState.Available -> "Yeni Güncelleme Mevcut"
                            is UpdateDialogState.UpToDate -> "Uygulamanız Güncel"
                        }
                        Text(
                            text = headlineText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                HorizontalDivider(thickness = 0.7.dp, color = borderColor.copy(alpha = 0.8f))

                // Body Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (state) {
                        is UpdateDialogState.Checking -> {
                            Text(
                                text = "Sunucudan en son sürüm bilgileri alınıyor, lütfen bekleyin...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = sky,
                                trackColor = sky.copy(alpha = 0.16f)
                            )
                        }

                        is UpdateDialogState.UpToDate -> {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(0.7.dp, borderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Yüklü Sürüm",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "v${state.currentVersion}",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontFeatureSettings = "tnum"
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = mint
                                    )
                                }
                            }
                            Text(
                                text = "En güncel özellikleri ve iyileştirmeleri kullanıyorsunuz.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("update_dialog_ok_button")
                            ) {
                                Text("Tamam", fontWeight = FontWeight.Bold)
                            }
                        }

                        is UpdateDialogState.Available -> {
                            val info = state.info

                            // Version comparison strip
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(0.7.dp, borderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = "Mevcut",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "v${BuildConfig.VERSION_NAME}",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = JetBrainsMonoFamily,
                                                fontFeatureSettings = "tnum"
                                            ),
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(mint.copy(alpha = if (isDark) 0.18f else 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                            contentDescription = null,
                                            tint = mint,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Yeni Sürüm",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = mint
                                        )
                                        Text(
                                            text = "v${info.versionName}",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = JetBrainsMonoFamily,
                                                fontFeatureSettings = "tnum"
                                            ),
                                            fontWeight = FontWeight.ExtraBold,
                                            color = mint
                                        )
                                    }
                                }
                            }

                            // Parsed release notes card
                            val parsedNotes = remember(info.releaseNotes) {
                                val lines = info.releaseNotes
                                    .lines()
                                    .map { it.trim().trimStart('-', '*', '•').trim() }
                                    .filter { it.isNotEmpty() }
                                if (lines.isEmpty()) {
                                    listOf(
                                        "Arayüz, hız ve kararlılık iyileştirmeleri",
                                        "Canlı piyasa veri akışı ve bildirim optimizasyonları"
                                    )
                                } else {
                                    lines
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "SÜRÜM NOTLARI",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 0.8.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.background.copy(alpha = if (isDark) 0.55f else 0.7f),
                                    border = BorderStroke(0.7.dp, borderColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 190.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState())
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        parsedNotes.forEach { note ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 6.dp)
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(mint)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = note,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Action Buttons
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onInstall(info) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = mint,
                                        contentColor = if (isDark) Color(0xFF031912) else Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("update_dialog_install_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(19.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Şimdi İndir ve Yükle",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                TextButton(
                                    onClick = onDismiss,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("update_dialog_later_button")
                                ) {
                                    Text(
                                        text = "Daha Sonra",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenuBottomSheet(
    displayName: String,
    accountSubtitle: String,
    profilePictureUrl: String?,
    isSignedIn: Boolean,
    isCloudAuthenticated: Boolean,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onCheckUpdate: () -> Unit,
    onOpenSavingsCalculator: () -> Unit,
    onOpenMortgageCalculator: () -> Unit,
    onOpenSettings: () -> Unit,
    onAuthAction: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val avatarInitial = displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "C"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. EXECUTIVE PROFILE IDENTITY CARD
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
                                    listOf(Color(0xFF131D2E), MaterialTheme.colorScheme.surface)
                                } else {
                                    listOf(mint.copy(alpha = 0.06f), Color.White)
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
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(mint.copy(alpha = if (isDark) 0.18f else 0.12f))
                                .border(1.5.dp, mint.copy(alpha = 0.45f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profilePictureUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = profilePictureUrl,
                                    contentDescription = "Profil Fotoğrafı",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = avatarInitial,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = mint
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (accountSubtitle.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = accountSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = (if (isCloudAuthenticated) mint else sky).copy(alpha = if (isDark) 0.16f else 0.11f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isCloudAuthenticated) mint else sky)
                                    )
                                    Text(
                                        text = if (isCloudAuthenticated) "Bulut Hesabı Aktif" else "Yerel Cihaz Modu",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isCloudAuthenticated) mint else sky
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.Top)
                                .size(36.dp)
                                .testTag("profile_sheet_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Kapat",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 0.7.dp, color = borderColor.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // App Version & Update Quick Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.VerifiedUser,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Sürüm v${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            onClick = onCheckUpdate,
                            shape = RoundedCornerShape(10.dp),
                            color = mint.copy(alpha = if (isDark) 0.18f else 0.12f),
                            border = BorderStroke(0.7.dp, mint.copy(alpha = 0.35f)),
                            modifier = Modifier.testTag("profile_check_update_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SystemUpdate,
                                    contentDescription = null,
                                    tint = mint,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Güncelleme Kontrol",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = mint
                                )
                            }
                        }
                    }
                }
            }

            // 2. FİNANSAL HESAPLAYICILAR
            Column {
                SettingsSectionHeader("Finansal Araçlar")
                SettingsGroupCard {
                    SettingsRowItem(
                        icon = Icons.Rounded.Savings,
                        iconTint = sky,
                        title = "Tasarruf Finansmanı",
                        subtitle = "Taksitli birikim ve teslimat planı hesapla",
                        testTag = "profile_savings_calc_row",
                        onClick = onOpenSavingsCalculator
                    )
                    SettingsInsetDivider()
                    SettingsRowItem(
                        icon = Icons.Rounded.HomeWork,
                        iconTint = amber,
                        title = "Konut Kredisi Hesaplama",
                        subtitle = "Faiz oranı, aylık taksit ve toplam maliyet",
                        testTag = "profile_mortgage_calc_row",
                        onClick = onOpenMortgageCalculator
                    )
                }
            }

            // 3. YÖNETİM & HESAP
            Column {
                SettingsSectionHeader("Yönetim & Hesap")
                SettingsGroupCard {
                    SettingsRowItem(
                        icon = Icons.Rounded.Tune,
                        iconTint = mint,
                        title = "Ayarlar & Yönetim",
                        subtitle = "Kategoriler, kişiler, bildirimler ve yedekleme",
                        testTag = "profile_settings_row",
                        onClick = onOpenSettings
                    )
                    SettingsInsetDivider()
                    SettingsRowItem(
                        icon = if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                        iconTint = if (isDarkTheme) amber else sky,
                        title = "Tema Modu",
                        subtitle = if (isDarkTheme) "Koyu görünüm" else "Açık görünüm",
                        showChevron = false,
                        testTag = "profile_theme_row",
                        trailing = {
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = onToggleTheme,
                                modifier = Modifier.testTag("profile_theme_switch")
                            )
                        }
                    )
                    SettingsInsetDivider()
                    SettingsRowItem(
                        icon = if (isSignedIn) Icons.AutoMirrored.Rounded.Logout else Icons.AutoMirrored.Rounded.Login,
                        iconTint = if (isSignedIn) coral else mint,
                        title = if (isSignedIn) "Çıkış Yap" else "Giriş Yap",
                        subtitle = if (isSignedIn) "Mevcut oturumu güvenli şekilde sonlandır" else "Bulut yedekleme için hesabınıza bağlanın",
                        titleColor = if (isSignedIn) coral else MaterialTheme.colorScheme.onSurface,
                        testTag = "profile_auth_action_row",
                        onClick = onAuthAction
                    )
                }
            }
        }
    }
}
