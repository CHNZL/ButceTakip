package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.data.TransactionType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class IncomeSortOption(val label: String) {
    NEWEST("En Yeni"),
    HIGHEST("En Yüksek"),
    LOWEST("En Düşük")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IncomesScreen(
    transactions: List<Transaction>,
    onAddIncome: (() -> Unit)? = null,
    onEditIncome: ((Transaction) -> Unit)? = null,
    onDeleteIncome: ((Transaction) -> Unit)? = null,
    onRepeatIncome: ((Transaction) -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val mintAccent = if (isDark) CyberNeonMint else Color(0xFF059669)
    val cyanAccent = if (isDark) CyberNeonCyan else Color(0xFF0284C7)
    val coralAccent = if (isDark) CyberNeonCoral else Color(0xFFDC2626)

    val format = remember {
        com.example.util.FormatUtil.getCurrencyFormat().apply { maximumFractionDigits = 0 }
    }
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale("tr", "TR")) }
    val monthYearSdf = remember { SimpleDateFormat("MMMM yyyy", Locale("tr", "TR")) }
    val currentMonthName = remember { monthYearSdf.format(Date()).uppercase(Locale("tr", "TR")) }

    val timeBounds = remember {
        val calThisStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val thisStart = calThisStart.timeInMillis

        val calThisEnd = (calThisStart.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }
        val thisEnd = calThisEnd.timeInMillis

        val calPrevStart = (calThisStart.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
        val prevStart = calPrevStart.timeInMillis

        val cal3MStart = (calThisStart.clone() as Calendar).apply {
            add(Calendar.MONTH, -2)
        }
        val threeMStart = cal3MStart.timeInMillis

        listOf(thisStart, thisEnd, prevStart, threeMStart)
    }
    val thisMonthStart = timeBounds[0]
    val thisMonthEnd = timeBounds[1]
    val prevMonthStart = timeBounds[2]
    val threeMonthsStart = timeBounds[3]

    val incomeTx = remember(transactions) {
        transactions.filter { it.type == TransactionType.INCOME }
    }

    val thisMonthIncomes = remember(incomeTx, thisMonthStart, thisMonthEnd) {
        incomeTx.filter { it.timestamp in thisMonthStart until thisMonthEnd }
    }
    val prevMonthIncomes = remember(incomeTx, prevMonthStart, thisMonthStart) {
        incomeTx.filter { it.timestamp in prevMonthStart until thisMonthStart }
    }

    val totalThisMonth = remember(thisMonthIncomes) { thisMonthIncomes.sumOf { it.amount } }
    val totalPrevMonth = remember(prevMonthIncomes) { prevMonthIncomes.sumOf { it.amount } }
    val momPct = remember(totalThisMonth, totalPrevMonth) {
        if (totalPrevMonth > 0.0) ((totalThisMonth - totalPrevMonth) / totalPrevMonth) * 100.0 else null
    }

    var isBalanceHidden by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf("Bu Ay") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedPerson by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(IncomeSortOption.NEWEST) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showSourceBreakdown by remember { mutableStateOf(true) }

    val periodIncomes = remember(incomeTx, selectedPeriod, thisMonthStart, thisMonthEnd, prevMonthStart, threeMonthsStart) {
        when (selectedPeriod) {
            "Bu Ay" -> incomeTx.filter { it.timestamp in thisMonthStart until thisMonthEnd }
            "Önceki Ay" -> incomeTx.filter { it.timestamp in prevMonthStart until thisMonthStart }
            "Son 3 Ay" -> incomeTx.filter { it.timestamp in threeMonthsStart until thisMonthEnd }
            else -> incomeTx
        }
    }

    val uniqueCategories = remember(periodIncomes) {
        periodIncomes.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val uniquePersons = remember(periodIncomes) {
        periodIncomes.mapNotNull { it.person?.takeIf { p -> p.isNotBlank() } }.distinct().sorted()
    }

    // Category distribution computed over the chosen period (before single-category filter so bars stay clickable!)
    val categoryBreakdown = remember(periodIncomes) {
        val total = periodIncomes.sumOf { it.amount }.coerceAtLeast(1.0)
        periodIncomes
            .groupBy { it.category.ifBlank { "Diğer" } }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                Triple(cat, sum, (sum / total).toFloat().coerceIn(0f, 1f))
            }
            .sortedByDescending { it.second }
    }

    val displayedIncomes = remember(periodIncomes, selectedCategory, selectedPerson, searchQuery, sortOption) {
        val q = searchQuery.trim().lowercase(Locale("tr", "TR"))
        val filtered = periodIncomes.filter { tx ->
            val catOk = selectedCategory == null || tx.category == selectedCategory
            val personOk = selectedPerson == null || tx.person == selectedPerson
            val searchOk = q.isEmpty() ||
                tx.title.lowercase(Locale("tr", "TR")).contains(q) ||
                tx.category.lowercase(Locale("tr", "TR")).contains(q) ||
                (tx.person?.lowercase(Locale("tr", "TR"))?.contains(q) == true)
            catOk && personOk && searchOk
        }
        when (sortOption) {
            IncomeSortOption.NEWEST -> filtered.sortedByDescending { it.timestamp }
            IncomeSortOption.HIGHEST -> filtered.sortedByDescending { it.amount }
            IncomeSortOption.LOWEST -> filtered.sortedBy { it.amount }
        }
    }

    val displayedTotal = remember(displayedIncomes) { displayedIncomes.sumOf { it.amount } }
    val avgDisplayed = if (displayedIncomes.isNotEmpty()) displayedTotal / displayedIncomes.size else 0.0
    val maxDisplayedTx = remember(displayedIncomes) { displayedIncomes.maxByOrNull { it.amount } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 6.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- 1. FLAGSHIP INCOME HERO COMMAND CARD ---
        item {
            val tileBg = if (isDark) Color(0xFF111827).copy(alpha = 0.85f) else Color(0xFFF8FAFC)
            val tileBorder = if (isDark) Color(0xFF243047) else Color(0xFFE2E8F0)

            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                borderColor = if (isDark) CyberBorderGlow else mintAccent.copy(alpha = 0.28f),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val w = size.width
                            val h = size.height
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        mintAccent.copy(alpha = if (isDark) 0.16f else 0.10f),
                                        Color.Transparent
                                    ),
                                    center = Offset(w * 0.88f, h * 0.14f),
                                    radius = w * 0.55f
                                ),
                                center = Offset(w * 0.88f, h * 0.14f),
                                radius = w * 0.55f
                            )
                            val wave = Path().apply {
                                moveTo(0f, h * 0.58f)
                                cubicTo(w * 0.30f, h * 0.45f, w * 0.62f, h * 0.68f, w, h * 0.34f)
                            }
                            drawPath(
                                path = wave,
                                color = mintAccent.copy(alpha = if (isDark) 0.18f else 0.10f),
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        .padding(18.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                        .background(mintAccent)
                                )
                                Text(
                                    text = "GELİR ÖZETİ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    softWrap = false,
                                    maxLines = 1
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (selectedPeriod == "Bu Ay") currentMonthName else selectedPeriod.uppercase(Locale("tr", "TR")),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        softWrap = false,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (momPct != null && selectedPeriod == "Bu Ay") {
                                    val isUp = momPct >= 0.0
                                    val badgeColor = if (isUp) mintAccent else coralAccent
                                    Surface(
                                        color = badgeColor.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = String.format(Locale("tr"), "%s%.1f%%", if (isUp) "+" else "", momPct),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = badgeColor,
                                            softWrap = false,
                                            maxLines = 1,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { isBalanceHidden = !isBalanceHidden },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("incomes_visibility_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (isBalanceHidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = "Gelirleri Gizle veya Göster",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isBalanceHidden) "•••••••• ₺" else format.format(displayedTotal),
                            fontSize = 33.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.8).sp,
                            color = mintAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${displayedIncomes.size} kayıt • Ort. ${if (isBalanceHidden) "••••" else format.format(avgDisplayed)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IncomeHeroKpiTile(
                                label = "BU AY",
                                value = if (isBalanceHidden) "••••" else format.format(totalThisMonth),
                                accent = mintAccent,
                                bgColor = tileBg,
                                borderColor = tileBorder,
                                modifier = Modifier.weight(1f)
                            )
                            IncomeHeroKpiTile(
                                label = "GEÇEN AY",
                                value = if (isBalanceHidden) "••••" else format.format(totalPrevMonth),
                                accent = cyanAccent,
                                bgColor = tileBg,
                                borderColor = tileBorder,
                                modifier = Modifier.weight(1f)
                            )
                            IncomeHeroKpiTile(
                                label = "EN YÜKSEK",
                                value = if (isBalanceHidden) "••••" else if (maxDisplayedTx != null) format.format(maxDisplayedTx.amount) else "0 ₺",
                                accent = MaterialTheme.colorScheme.onSurface,
                                bgColor = tileBg,
                                borderColor = tileBorder,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // --- 2. PERIOD SELECTOR CAPSULE + SEARCH & SORT BAR ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val periods = listOf("Bu Ay", "Önceki Ay", "Son 3 Ay", "Tümü")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) CyberDarkSurface else Color(0xFFF1F5F9))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    periods.forEach { period ->
                        val selected = selectedPeriod == period
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        selected -> MaterialTheme.colorScheme.primary
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable {
                                    selectedPeriod = period
                                    selectedCategory = null
                                    selectedPerson = null
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                softWrap = false,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Search & Sort row (shown when period has records or search is active)
                if (periodIncomes.size >= 2 || searchQuery.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Gelir ara...", fontSize = 13.sp) },
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
                                        Icon(
                                            Icons.Rounded.Close,
                                            contentDescription = "Aramayı Temizle",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDark) CyberDarkSurface else Color.White,
                                unfocusedContainerColor = if (isDark) CyberDarkSurface else Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)
                            )
                        )

                        Box {
                            Surface(
                                onClick = { showSortMenu = true },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isDark) CyberDarkSurface else Color.White,
                                border = BorderStroke(1.dp, if (isDark) CyberBorderStroke else Color(0xFFE2E8F0)),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.SwapVert,
                                        contentDescription = "Sırala",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = sortOption.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        softWrap = false,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                IncomeSortOption.entries.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt.label, fontWeight = if (opt == sortOption) FontWeight.Bold else FontWeight.Normal) },
                                        leadingIcon = {
                                            if (opt == sortOption) {
                                                Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            }
                                        },
                                        onClick = {
                                            sortOption = opt
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Category Filter Chips
                if (uniqueCategories.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("Tüm Kaynaklar", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        items(uniqueCategories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                                label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Person Filter Chips (when multiple persons exist)
                if (uniquePersons.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedPerson == null,
                                onClick = { selectedPerson = null },
                                label = { Text("Tüm Kişiler", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        items(uniquePersons) { person ->
                            FilterChip(
                                selected = selectedPerson == person,
                                onClick = { selectedPerson = if (selectedPerson == person) null else person },
                                label = { Text(person, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 3. INCOME SOURCE BREAKDOWN SMART CARD ---
        if (categoryBreakdown.size >= 2) {
            item {
                val trackColor = if (isDark) Color(0xFF1B2338) else Color(0xFFE2E8F0)
                val barPalette = listOf(mintAccent, cyanAccent, CyberNeonPurple, CyberNeonOrange)

                CyberCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSourceBreakdown = !showSourceBreakdown },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(mintAccent.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.DonutLarge,
                                    contentDescription = null,
                                    tint = mintAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Kaynak Dağılımı",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = if (showSourceBreakdown) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = if (showSourceBreakdown) "Daralt" else "Genişlet",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = showSourceBreakdown) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            categoryBreakdown.take(5).forEachIndexed { idx, (cat, amount, ratio) ->
                                val color = barPalette[idx % barPalette.size]
                                val isCatSelected = selectedCategory == cat
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedCategory = if (isCatSelected) null else cat
                                        }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                            )
                                            Text(
                                                text = cat,
                                                fontSize = 12.sp,
                                                fontWeight = if (isCatSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                color = if (isCatSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = "%${(ratio * 100).toInt()} • ${if (isBalanceHidden) "••••" else format.format(amount)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = color,
                                            softWrap = false,
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(CircleShape)
                                            .background(trackColor)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(ratio.coerceAtLeast(0.03f))
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. SECTION HEADER ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gelir Kayıtları (${displayedIncomes.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (displayedIncomes.isNotEmpty()) {
                    Surface(
                        color = mintAccent.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isBalanceHidden) "••••" else "+${format.format(displayedTotal)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = mintAccent,
                            softWrap = false,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // --- 5. INCOME LIST OR EMPTY STATE ---
        if (displayedIncomes.isEmpty()) {
            item {
                CyberCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(28.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(mintAccent.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = mintAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedCategory != null || selectedPerson != null) {
                                "Filtreye uygun gelir bulunamadı."
                            } else {
                                "Bu dönem için gelir kaydı yok."
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Maaş, ek gelir veya iade eklemek için '+' butonuna dokunabilirsiniz.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (onAddIncome != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = onAddIncome,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("empty_add_income_btn")
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gelir Ekle", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            val groupByMonth = (selectedPeriod == "Son 3 Ay" || selectedPeriod == "Tümü") && sortOption == IncomeSortOption.NEWEST
            if (groupByMonth) {
                val grouped = displayedIncomes.groupBy { tx ->
                    val c = Calendar.getInstance().apply {
                        timeInMillis = tx.timestamp
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    c.timeInMillis
                }
                grouped.forEach { (monthMs, monthList) ->
                    val monthTitle = monthYearSdf.format(Date(monthMs)).uppercase(Locale("tr", "TR"))
                    val monthSum = monthList.sumOf { it.amount }
                    item(key = "hdr_$monthMs") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = monthTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isBalanceHidden) "••••" else "+${format.format(monthSum)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = mintAccent
                            )
                        }
                    }
                    items(monthList, key = { it.id }) { tx ->
                        IncomeRowCard(
                            tx = tx,
                            dateText = sdf.format(Date(tx.timestamp)),
                            amountText = if (isBalanceHidden) "••••" else "+${format.format(tx.amount)}",
                            mintAccent = mintAccent,
                            onEdit = onEditIncome,
                            onDelete = onDeleteIncome,
                            onRepeat = onRepeatIncome
                        )
                    }
                }
            } else {
                items(displayedIncomes, key = { it.id }) { tx ->
                    IncomeRowCard(
                        tx = tx,
                        dateText = sdf.format(Date(tx.timestamp)),
                        amountText = if (isBalanceHidden) "••••" else "+${format.format(tx.amount)}",
                        mintAccent = mintAccent,
                        onEdit = onEditIncome,
                        onDelete = onDeleteIncome,
                        onRepeat = onRepeatIncome
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeHeroKpiTile(
    label: String,
    value: String,
    accent: Color,
    bgColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                softWrap = false,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = accent,
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IncomeRowCard(
    tx: Transaction,
    dateText: String,
    amountText: String,
    mintAccent: Color,
    onEdit: ((Transaction) -> Unit)?,
    onDelete: ((Transaction) -> Unit)?,
    onRepeat: ((Transaction) -> Unit)?
) {
    var expandedMenu by remember { mutableStateOf(false) }
    val hasMenuActions = onEdit != null || onDelete != null || onRepeat != null

    val icon = when {
        tx.category.lowercase(Locale("tr", "TR")).contains("maaş") -> Icons.Rounded.AccountBalanceWallet
        tx.category.lowercase(Locale("tr", "TR")).contains("prim") || tx.category.lowercase(Locale("tr", "TR")).contains("bonus") -> Icons.Rounded.WorkspacePremium
        tx.category.lowercase(Locale("tr", "TR")).contains("kira") -> Icons.Rounded.Home
        tx.category.lowercase(Locale("tr", "TR")).contains("yatırım") || tx.category.lowercase(Locale("tr", "TR")).contains("faiz") || tx.category.lowercase(Locale("tr", "TR")).contains("temettü") -> Icons.Rounded.ShowChart
        else -> Icons.Rounded.TrendingUp
    }

    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = hasMenuActions) { expandedMenu = true },
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(13.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(mintAccent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = mintAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tx.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (tx.category.isNotBlank() && !tx.category.equals(tx.title, ignoreCase = true)) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = tx.category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    softWrap = false,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (!tx.person.isNullOrBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = tx.person,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    softWrap = false,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = dateText,
                                fontSize = 10.sp,
                                softWrap = false,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = amountText,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    softWrap = false,
                    maxLines = 1,
                    color = mintAccent
                )
            }

            if (hasMenuActions) {
                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false }
                ) {
                    if (onEdit != null) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            text = { Text("Düzenle") },
                            onClick = {
                                expandedMenu = false
                                onEdit(tx)
                            }
                        )
                    }
                    if (onRepeat != null) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Rounded.Replay, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                            text = { Text("Bugüne Tekrarla") },
                            onClick = {
                                expandedMenu = false
                                onRepeat(tx)
                            }
                        )
                    }
                    if (onDelete != null) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                            text = { Text("Sil", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                expandedMenu = false
                                onDelete(tx)
                            }
                        )
                    }
                }
            }
        }
    }
}
