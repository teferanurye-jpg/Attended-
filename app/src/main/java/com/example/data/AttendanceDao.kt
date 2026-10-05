package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay ORDER BY timestamp ASC")
    fun getRecordsForDay(startOfDay: Long, endOfDay: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC LIMIT 1")
    fun getLatestRecord(): Flow<AttendanceRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecord): Long

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)

    @Query("DELETE FROM attendance_records")
    suspend fun clearAll()
}
