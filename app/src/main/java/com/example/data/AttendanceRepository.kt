package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AttendanceRepository(
    private val attendanceDao: AttendanceDao,
    private val branchDao: BranchDao,
    private val workLogDao: WorkLogDao,
    context: Context
) {
    val allRecords: Flow<List<AttendanceRecord>> = attendanceDao.getAllRecords()
    val allBranches: Flow<List<CompanyBranch>> = branchDao.getAllBranches()
    val latestRecord: Flow<AttendanceRecord?> = attendanceDao.getLatestRecord()
    val allWorkLogs: Flow<List<WorkLog>> = workLogDao.getAllWorkLogs()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ethio_attendance_prefs", Context.MODE_PRIVATE)

    private val _employeeProfile = MutableStateFlow(loadProfile())
    val employeeProfile: StateFlow<EmployeeProfile> = _employeeProfile.asStateFlow()

    fun getRecordsForDay(startOfDay: Long, endOfDay: Long): Flow<List<AttendanceRecord>> {
        return attendanceDao.getRecordsForDay(startOfDay, endOfDay)
    }

    suspend fun insertRecord(record: AttendanceRecord): Long = withContext(Dispatchers.IO) {
        attendanceDao.insertRecord(record)
    }

    suspend fun deleteRecord(id: Long) = withContext(Dispatchers.IO) {
        attendanceDao.deleteRecord(id)
    }

    suspend fun clearAllRecords() = withContext(Dispatchers.IO) {
        attendanceDao.clearAll()
    }

    suspend fun insertBranch(branch: CompanyBranch): Long = withContext(Dispatchers.IO) {
        branchDao.insertBranch(branch)
    }

    suspend fun deleteBranch(id: Long) = withContext(Dispatchers.IO) {
        branchDao.deleteBranch(id)
    }

    suspend fun insertWorkLog(log: WorkLog): Long = withContext(Dispatchers.IO) {
        workLogDao.insertWorkLog(log)
    }

    suspend fun deleteWorkLog(id: Long) = withContext(Dispatchers.IO) {
        workLogDao.deleteWorkLog(id)
    }

    suspend fun clearAllWorkLogs() = withContext(Dispatchers.IO) {
        workLogDao.clearAll()
    }

    suspend fun ensureDefaultBranches() = withContext(Dispatchers.IO) {
        branchDao.insertBranches(AppDatabase.DEFAULT_BRANCHES)
    }

    fun updateProfile(profile: EmployeeProfile) {
        _employeeProfile.value = profile
        saveProfile(profile)
    }

    private fun loadProfile(): EmployeeProfile {
        val id = prefs.getString("emp_id", "ETH-8942") ?: "ETH-8942"
        val name = prefs.getString("emp_name", "አበበ ታደሰ (Abebe Tadesse)") ?: "አበበ ታደሰ (Abebe Tadesse)"
        val dept = prefs.getString("emp_dept", "ሶፍትዌር ኢንጂነሪንግ (Software Engineering)") ?: "ሶፍትዌር ኢንጂነሪንግ (Software Engineering)"
        val role = prefs.getString("emp_role", "ሲኒየር ሲስተምስ መሃንዲስ (Senior Systems Engineer)") ?: "ሲኒየር ሲስተምስ መሃንዲስ (Senior Systems Engineer)"
        val email = prefs.getString("emp_email", "abebe.tadesse@ethiocompany.et") ?: "abebe.tadesse@ethiocompany.et"
        val phone = prefs.getString("emp_phone", "+251 91 123 4567") ?: "+251 91 123 4567"
        val branchId = prefs.getLong("emp_branch_id", 1L)
        val shift = prefs.getString("emp_shift", "መደበኛ የሥራ ሰዓት (ከጧቱ 2:30 - ከሰዓት 11:30)") ?: "መደበኛ የሥራ ሰዓት (ከጧቱ 2:30 - ከሰዓት 11:30)"
        val bypass = prefs.getBoolean("emp_bypass", false)
        return EmployeeProfile(
            employeeId = id,
            fullName = name,
            department = dept,
            role = role,
            email = email,
            phone = phone,
            assignedBranchId = branchId,
            shiftName = shift,
            isDemoGeofenceBypassEnabled = bypass
        )
    }

    private fun saveProfile(profile: EmployeeProfile) {
        prefs.edit()
            .putString("emp_id", profile.employeeId)
            .putString("emp_name", profile.fullName)
            .putString("emp_dept", profile.department)
            .putString("emp_role", profile.role)
            .putString("emp_email", profile.email)
            .putString("emp_phone", profile.phone)
            .putLong("emp_branch_id", profile.assignedBranchId)
            .putString("emp_shift", profile.shiftName)
            .putBoolean("emp_bypass", profile.isDemoGeofenceBypassEnabled)
            .apply()
    }
}
