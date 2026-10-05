package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkLogDao {
    @Query("SELECT * FROM work_logs ORDER BY timestamp DESC")
    fun getAllWorkLogs(): Flow<List<WorkLog>>

    @Query("SELECT * FROM work_logs WHERE dateString = :dateString ORDER BY timestamp DESC")
    fun getWorkLogsForDate(dateString: String): Flow<List<WorkLog>>

    @Query("SELECT * FROM work_logs WHERE id = :id LIMIT 1")
    fun getWorkLogById(id: Long): Flow<WorkLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkLog(log: WorkLog): Long

    @Update
    suspend fun updateWorkLog(log: WorkLog)

    @Query("DELETE FROM work_logs WHERE id = :id")
    suspend fun deleteWorkLog(id: Long)

    @Query("DELETE FROM work_logs")
    suspend fun clearAll()
}
