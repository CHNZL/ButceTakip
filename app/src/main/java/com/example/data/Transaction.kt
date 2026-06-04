package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    INCOME, EXPENSE, SAVING
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val title: String,
    val type: TransactionType,
    val category: String,
    val person: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val quantity: Double? = null,
    val unitPrice: Double? = null,
    val installments: Int? = null
)
