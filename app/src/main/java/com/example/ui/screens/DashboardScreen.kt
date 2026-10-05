package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AttendanceRecord
import com.example.data.CompanyBranch
import com.example.data.EmployeeProfile
import com.example.location.LocationStatus
import com.example.location.UserLocation
import com.example.ui.AttendanceStatus
import com.example.ui.DashboardUiState
import com.example.ui.components.AttendanceStatusBadge
import com.example.ui.components.LocationRadarCard
import com.example.ui.theme.EthioEmerald
import com.example.ui.theme.EthioEmeraldBright
import com.example.ui.theme.EthioEmeraldDark
import com.example.ui.theme.EthioGold
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.util.AppStrings
import com.example.util.EthioDateTimeUtil

@Composable
fun DashboardScreen(
    profile: EmployeeProfile,
    dashboardState: DashboardUiState,
    currentLocation: UserLocation?,
    locationStatus: LocationStatus,
    selectedBranch: CompanyBranch?,
    allBranches: List<CompanyBranch>,
    distanceToBranch: Float?,
    isWithinGeofence: Boolean,
    todayRecords: List<AttendanceRecord>,
    onClockIn: (notes: String) -> Unit,
    onClockOut: (notes: String) -> Unit,
    onStartBreak: (reason: String) -> Unit,
    onEndBreak: () -> Unit,
    onTagFieldVisit: (client: String, notes: String) -> Unit,
    onSelectBranch: (CompanyBranch) -> Unit,
    onRefreshLocation: () -> Unit,
    onSimulateHq: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showBranchSelectorDialog by remember { mutableStateOf(false) }
    var showFieldVisitDialog by remember { mutableStateOf(false) }

    fun vibrate() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header with corporate banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ethio_attendance_banner_1791140520755),
                    contentDescription = "Ethio Company Campus Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.2f),
                                    EthioEmeraldDark.copy(alpha = 0.88f)
                                )
                            )
                        )
                )

                // Header text content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = EthioEmerald.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = AppStrings.companyName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        AttendanceStatusBadge(status = dashboardState.currentStatus)
                    }

                    Column {
                        Text(
                            text = AppStrings.greeting(profile.fullName.split(" ").firstOrNull() ?: profile.fullName),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val now = System.currentTimeMillis()
                        Text(
                            text = "${EthioDateTimeUtil.formatDate(now)} • ${EthioDateTimeUtil.getEthiopianDateApprox(now)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${AppStrings.ethioLocalTimeLabel}፡ ${EthioDateTimeUtil.getEthiopianTime(now)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EthioGold,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Location & Geofence Status Card
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                LocationRadarCard(
                    currentLocation = currentLocation,
                    locationStatus = locationStatus,
                    selectedBranch = selectedBranch,
                    distanceToBranch = distanceToBranch,
                    isWithinGeofence = isWithinGeofence,
                    onRefreshLocation = onRefreshLocation,
                    onSimulateHq = onSimulateHq,
                    onChangeBranchClick = { showBranchSelectorDialog = true }
                )
            }
        }

        // Punch Clock Action Card
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = AppStrings.clockInHeader,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Live Work Timer
                        val durationText = when (dashboardState.currentStatus) {
                            AttendanceStatus.CLOCKED_IN -> {
                                val dur = dashboardState.totalWorkDurationMillis
                                val hrs = dur / (1000 * 60 * 60)
                                val mins = (dur / (1000 * 60)) % 60
                                val secs = (dur / 1000) % 60
                                "%02d:%02d:%02d".format(hrs, mins, secs)
                            }
                            AttendanceStatus.ON_BREAK -> AppStrings.statusOnBreak
                            AttendanceStatus.CLOCKED_OUT -> "--:--:--"
                        }

                        Text(
                            text = durationText,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = when (dashboardState.currentStatus) {
                                AttendanceStatus.CLOCKED_IN -> EthioEmerald
                                AttendanceStatus.ON_BREAK -> EthioGold
                                AttendanceStatus.CLOCKED_OUT -> MaterialTheme.colorScheme.onSurface
                            }
                        )

                        Text(
                            text = when (dashboardState.currentStatus) {
                                AttendanceStatus.CLOCKED_IN -> "ሥራ ላይ ነዎት • የገቡበት ሰዓት ${EthioDateTimeUtil.formatTime(dashboardState.clockInTimestamp ?: System.currentTimeMillis())}"
                                AttendanceStatus.ON_BREAK -> "በእረፍት ላይ ነዎት"
                                AttendanceStatus.CLOCKED_OUT -> AppStrings.shiftLabel
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Main Big Punch Button
                        when (dashboardState.currentStatus) {
                            AttendanceStatus.CLOCKED_OUT -> {
                                Button(
                                    onClick = {
                                        vibrate()
                                        onClockIn("")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .testTag("clock_in_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EthioEmerald,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (isWithinGeofence) AppStrings.clockInOnSiteButton else AppStrings.clockInRemoteButton,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            AttendanceStatus.CLOCKED_IN -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            vibrate()
                                            onStartBreak("Lunch Break")
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .testTag("start_break_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Coffee,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(AppStrings.startBreakButton, fontWeight = FontWeight.SemiBold)
                                    }

                                    Button(
                                        onClick = {
                                            vibrate()
                                            onClockOut("")
                                        },
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(52.dp)
                                            .testTag("clock_out_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = StatusError,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(AppStrings.clockOutButton, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            AttendanceStatus.ON_BREAK -> {
                                Button(
                                    onClick = {
                                        vibrate()
                                        onEndBreak()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .testTag("resume_work_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EthioGold,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = AppStrings.resumeWorkButton,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tag Client / Field Visit Button
                        OutlinedButton(
                            onClick = { showFieldVisitDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tag_field_visit_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = EthioEmerald
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.tagFieldVisitButton,
                                color = EthioEmerald,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Today's Activity Timeline Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.todaysActivity,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = AppStrings.eventsCount(todayRecords.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // Today's records list
        if (todayRecords.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = AppStrings.noActivityToday,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = AppStrings.tapToClockIn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(todayRecords.reversed()) { record ->
                AttendanceRecordItem(
                    record = record,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Branch selector modal
    if (showBranchSelectorDialog) {
        AlertDialog(
            onDismissRequest = { showBranchSelectorDialog = false },
            title = { Text(AppStrings.branchesTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allBranches.forEach { branch ->
                        val isSelected = branch.id == selectedBranch?.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onSelectBranch(branch)
                                    showBranchSelectorDialog = false
                                },
                            color = if (isSelected) EthioEmerald.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = branch.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) EthioEmerald else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${branch.city} • ራዲየስ ${branch.radiusMeters} ሜትር",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = EthioEmerald
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBranchSelectorDialog = false }) {
                    Text(AppStrings.close)
                }
            }
        )
    }

    // Field Visit Tagging Dialog
    if (showFieldVisitDialog) {
        var clientName by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showFieldVisitDialog = false },
            title = { Text(AppStrings.fieldVisitDialogTitle) },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = AppStrings.fieldVisitDialogDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text(AppStrings.clientNameLabel) },
                        placeholder = { Text(AppStrings.clientNamePlaceholder) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("field_client_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(AppStrings.fieldNotesLabel) },
                        placeholder = { Text(AppStrings.fieldNotesPlaceholder) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("field_notes_input"),
                        minLines = 2
                    )

                    if (currentLocation != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "GPS: %.5f, %.5f (±%dm)".format(
                                    currentLocation.latitude,
                                    currentLocation.longitude,
                                    currentLocation.accuracy.toInt()
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (clientName.isNotBlank()) {
                            onTagFieldVisit(clientName, notes)
                            showFieldVisitDialog = false
                        }
                    },
                    enabled = clientName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = EthioEmerald),
                    modifier = Modifier.testTag("confirm_field_visit_button")
                ) {
                    Text(AppStrings.stampVisitButton)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFieldVisitDialog = false }) {
                    Text(AppStrings.cancel)
                }
            }
        )
    }
}

@Composable
fun AttendanceRecordItem(
    record: AttendanceRecord,
    modifier: Modifier = Modifier
) {
    val (typeTitle, typeIcon, iconColor) = when (record.type) {
        "CLOCK_IN" -> Triple(AppStrings.typeClockIn, Icons.Default.Fingerprint, EthioEmerald)
        "CLOCK_OUT" -> Triple(AppStrings.typeClockOut, Icons.Default.Stop, StatusError)
        "BREAK_START" -> Triple(AppStrings.typeBreakStart, Icons.Default.Coffee, EthioGold)
        "BREAK_END" -> Triple(AppStrings.typeBreakEnd, Icons.Default.PlayArrow, EthioEmeraldBright)
        "FIELD_VISIT" -> Triple(AppStrings.typeFieldVisit, Icons.Default.LocationOn, Color(0xFF1E3A5F))
        else -> Triple(record.type, Icons.Default.AccessTime, Color.Gray)
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = typeTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = EthioDateTimeUtil.formatTime(record.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = record.branchName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

                if (record.notes.isNotBlank()) {
                    Text(
                        text = record.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Geofence status tag
                    Surface(
                        color = if (record.isWithinGeofence) StatusSuccess.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (record.isWithinGeofence) AppStrings.verifiedOnSite else AppStrings.remoteTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (record.isWithinGeofence) StatusSuccess else MaterialTheme.colorScheme.outline,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Accuracy & Coordinates tag
                    Text(
                        text = "±%dm • %.4f, %.4f".format(record.accuracy.toInt(), record.latitude, record.longitude),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
