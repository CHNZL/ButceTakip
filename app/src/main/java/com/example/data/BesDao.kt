package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BesDao {
    @Query("SELECT * FROM bes_portfolio")
    fun getAllBesPortfolios(): Flow<List<BesPortfolio>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(besPortfolio: BesPortfolio)

    @Delete
    suspend fun delete(besPortfolio: BesPortfolio)
}

