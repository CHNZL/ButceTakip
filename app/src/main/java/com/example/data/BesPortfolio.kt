package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bes_portfolio")
data class BesPortfolio(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val holderName: String = "BES",
    val startDate: Long,
    val investment: Double,
    val investmentReturn: Double,
    val stateContribution: Double,
    val stateContributionReturn: Double,
    val isRetired: Boolean = false
)

