package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.data.CloudSyncManager
import com.example.data.PreferenceManager
import com.example.data.TransactionRepository
import kotlinx.coroutines.flow.first

class CloudBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefManager = PreferenceManager(applicationContext)
        val userId = prefManager.userId
        
        if (userId.isBlank()) {
            return Result.success()
        }

        if (!CloudSyncManager.isConfigured) {
            val initialized = CloudSyncManager.initialize(applicationContext)
            if (!initialized) {
                return Result.success()
            }
        }

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TransactionRepository(
            database.transactionDao(),
            database.savingDao(),
            database.categoryDao(),
            database.personDao()
        )

        try {
            val txs = repository.allTransactions.first()
            val savings = repository.allSavings.first()
            val allCats = repository.getCategoriesByType("INCOME").first() + 
                          repository.getCategoriesByType("EXPENSE").first() + 
                          repository.getCategoriesByType("SAVING").first()
            val ppl = repository.allPersons.first()

            val data = mapOf(
                "transactions" to txs,
                "categories" to allCats,
                "persons" to ppl,
                "savings" to savings
            )

            val success = CloudSyncManager.backupData(userId, data)
            return if (success) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
