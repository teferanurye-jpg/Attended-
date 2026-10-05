package com.example.data

data class EmployeeProfile(
    val employeeId: String = "ETH-8942",
    val fullName: String = "አበበ ታደሰ (Abebe Tadesse)",
    val department: String = "ሶፍትዌር ኢንጂነሪንግ (Software Engineering)",
    val role: String = "ሲኒየር ሲስተምስ መሃንዲስ (Senior Systems Engineer)",
    val email: String = "abebe.tadesse@ethiocompany.et",
    val phone: String = "+251 91 123 4567",
    val assignedBranchId: Long = 1L,
    val shiftName: String = "መደበኛ የሥራ ሰዓት (ከጧቱ 2:30 - ከሰዓት 11:30)",
    val isDemoGeofenceBypassEnabled: Boolean = false
)
