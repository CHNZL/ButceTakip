package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BesPortfolio
import com.example.ui.theme.*
import com.example.util.AmountVisualTransformation
import com.example.util.FormatUtil
import com.example.util.formatDoubleForInput
import com.example.util.formatInputAmount
import com.example.util.parseFormattedAmount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BesDialog(
    besPortfolio: BesPortfolio?,
    onDismiss: () -> Unit,
    onSave: (BesPortfolio) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val indigo = if (isDark) CyberNeonPurple else Color(0xFF6366F1)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)

    var holderNameText by remember { mutableStateOf(besPortfolio?.holderName ?: "BES") }
    var startDate by remember { mutableLongStateOf(besPortfolio?.startDate ?: System.currentTimeMillis()) }
    var investmentText by remember { mutableStateOf(besPortfolio?.investment?.let { formatDoubleForInput(it) } ?: "") }
    var investmentReturnText by remember { mutableStateOf(besPortfolio?.investmentReturn?.let { formatDoubleForInput(it) } ?: "") }
    var stateContributionText by remember { mutableStateOf(besPortfolio?.stateContribution?.let { formatDoubleForInput(it) } ?: "") }
    var stateContributionReturnText by remember { mutableStateOf(besPortfolio?.stateContributionReturn?.let { formatDoubleForInput(it) } ?: "") }
    var isRetired by remember { mutableStateOf(besPortfolio?.isRetired ?: false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale("tr", "TR")) }
    val currencyFormat = remember { FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 } }
    val amountVisual = remember { AmountVisualTransformation() }

    val inv = remember(investmentText) { parseFormattedAmount(investmentText) }
    val invRet = remember(investmentReturnText) { parseFormattedAmount(investmentReturnText) }
    val stateCont = remember(stateContributionText) { parseFormattedAmount(stateContributionText) }
    val stateRet = remember(stateContributionReturnText) { parseFormattedAmount(stateContributionReturnText) }
    val totalBes = inv + invRet + stateCont + stateRet
    val totalReturn = invRet + stateRet

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDate = it }
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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(indigo.copy(alpha = if (isDark) 0.18f else 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = indigo,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (besPortfolio != null) "BES Portföyünü Düzenle" else "Bireysel Emeklilik (BES)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Katkı payı ve devlet katkısı değerlerini güncelleyin",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Kapat")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Live BES Hero Summary
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.9.dp, indigo.copy(alpha = if (isDark) 0.42f else 0.30f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        indigo.copy(alpha = if (isDark) 0.12f else 0.07f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "TOPLAM BES BİRİKİMİ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.8.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currencyFormat.format(totalBes),
                            style = TabularHeroStyle,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(thickness = 0.7.dp, color = borderColor.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Toplam Getiri",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "+${currencyFormat.format(totalReturn)}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = mint
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Devlet Katkısı Toplamı",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = currencyFormat.format(stateCont + stateRet),
                                    style = MaterialTheme.typography.titleSmall.copy(
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

                // Account Info
                SettingsGroupCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = holderNameText,
                            onValueChange = { holderNameText = it },
                            label = { Text("Hesap / Kişi Adı") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Person, contentDescription = null, tint = indigo, modifier = Modifier.size(19.dp))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bes_holder_name_input")
                        )

                        Surface(
                            onClick = { showDatePicker = true },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(0.8.dp, borderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = null,
                                    tint = indigo,
                                    modifier = Modifier.size(19.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "BES Başlangıç Tarihi",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = dateFormat.format(Date(startDate)),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 2x2 Monetary Grid
                SettingsGroupCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "TUTAR VE GETİRİ BİLGİLERİ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.8.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BesAmountField(
                                value = investmentText,
                                onValueChange = { investmentText = formatInputAmount(it) },
                                label = "Yatırım Tutarı",
                                visual = amountVisual,
                                modifier = Modifier.weight(1f)
                            )
                            BesAmountField(
                                value = investmentReturnText,
                                onValueChange = { investmentReturnText = formatInputAmount(it) },
                                label = "Yatırım Getirisi",
                                visual = amountVisual,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BesAmountField(
                                value = stateContributionText,
                                onValueChange = { stateContributionText = formatInputAmount(it) },
                                label = "Devlet Katkısı",
                                visual = amountVisual,
                                modifier = Modifier.weight(1f)
                            )
                            BesAmountField(
                                value = stateContributionReturnText,
                                onValueChange = { stateContributionReturnText = formatInputAmount(it) },
                                label = "Katkı Getirisi",
                                visual = amountVisual,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Vesting switch
                SettingsGroupCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isRetired = !isRetired }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(mint.copy(alpha = if (isDark) 0.18f else 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = null,
                                tint = mint,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Emekliliği Hak Ettim",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "%100 devlet katkısı hak edişi uygulanır",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isRetired,
                            onCheckedChange = { isRetired = it },
                            modifier = Modifier.testTag("bes_retired_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // Sticky Bottom Bar
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
                        val newBes = BesPortfolio(
                            id = besPortfolio?.id ?: 0,
                            holderName = holderNameText.trim().ifBlank { "BES" },
                            startDate = startDate,
                            investment = inv,
                            investmentReturn = invRet,
                            stateContribution = stateCont,
                            stateContributionReturn = stateRet,
                            isRetired = isRetired
                        )
                        onSave(newBes)
                    },
                    modifier = Modifier
                        .weight(1.45f)
                        .height(50.dp)
                        .testTag("save_bes_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kaydet", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BesAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visual: AmountVisualTransformation,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, maxLines = 1) },
        suffix = { Text("₺", fontWeight = FontWeight.Bold) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        visualTransformation = visual,
        singleLine = true,
        shape = RoundedCornerShape(13.dp),
        textStyle = TextStyle(
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            fontFeatureSettings = "tnum"
        ),
        modifier = modifier
    )
}
