package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BranchDao {
    @Query("SELECT * FROM company_branches ORDER BY id ASC")
    fun getAllBranches(): Flow<List<CompanyBranch>>

    @Query("SELECT * FROM company_branches WHERE id = :id LIMIT 1")
    fun getBranchById(id: Long): Flow<CompanyBranch?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranch(branch: CompanyBranch): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranches(branches: List<CompanyBranch>)

    @Update
    suspend fun updateBranch(branch: CompanyBranch)

    @Query("DELETE FROM company_branches WHERE id = :id")
    suspend fun deleteBranch(id: Long)
}
