package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppCategory
import com.example.data.Person
import com.example.viewmodel.BudgetViewModel

@Composable
fun SettingsScreen(viewModel: BudgetViewModel) {
    val incomeCategories by viewModel.incomeCategories.collectAsStateWithLifecycle()
    val expenseCategories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val savingCategories by viewModel.savingCategories.collectAsStateWithLifecycle()
    val persons by viewModel.persons.collectAsStateWithLifecycle()

    var showCategoryDialog by remember { mutableStateOf(false) }
    var categoryDialogType by remember { mutableStateOf("INCOME") }
    var editingCategory by remember { mutableStateOf<AppCategory?>(null) }

    var showPersonDialog by remember { mutableStateOf(false) }
    var editingPerson by remember { mutableStateOf<Person?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Gelir Kategorileri", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
        items(incomeCategories) { cat ->
            ListItem(
                headlineContent = { Text(cat.name) },
                trailingContent = {
                    Row {
                        IconButton(onClick = { 
                            editingCategory = cat
                            categoryDialogType = "INCOME"
                            errorMessage = ""
                            showCategoryDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Düzenle")
                        }
                        IconButton(onClick = { viewModel.deleteCategory(cat) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
        item {
            OutlinedButton(
                onClick = { 
                    editingCategory = null
                    categoryDialogType = "INCOME"
                    errorMessage = ""
                    showCategoryDialog = true 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yeni Gelir Kategorisi Ekle")
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Gider Kategorileri", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
        items(expenseCategories) { cat ->
            ListItem(
                headlineContent = { Text(cat.name) },
                trailingContent = {
                    Row {
                        IconButton(onClick = { 
                            editingCategory = cat
                            categoryDialogType = "EXPENSE"
                            errorMessage = ""
                            showCategoryDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Düzenle")
                        }
                        IconButton(onClick = { viewModel.deleteCategory(cat) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
        item {
            OutlinedButton(
                onClick = { 
                    editingCategory = null
                    categoryDialogType = "EXPENSE"
                    errorMessage = ""
                    showCategoryDialog = true 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yeni Gider Kategorisi Ekle")
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Birikim Kategorileri", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
        items(savingCategories) { cat ->
            ListItem(
                headlineContent = { Text(cat.name) },
                trailingContent = {
                    Row {
                        IconButton(onClick = { 
                            editingCategory = cat
                            categoryDialogType = "SAVING"
                            errorMessage = ""
                            showCategoryDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Düzenle")
                        }
                        IconButton(onClick = { viewModel.deleteCategory(cat) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
        item {
            OutlinedButton(
                onClick = { 
                    editingCategory = null
                    categoryDialogType = "SAVING"
                    errorMessage = ""
                    showCategoryDialog = true 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yeni Birikim Kategorisi Ekle")
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Kişiler", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
        items(persons) { p ->
            ListItem(
                headlineContent = { Text(p.name) },
                trailingContent = {
                    Row {
                        IconButton(onClick = { 
                            editingPerson = p
                            errorMessage = ""
                            showPersonDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Düzenle")
                        }
                        IconButton(onClick = { viewModel.deletePerson(p) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
        item {
            OutlinedButton(
                onClick = { 
                    editingPerson = null
                    errorMessage = ""
                    showPersonDialog = true 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yeni Kişi Ekle")
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showCategoryDialog) {
        var inputName by remember { mutableStateOf(editingCategory?.name ?: "") }
        
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text(if (editingCategory == null) "Kategori Ekle" else "Kategoriyi Düzenle") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { 
                            inputName = it
                            errorMessage = ""
                        },
                        label = { Text("Kategori Adı") },
                        singleLine = true,
                        isError = errorMessage.isNotEmpty()
                    )
                    if (errorMessage.isNotEmpty()) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nameStr = inputName.trim()
                        if (nameStr.isBlank()) {
                            errorMessage = "Kategori adı boş olamaz"
                            return@Button
                        }
                        val currentList = when (categoryDialogType) {
                            "INCOME" -> incomeCategories
                            "EXPENSE" -> expenseCategories
                            else -> savingCategories
                        }
                        val exists = currentList.any { it.name.equals(nameStr, ignoreCase = true) && it.id != (editingCategory?.id ?: -1) }
                        if (exists) {
                            errorMessage = "Bu isimde bir kategori zaten var"
                            return@Button
                        }

                        if (editingCategory != null) {
                            viewModel.updateCategory(editingCategory!!.copy(name = nameStr))
                        } else {
                            viewModel.addCategory(AppCategory(name = nameStr, type = categoryDialogType))
                        }
                        showCategoryDialog = false
                    }
                ) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { showCategoryDialog = false }) { Text("İptal") }
            }
        )
    }

    if (showPersonDialog) {
        var inputName by remember { mutableStateOf(editingPerson?.name ?: "") }
        AlertDialog(
            onDismissRequest = { showPersonDialog = false },
            title = { Text(if (editingPerson == null) "Kişi Ekle" else "Kişiyi Düzenle") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { 
                            inputName = it 
                            errorMessage = ""
                        },
                        label = { Text("Kişi Adı/Soyadı") },
                        singleLine = true,
                        isError = errorMessage.isNotEmpty()
                    )
                    if (errorMessage.isNotEmpty()) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nameStr = inputName.trim()
                        if (nameStr.isBlank()) {
                            errorMessage = "Kişi adı boş olamaz"
                            return@Button
                        }
                        val exists = persons.any { it.name.equals(nameStr, ignoreCase = true) && it.id != (editingPerson?.id ?: -1) }
                        if (exists) {
                            errorMessage = "Bu isimde bir kişi zaten var"
                            return@Button
                        }

                        if (editingPerson != null) {
                            viewModel.updatePerson(editingPerson!!.copy(name = nameStr))
                        } else {
                            viewModel.addPerson(Person(name = nameStr))
                        }
                        showPersonDialog = false
                    }
                ) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { showPersonDialog = false }) { Text("İptal") }
            }
        )
    }
}
