package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AttendanceRecord
import com.example.data.WorkLog
import com.example.ui.theme.EthioEmerald
import com.example.ui.theme.EthioGold
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.util.AppStrings
import com.example.util.EthioDateTimeUtil
import com.example.util.LanguageManager

@Composable
fun HistoryScreen(
    records: List<AttendanceRecord>,
    workLogs: List<WorkLog>,
    onAddWorkLog: (project: String, tasks: String, hours: Double, status: String, notes: String) -> Unit,
    onDeleteWorkLog: (Long) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = Attendance, 1 = Work Logs
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showClearDialog by remember { mutableStateOf(false) }
    var showAddWorkLogDialog by remember { mutableStateOf(false) }

    val filteredRecords = remember(records, selectedFilter) {
        when (selectedFilter) {
            "CLOCK_IN" -> records.filter { it.type == "CLOCK_IN" }
            "CLOCK_OUT" -> records.filter { it.type == "CLOCK_OUT" }
            "FIELD_VISIT" -> records.filter { it.type == "FIELD_VISIT" }
            "BREAK" -> records.filter { it.type.startsWith("BREAK") }
            else -> records
        }
    }

    // Stats
    val clockInCount = records.count { it.type == "CLOCK_IN" }
    val fieldVisitCount = records.count { it.type == "FIELD_VISIT" }
    val onSitePunches = records.count { it.isWithinGeofence }
    val onSiteRate = if (records.isNotEmpty()) (onSitePunches * 100) / records.size else 100
    val totalLoggedHours = workLogs.sumOf { it.hoursSpent }

    fun exportReport() {
        if (records.isEmpty() && workLogs.isEmpty()) {
            Toast.makeText(context, "ምንም የተመዘገበ ታሪክ የለም", Toast.LENGTH_SHORT).show()
            return
        }

        val sb = StringBuilder()
        sb.append("የኢትዮጵያ ኩባንያ - የሠራተኞች የሥራ ሰዓትና የቦታ ክትትል ሪፖርት\n")
        sb.append("የወጣበት ቀን፡ ${EthioDateTimeUtil.formatDate(System.currentTimeMillis())}\n")
        sb.append("--------------------------------------------------\n\n")

        if (records.isNotEmpty()) {
            sb.append("--- የሰዓት ምዝገባ ታሪክ (ATTENDANCE LOGS) ---\n")
            records.forEach { r ->
                sb.append("[${r.type}] ${EthioDateTimeUtil.formatDate(r.timestamp)} ${EthioDateTimeUtil.formatTime(r.timestamp)}\n")
                sb.append("ሠራተኛ: ${r.employeeName} (${r.employeeId})\n")
                sb.append("ቢሮ/ሳይት: ${r.branchName}\n")
                sb.append("ጂፒኤስ: ${r.latitude}, ${r.longitude} (ትክክለኛነት፡ ±${r.accuracy.toInt()} ሜትር)\n")
                sb.append("ሁኔታ: ${if (r.isWithinGeofence) "በቢሮ ውስጥ የተረጋገጠ" else "የመስክ/የርቀት ምዝገባ"}\n")
                if (r.notes.isNotBlank()) sb.append("ማስታወሻ: ${r.notes}\n")
                sb.append("\n")
            }
        }

        if (workLogs.isNotEmpty()) {
            sb.append("--- የዕለት ሥራ ሪፖርት (WORK LOGS) ---\n")
            workLogs.forEach { log ->
                sb.append("ቀን፡ ${log.dateString} • ሰዓት፡ ${log.hoursSpent} ሰዓት • ፕሮጀክት፡ ${log.projectOrClient}\n")
                sb.append("ተግባራት፡ ${log.tasksAccomplished}\n")
                sb.append("ቦታ፡ ${log.locationName}\n")
                if (log.notes.isNotBlank()) sb.append("ማስታወሻ፡ ${log.notes}\n")
                sb.append("\n")
            }
        }

        val text = sb.toString()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "የሥራ ሰዓት ክትትል ሪፖርት ላክ")
        context.startActivity(shareIntent)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (selectedSection == 1) {
                FloatingActionButton(
                    onClick = { showAddWorkLogDialog = true },
                    containerColor = EthioEmerald,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_work_log_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Work Log")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("history_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppStrings.timesheetTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = AppStrings.timesheetSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { exportReport() },
                            modifier = Modifier.testTag("export_timesheet_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Export Report", tint = EthioEmerald)
                        }

                        if (records.isNotEmpty() || workLogs.isNotEmpty()) {
                            IconButton(
                                onClick = { showClearDialog = true },
                                modifier = Modifier.testTag("clear_history_button")
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = StatusError)
                            }
                        }
                    }
                }
            }

            // Tab switch between Attendance Punches and Work Logs
            item {
                TabRow(
                    selectedTabIndex = selectedSection,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = EthioEmerald,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedSection == 0,
                        onClick = { selectedSection = 0 },
                        text = {
                            Text(
                                text = if (LanguageManager.isAmharic()) "የሰዓት ምዝገባ (${records.size})" else "Punches (${records.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        text = {
                            Text(
                                text = if (LanguageManager.isAmharic()) "የሥራ ሪፖርት (${workLogs.size})" else "Work Logs (${workLogs.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            if (selectedSection == 0) {
                // Stats Row Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$clockInCount",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EthioEmerald
                                )
                                Text(
                                    text = AppStrings.statClockIns,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$fieldVisitCount",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A5F)
                                )
                                Text(
                                    text = AppStrings.statFieldVisits,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$onSiteRate%",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusSuccess
                                )
                                Text(
                                    text = AppStrings.statOnSiteRate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                // Filters row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "ALL" to AppStrings.filterAll,
                            "CLOCK_IN" to AppStrings.filterClockIn,
                            "CLOCK_OUT" to AppStrings.filterClockOut,
                            "FIELD_VISIT" to AppStrings.filterField,
                            "BREAK" to AppStrings.filterBreaks
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = selectedFilter == key,
                                onClick = { selectedFilter = key },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                // Punch list
                if (filteredRecords.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = AppStrings.noRecordsFilter,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                } else {
                    items(filteredRecords, key = { it.id }) { record ->
                        HistoryRecordCard(record = record)
                    }
                }
            } else {
                // Work Logs Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${workLogs.size}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EthioEmerald
                                )
                                Text(
                                    text = if (LanguageManager.isAmharic()) "የተመዘገቡ ሥራዎች" else "Total Logs",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format("%.1f ሰዓት", totalLoggedHours),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EthioGold
                                )
                                Text(
                                    text = if (LanguageManager.isAmharic()) "የተሠራበት ድምር ሰዓት" else "Total Hours",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                if (workLogs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (LanguageManager.isAmharic()) "ምንም የተመዘገበ የሥራ ሪፖርት የለም" else "No work logs recorded yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = if (LanguageManager.isAmharic()) "የዕለት ተግባር ለመመዝገብ ከታች ያለውን '+' ምልክት ይጫኑ" else "Tap '+' to add a daily work log",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                } else {
                    items(workLogs, key = { it.id }) { log ->
                        WorkLogItemCard(
                            log = log,
                            onDelete = { onDeleteWorkLog(log.id) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    // Add Work Log Dialog
    if (showAddWorkLogDialog) {
        var project by remember { mutableStateOf("") }
        var tasks by remember { mutableStateOf("") }
        var hoursStr by remember { mutableStateOf("4.0") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddWorkLogDialog = false },
            title = { Text(if (LanguageManager.isAmharic()) "የዕለት ሥራ ሪፖርት መመዝገቢያ" else "Add Daily Work Log") },
            text = {
                val scroll = rememberScrollState()
                Column(
                    modifier = Modifier
                        .verticalScroll(scroll)
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = project,
                        onValueChange = { project = it },
                        label = { Text(if (LanguageManager.isAmharic()) "ፕሮጀክት ወይም ደንበኛ" else "Project / Client") },
                        placeholder = { Text(if (LanguageManager.isAmharic()) "ለምሳሌ፡ የዳታቤዝ ጥገና" else "e.g. System upgrade") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("work_log_project_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = tasks,
                        onValueChange = { tasks = it },
                        label = { Text(if (LanguageManager.isAmharic()) "የተከናወኑ ተግባራት" else "Tasks Accomplished") },
                        placeholder = { Text(if (LanguageManager.isAmharic()) "ዝርዝር ተግባራትን ይጻፉ..." else "Describe your accomplishments...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("work_log_tasks_input"),
                        minLines = 3
                    )

                    OutlinedTextField(
                        value = hoursStr,
                        onValueChange = { hoursStr = it },
                        label = { Text(if (LanguageManager.isAmharic()) "የፈጀው ሰዓት (በቁጥር)" else "Hours Spent") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(if (LanguageManager.isAmharic()) "ተጨማሪ ማስታወሻ" else "Additional Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hours = hoursStr.toDoubleOrNull() ?: 1.0
                        if (project.isNotBlank() && tasks.isNotBlank()) {
                            onAddWorkLog(project, tasks, hours, "COMPLETED", notes)
                            showAddWorkLogDialog = false
                        }
                    },
                    enabled = project.isNotBlank() && tasks.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = EthioEmerald),
                    modifier = Modifier.testTag("save_work_log_button")
                ) {
                    Text(if (LanguageManager.isAmharic()) "መዝግብ" else "Save Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddWorkLogDialog = false }) {
                    Text(AppStrings.cancel)
                }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(AppStrings.clearHistoryConfirmTitle) },
            text = { Text(AppStrings.clearHistoryConfirmBody) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text(AppStrings.deleteAll)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(AppStrings.cancel)
                }
            }
        )
    }
}

@Composable
fun WorkLogItemCard(
    log: WorkLog,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = EthioEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.projectOrClient,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = EthioEmerald.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${log.hoursSpent} ሰዓት",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EthioEmerald,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.tasksAccomplished,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (log.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "ማስታወሻ፡ ${log.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ቀን፡ ${log.dateString}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Text(
                    text = log.locationName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun HistoryRecordCard(
    record: AttendanceRecord,
    modifier: Modifier = Modifier
) {
    val (typeTitle, typeColor) = when (record.type) {
        "CLOCK_IN" -> AppStrings.typeClockIn to EthioEmerald
        "CLOCK_OUT" -> AppStrings.typeClockOut to StatusError
        "BREAK_START" -> AppStrings.typeBreakStart to EthioGold
        "BREAK_END" -> AppStrings.typeBreakEnd to EthioEmerald
        "FIELD_VISIT" -> AppStrings.typeFieldVisit to Color(0xFF1E3A5F)
        else -> record.type to Color.Gray
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(typeColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = typeTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = EthioDateTimeUtil.formatDateShort(record.timestamp) + " • " + EthioDateTimeUtil.formatTime(record.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "ቦታ: ${record.branchName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (record.notes.isNotBlank()) {
                Text(
                    text = record.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (record.isWithinGeofence) StatusSuccess.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (record.isWithinGeofence) AppStrings.verifiedOnSite else AppStrings.remoteTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (record.isWithinGeofence) StatusSuccess else MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "GPS: %.4f, %.4f (±%dm)".format(record.latitude, record.longitude, record.accuracy.toInt()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }
    }
}
