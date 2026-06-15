package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.SavingGoal
import com.example.data.Transaction
import com.example.data.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.example.data.AppCategory
import com.example.data.Person
import com.example.data.BankRate

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.data.CloudSyncManager
import com.example.data.PreferenceManager
import com.example.data.TransactionType
import java.util.Calendar
import com.example.data.GoldPrice
import kotlinx.coroutines.Dispatchers
import org.jsoup.Jsoup

data class BudgetUiState(
    val transactions: List<Transaction> = emptyList(),
    val savings: List<SavingGoal> = emptyList(),
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalSaved: Double = 0.0
)

class BudgetViewModel(
    application: Application,
    private val repository: TransactionRepository,
    val preferenceManager: PreferenceManager
) : AndroidViewModel(application) {

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus = _syncStatus.asStateFlow()
    
    private val _currentUserId = MutableStateFlow(preferenceManager.userId)
    val currentUserId = _currentUserId.asStateFlow()
    
    private val _goldPrices = MutableStateFlow<List<GoldPrice>>(emptyList())
    val goldPrices = _goldPrices.asStateFlow()
    
    private val _isFetchingGold = MutableStateFlow(false)
    val isFetchingGold = _isFetchingGold.asStateFlow()

    private val _bankRates = MutableStateFlow<List<BankRate>>(emptyList())
    val bankRates = _bankRates.asStateFlow()
    
    private val _isFetchingBankRates = MutableStateFlow(false)
    val isFetchingBankRates = _isFetchingBankRates.asStateFlow()

    private val _lastBankRateUpdate = MutableStateFlow<String?>(null)
    val lastBankRateUpdate = _lastBankRateUpdate.asStateFlow()

    private val _lastGoldUpdate = MutableStateFlow<String?>(null)
    val lastGoldUpdate = _lastGoldUpdate.asStateFlow()

    private val _darkThemeEnabled = MutableStateFlow(preferenceManager.darkThemeEnabled)
    val darkThemeEnabled = _darkThemeEnabled.asStateFlow()

    fun toggleDarkTheme(enabled: Boolean) {
        preferenceManager.darkThemeEnabled = enabled
        _darkThemeEnabled.value = enabled
    }

    init {
        fetchGoldPrices()
        fetchBankRates()
        viewModelScope.launch(Dispatchers.IO) {
            preloadCategoriesAndPeople()
        }
    }
    
    fun fetchBankRates() {
        if (_isFetchingBankRates.value) return
        _isFetchingBankRates.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rates = mutableListOf<BankRate>()
                var success = false
                try {
                    val doc = Jsoup.connect("https://www.yapikredi.com.tr/yatirimci-kosesi/doviz-bilgileri/")
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                        .timeout(4000)
                        .get()
                    
                    var usdBuy = "32,62"
                    var usdSell = "34,29"
                    var eurBuy = "35,55"
                    var eurSell = "37,35"
                    var xauBuy = ""
                    var xauSell = ""
                    var xagBuy = ""
                    var xagSell = ""
                    var foundAny = false

                    val rows = doc.select("table tbody tr")
                    for (row in rows) {
                        val cells = row.select("td")
                        // Based on inspection: [Code, Name, Buy, Sell, Change%]
                        if (cells.size >= 4) {
                            val code = cells[0].text().trim().lowercase()
                            val name = cells[1].text().trim().lowercase()
                            val buy = cells[2].text().trim()
                            val sell = cells[3].text().trim()
                            
                            if (code == "usd") {
                                usdBuy = buy
                                usdSell = sell
                                foundAny = true
                            } else if (code == "eur") {
                                eurBuy = buy
                                eurSell = sell
                                foundAny = true
                            } else if (name.contains("altın") || name.contains("altin") || code.contains("xau")) {
                                xauBuy = buy
                                xauSell = sell
                                foundAny = true
                            } else if (name.contains("gümüş") || name.contains("gumus") || code.contains("xag")) {
                                xagBuy = buy
                                xagSell = sell
                                foundAny = true
                            }
                        }
                    }

                    if (foundAny) {
                        rates.add(BankRate("USD", "Amerikan Doları", usdBuy, usdSell))
                        rates.add(BankRate("EUR", "Euro", eurBuy, eurSell))
                        // If scrape failed, notify user in UI if possible, instead of silent hardcoding
                        // For now allow manual entry as the primary source for Bank Prices if scraping fails
                        if (xauBuy.isEmpty()) {
                             xauBuy = "0,00"
                             xauSell = "0,00"
                        }
                        rates.add(BankRate("XAU", "Altın (gram)", xauBuy, xauSell))

                        if (xagBuy.isEmpty()) {
                            try {
                                val buyD = xauBuy.replace(".", "").replace(",", ".").trim().toDouble()
                                val sBuy = buyD / 85.0
                                val sSell = buyD / 85.0
                                xagBuy = String.format(java.util.Locale("tr"), "%.2f", sBuy)
                                xagSell = String.format(java.util.Locale("tr"), "%.2f", sSell)
                            } catch (e: Exception) {
                                xagBuy = "34,90"
                                xagSell = "41,60"
                            }
                        }
                        rates.add(BankRate("XAG", "Gümüş (gram)", xagBuy, xagSell))
                        success = true
                    }
                } catch(e: Exception) { 
                    e.printStackTrace()
                }
                
                if (!success || rates.isEmpty()) {
                    // FALLBACK: Read from reliable TCMB XML for currencies
                    try {
                        val doc = Jsoup.connect("https://www.tcmb.gov.tr/kurlar/today.xml")
                            .userAgent("Mozilla/5.0")
                            .timeout(5000)
                            .get()
                            
                        val currencies = doc.select("Currency")
                        var usdAlis = "-"
                        var usdSatis = "-"
                        var eurAlis = "-"
                        var eurSatis = "-"
                        for (c in currencies) {
                            if (c.attr("CurrencyCode") == "USD") {
                                usdAlis = c.select("ForexBuying").text().replace(".", ",")
                                usdSatis = c.select("ForexSelling").text().replace(".", ",")
                            } else if (c.attr("CurrencyCode") == "EUR") {
                                eurAlis = c.select("ForexBuying").text().replace(".", ",")
                                eurSatis = c.select("ForexSelling").text().replace(".", ",")
                            }
                        }
                        
                        rates.add(BankRate("USD", "Amerikan Doları", usdAlis, usdSatis))
                        rates.add(BankRate("EUR", "Euro", eurAlis, eurSatis))
                        
                        // Pick Gold from existing goldPrices fallback or placeholder
                        rates.add(BankRate("XAU", "Altın (gram)", "0,00", "0,00"))
                        rates.add(BankRate("XAG", "Gümüş (gram)", "0,00", "0,00"))                
                    } catch(e: Exception) {
                        // Hardcode fail safe so UI looks complete
                        rates.add(BankRate("USD", "Amerikan Doları", "0,00", "0,00"))
                        rates.add(BankRate("EUR", "Euro", "0,00", "0,00"))
                        rates.add(BankRate("XAU", "Altın (gram)", "0,00", "0,00"))
                        rates.add(BankRate("XAG", "Gümüş (gram)", "0,00", "0,00"))
                    }
                }
                
                _bankRates.value = rates
                val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale("tr"))
                _lastBankRateUpdate.value = sdf.format(java.util.Date())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isFetchingBankRates.value = false
            }
        }
    }
    
    fun fetchGoldPrices() {
        if (_isFetchingGold.value) return
        _isFetchingGold.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val doc = Jsoup.connect("https://www.sivaskuyumder.org.tr/")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .get()
                val text = doc.body().text()
                val prices = mutableListOf<GoldPrice>()
                
                // Ex: "22 Ayar Bile 5850 6350"
                val r22Bilezik = Regex("22 Ayar Bile\\s+([\\d,.]+)\\s+([\\d,.]+)").find(text)
                if (r22Bilezik != null) prices.add(GoldPrice("22 AYAR BİLEZİK", r22Bilezik.groupValues[1], r22Bilezik.groupValues[2]))

                // Ex: "14 Ayar 1 Gr 6000"
                val r14Gr = Regex("14 Ayar 1 Gr\\s+([\\d,.]+)").find(text)
                if (r14Gr != null) prices.add(GoldPrice("14 AYAR GRAM ALTIN", "-", r14Gr.groupValues[1]))

                // Ex: "22 Ayar 1 Gr 5900 6400"
                val r22Gr = Regex("22 Ayar 1 Gr\\s+([\\d,.]+)\\s+([\\d,.]+)").find(text)
                if (r22Gr != null) prices.add(GoldPrice("22 AYAR GRAM ALTIN", r22Gr.groupValues[1], r22Gr.groupValues[2]))

                // Ex: "24 Ayar 1 Gr 6400 6700"
                val r24Gr = Regex("24 Ayar 1 Gr\\s+([\\d,.]+)\\s+([\\d,.]+)").find(text)
                if (r24Gr != null) prices.add(GoldPrice("24 AYAR GRAM ALTIN", r24Gr.groupValues[1], r24Gr.groupValues[2]))

                // Ex: "Çeyrek 10600 11100 10350 10800" (Y.Alış, Y.Satış, E.Alış, E.Satış)
                val rCeyrek = Regex("Çeyrek\\s+([\\d,.]+)\\s+([\\d,.]+)\\s+([\\d,.]+)\\s+([\\d,.]+)").find(text)
                if (rCeyrek != null) {
                    prices.add(GoldPrice("ÇEYREK (YENİ)", rCeyrek.groupValues[1], rCeyrek.groupValues[2]))
                    prices.add(GoldPrice("ÇEYREK (ESKİ)", rCeyrek.groupValues[3], rCeyrek.groupValues[4]))
                }
                
                _goldPrices.value = prices
                // REMOVED updateXauInBankRates(r24.buy, r24.sell) so that Sivas local market prices matching 24 AYAR never overwrite the live bank XAU rate.
                val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale("tr"))
                _lastGoldUpdate.value = sdf.format(java.util.Date())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isFetchingGold.value = false
            }
        }
    }

    val incomeCategories = repository.getCategoriesByType("INCOME").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenseCategories = repository.getCategoriesByType("EXPENSE").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val savingCategories = repository.getCategoriesByType("SAVING").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val persons = repository.allPersons.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<BudgetUiState> = combine(
        repository.allTransactions,
        repository.allSavings
    ) { transactions, savings ->
        var income = 0.0
        var expense = 0.0
        var savingTx = 0.0
        transactions.forEach { t ->
            if (t.type == com.example.data.TransactionType.INCOME) {
                income += t.amount
            } else if (t.type == com.example.data.TransactionType.EXPENSE) {
                expense += t.amount
            } else if (t.type == com.example.data.TransactionType.SAVING) {
                savingTx += t.amount
            }
        }
        
        val totalSaved = savings.sumOf { it.savedAmount }
        
        BudgetUiState(
            transactions = transactions,
            savings = savings,
            totalBalance = income - expense - savingTx, // Saved money might still be in balance or excluded based on user preference, we can just show it.
            totalIncome = income,
            totalExpense = expense,
            totalSaved = totalSaved
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetUiState()
    )

    init {
        // Auto-restore on startup if logged in
        if (preferenceManager.userId.isNotBlank()) {
            restoreFromCloudSilently()
        }
    }

    private fun executeAndBackup(action: suspend () -> Unit) {
        viewModelScope.launch {
            action()
            kotlinx.coroutines.delay(150) // wait for DB changes to propagate to Flows
            backupToCloudSilently()
        }
    }

    fun addTransaction(transaction: Transaction) {
        executeAndBackup { repository.insert(transaction) }
    }
    
    fun updateTransaction(transaction: Transaction) {
        executeAndBackup { repository.insert(transaction) }
    }
    
    fun toggleTransactionPaid(transaction: Transaction, isPaid: Boolean) {
        executeAndBackup { repository.insert(transaction.copy(isPaid = isPaid)) }
    }
    
    fun deleteAllData() {
        executeAndBackup {
            repository.clearAllData()
        }
    }

    fun addTransactionWithInstallments(baseTransaction: Transaction, repeatUntilYearEnd: Boolean = false) {
        executeAndBackup {
            val inst = baseTransaction.installments ?: 1
            if (baseTransaction.id == 0 && repeatUntilYearEnd && baseTransaction.type == TransactionType.EXPENSE) {
                val calendar = Calendar.getInstance().apply { timeInMillis = baseTransaction.timestamp }
                val startYear = calendar.get(Calendar.YEAR)
                var i = 0
                while (calendar.get(Calendar.YEAR) == startYear) {
                    val finalTx = baseTransaction.copy(
                        id = 0,
                        timestamp = calendar.timeInMillis,
                        isPaid = if (i == 0) baseTransaction.isPaid else false,
                        installments = 1
                    )
                    repository.insert(finalTx)
                    calendar.add(Calendar.MONTH, 1)
                    i++
                }
            } else if (baseTransaction.id == 0 && inst > 1 && baseTransaction.type == TransactionType.EXPENSE) {
                val splitAmount = baseTransaction.amount / inst
                val calendar = Calendar.getInstance().apply { timeInMillis = baseTransaction.timestamp }
                
                for (i in 0 until inst) {
                    val finalTx = baseTransaction.copy(
                        id = 0,
                        amount = splitAmount,
                        timestamp = calendar.timeInMillis,
                        isPaid = if (i == 0) baseTransaction.isPaid else false, 
                        title = "${baseTransaction.title} (${i+1}/$inst)"
                    )
                    repository.insert(finalTx)
                    
                    // Add 1 month for next installment
                    calendar.add(Calendar.MONTH, 1)
                }
            } else {
                repository.insert(baseTransaction)
            }
        }
    }

    fun deleteTransaction(id: Int) {
        executeAndBackup { repository.deleteById(id) }
    }
    
    fun addSaving(saving: SavingGoal) {
        executeAndBackup { repository.insertSaving(saving) }
    }
    
    fun deleteSaving(id: Int) {
        executeAndBackup { repository.deleteSavingById(id) }
    }

    private fun updateXauInBankRates(sivas24Buy: String, sivas24Sell: String) {
        val current = _bankRates.value.toMutableList()
        val index = current.indexOfFirst { it.code == "XAU" }
        try {
            val buyD = sivas24Buy.replace(".", "").replace(",", ".").trim().toDoubleOrNull() ?: 3000.0
            val sellD = sivas24Sell.replace(".", "").replace(",", ".").trim().toDoubleOrNull() ?: 3130.0
            
            val formattedBuy = String.format(java.util.Locale("tr"), "%,.2f", buyD)
            val formattedSell = String.format(java.util.Locale("tr"), "%,.2f", sellD)
            
            val newXau = BankRate("XAU", "Altın (gram)", formattedBuy, formattedSell)
            if (index >= 0) {
                current[index] = newXau
            } else {
                current.add(newXau)
            }
            _bankRates.value = current
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    private val _customPricesRefreshTrigger = MutableStateFlow(0L)
    val customPricesRefreshTrigger = _customPricesRefreshTrigger.asStateFlow()

    fun updateCustomPrice(category: String, price: Double) {
        preferenceManager.setCustomPrice(category, price)
        _customPricesRefreshTrigger.value = System.currentTimeMillis()
    }
    
    fun triggerCustomPricesRefresh() {
        _customPricesRefreshTrigger.value = System.currentTimeMillis()
    }
    
    fun addCategory(category: AppCategory) {
        val trimmedName = category.name.trim()
        if (trimmedName.isBlank()) return
        executeAndBackup {
            val existing = repository.getCategoriesByType(category.type).first()
            val exists = existing.any { it.name.trim().lowercase() == trimmedName.lowercase() }
            if (!exists) {
                repository.insertCategory(category.copy(name = trimmedName))
            }
        }
    }
    
    fun deleteCategory(category: AppCategory) {
        executeAndBackup { repository.deleteCategory(category) }
    }
    
    fun addPerson(person: Person) {
        val trimmedName = person.name.trim()
        if (trimmedName.isBlank()) return
        executeAndBackup {
            val existing = repository.allPersons.first()
            val exists = existing.any { it.name.trim().lowercase() == trimmedName.lowercase() }
            if (!exists) {
                repository.insertPerson(person.copy(name = trimmedName))
            }
        }
    }
    
    fun deletePerson(person: Person) {
        executeAndBackup { repository.deletePerson(person) }
    }

    fun updateCategory(category: AppCategory) {
        val trimmedName = category.name.trim()
        if (trimmedName.isBlank()) return
        executeAndBackup {
            val existing = repository.getCategoriesByType(category.type).first()
            val exists = existing.any { 
                it.id != category.id && it.name.trim().lowercase() == trimmedName.lowercase() 
            }
            if (!exists) {
                repository.updateCategory(category.copy(name = trimmedName))
            }
        }
    }

    fun updatePerson(person: Person) {
        val trimmedName = person.name.trim()
        if (trimmedName.isBlank()) return
        executeAndBackup {
            val existing = repository.allPersons.first()
            val exists = existing.any { 
                it.id != person.id && it.name.trim().lowercase() == trimmedName.lowercase() 
            }
            if (!exists) {
                repository.updatePerson(person.copy(name = trimmedName))
            }
        }
    }

    private fun initCloud(): Boolean {
        return CloudSyncManager.initialize(getApplication())
    }

    fun login(userId: String) {
        if (userId.isNotBlank()) {
            preferenceManager.userId = userId
            _currentUserId.value = userId
            _syncStatus.value = "Oturum açılıyor ve eşitleniyor..."
            restoreFromCloudSilently {
                _syncStatus.value = null
            }
        }
    }

    fun logout() {
        preferenceManager.userId = ""
        _currentUserId.value = ""
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    private fun backupToCloudSilently() {
        val userId = preferenceManager.userId
        if (userId.isBlank()) return
        if (!initCloud()) return

        viewModelScope.launch {
            val txs = uiState.value.transactions
            val incCats = incomeCategories.value
            val expCats = expenseCategories.value
            val savCats = savingCategories.value
            val ppl = persons.value
            val savings = uiState.value.savings
            val allCats = incCats + expCats + savCats

            val data = mapOf(
                "transactions" to txs,
                "categories" to allCats,
                "persons" to ppl,
                "savings" to savings
            )

            CloudSyncManager.backupData(userId, data)
        }
    }

    private fun restoreFromCloudSilently(onComplete: (() -> Unit)? = null) {
        val userId = preferenceManager.userId
        if (userId.isBlank()) return
        if (!initCloud()) return

        viewModelScope.launch {
            val data = CloudSyncManager.restoreData(userId)
            if (data != null) {
                try {
                    val rawTxs = data["transactions"] as? List<Map<String, Any>> ?: emptyList()
                    val newTxs = rawTxs.map { 
                        Transaction(
                            id = (it["id"] as? Number)?.toInt() ?: 0,
                            amount = (it["amount"] as? Number)?.toDouble() ?: 0.0,
                            title = it["title"] as? String ?: "",
                            type = TransactionType.valueOf(it["type"] as? String ?: "EXPENSE"),
                            category = it["category"] as? String ?: "",
                            person = it["person"] as? String,
                            timestamp = (it["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            quantity = (it["quantity"] as? Number)?.toDouble(),
                            unitPrice = (it["unitPrice"] as? Number)?.toDouble(),
                            installments = (it["installments"] as? Number)?.toInt(),
                            isPaid = (it["isPaid"] as? Boolean) ?: (it["paid"] as? Boolean) ?: false
                        )
                    }

                    val rawCats = data["categories"] as? List<Map<String, Any>> ?: emptyList()
                    val newCats = rawCats.map {
                        AppCategory(
                            id = (it["id"] as? Number)?.toInt() ?: 0,
                            name = it["name"] as? String ?: "",
                            type = it["type"] as? String ?: "EXPENSE"
                        )
                    }

                    val rawPpl = data["persons"] as? List<Map<String, Any>> ?: emptyList()
                    val newPpl = rawPpl.map {
                        Person(
                            id = (it["id"] as? Number)?.toInt() ?: 0,
                            name = it["name"] as? String ?: ""
                        )
                    }
                    
                    val rawSavs = data["savings"] as? List<Map<String, Any>> ?: emptyList()
                    val newSavs = rawSavs.map {
                        SavingGoal(
                            id = (it["id"] as? Number)?.toInt() ?: 0,
                            title = it["title"] as? String ?: "",
                            targetAmount = (it["targetAmount"] as? Number)?.toDouble() ?: 0.0,
                            savedAmount = (it["savedAmount"] as? Number)?.toDouble() ?: 0.0,
                            timestamp = (it["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                        )
                    }

                    repository.clearAndRestoreData(newTxs, newSavs, newCats, newPpl)
                    restoreCategoriesAndPeopleFromTransactions()
                } catch (e: Exception) {
                    // ignore
                }
            } else {
                try {
                    restoreCategoriesAndPeopleFromTransactions()
                } catch (e: Exception) {
                    // ignore
                }
            }
            onComplete?.invoke()
        }
    }
    
    fun clearSyncStatus() {
        _syncStatus.value = null
    }

    fun exportTransactionsXls(context: android.content.Context, uri: android.net.Uri, onResult: (Boolean, String) -> Unit) {
        val txs = uiState.value.transactions
        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale("tr"))
        executeAndBackup {
            try {
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        val workbook = jxl.Workbook.createWorkbook(outputStream)
                        val sheet = workbook.createSheet("Islemler", 0)
                        
                        val headers = listOf("Id", "Tarih", "Tür", "Kategori", "Kisi", "Tutar", "Miktar", "Birim Fiyat", "Odendi")
                        headers.forEachIndexed { index, header ->
                            sheet.addCell(jxl.write.Label(index, 0, header))
                        }
                        
                        txs.forEachIndexed { rowIndex, tx ->
                            val row = rowIndex + 1
                            val date = sdf.format(java.util.Date(tx.timestamp))
                            val type = when (tx.type) {
                                TransactionType.INCOME -> "Gelir"
                                TransactionType.EXPENSE -> "Gider"
                                TransactionType.SAVING -> "Birikim"
                            }
                            
                            sheet.addCell(jxl.write.Label(0, row, tx.id.toString()))
                            sheet.addCell(jxl.write.Label(1, row, date))
                            sheet.addCell(jxl.write.Label(2, row, type))
                            sheet.addCell(jxl.write.Label(3, row, tx.category ?: ""))
                            sheet.addCell(jxl.write.Label(4, row, tx.person ?: ""))
                            sheet.addCell(jxl.write.Number(5, row, tx.amount))
                            if (tx.quantity != null) sheet.addCell(jxl.write.Number(6, row, tx.quantity!!))
                            if (tx.unitPrice != null) sheet.addCell(jxl.write.Number(7, row, tx.unitPrice!!))
                            sheet.addCell(jxl.write.Label(8, row, if (tx.isPaid) "Evet" else "Hayir"))
                        }
                        
                        workbook.write()
                        workbook.close()
                    }
                }
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onResult(true, "Dışa aktarma başarılı")
                }
            } catch (e: Exception) {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onResult(false, e.message ?: "Bilinmeyen hata")
                }
            }
        }
    }

    fun importTransactionsXls(context: android.content.Context, uri: android.net.Uri, onResult: (Boolean, String) -> Unit) {
        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale("tr"))
        executeAndBackup {
            try {
                val transactionsToInsert = mutableListOf<Transaction>()
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val workbook = jxl.Workbook.getWorkbook(inputStream)
                        val sheet = workbook.getSheet(0)
                        val rows = sheet.rows
                        for (i in 1 until rows) {
                            val row = sheet.getRow(i)
                            if (row.size < 6) continue // Must have at least Id..Tutar
                            
                            fun cellStr(idx: Int): String = if (idx < row.size) row[idx].contents.trim() else ""
                            
                            try {
                                val dateStr = cellStr(1).takeIf { it.isNotBlank() }
                                val date = if (dateStr != null) sdf.parse(dateStr)?.time ?: System.currentTimeMillis() else System.currentTimeMillis()
                                val typeStr = cellStr(2).lowercase(java.util.Locale("tr"))
                                val type = when {
                                    typeStr.contains("gelir") || typeStr.contains("income") -> TransactionType.INCOME
                                    typeStr.contains("birikim") || typeStr.contains("saving") -> TransactionType.SAVING
                                    else -> TransactionType.EXPENSE
                                }
                                val category = cellStr(3)
                                val person = cellStr(4).takeIf { it.isNotBlank() }
                                
                                val parseNum: (String) -> Double? = { str ->
                                    val s = str.trim()
                                    if (s.isEmpty()) null else {
                                        if (s.contains(",") && !s.contains(".")) {
                                            s.replace(",", ".").toDoubleOrNull()
                                        } else if (s.contains(".") && s.contains(",")) {
                                            s.replace(".", "").replace(",", ".").toDoubleOrNull()
                                        } else {
                                            s.toDoubleOrNull()
                                        }
                                    }
                                }
                                
                                val amount = parseNum(cellStr(5)) ?: 0.0
                                val qty = parseNum(cellStr(6))
                                val uprice = parseNum(cellStr(7))
                                
                                val isPaidStr = cellStr(8)
                                val isPaid = isPaidStr.equals("Evet", ignoreCase = true) || isPaidStr.equals("Yes", ignoreCase = true)
                                
                                transactionsToInsert.add(Transaction(
                                    amount = amount,
                                    title = category,
                                    type = type,
                                    category = category,
                                    person = person,
                                    timestamp = date,
                                    quantity = qty,
                                    unitPrice = uprice,
                                    isPaid = isPaid
                                ))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        workbook.close()
                    }
                    for (tx in transactionsToInsert) {
                        repository.insert(tx)
                    }
                    restoreCategoriesAndPeopleFromTransactions()
                }
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onResult(true, "İçe aktarma başarılı. ${transactionsToInsert.size} kayıt eklendi.")
                }
            } catch (e: Exception) {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    val msg = if (e.message?.contains("OLE stream", ignoreCase = true) == true) {
                        "HATA: Yüklediğiniz dosya yeni nesil XLSX formatında. Lütfen Excel'de 'Farklı Kaydet' diyerek 'Excel 97-2003 Çalışma Kitabı (*.xls)' formatında kaydedip tekrar yüklemeyi deneyin."
                    } else if (e.message?.contains("jxl.read.biff.BiffException", ignoreCase = true) == true) {
                        "Geçersiz dosya formatı. Lütfen Excel 97-2003 (*.xls) dosya formatını kullandığınızdan emin olun."
                    } else {
                        e.message ?: "Bilinmeyen hata"
                    }
                    onResult(false, msg)
                }
            }
        }
    }

    private suspend fun deduplicateCurrentCategoriesAndPersons() {
        try {
            // Deduplicate categories: Keep only the first unique ID for each lowercase trimmed name within each type
            val allTypes = listOf("INCOME", "EXPENSE", "SAVING")
            for (type in allTypes) {
                val dbCategories = repository.getCategoriesByType(type).first()
                val seenNames = mutableSetOf<String>()
                for (cat in dbCategories) {
                    val nameLower = cat.name.trim().lowercase()
                    if (seenNames.contains(nameLower)) {
                        // Already exists another category with this name under this type, delete this redundant row
                        repository.deleteCategory(cat)
                    } else {
                        seenNames.add(nameLower)
                    }
                }
            }

            // Deduplicate persons
            val dbPersons = repository.allPersons.first()
            val seenPersonNames = mutableSetOf<String>()
            for (person in dbPersons) {
                val nameLower = person.name.trim().lowercase()
                if (seenPersonNames.contains(nameLower)) {
                    repository.deletePerson(person)
                } else {
                    seenPersonNames.add(nameLower)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun preloadCategoriesAndPeople() {
        try {
            // First, deduplicate any legacy redundant entries currently in the database
            deduplicateCurrentCategoriesAndPersons()

            // Run transaction-based reconstruction
            restoreCategoriesAndPeopleFromTransactions()
            
            val incomes = repository.getCategoriesByType("INCOME").first()
            val expenses = repository.getCategoriesByType("EXPENSE").first()
            val savings = repository.getCategoriesByType("SAVING").first()
            val txs = repository.allTransactions.first()
            
            // Only add clean generic fallbacks if there are absolutely NO existing transactions AND no categories
            if (incomes.isEmpty() && expenses.isEmpty() && savings.isEmpty() && txs.isEmpty()) {
                val defaultCategoryList = listOf(
                    AppCategory(name = "Maaş", type = "INCOME"),
                    AppCategory(name = "Ek Gelir", type = "INCOME"),
                    AppCategory(name = "Market & Gıda", type = "EXPENSE"),
                    AppCategory(name = "Ev Kirası", type = "EXPENSE"),
                    AppCategory(name = "Faturalar", type = "EXPENSE"),
                    AppCategory(name = "Ulaşım / Yakıt", type = "EXPENSE"),
                    AppCategory(name = "Altın", type = "SAVING"),
                    AppCategory(name = "Döviz", type = "SAVING")
                )
                for (cat in defaultCategoryList) {
                    repository.insertCategory(cat)
                }
            }
            
            val currentPersons = repository.allPersons.first()
            if (currentPersons.isEmpty() && txs.isEmpty()) {
                val defaultPersonList = listOf(
                    Person(name = "Kendim")
                )
                for (p in defaultPersonList) {
                    repository.insertPerson(p)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun restoreCategoriesAndPeopleFromTransactions() {
        try {
            val txs = repository.allTransactions.first()
            if (txs.isEmpty()) return
            
            // Use mutable sets to immediately track insertions inside the loop
            val currentIncomes = repository.getCategoriesByType("INCOME").first().map { it.name.trim().lowercase() }.toMutableSet()
            val currentExpenses = repository.getCategoriesByType("EXPENSE").first().map { it.name.trim().lowercase() }.toMutableSet()
            val currentSavings = repository.getCategoriesByType("SAVING").first().map { it.name.trim().lowercase() }.toMutableSet()
            val currentPersons = repository.allPersons.first().map { it.name.trim().lowercase() }.toMutableSet()
            
            txs.forEach { tx ->
                val catName = tx.category.trim()
                if (catName.isNotBlank()) {
                    val catLower = catName.lowercase()
                    when (tx.type) {
                        TransactionType.INCOME -> {
                            if (!currentIncomes.contains(catLower)) {
                                currentIncomes.add(catLower)
                                repository.insertCategory(AppCategory(name = catName, type = "INCOME"))
                            }
                        }
                        TransactionType.EXPENSE -> {
                            if (!currentExpenses.contains(catLower)) {
                                currentExpenses.add(catLower)
                                repository.insertCategory(AppCategory(name = catName, type = "EXPENSE"))
                            }
                        }
                        TransactionType.SAVING -> {
                            if (!currentSavings.contains(catLower)) {
                                currentSavings.add(catLower)
                                repository.insertCategory(AppCategory(name = catName, type = "SAVING"))
                            }
                        }
                    }
                }
                
                val pName = tx.person?.trim()
                if (!pName.isNullOrBlank()) {
                    val pLower = pName.lowercase()
                    if (!currentPersons.contains(pLower)) {
                        currentPersons.add(pLower)
                        repository.insertPerson(Person(name = pName))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class BudgetViewModelFactory(
    private val application: Application,
    private val repository: TransactionRepository,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(application, repository, preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
