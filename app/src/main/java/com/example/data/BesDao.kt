package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BesDao {
    @Query("SELECT * FROM bes_portfolio WHERE id = 1")
    fun getBesPortfolio(): Flow<BesPortfolio?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(besPortfolio: BesPortfolio)
}
