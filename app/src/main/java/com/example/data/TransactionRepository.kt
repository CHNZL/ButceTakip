package com.example.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val savingDao: SavingDao,
    private val categoryDao: CategoryDao,
    private val personDao: PersonDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allSavings: Flow<List<SavingGoal>> = savingDao.getAllSavings()
    
    fun getCategoriesByType(type: String) = categoryDao.getCategoriesByType(type)
    val allPersons = personDao.getAllPersons()

    suspend fun insert(transaction: Transaction) = transactionDao.insertTransaction(transaction)
    suspend fun deleteById(id: Int) = transactionDao.deleteTransactionById(id)
    
    suspend fun insertSaving(saving: SavingGoal) = savingDao.insertSaving(saving)
    suspend fun deleteSavingById(id: Int) = savingDao.deleteSavingById(id)
    
    suspend fun insertCategory(category: AppCategory) = categoryDao.insertCategory(category)
    suspend fun updateCategory(category: AppCategory) = categoryDao.updateCategory(category)
    suspend fun deleteCategory(category: AppCategory) = categoryDao.deleteCategory(category)
    
    suspend fun insertPerson(person: Person) = personDao.insertPerson(person)
    suspend fun updatePerson(person: Person) = personDao.updatePerson(person)
    suspend fun deletePerson(person: Person) = personDao.deletePerson(person)

    suspend fun clearAllData() {
        transactionDao.clearAllTransactions()
        savingDao.clearAllSavings()
        categoryDao.clearAllCategories()
        personDao.clearAllPersons()
    }

    suspend fun clearAndRestoreData(transactions: List<Transaction>, savings: List<SavingGoal>, categories: List<AppCategory>, persons: List<Person>) {
        clearAllData()
        
        transactionDao.insertTransactions(transactions)
        savingDao.insertSavings(savings)
        categoryDao.insertCategories(categories)
        personDao.insertPersons(persons)
    }
}
