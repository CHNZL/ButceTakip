package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.AmountVisualTransformation
import com.example.util.FormatUtil
import com.example.util.formatInputAmount
import com.example.util.parseFormattedAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, Double) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)

    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf("") }

    val amountVisual = remember { AmountVisualTransformation() }
    val currencyFormat = remember { FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 } }

    val targetVal = remember(target) { parseFormattedAmount(target) }
    val savedVal = remember(saved) { parseFormattedAmount(saved) }
    val progress = if (targetVal > 0.0) (savedVal / targetVal).toFloat().coerceIn(0f, 1f) else 0f
    val pct = (progress * 100).toInt()
    val remaining = (targetVal - savedVal).coerceAtLeast(0.0)

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
                            .background(mint.copy(alpha = if (isDark) 0.18f else 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Savings,
                            contentDescription = null,
                            tint = mint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Yeni Birikim Hedefi",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Hedef tutarınızı ve başlangıç birikiminizi belirleyin",
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
                // Live Goal Progress Preview
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.9.dp, mint.copy(alpha = if (isDark) 0.40f else 0.30f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        mint.copy(alpha = if (isDark) 0.10f else 0.06f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = title.trim().ifBlank { "HEDEF ÖNİZLEME" }.uppercase(java.util.Locale("tr", "TR")),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 0.8.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "%$pct",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontFeatureSettings = "tnum"
                                ),
                                fontWeight = FontWeight.Bold,
                                color = mint
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(CircleShape),
                            color = mint,
                            trackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Biriken: ${currencyFormat.format(savedVal)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Kalan: ${currencyFormat.format(remaining)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontFeatureSettings = "tnum"
                                ),
                                fontWeight = FontWeight.SemiBold,
                                color = sky
                            )
                        }
                    }
                }

                SettingsGroupCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Hedef Adı (Araba, Ev, Tatil vb.)") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Flag, contentDescription = null, tint = mint, modifier = Modifier.size(19.dp))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("saving_title")
                        )

                        OutlinedTextField(
                            value = target,
                            onValueChange = { target = formatInputAmount(it) },
                            label = { Text("Hedeflenen Tutar") },
                            suffix = { Text("₺", fontWeight = FontWeight.Bold, color = mint) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            visualTransformation = amountVisual,
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            textStyle = TextStyle(
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                fontFeatureSettings = "tnum"
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("saving_target")
                        )

                        OutlinedTextField(
                            value = saved,
                            onValueChange = { saved = formatInputAmount(it) },
                            label = { Text("Mevcut Birikim (Opsiyonel)") },
                            suffix = { Text("₺", fontWeight = FontWeight.Bold, color = sky) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            visualTransformation = amountVisual,
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            textStyle = TextStyle(
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                fontFeatureSettings = "tnum"
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("saving_current")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

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
                        if (title.isNotBlank() && targetVal > 0.0) {
                            onSave(title.trim(), targetVal, savedVal)
                        }
                    },
                    enabled = title.isNotBlank() && targetVal > 0.0,
                    modifier = Modifier
                        .weight(1.45f)
                        .height(50.dp)
                        .testTag("save_saving_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = mint, contentColor = Color.White),
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
