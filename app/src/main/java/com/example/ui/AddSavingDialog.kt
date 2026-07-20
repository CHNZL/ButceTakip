package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddSavingDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, Double) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Birikim Hedefi Ekle") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Hedef Adı (Araba, Ev vb.)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("saving_title")
                )

                OutlinedTextField(
                    value = target,
                    onValueChange = { target = com.example.util.formatInputAmount(it) },
                    label = { Text("Hedef Tutar (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("saving_target")
                )

                OutlinedTextField(
                    value = saved,
                    onValueChange = { saved = com.example.util.formatInputAmount(it) },
                    label = { Text("Mevcut Birikim (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("saving_current")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetVal = com.example.util.parseFormattedAmount(target) ?: 0.0
                    val savedVal = com.example.util.parseFormattedAmount(saved) ?: 0.0
                    if (title.isNotBlank() && targetVal > 0) {
                        onSave(title, targetVal, savedVal)
                    }
                },
                modifier = Modifier.testTag("save_saving_btn")
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
