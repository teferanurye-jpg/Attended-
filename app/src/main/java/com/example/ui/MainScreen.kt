package com.example.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.BranchesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.EthioEmerald
import com.example.ui.theme.EthioEmeraldDark
import com.example.ui.theme.EthioGold
import com.example.ui.theme.EthioMint
import com.example.ui.theme.StatusWarning
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.LanguageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var currentLang by remember { mutableStateOf(LanguageManager.currentLanguage) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var hasLocationPermission by remember {
        mutableStateOf(viewModel.locationTracker.hasLocationPermission())
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            viewModel.locationTracker.startTracking()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val employeeProfile by viewModel.employeeProfile.collectAsStateWithLifecycle()
    val allBranches by viewModel.allBranches.collectAsStateWithLifecycle()
    val selectedBranch by viewModel.selectedBranch.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val locationStatus by viewModel.locationStatus.collectAsStateWithLifecycle()
    val distanceToBranch by viewModel.distanceToBranch.collectAsStateWithLifecycle()
    val isWithinGeofence by viewModel.isWithinGeofence.collectAsStateWithLifecycle()
    val todayRecords by viewModel.todayRecords.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val allWorkLogs by viewModel.allWorkLogs.collectAsStateWithLifecycle()

    // Handle Snackbar messages
    LaunchedEffect(dashboardState.messageSnackbar) {
        dashboardState.messageSnackbar?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(EthioGold, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.appTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }

                        // Language Toggle Chip (አማርኛ / EN)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    val newLang = if (LanguageManager.isAmharic()) AppLanguage.ENGLISH else AppLanguage.AMHARIC
                                    LanguageManager.currentLanguage = newLang
                                    currentLang = newLang
                                }
                                .padding(end = 12.dp)
                                .testTag("language_toggle"),
                            color = EthioEmerald.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Switch Language",
                                    tint = EthioMint,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (LanguageManager.isAmharic()) "አማርኛ" else "English",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EthioEmeraldDark,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = AppStrings.tabPunch
                        )
                    },
                    label = { Text(AppStrings.tabPunch, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EthioEmerald,
                        selectedTextColor = EthioEmerald,
                        indicatorColor = EthioEmerald.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_punch_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = AppStrings.tabBranches
                        )
                    },
                    label = { Text(AppStrings.tabBranches, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EthioEmerald,
                        selectedTextColor = EthioEmerald,
                        indicatorColor = EthioEmerald.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_branches_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = AppStrings.tabHistory
                        )
                    },
                    label = { Text(AppStrings.tabHistory, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EthioEmerald,
                        selectedTextColor = EthioEmerald,
                        indicatorColor = EthioEmerald.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_timesheet_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = AppStrings.tabProfile
                        )
                    },
                    label = { Text(AppStrings.tabProfile, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EthioEmerald,
                        selectedTextColor = EthioEmerald,
                        indicatorColor = EthioEmerald.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_profile_tab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Permission Banner if denied
            AnimatedVisibility(visible = !hasLocationPermission) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = StatusWarning.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOff,
                            contentDescription = null,
                            tint = EthioGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.locationPermRequired,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = EthioEmeraldDark
                            )
                            Text(
                                text = AppStrings.locationPermDesc,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EthioEmerald),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(AppStrings.grantPermButton, fontSize = 12.sp)
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> DashboardScreen(
                    profile = employeeProfile,
                    dashboardState = dashboardState,
                    currentLocation = currentLocation,
                    locationStatus = locationStatus,
                    selectedBranch = selectedBranch,
                    allBranches = allBranches,
                    distanceToBranch = distanceToBranch,
                    isWithinGeofence = isWithinGeofence,
                    todayRecords = todayRecords,
                    onClockIn = { notes -> viewModel.clockIn(notes) },
                    onClockOut = { notes -> viewModel.clockOut(notes) },
                    onStartBreak = { reason -> viewModel.startBreak(reason) },
                    onEndBreak = { viewModel.endBreak() },
                    onTagFieldVisit = { client, notes -> viewModel.tagFieldVisit(client, notes) },
                    onSelectBranch = { branch -> viewModel.setSelectedBranch(branch) },
                    onRefreshLocation = { viewModel.refreshRealLocation() },
                    onSimulateHq = { viewModel.simulateAtHQ() }
                )
                1 -> BranchesScreen(
                    branches = allBranches,
                    selectedBranch = selectedBranch,
                    currentLocation = currentLocation,
                    onSelectBranch = { branch -> viewModel.setSelectedBranch(branch) },
                    onAddBranch = { name, city, lat, lon, radius, address ->
                        viewModel.addBranch(name, city, lat, lon, radius, address)
                    },
                    onDeleteBranch = { id -> viewModel.deleteBranch(id) }
                )
                2 -> HistoryScreen(
                    records = allRecords,
                    workLogs = allWorkLogs,
                    onAddWorkLog = { project, tasks, hours, status, notes ->
                        viewModel.addWorkLog(project, tasks, hours, status, notes)
                    },
                    onDeleteWorkLog = { id -> viewModel.deleteWorkLog(id) },
                    onClearHistory = { viewModel.clearAllAttendanceHistory() }
                )
                3 -> ProfileScreen(
                    profile = employeeProfile,
                    assignedBranch = selectedBranch,
                    onUpdateProfile = { updated -> viewModel.updateProfile(updated) }
                )
            }
        }
    }
}
