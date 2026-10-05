package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company_branches")
data class CompanyBranch(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Int = 250, // default 250 meters geofence radius
    val isDefault: Boolean = false,
    val address: String = ""
)
