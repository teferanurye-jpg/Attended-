package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: String,
    val employeeName: String,
    val type: String, // "CLOCK_IN", "CLOCK_OUT", "BREAK_START", "BREAK_END", "FIELD_VISIT"
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val branchId: Long,
    val branchName: String,
    val distanceToBranchMeters: Float,
    val isWithinGeofence: Boolean,
    val notes: String = "",
    val locationAddress: String = ""
)
