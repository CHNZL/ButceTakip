package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Warning
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

fun getCurrencyFormat(): NumberFormat {
    return NumberFormat.getCurrencyInstance(Locale("tr", "TR")).apply {
        maximumFractionDigits = 0
    }
}

@Composable
fun AnalyticsScreen(
    transactions: List<Transaction>,
    goldPrices: List<com.example.data.GoldPrice> = emptyList(),
    bankRates: List<com.example.data.BankRate> = emptyList(),
    preferenceManager: com.example.data.PreferenceManager? = null,
    ziraatRates: List<com.example.data.BankRate> = emptyList()
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Genel Analiz, 1: Kişi Analizi

    val currentMonthEnd = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    val validTransactions = remember(transactions) {
        transactions.filter { it.timestamp < currentMonthEnd }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Analiz ve Raporlar", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    Text("FİNANSAL DURUM VE GEÇMİŞ DÖNEM ANALİZLERİ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
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
            GeneralAnalysisScreen(validTransactions, goldPrices, bankRates, preferenceManager, ziraatRates)
        } else {
            PersonAnalysisScreen(validTransactions)
        }
    }
}

@Composable
fun GeneralAnalysisScreen(
    transactions: List<Transaction>,
    goldPrices: List<com.example.data.GoldPrice>,
    bankRates: List<com.example.data.BankRate>,
    preferenceManager: com.example.data.PreferenceManager?,
    ziraatRates: List<com.example.data.BankRate> = emptyList()
) {
    var selectedTime by remember { mutableStateOf("Bu Ay") }
    val timeFilters = listOf("Bu Ay", "Önceki Ay", "Son 3 Ay", "Son 6 Ay", "Bu Yıl")
    val format = remember { getCurrencyFormat() }
    
    val bounds = getTimeBounds(selectedTime)
    val filteredTx = transactions.filter { it.timestamp in bounds.first until bounds.second }
    val inc = filteredTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val exp = filteredTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val sav = filteredTx.filter { it.type == TransactionType.SAVING }.sumOf { it.amount }
    val bal = inc - exp - sav

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                var expandedTime by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { expandedTime = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                        Text(selectedTime, fontWeight = FontWeight.Bold)
                        Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = expandedTime, onDismissRequest = { expandedTime = false }) {
                        timeFilters.forEach { t ->
                            DropdownMenuItem(text = { Text(t, fontWeight = if (selectedTime == t) FontWeight.Bold else FontWeight.Normal) }, onClick = { selectedTime = t; expandedTime = false })
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
                        SummaryColumn("GELİR", inc, Color(0xFF10B981), format)
                        SummaryColumn("GİDER", exp, Color(0xFFEF4444), format)
                        SummaryColumn("BİRİKİM", sav, Color(0xFF3B82F6), format)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("NET BAKİYE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(format.format(bal), fontSize = 24.sp, fontWeight = FontWeight.Black, color = if (bal >= 0) Color(0xFF10B981) else Color(0xFFEF4444))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TASARRUF ORANI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val rate = if (inc > 0) ((sav / inc) * 100).toInt() else 0
                            Text("%$rate", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF8B5CF6))
                        }
                    }
                }
            }
        }
        item {
            FinancialHealthCard(inc, exp, sav)
        }
        item {
            MonthlyProgressCard(transactions)
        }
        item {
            val incomeTxs = filteredTx.filter { it.type == TransactionType.INCOME }
            if (incomeTxs.isNotEmpty()) {
                DistributionCard("Gelir Dağılımı", "KAYNAKLARA GÖRE GELİR ANALİZİ", incomeTxs)
            }
        }
        item {
            val expenseTxs = filteredTx.filter { it.type == TransactionType.EXPENSE }
            if (expenseTxs.isNotEmpty()) {
                DistributionCard("Harcama Dağılımı", "KATEGORİLERE GÖRE GİDER ANALİZİ", expenseTxs)
            }
        }
        item {
            val allSavingTxs = transactions.filter { it.type == TransactionType.SAVING }
            if (allSavingTxs.isNotEmpty()) {
                SavingsDistributionCard("Birikim Dağılımı", "GÜNCEL DEĞERLERE GÖRE BİRİKİM ANALİZİ (TÜM ZAMANLAR)", allSavingTxs, goldPrices, bankRates, preferenceManager, ziraatRates)
                Spacer(modifier = Modifier.height(16.dp))
                SavingsTrendCard(allSavingTxs, transactions, goldPrices, bankRates, preferenceManager, ziraatRates)
            }
        }
    }
}

@Composable
fun SummaryColumn(title: String, amount: Double, color: Color, format: NumberFormat) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(4.dp))
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(format.format(amount), fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun FinancialHealthCard(inc: Double, exp: Double, sav: Double) {
    val healthScore = if (inc > 0) {
        val expRatio = exp / inc
        val savRatio = sav / inc
        when {
            expRatio <= 0.6 && savRatio >= 0.2 -> 100
            expRatio <= 0.7 && savRatio >= 0.1 -> 80
            expRatio <= 0.85 -> 60
            expRatio < 1.0 -> 40
            else -> 20
        }
    } else 0

    val healthText = when (healthScore) {
        100 -> "Mükemmel! Finansal dengeniz harika."
        80 -> "İyi. Tasarruflarınızı artırabilirsiniz."
        60 -> "Orta. Harcamalarınızı gözden geçirin."
        40 -> "Dikkatli olmalısınız! Giderler çok fazla."
        else -> "Riskli! Giderleriniz gelirinizi aşıyor."
    }

    val healthColor = when (healthScore) {
        100 -> Color(0xFF10B981)
        80 -> Color(0xFF34D399)
        60 -> Color(0xFFFBBF24)
        40 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = healthColor.copy(alpha=0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(healthColor, CircleShape), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (healthScore >= 60) Icons.Rounded.ThumbUp else Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Finansal Sağlık Puanı: $healthScore", fontWeight = FontWeight.Black, color = healthColor.copy(alpha=0.8f))
                Text(healthText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
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
            Text("Aylık Gelişim", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("GELİR, GİDER VE BİRİKİM YILLIK TRENDİ", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LegendItem("GELİR", Color(0xFF10B981))
                    LegendItem("GİDER", Color(0xFFEF4444))
                    LegendItem("BİRİKİM", Color(0xFF3B82F6))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            // Generate Data
            val months = listOf("Oca","Şub","Mar","Nis","May","Haz","Tem","Ağu","Eyl","Eki","Kas","Ara")
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

            Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                // Background Lines
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    repeat(5) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.05f))
                    }
                }
                
                Row(modifier = Modifier.fillMaxSize().padding(top=8.dp, bottom=8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                    monthlyData.forEachIndexed { i, (inc, exp, sav) ->
                        val isFuture = i > currentMonthIdx
                        val alpha = if (isFuture) 0.3f else 1f
                        var incAnim by remember { mutableStateOf(0f) }
                        var expAnim by remember { mutableStateOf(0f) }
                        var savAnim by remember { mutableStateOf(0f) }
                        
                        LaunchedEffect(inc, exp, sav) {
                            incAnim = inc / absoluteMax
                            expAnim = exp / absoluteMax
                            savAnim = sav / absoluteMax
                        }
                        
                        val incAn = animateFloatAsState(targetValue = incAnim, animationSpec = tween(durationMillis = 800), label = "").value
                        val expAn = animateFloatAsState(targetValue = expAnim, animationSpec = tween(durationMillis = 800), label = "").value
                        val savAn = animateFloatAsState(targetValue = savAnim, animationSpec = tween(durationMillis = 800), label = "").value
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.weight(1f).padding(horizontal = 2.dp), verticalAlignment = Alignment.Bottom) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight(incAn).background(Color(0xFF10B981).copy(alpha), RoundedCornerShape(topStart=4.dp, topEnd=4.dp)))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight(expAn).background(Color(0xFFEF4444).copy(alpha), RoundedCornerShape(topStart=4.dp, topEnd=4.dp)))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight(savAn).background(Color(0xFF3B82F6).copy(alpha), RoundedCornerShape(topStart=4.dp, topEnd=4.dp)))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                months.forEach { m ->
                    Text(m, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun DistributionCard(title: String, subtitle: String, transactions: List<Transaction>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text(subtitle, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            val distByCategory = transactions.groupBy { it.category ?: "Diğer" }
                .mapValues { it.value.sumOf { tx -> tx.amount } }
                .toList()
                .sortedByDescending { it.second }
                .take(6)
            val totalDist = transactions.sumOf { it.amount }

            PieChartAndLegend(distByCategory, totalDist)
        }
    }
}

@Composable
fun PieChartAndLegend(distByCategory: List<Pair<String, Double>>, totalDist: Double) {
    val format = remember { getCurrencyFormat() }
    val colors = listOf(Color(0xFF3B82F6), Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFFEF4444), Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF14B8A6))

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(240.dp).padding(16.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                val strokeWidth = 50.dp.toPx()
                val d = size.width - strokeWidth
                if (totalDist > 0 && distByCategory.isNotEmpty()) {
                    distByCategory.forEachIndexed { index, pair ->
                        val sweepAngle = ((pair.second / totalDist) * 360f).toFloat()
                        drawArc(
                            color = colors[index % colors.size],
                            startAngle = startAngle,
                            sweepAngle = maxOf(sweepAngle - 2f, 1f), // slight gap
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TOPLAM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(format.format(totalDist), fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            distByCategory.forEachIndexed { index, pair ->
                val percentage = if (totalDist > 0) ((pair.second / totalDist) * 100).toInt() else 0
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(colors[index % colors.size], RoundedCornerShape(6.dp)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(pair.first.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(format.format(pair.second), fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                        Box(modifier = Modifier.fillMaxWidth(percentage / 100f).height(8.dp).background(colors[index % colors.size], CircleShape))
                    }
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

@Composable
fun SavingsDistributionCard(
    title: String, 
    subtitle: String, 
    transactions: List<Transaction>,
    goldPrices: List<com.example.data.GoldPrice>,
    bankRates: List<com.example.data.BankRate>,
    preferenceManager: com.example.data.PreferenceManager?,
    ziraatRates: List<com.example.data.BankRate> = emptyList()
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text(subtitle, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            val distByCategory = remember(transactions, goldPrices, bankRates, ziraatRates) {
                transactions.groupBy { it.category ?: "Diğer" }.mapValues { (cat, txList) ->
                    val totalQuantity = txList.sumOf { it.quantity ?: 0.0 }
                    val livePrice = resolveCurrentUnitPrice(cat, goldPrices, bankRates, preferenceManager, ziraatRates)
                    val latestPurchasePrice = txList.maxByOrNull { it.timestamp }?.unitPrice ?: 0.0
                    val currentUnitPrice = livePrice ?: latestPurchasePrice
                    totalQuantity * currentUnitPrice
                }.toList()
                .sortedByDescending { it.second }
                .take(6)
            }
            val totalDist = distByCategory.sumOf { it.second }

            PieChartAndLegend(distByCategory, totalDist)
        }
    }
}

@Composable
fun SavingsTrendCard(
    savingTxs: List<Transaction>,
    allTransactions: List<Transaction>,
    goldPrices: List<com.example.data.GoldPrice>,
    bankRates: List<com.example.data.BankRate>,
    preferenceManager: com.example.data.PreferenceManager?,
    ziraatRates: List<com.example.data.BankRate> = emptyList()
) {
    var selectedTime by remember { mutableStateOf("Tüm Zamanlar") }
    var selectedCategory by remember { mutableStateOf("Tüm Varlıklar") }
    
    val timeFilters = listOf("Bu Ay", "Önceki Ay", "Son 3 Ay", "Son 6 Ay", "Bu Yıl", "Tüm Zamanlar")
    val uniqueCategories = remember(savingTxs) { savingTxs.mapNotNull { it.category }.distinct().sorted() }
    val categoryFilters = listOf("Tüm Varlıklar") + uniqueCategories
    
    val format = remember { getCurrencyFormat() }
    
    val bounds = if (selectedTime == "Tüm Zamanlar") Pair(0L, Long.MAX_VALUE) else getTimeBounds(selectedTime)
    val filteredTx = savingTxs.filter { it.timestamp in bounds.first until bounds.second }.filter { 
        selectedCategory == "Tüm Varlıklar" || it.category == selectedCategory
    }
    
    val currentValues = remember(filteredTx, goldPrices, bankRates, ziraatRates) {
        filteredTx.groupBy { it.category ?: "Diğer" }.map { (cat, txList) ->
            val totalQuantity = txList.sumOf { it.quantity ?: 0.0 }
            val livePrice = resolveCurrentUnitPrice(cat, goldPrices, bankRates, preferenceManager, ziraatRates)
            val latestPurchasePrice = txList.maxByOrNull { it.timestamp }?.unitPrice ?: 0.0
            val currentUnitPrice = livePrice ?: latestPurchasePrice
            val totalPaid = txList.sumOf { it.amount }
            Pair(totalQuantity * currentUnitPrice, totalPaid)
        }
    }
    
    val totalCurrentValue = currentValues.sumOf { it.first }
    val totalMaliyet = currentValues.sumOf { it.second }
    val profitLoss = totalCurrentValue - totalMaliyet
    val profitLossPercent = if (totalMaliyet > 0.0) (profitLoss / totalMaliyet) * 100.0 else 0.0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Birikim Getiri Analizi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text("SEÇİLİ DÖNEME AİT GETİRİ PERFORMANSI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                var expandedCat by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { expandedCat = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                        Text(selectedCategory, fontWeight = FontWeight.Bold, maxLines = 1)
                        Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                        categoryFilters.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c, fontWeight = if (selectedCategory == c) FontWeight.Bold else FontWeight.Normal) }, 
                                onClick = { selectedCategory = c; expandedCat = false }
                            )
                        }
                    }
                }
                
                var expandedTime by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { expandedTime = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                        Text(selectedTime, fontWeight = FontWeight.Bold)
                        Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = expandedTime, onDismissRequest = { expandedTime = false }) {
                        timeFilters.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t, fontWeight = if (selectedTime == t) FontWeight.Bold else FontWeight.Normal) }, 
                                onClick = { selectedTime = t; expandedTime = false }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("TOPLAM MALİYET", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(format.format(totalMaliyet), fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("GÜNCEL DEĞER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(format.format(totalCurrentValue), fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("NET GETİRİ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(horizontalAlignment = Alignment.End) {
                    val color = if (profitLoss >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    Text("%s%s".format(if (profitLoss > 0) "+" else "", format.format(profitLoss)), fontSize = 24.sp, fontWeight = FontWeight.Black, color = color)
                    Text("%s%.2f%%".format(if (profitLossPercent > 0) "+" else "", profitLossPercent), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
                }
            }
        }
    }
}

