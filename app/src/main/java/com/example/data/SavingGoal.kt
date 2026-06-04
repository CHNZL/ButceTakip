package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings")
data class SavingGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val timestamp: Long = System.currentTimeMillis()
)
