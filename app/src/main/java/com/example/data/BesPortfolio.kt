package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bes_portfolio")
data class BesPortfolio(
    @PrimaryKey val id: Int = 1,
    val startDate: Long,
    val investment: Double,
    val investmentReturn: Double,
    val stateContribution: Double,
    val stateContributionReturn: Double,
    val isRetired: Boolean = false
)
