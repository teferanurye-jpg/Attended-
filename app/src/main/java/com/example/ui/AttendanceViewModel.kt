package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AttendanceRecord
import com.example.data.AttendanceRepository
import com.example.data.CompanyBranch
import com.example.data.EmployeeProfile
import com.example.data.WorkLog
import com.example.location.GeofenceCalculator
import com.example.location.LocationStatus
import com.example.location.LocationTracker
import com.example.location.UserLocation
import com.example.util.EthioDateTimeUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AttendanceStatus {
    CLOCKED_OUT,
    CLOCKED_IN,
    ON_BREAK
}

data class DashboardUiState(
    val currentStatus: AttendanceStatus = AttendanceStatus.CLOCKED_OUT,
    val clockInTimestamp: Long? = null,
    val breakStartTimestamp: Long? = null,
    val totalWorkDurationMillis: Long = 0L,
    val activeBreakDurationMillis: Long = 0L,
    val isPunctual: Boolean = true,
    val messageSnackbar: String? = null
)

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AttendanceRepository(db.attendanceDao(), db.branchDao(), db.workLogDao(), application)
    val locationTracker = LocationTracker(application)

    val employeeProfile: StateFlow<EmployeeProfile> = repository.employeeProfile
    val allBranches: StateFlow<List<CompanyBranch>> = repository.allBranches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecords: StateFlow<List<AttendanceRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkLogs: StateFlow<List<WorkLog>> = repository.allWorkLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentLocation: StateFlow<UserLocation?> = locationTracker.currentLocation
    val locationStatus: StateFlow<LocationStatus> = locationTracker.locationStatus

    private val _selectedBranchId = MutableStateFlow<Long?>(null)

    val selectedBranch: StateFlow<CompanyBranch?> = combine(allBranches, _selectedBranchId) { branches, id ->
        if (branches.isEmpty()) null
        else branches.firstOrNull { it.id == id } ?: branches.firstOrNull { it.isDefault } ?: branches.first()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Calculate real-time distance to selected company branch
    val distanceToBranch: StateFlow<Float?> = combine(currentLocation, selectedBranch) { loc, branch ->
        if (loc != null && branch != null) {
            GeofenceCalculator.calculateDistanceMeters(
                loc.latitude,
                loc.longitude,
                branch.latitude,
                branch.longitude
            )
        } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Calculate geofence status
    val isWithinGeofence: StateFlow<Boolean> = combine(
        distanceToBranch,
        selectedBranch,
        employeeProfile
    ) { distance, branch, profile ->
        if (profile.isDemoGeofenceBypassEnabled) {
            true
        } else if (distance != null && branch != null) {
            distance <= branch.radiusMeters
        } else {
            false
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Today's records
    private val _todayStart = EthioDateTimeUtil.getStartOfDay()
    private val _todayEnd = EthioDateTimeUtil.getEndOfDay()

    val todayRecords: StateFlow<List<AttendanceRecord>> = repository.getRecordsForDay(_todayStart, _todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dashboardState = MutableStateFlow(DashboardUiState())
    val dashboardState: StateFlow<DashboardUiState> = _dashboardState.asStateFlow()

    private var durationTickerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureDefaultBranches()
        }

        // Start location tracking
        locationTracker.startTracking()

        // Sync dashboard status from today's latest records
        viewModelScope.launch {
            todayRecords.collect { records ->
                computeAttendanceStatus(records)
            }
        }

        startDurationTicker()
    }

    private fun computeAttendanceStatus(records: List<AttendanceRecord>) {
        if (records.isEmpty()) {
            _dashboardState.value = _dashboardState.value.copy(
                currentStatus = AttendanceStatus.CLOCKED_OUT,
                clockInTimestamp = null,
                breakStartTimestamp = null,
                totalWorkDurationMillis = 0L
            )
            return
        }

        val lastRecord = records.last()
        val firstClockIn = records.firstOrNull { it.type == "CLOCK_IN" }

        val status = when (lastRecord.type) {
            "CLOCK_IN", "BREAK_END" -> AttendanceStatus.CLOCKED_IN
            "BREAK_START" -> AttendanceStatus.ON_BREAK
            "CLOCK_OUT" -> AttendanceStatus.CLOCKED_OUT
            else -> _dashboardState.value.currentStatus
        }

        val breakStart = if (lastRecord.type == "BREAK_START") lastRecord.timestamp else null
        val clockInTime = firstClockIn?.timestamp

        val isPunctual = if (clockInTime != null) {
            EthioDateTimeUtil.isPunctual(clockInTime)
        } else true

        _dashboardState.value = _dashboardState.value.copy(
            currentStatus = status,
            clockInTimestamp = clockInTime,
            breakStartTimestamp = breakStart,
            isPunctual = isPunctual
        )
    }

    private fun startDurationTicker() {
        durationTickerJob?.cancel()
        durationTickerJob = viewModelScope.launch {
            while (true) {
                val state = _dashboardState.value
                val clockIn = state.clockInTimestamp
                if (clockIn != null && state.currentStatus == AttendanceStatus.CLOCKED_IN) {
                    val worked = System.currentTimeMillis() - clockIn
                    _dashboardState.value = _dashboardState.value.copy(
                        totalWorkDurationMillis = maxOf(0L, worked)
                    )
                }
                delay(1000L)
            }
        }
    }

    fun setSelectedBranch(branch: CompanyBranch) {
        _selectedBranchId.value = branch.id
    }

    fun clockIn(notes: String = "") {
        val loc = currentLocation.value
        val branch = selectedBranch.value
        val profile = employeeProfile.value
        val now = System.currentTimeMillis()

        val record = AttendanceRecord(
            employeeId = profile.employeeId,
            employeeName = profile.fullName,
            type = "CLOCK_IN",
            timestamp = now,
            latitude = loc?.latitude ?: (branch?.latitude ?: 9.0227),
            longitude = loc?.longitude ?: (branch?.longitude ?: 38.7468),
            accuracy = loc?.accuracy ?: 5f,
            branchId = branch?.id ?: 1L,
            branchName = branch?.name ?: "Ethio HQ",
            distanceToBranchMeters = distanceToBranch.value ?: 0f,
            isWithinGeofence = isWithinGeofence.value,
            notes = notes.ifBlank { if (isWithinGeofence.value) "On-Site Clock In" else "Remote / Field Clock In" },
            locationAddress = loc?.address ?: (branch?.address ?: "Addis Ababa, Ethiopia")
        )

        viewModelScope.launch {
            repository.insertRecord(record)
            showSnackbar("Successfully Clocked In at ${EthioDateTimeUtil.formatTime(now)}!")
        }
    }

    fun clockOut(notes: String = "") {
        val loc = currentLocation.value
        val branch = selectedBranch.value
        val profile = employeeProfile.value
        val now = System.currentTimeMillis()

        val record = AttendanceRecord(
            employeeId = profile.employeeId,
            employeeName = profile.fullName,
            type = "CLOCK_OUT",
            timestamp = now,
            latitude = loc?.latitude ?: (branch?.latitude ?: 9.0227),
            longitude = loc?.longitude ?: (branch?.longitude ?: 38.7468),
            accuracy = loc?.accuracy ?: 5f,
            branchId = branch?.id ?: 1L,
            branchName = branch?.name ?: "Ethio HQ",
            distanceToBranchMeters = distanceToBranch.value ?: 0f,
            isWithinGeofence = isWithinGeofence.value,
            notes = notes.ifBlank { "Daily Shift Completed" },
            locationAddress = loc?.address ?: (branch?.address ?: "Addis Ababa, Ethiopia")
        )

        viewModelScope.launch {
            repository.insertRecord(record)
            showSnackbar("Successfully Clocked Out at ${EthioDateTimeUtil.formatTime(now)}!")
        }
    }

    fun startBreak(reason: String = "Lunch Break") {
        val loc = currentLocation.value
        val branch = selectedBranch.value
        val profile = employeeProfile.value
        val now = System.currentTimeMillis()

        val record = AttendanceRecord(
            employeeId = profile.employeeId,
            employeeName = profile.fullName,
            type = "BREAK_START",
            timestamp = now,
            latitude = loc?.latitude ?: (branch?.latitude ?: 9.0227),
            longitude = loc?.longitude ?: (branch?.longitude ?: 38.7468),
            accuracy = loc?.accuracy ?: 5f,
            branchId = branch?.id ?: 1L,
            branchName = branch?.name ?: "Ethio HQ",
            distanceToBranchMeters = distanceToBranch.value ?: 0f,
            isWithinGeofence = isWithinGeofence.value,
            notes = reason,
            locationAddress = loc?.address ?: ""
        )

        viewModelScope.launch {
            repository.insertRecord(record)
            showSnackbar("Break started ($reason)")
        }
    }

    fun endBreak() {
        val loc = currentLocation.value
        val branch = selectedBranch.value
        val profile = employeeProfile.value
        val now = System.currentTimeMillis()

        val record = AttendanceRecord(
            employeeId = profile.employeeId,
            employeeName = profile.fullName,
            type = "BREAK_END",
            timestamp = now,
            latitude = loc?.latitude ?: (branch?.latitude ?: 9.0227),
            longitude = loc?.longitude ?: (branch?.longitude ?: 38.7468),
            accuracy = loc?.accuracy ?: 5f,
            branchId = branch?.id ?: 1L,
            branchName = branch?.name ?: "Ethio HQ",
            distanceToBranchMeters = distanceToBranch.value ?: 0f,
            isWithinGeofence = isWithinGeofence.value,
            notes = "Resumed work from break",
            locationAddress = loc?.address ?: ""
        )

        viewModelScope.launch {
            repository.insertRecord(record)
            showSnackbar("Resumed work from break!")
        }
    }

    fun tagFieldVisit(clientOrSiteName: String, notes: String) {
        val loc = currentLocation.value
        val branch = selectedBranch.value
        val profile = employeeProfile.value
        val now = System.currentTimeMillis()

        val record = AttendanceRecord(
            employeeId = profile.employeeId,
            employeeName = profile.fullName,
            type = "FIELD_VISIT",
            timestamp = now,
            latitude = loc?.latitude ?: 9.0227,
            longitude = loc?.longitude ?: 38.7468,
            accuracy = loc?.accuracy ?: 10f,
            branchId = branch?.id ?: 1L,
            branchName = "Field: $clientOrSiteName",
            distanceToBranchMeters = distanceToBranch.value ?: 0f,
            isWithinGeofence = false,
            notes = "Client/Site: $clientOrSiteName. $notes",
            locationAddress = loc?.address ?: clientOrSiteName
        )

        viewModelScope.launch {
            repository.insertRecord(record)
            showSnackbar("Field Visit Tagged at $clientOrSiteName!")
        }
    }

    fun addBranch(name: String, city: String, lat: Double, lon: Double, radius: Int, address: String) {
        viewModelScope.launch {
            val newBranch = CompanyBranch(
                name = name,
                city = city,
                latitude = lat,
                longitude = lon,
                radiusMeters = radius,
                isDefault = false,
                address = address
            )
            val id = repository.insertBranch(newBranch)
            _selectedBranchId.value = id
            showSnackbar("Branch '$name' added successfully!")
        }
    }

    fun deleteBranch(id: Long) {
        viewModelScope.launch {
            repository.deleteBranch(id)
            showSnackbar("Branch removed.")
        }
    }

    fun updateProfile(profile: EmployeeProfile) {
        repository.updateProfile(profile)
        showSnackbar("Profile updated!")
    }

    fun simulateAtHQ() {
        val branch = selectedBranch.value ?: allBranches.value.firstOrNull()
        if (branch != null) {
            locationTracker.setSimulatedLocation(
                latitude = branch.latitude,
                longitude = branch.longitude,
                label = "Inside ${branch.name}, ${branch.city}"
            )
            showSnackbar("Simulated GPS location inside ${branch.name}!")
        }
    }

    fun refreshRealLocation() {
        locationTracker.startTracking()
        showSnackbar("Refreshing real GPS coordinates...")
    }

    fun addWorkLog(projectOrClient: String, tasks: String, hours: Double, status: String, notes: String) {
        val loc = currentLocation.value
        val profile = employeeProfile.value
        val now = System.currentTimeMillis()
        val log = WorkLog(
            employeeId = profile.employeeId,
            employeeName = profile.fullName,
            dateString = EthioDateTimeUtil.formatDateShort(now),
            timestamp = now,
            projectOrClient = projectOrClient,
            tasksAccomplished = tasks,
            hoursSpent = hours,
            locationName = loc?.address ?: (selectedBranch.value?.name ?: "Addis Ababa"),
            latitude = loc?.latitude ?: 9.0227,
            longitude = loc?.longitude ?: 38.7468,
            status = status,
            notes = notes
        )
        viewModelScope.launch {
            repository.insertWorkLog(log)
            showSnackbar("የዕለት ሥራ ሪፖርት በተሳካ ሁኔታ ተመዝግቧል!")
        }
    }

    fun deleteWorkLog(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkLog(id)
            showSnackbar("የሥራ ሪፖርቱ ተሰርዟል።")
        }
    }

    fun clearAllAttendanceHistory() {
        viewModelScope.launch {
            repository.clearAllRecords()
            repository.clearAllWorkLogs()
            showSnackbar("Attendance history cleared.")
        }
    }

    private fun showSnackbar(message: String) {
        _dashboardState.value = _dashboardState.value.copy(messageSnackbar = message)
    }

    fun dismissSnackbar() {
        _dashboardState.value = _dashboardState.value.copy(messageSnackbar = null)
    }

    override fun onCleared() {
        super.onCleared()
        locationTracker.stopTracking()
        durationTickerJob?.cancel()
    }
}
