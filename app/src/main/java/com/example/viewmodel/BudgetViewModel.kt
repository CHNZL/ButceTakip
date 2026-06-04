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
import kotlinx.coroutines.launch

import com.example.data.AppCategory
import com.example.data.Person

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.data.CloudSyncManager
import com.example.data.PreferenceManager
import com.example.data.TransactionType

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

    fun deleteTransaction(id: Int) {
        executeAndBackup { repository.deleteById(id) }
    }
    
    fun addSaving(saving: SavingGoal) {
        executeAndBackup { repository.insertSaving(saving) }
    }
    
    fun deleteSaving(id: Int) {
        executeAndBackup { repository.deleteSavingById(id) }
    }
    
    fun addCategory(category: AppCategory) {
        executeAndBackup { repository.insertCategory(category) }
    }
    
    fun deleteCategory(category: AppCategory) {
        executeAndBackup { repository.deleteCategory(category) }
    }
    
    fun addPerson(person: Person) {
        executeAndBackup { repository.insertPerson(person) }
    }
    
    fun deletePerson(person: Person) {
        executeAndBackup { repository.deletePerson(person) }
    }

    fun updateCategory(category: AppCategory) {
        executeAndBackup { repository.updateCategory(category) }
    }

    fun updatePerson(person: Person) {
        executeAndBackup { repository.updatePerson(person) }
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
                            installments = (it["installments"] as? Number)?.toInt()
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
