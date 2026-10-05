package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_logs")
data class WorkLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: String,
    val employeeName: String,
    val dateString: String,
    val timestamp: Long,
    val projectOrClient: String,
    val tasksAccomplished: String,
    val hoursSpent: Double,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val status: String = "COMPLETED", // "COMPLETED", "IN_PROGRESS", "REVIEW"
    val notes: String = ""
)
