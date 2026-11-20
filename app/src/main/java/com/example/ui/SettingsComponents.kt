package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppCategory
import com.example.data.GoldPrice
import com.example.data.Person
import com.example.ui.theme.*
import java.util.Locale

@Composable
internal fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(Locale("tr", "TR")),
        style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.9.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 6.dp, bottom = 6.dp, top = 4.dp)
    )
}

@Composable
internal fun SettingsGroupCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.8.dp, borderColor),
        shadowElevation = if (isDark) 0.dp else 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
internal fun SettingsInsetDivider() {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    HorizontalDivider(
        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
        thickness = 0.7.dp,
        color = if (isDark) CyberBorderStroke.copy(alpha = 0.75f) else Color(0xFFE2E8F0)
    )
}

@Composable
internal fun SettingsRowItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    badgeText: String? = null,
    badgeColor: Color = iconTint,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: (@Composable () -> Unit)? = null,
    showChevron: Boolean = trailing == null,
    testTag: String,
    onClick: (() -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val rowModifier = Modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 60.dp)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(horizontal = 14.dp, vertical = 12.dp)
        .testTag(testTag)

    Row(
        modifier = rowModifier,
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
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (!badgeText.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor.copy(alpha = if (isDark) 0.16f else 0.11f)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontFeatureSettings = "tnum"
                    ),
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(10.dp))
            trailing()
        } else if (showChevron) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategoryManagerBottomSheet(
    incomeCategories: List<AppCategory>,
    expenseCategories: List<AppCategory>,
    savingCategories: List<AppCategory>,
    onAddCategory: (String, String) -> Unit,
    onUpdateCategory: (AppCategory, String) -> Unit,
    onDeleteCategory: (AppCategory) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val coral = if (isDark) CyberNeonCoral else Color(0xFFE11D48)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)

    var selectedType by remember { mutableStateOf("INCOME") }
    var searchQuery by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<AppCategory?>(null) }
    var pendingDelete by remember { mutableStateOf<AppCategory?>(null) }

    val activeAccent = when (selectedType) {
        "INCOME" -> mint
        "EXPENSE" -> coral
        else -> sky
    }
    val activeList = when (selectedType) {
        "INCOME" -> incomeCategories
        "EXPENSE" -> expenseCategories
        else -> savingCategories
    }
    val activeTypeLabel = when (selectedType) {
        "INCOME" -> "Gelir"
        "EXPENSE" -> "Gider"
        else -> "Birikim"
    }
    val filteredList = remember(activeList, searchQuery) {
        val q = searchQuery.trim().lowercase(Locale("tr", "TR"))
        if (q.isEmpty()) activeList
        else activeList.filter { it.name.lowercase(Locale("tr", "TR")).contains(q) }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Kategori Yönetimi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Gelir, gider ve birikim kalemlerini düzenleyin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_category_sheet_button")
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Kapat")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Segment Type Selector
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
                    val tabs = listOf(
                        Triple("INCOME", "Gelir (${incomeCategories.size})", mint),
                        Triple("EXPENSE", "Gider (${expenseCategories.size})", coral),
                        Triple("SAVING", "Birikim (${savingCategories.size})", sky)
                    )
                    tabs.forEach { (typeKey, label, accent) ->
                        val selected = selectedType == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) accent.copy(alpha = if (isDark) 0.20f else 0.14f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    selectedType = typeKey
                                    searchQuery = ""
                                }
                                .testTag("category_tab_${typeKey.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 12.sp,
                                    fontFeatureSettings = "tnum"
                                ),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (activeList.size > 5) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Kategori ara...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Temizle", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_search_input")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredList.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
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
                            imageVector = Icons.Rounded.Category,
                            contentDescription = null,
                            tint = activeAccent.copy(alpha = 0.6f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Eşleşen kategori bulunamadı" else "Henüz $activeTypeLabel kategorisi yok",
                            style = MaterialTheme.typography.bodyMedium,
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
                        .heightIn(max = 360.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(filteredList, key = { it.id }) { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 52.dp)
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(activeAccent)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        editingCategory = cat
                                        showEditDialog = true
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("edit_category_${cat.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "${cat.name} düzenle",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { pendingDelete = cat },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("delete_category_${cat.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "${cat.name} sil",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            if (cat.id != filteredList.lastOrNull()?.id) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 14.dp),
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
                onClick = {
                    editingCategory = null
                    showEditDialog = true
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = activeAccent,
                    contentColor = if (isDark) Color(0xFF041410) else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("add_category_button")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Yeni $activeTypeLabel Kategorisi Ekle",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showEditDialog) {
        NameInputDialog(
            title = if (editingCategory == null) "Yeni $activeTypeLabel Kategorisi" else "Kategoriyi Düzenle",
            fieldLabel = "Kategori Adı",
            initialValue = editingCategory?.name ?: "",
            existingNames = activeList.filter { it.id != (editingCategory?.id ?: -1) }.map { it.name },
            emptyError = "Kategori adı boş olamaz",
            duplicateError = "Bu isimde bir kategori zaten var",
            onConfirm = { newName ->
                if (editingCategory != null) {
                    onUpdateCategory(editingCategory!!, newName)
                } else {
                    onAddCategory(newName, selectedType)
                }
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    if (pendingDelete != null) {
        val catToDelete = pendingDelete!!
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Kategori Silinsin Mi?", fontWeight = FontWeight.Bold) },
            text = { Text("\"${catToDelete.name}\" kategorisi listeden kaldırılacak. Mevcut geçmiş işlemleriniz etkilenmez.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCategory(catToDelete)
                        pendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sil") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("İptal") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PersonManagerBottomSheet(
    persons: List<Person>,
    onAddPerson: (String) -> Unit,
    onUpdatePerson: (Person, String) -> Unit,
    onDeletePerson: (Person) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)
    val accent = if (isDark) CyberNeonPurple else Color(0xFF6366F1)

    var showEditDialog by remember { mutableStateOf(false) }
    var editingPerson by remember { mutableStateOf<Person?>(null) }
    var pendingDelete by remember { mutableStateOf<Person?>(null) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Aile & Kişiler",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "İşlemleri hane üyelerine göre takip edin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_person_sheet_button")
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Kapat")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (persons.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
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
                            imageVector = Icons.Rounded.Groups,
                            contentDescription = null,
                            tint = accent.copy(alpha = 0.6f),
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Henüz kişi eklenmedi",
                            style = MaterialTheme.typography.bodyMedium,
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
                        .heightIn(max = 360.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(persons, key = { it.id }) { person ->
                            val initial = person.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 56.dp)
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(accent.copy(alpha = if (isDark) 0.20f else 0.13f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initial,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = accent
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = person.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        editingPerson = person
                                        showEditDialog = true
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("edit_person_${person.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "${person.name} düzenle",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { pendingDelete = person },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("delete_person_${person.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "${person.name} sil",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            if (person.id != persons.lastOrNull()?.id) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 62.dp, end = 14.dp),
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
                onClick = {
                    editingPerson = null
                    showEditDialog = true
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("add_person_button")
            ) {
                Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Yeni Kişi Ekle",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showEditDialog) {
        NameInputDialog(
            title = if (editingPerson == null) "Yeni Kişi Ekle" else "Kişiyi Düzenle",
            fieldLabel = "Ad Soyad",
            initialValue = editingPerson?.name ?: "",
            existingNames = persons.filter { it.id != (editingPerson?.id ?: -1) }.map { it.name },
            emptyError = "Kişi adı boş olamaz",
            duplicateError = "Bu isimde bir kişi zaten var",
            onConfirm = { newName ->
                if (editingPerson != null) {
                    onUpdatePerson(editingPerson!!, newName)
                } else {
                    onAddPerson(newName)
                }
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    if (pendingDelete != null) {
        val personToDelete = pendingDelete!!
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Kişi Silinsin Mi?", fontWeight = FontWeight.Bold) },
            text = { Text("\"${personToDelete.name}\" kişi listesinden kaldırılacak. Eski işlem kayıtlarınız silinmez.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePerson(personToDelete)
                        pendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sil") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("İptal") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun PaymentRemindersBottomSheet(
    remindDueDay: Boolean,
    remindOverdue: Boolean,
    remindUpcomingDays: Int,
    updateIntervalHours: Int,
    silentHoursEnabled: Boolean,
    silentHoursStart: String,
    silentHoursEnd: String,
    onRemindDueDayChange: (Boolean) -> Unit,
    onRemindOverdueChange: (Boolean) -> Unit,
    onRemindUpcomingDaysChange: (Int) -> Unit,
    onUpdateIntervalHoursChange: (Int) -> Unit,
    onSilentHoursEnabledChange: (Boolean) -> Unit,
    onSilentHoursStartChange: (String) -> Unit,
    onSilentHoursEndChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

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
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ödeme Hatırlatmaları",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Vadesi yaklaşan ve geciken ödemeler için uyarılar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_reminders_sheet_button")
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Kapat")
                }
            }

            // Triggers card
            SettingsGroupCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Son Gün Hatırlatması", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Ödeme gününde bildirim gönder", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = remindDueDay,
                        onCheckedChange = onRemindDueDayChange,
                        modifier = Modifier.testTag("switch_remind_due_day")
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    thickness = 0.7.dp,
                    color = borderColor.copy(alpha = 0.75f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gecikmiş Ödeme Uyarısı", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Vadesi geçmiş ödenmemiş kalemleri hatırlat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = remindOverdue,
                        onCheckedChange = onRemindOverdueChange,
                        modifier = Modifier.testTag("switch_remind_overdue")
                    )
                }
            }

            // Lead time + interval card
            SettingsGroupCard {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Yaklaşan Ödeme Uyarısı", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "$remindUpcomingDays gün önce",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontFeatureSettings = "tnum"
                            ),
                            color = amber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (1..7).forEach { day ->
                            val selected = remindUpcomingDays == day
                            FilterChip(
                                selected = selected,
                                onClick = { onRemindUpcomingDaysChange(day) },
                                label = {
                                    Text(
                                        text = "${day}G",
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
                                ),
                                modifier = Modifier.testTag("upcoming_days_chip_$day")
                            )
                        }
                    }

                    HorizontalDivider(thickness = 0.7.dp, color = borderColor.copy(alpha = 0.75f))

                    Text("Kontrol Sıklığı", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 3, 6).forEach { hours ->
                            val selected = updateIntervalHours == hours
                            Surface(
                                onClick = { onUpdateIntervalHoursChange(hours) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) mint.copy(alpha = if (isDark) 0.20f else 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(0.9.dp, if (selected) mint else borderColor),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("interval_hours_$hours")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$hours saatte bir",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) mint else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Silent hours card
            SettingsGroupCard {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.16f else 0.11f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Bedtime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Sessiz Saatler", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("Bu aralıkta hiçbir bildirim gönderilmez", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = silentHoursEnabled,
                            onCheckedChange = onSilentHoursEnabledChange,
                            modifier = Modifier.testTag("switch_silent_hours")
                        )
                    }

                    AnimatedVisibility(visible = silentHoursEnabled) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                onClick = { showStartPicker = true },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(0.8.dp, borderColor),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("silent_start_button")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Başlangıç", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = silentHoursStart,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontFeatureSettings = "tnum"
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Surface(
                                onClick = { showEndPicker = true },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(0.8.dp, borderColor),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("silent_end_button")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Bitiş", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = silentHoursEnd,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontFeatureSettings = "tnum"
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showStartPicker) {
        MinimalTimePickerDialog(
            title = "Sessiz Saatler Başlangıcı",
            currentValue = silentHoursStart,
            onConfirm = {
                onSilentHoursStartChange(it)
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        MinimalTimePickerDialog(
            title = "Sessiz Saatler Bitişi",
            currentValue = silentHoursEnd,
            onConfirm = {
                onSilentHoursEndChange(it)
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MarketMatchingBottomSheet(
    savingCategories: List<String>,
    goldPrices: List<GoldPrice>,
    getMatchCode: (String) -> String?,
    onSelectMatch: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)

    var activeCategoryForPicker by remember { mutableStateOf<String?>(null) }
    var refreshVersion by remember { mutableIntStateOf(0) }

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
                .padding(bottom = 22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Birikim Kur Eşleşmeleri",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Her birikim kaleminin canlı fiyat kaynağını seçin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_market_match_sheet_button")
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Kapat")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (savingCategories.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CurrencyExchange,
                            contentDescription = null,
                            tint = sky.copy(alpha = 0.65f),
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Henüz birikim kalemi bulunamadı",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Birikimler ekranından birikim eklediğinizde buradan Yapı Kredi, Ziraat veya Sivas Sarraf kurlarına bağlayabilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
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
                        .heightIn(max = 420.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(savingCategories, key = { "$it-$refreshVersion" }) { category ->
                            val currentCode = getMatchCode(category)
                            val label = formatMarketMatchLabel(currentCode)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activeCategoryForPicker = category }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .testTag("market_match_row_$category"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (currentCode == null) MaterialTheme.colorScheme.onSurfaceVariant else sky,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = sky.copy(alpha = if (isDark) 0.16f else 0.11f)
                                ) {
                                    Text(
                                        text = "Değiştir",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = sky,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            if (category != savingCategories.lastOrNull()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    thickness = 0.6.dp,
                                    color = borderColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (activeCategoryForPicker != null) {
        val cat = activeCategoryForPicker!!
        val selectedCode = getMatchCode(cat)
        MarketSourcePickerDialog(
            categoryName = cat,
            selectedCode = selectedCode,
            goldPrices = goldPrices,
            onSelect = { newCode ->
                onSelectMatch(cat, newCode)
                refreshVersion++
                activeCategoryForPicker = null
            },
            onDismiss = { activeCategoryForPicker = null }
        )
    }
}

private fun formatMarketMatchLabel(code: String?): String = when {
    code == null -> "Otomatik (Akıllı Eşleşme)"
    code == "manual" -> "Manuel Giriş"
    code == "yk_USD" -> "Yapı Kredi · USD (Dolar)"
    code == "yk_EUR" -> "Yapı Kredi · EUR (Euro)"
    code == "yk_XAU" -> "Yapı Kredi · XAU (gr Altın)"
    code == "yk_XAG" -> "Yapı Kredi · XAG (gr Gümüş)"
    code == "zr_USD" -> "Ziraat Bankası · USD (Dolar)"
    code == "zr_EUR" -> "Ziraat Bankası · EUR (Euro)"
    code == "zr_XAU" -> "Ziraat Bankası · XAU (gr Altın)"
    code == "zr_XAG" -> "Ziraat Bankası · XAG (gr Gümüş)"
    code.startsWith("gp_") -> "Sivas Sarraf · ${code.removePrefix("gp_")}"
    else -> "Otomatik (Akıllı Eşleşme)"
}

@Composable
private fun MarketSourcePickerDialog(
    categoryName: String,
    selectedCode: String?,
    goldPrices: List<GoldPrice>,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)
    val mint = if (isDark) CyberNeonMint else Color(0xFF059669)
    val sky = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val amber = if (isDark) CyberNeonOrange else Color(0xFFD97706)
    val ziraatRed = if (isDark) Color(0xFFFB7185) else Color(0xFFE11D48)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Canlı fiyat kaynağını seçin",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MarketPickerOptionRow(
                    label = "Otomatik (Akıllı Eşleşme)",
                    selected = selectedCode == null,
                    accent = mint,
                    onClick = { onSelect(null) }
                )
                MarketPickerOptionRow(
                    label = "Manuel Giriş (Kendim belirlerim)",
                    selected = selectedCode == "manual",
                    accent = mint,
                    onClick = { onSelect("manual") }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = borderColor)
                Text("YAPI KREDİ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = sky)
                listOf(
                    "yk_XAU" to "XAU · Gram Altın",
                    "yk_XAG" to "XAG · Gram Gümüş",
                    "yk_USD" to "USD · Amerikan Doları",
                    "yk_EUR" to "EUR · Euro"
                ).forEach { (code, title) ->
                    MarketPickerOptionRow(
                        label = title,
                        selected = selectedCode == code,
                        accent = sky,
                        onClick = { onSelect(code) }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = borderColor)
                Text("ZİRAAT BANKASI", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ziraatRed)
                listOf(
                    "zr_XAU" to "XAU · Gram Altın",
                    "zr_XAG" to "XAG · Gram Gümüş",
                    "zr_USD" to "USD · Amerikan Doları",
                    "zr_EUR" to "EUR · Euro"
                ).forEach { (code, title) ->
                    MarketPickerOptionRow(
                        label = title,
                        selected = selectedCode == code,
                        accent = ziraatRed,
                        onClick = { onSelect(code) }
                    )
                }

                if (goldPrices.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = borderColor)
                    Text("SİVAS SARRAF", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = amber)
                    goldPrices.forEach { gp ->
                        val code = "gp_${gp.name}"
                        MarketPickerOptionRow(
                            label = gp.name,
                            selected = selectedCode == code,
                            accent = amber,
                            onClick = { onSelect(code) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Kapat") }
        }
    )
}

@Composable
private fun MarketPickerOptionRow(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) accent.copy(alpha = if (isDark) 0.16f else 0.10f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) accent else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "Seçili",
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun NameInputDialog(
    title: String,
    fieldLabel: String,
    initialValue: String,
    existingNames: List<String>,
    emptyError: String,
    duplicateError: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputName by remember(initialValue) { mutableStateOf(initialValue) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = {
                        inputName = it
                        errorMessage = ""
                    },
                    label = { Text(fieldLabel) },
                    singleLine = true,
                    isError = errorMessage.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = inputName.trim()
                    if (trimmed.isBlank()) {
                        errorMessage = emptyError
                        return@Button
                    }
                    if (existingNames.any { it.equals(trimmed, ignoreCase = true) }) {
                        errorMessage = duplicateError
                        return@Button
                    }
                    onConfirm(trimmed)
                }
            ) { Text("Kaydet") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Composable
fun MinimalTimePickerDialog(
    title: String,
    currentValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val parts = currentValue.split(":")
    val curHour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 0
    val curMin = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0

    var selectedHour by remember(currentValue) { mutableIntStateOf(curHour) }
    var selectedMin by remember(currentValue) { mutableIntStateOf(curMin) }

    var hourExpanded by remember { mutableStateOf(false) }
    var minExpanded by remember { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val borderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Surface(
                        onClick = { hourExpanded = true },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.width(88.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("SAAT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format(Locale.US, "%02d", selectedHour),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontFeatureSettings = "tnum"
                                ),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    DropdownMenu(expanded = hourExpanded, onDismissRequest = { hourExpanded = false }) {
                        (0..23).forEach { h ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = String.format(Locale.US, "%02d", h),
                                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMonoFamily)
                                    )
                                },
                                onClick = {
                                    selectedHour = h
                                    hourExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMonoFamily),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Box {
                    Surface(
                        onClick = { minExpanded = true },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.width(88.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("DAKİKA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format(Locale.US, "%02d", selectedMin),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontFeatureSettings = "tnum"
                                ),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    DropdownMenu(expanded = minExpanded, onDismissRequest = { minExpanded = false }) {
                        (0..59 step 5).forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = String.format(Locale.US, "%02d", m),
                                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMonoFamily)
                                    )
                                },
                                onClick = {
                                    selectedMin = m
                                    minExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(String.format(Locale.US, "%02d:%02d", selectedHour, selectedMin))
                }
            ) {
                Text("Tamam")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
