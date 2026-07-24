package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfExportDialog(
    onDismiss: () -> Unit,
    onExport: (year: Int?, month: Int?) -> Unit // null means all time
) {
    var isAllTime by remember { mutableStateOf(true) }
    
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    var selectedYear by remember { mutableStateOf(currentYear) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) } // 0-indexed

    val years = (currentYear - 5..currentYear).toList()
    val months = listOf("Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")

    var expandedYear by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("PDF Olarak İndir", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Hangi döneme ait kayıtları indirmek istiyorsunuz?")
                
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = isAllTime,
                        onClick = { isAllTime = true }
                    )
                    Text("Tüm Zamanlar")
                }

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = !isAllTime,
                        onClick = { isAllTime = false }
                    )
                    Text("Belirli Bir Ay")
                }

                if (!isAllTime) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = expandedMonth,
                            onExpandedChange = { expandedMonth = !expandedMonth },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = months[selectedMonth],
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Ay") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                                modifier = Modifier.menuAnchor(),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedMonth,
                                onDismissRequest = { expandedMonth = false }
                            ) {
                                months.forEachIndexed { index, month ->
                                    DropdownMenuItem(
                                        text = { Text(month) },
                                        onClick = {
                                            selectedMonth = index
                                            expandedMonth = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = expandedYear,
                            onExpandedChange = { expandedYear = !expandedYear },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedYear.toString(),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Yıl") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
                                modifier = Modifier.menuAnchor(),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedYear,
                                onDismissRequest = { expandedYear = false }
                            ) {
                                years.forEach { year ->
                                    DropdownMenuItem(
                                        text = { Text(year.toString()) },
                                        onClick = {
                                            selectedYear = year
                                            expandedYear = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isAllTime) {
                        onExport(null, null)
                    } else {
                        onExport(selectedYear, selectedMonth)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B2349)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("İndir", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = Color(0xFF475569))
            }
        }
    )
}
