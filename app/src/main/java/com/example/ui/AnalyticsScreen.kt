package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.data.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(transactions: List<Transaction>) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Genel Analiz, 1: Kişi Analizi

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Analiz ve Raporlar", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    Text("FİNANSAL DURUM VE GELECEK TAHMİNLERİ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).clickable { selectedTab = 0 }.background(if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(24.dp)).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("GENEL ANALİZ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(modifier = Modifier.weight(1f).clickable { selectedTab = 1 }.background(if (selectedTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(24.dp)).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("KİŞİ ANALİZİ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (selectedTab == 0) {
            GeneralAnalysisScreen(transactions)
        } else {
            PersonAnalysisScreen(transactions)
        }
    }
}

@Composable
fun GeneralAnalysisScreen(transactions: List<Transaction>) {
    var selectedTime by remember { mutableStateOf("Bu Ay") }
    val timeFilters = listOf("Bu Ay", "Önceki Ay", "Son 3 Ay", "Son 6 Ay", "Bu Yıl")
    val format = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
    
    val bounds = getTimeBounds(selectedTime)
    val filteredTx = transactions.filter { it.timestamp in bounds.first until bounds.second }
    val inc = filteredTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val exp = filteredTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val sav = filteredTx.filter { it.type == TransactionType.SAVING }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                var expandedTime by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = selectedTime,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.width(160.dp).clickable { expandedTime = true },
                        shape = RoundedCornerShape(12.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    DropdownMenu(expanded = expandedTime, onDismissRequest = { expandedTime = false }) {
                        timeFilters.forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = { selectedTime = t; expandedTime = false })
                        }
                    }
                }
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Özet Tablo ($selectedTime)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("GELİR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            Text(format.format(inc), fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                        Column {
                            Text("GİDER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            Text(format.format(exp), fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                        Column {
                            Text("BİRİKİM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                            Text(format.format(sav), fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
        item {
            MonthlyProgressCard(transactions)
        }
        item {
            ExpenseDistributionCard(filteredTx)
        }
    }
}

@Composable
fun PersonAnalysisScreen(transactions: List<Transaction>) {
    val persons = transactions.mapNotNull { it.person }.filter { it.isNotBlank() }.distinct()
    var selectedPerson by remember { mutableStateOf(persons.firstOrNull() ?: "") }
    var selectedTime by remember { mutableStateOf("Bu Yıl") }
    val format = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
    
    val timeFilters = listOf("Bu Ay", "Önceki Ay", "Son 3 Ay", "Son 6 Ay", "Son 1 Yıl", "Bu Yıl", "Önceki Yıl")

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Kisi Secimi
                Column(modifier = Modifier.weight(1f)) {
                    Text("KİŞİ SEÇİN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    var expandedPerson by remember { mutableStateOf(false) }
                    Box {
                        OutlinedTextField(
                            value = selectedPerson.ifBlank { "Kişi Yok" },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth().clickable { expandedPerson = true },
                            shape = RoundedCornerShape(12.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        DropdownMenu(expanded = expandedPerson, onDismissRequest = { expandedPerson = false }) {
                            persons.forEach { p ->
                                DropdownMenuItem(text = { Text(p) }, onClick = { selectedPerson = p; expandedPerson = false })
                            }
                        }
                    }
                }

                // Zaman Araligi
                Column(modifier = Modifier.weight(1f)) {
                    Text("ZAMAN ARALIĞI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    var expandedTime by remember { mutableStateOf(false) }
                    Box {
                        OutlinedTextField(
                            value = selectedTime,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth().clickable { expandedTime = true },
                            shape = RoundedCornerShape(12.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        DropdownMenu(expanded = expandedTime, onDismissRequest = { expandedTime = false }) {
                            timeFilters.forEach { t ->
                                DropdownMenuItem(text = { Text(t) }, onClick = { selectedTime = t; expandedTime = false })
                            }
                        }
                    }
                }
            }
        }

        val bounds = getTimeBounds(selectedTime)
        val filteredTxs = transactions.filter { it.person == selectedPerson && it.timestamp in bounds.first until bounds.second }
        val expenses = filteredTxs.filter { it.type == TransactionType.EXPENSE }
        val totalExpense = expenses.sumOf { it.amount }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOPLAM HARCAMA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(format.format(totalExpense), fontSize = 28.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("KATEGORİ DAĞILIMI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                val expenseByCategory = expenses.groupBy { it.category ?: "Diğer" }
                    .mapValues { it.value.sumOf { tx -> tx.amount } }
                    .toList()
                    .sortedByDescending { it.second }
                    .take(5)

                PieChartAndLegend(expenseByCategory, totalExpense)
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("SON İŞLEMLER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(16.dp))
                val lastTxs = expenses.sortedByDescending { it.timestamp }.take(5)
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale("tr"))
                lastTxs.forEach { tx ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(tx.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(format.format(tx.amount), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(sdf.format(Date(tx.timestamp)), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (lastTxs.isEmpty()) {
                    Text("İşlem bulunamadı.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun MonthlyProgressCard(transactions: List<Transaction>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Yıllık Tahmin ve Analiz", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("MEVCUT VERİLERE DAYALI GELECEK AY TAHMİNLERİ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LegendItem("GELİR", Color(0xFF10B981))
                    LegendItem("GİDER", Color(0xFFEF4444))
                    LegendItem("BİRİKİM", Color(0xFF3B82F6))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            // Generate Data
            val months = listOf("Ocak","Şubat","Mart","Nisan","Mayıs","Haziran","Temmuz","Ağustos","Eylül","Ekim","Kasım","Aralık")
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val currentMonthIdx = Calendar.getInstance().get(Calendar.MONTH)
            
            val monthlyData = (0..11).map { monthIdx ->
                val start = Calendar.getInstance().apply { set(currentYear, monthIdx, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                val end = Calendar.getInstance().apply { set(currentYear, monthIdx, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0); add(Calendar.MONTH, 1) }.timeInMillis
                val txs = transactions.filter { it.timestamp in start until end }
                val inc = txs.filter{ it.type == TransactionType.INCOME }.sumOf{ it.amount }.toFloat()
                val exp = txs.filter{ it.type == TransactionType.EXPENSE }.sumOf{ it.amount }.toFloat()
                val sav = txs.filter{ it.type == TransactionType.SAVING }.sumOf{ it.amount }.toFloat()
                Triple(inc, exp, sav)
            }
            
            val maxVal = monthlyData.maxOfOrNull { maxOf(it.first, it.second, it.third) } ?: 1000f
            val absoluteMax = if (maxVal < 1000f) 1000f else maxVal * 1.2f

            Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                // Background Lines
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    repeat(5) {
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.05f))
                    }
                }
                
                Row(modifier = Modifier.fillMaxSize().padding(top=8.dp, bottom=8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                    monthlyData.forEachIndexed { i, (inc, exp, sav) ->
                        val isFuture = i > currentMonthIdx
                        val alpha = if (isFuture) 0.3f else 1f
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f).padding(horizontal = 2.dp), verticalAlignment = Alignment.Bottom) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight(inc/absoluteMax).background(Color(0xFF10B981).copy(alpha), RoundedCornerShape(topStart=2.dp, topEnd=2.dp)))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight(exp/absoluteMax).background(Color(0xFFEF4444).copy(alpha), RoundedCornerShape(topStart=2.dp, topEnd=2.dp)))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight(sav/absoluteMax).background(Color(0xFF3B82F6).copy(alpha), RoundedCornerShape(topStart=2.dp, topEnd=2.dp)))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                months.forEach { m ->
                    Text(m.take(3), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun ExpenseDistributionCard(transactions: List<Transaction>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Harcama Dağılımı", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text("KATEGORİLERE GÖRE GİDER ANALİZİ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            val filteredTxs = transactions.filter { it.type == TransactionType.EXPENSE }
            val expenseByCategory = filteredTxs.groupBy { it.category ?: "Diğer" }
                .mapValues { it.value.sumOf { tx -> tx.amount } }
                .toList()
                .sortedByDescending { it.second }
                .take(6)
            val totalExpense = filteredTxs.sumOf { it.amount }

            PieChartAndLegend(expenseByCategory, totalExpense)
        }
    }
}

@Composable
fun PieChartAndLegend(expenseByCategory: List<Pair<String, Double>>, totalExpense: Double) {
    val format = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
    val colors = listOf(Color(0xFFEF4444), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF14B8A6))

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(240.dp).padding(16.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                val strokeWidth = 56.dp.toPx()
                val d = size.width - strokeWidth
                if (totalExpense > 0 && expenseByCategory.isNotEmpty()) {
                    expenseByCategory.forEachIndexed { index, pair ->
                        val sweepAngle = ((pair.second / totalExpense) * 360f).toFloat()
                        drawArc(
                            color = colors[index % colors.size],
                            startAngle = startAngle,
                            sweepAngle = maxOf(sweepAngle - 2f, 1f), // slight gap, minimum width
                            useCenter = false,
                            topLeft = Offset(strokeWidth/2, strokeWidth/2),
                            size = Size(d, d),
                            style = Stroke(width = strokeWidth)
                        )
                        startAngle += sweepAngle
                    }
                } else {
                    drawArc(color = Color.LightGray.copy(alpha=0.3f), startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = Offset(strokeWidth/2, strokeWidth/2), size = Size(d, d), style = Stroke(width = strokeWidth))
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            expenseByCategory.forEachIndexed { index, pair ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).background(colors[index % colors.size], RoundedCornerShape(6.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(pair.first.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(format.format(pair.second), fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun LegendItem(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

fun getTimeBounds(filter: String): Pair<Long, Long> {
    val cal = Calendar.getInstance()
    var start = 0L
    var end = Long.MAX_VALUE
    
    when (filter) {
        "Bu Ay" -> {
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            start = cal.timeInMillis
        }
        "Önceki Ay" -> {
            cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            end = cal.timeInMillis
            cal.add(Calendar.MONTH, -1)
            start = cal.timeInMillis
        }
        "Son 3 Ay" -> {
            cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.MONTH, -2) // includes current + prev 2
            start = cal.timeInMillis
        }
        "Son 6 Ay" -> {
            cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.MONTH, -5)
            start = cal.timeInMillis
        }
        "Son 1 Yıl" -> {
            cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.MONTH, -11)
            start = cal.timeInMillis
        }
        "Bu Yıl" -> {
            cal.set(Calendar.DAY_OF_YEAR, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            start = cal.timeInMillis
        }
        "Önceki Yıl" -> {
            cal.set(Calendar.DAY_OF_YEAR, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            end = cal.timeInMillis
            cal.add(Calendar.YEAR, -1)
            start = cal.timeInMillis
        }
    }
    return Pair(start, end)
}

