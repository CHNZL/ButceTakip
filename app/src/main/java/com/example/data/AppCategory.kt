package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class AppCategory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String // "INCOME", "EXPENSE", "SAVING"
)
