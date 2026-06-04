package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingDao {
    @Query("SELECT * FROM savings ORDER BY timestamp DESC")
    fun getAllSavings(): Flow<List<SavingGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaving(saving: SavingGoal)

    @Query("DELETE FROM savings WHERE id = :id")
    suspend fun deleteSavingById(id: Int)

    @Query("DELETE FROM savings")
    suspend fun clearAllSavings()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavings(savings: List<SavingGoal>)
}
