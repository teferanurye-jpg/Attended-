package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CompanyBranch
import com.example.location.GeofenceCalculator
import com.example.location.UserLocation
import com.example.ui.theme.EthioEmerald
import com.example.ui.theme.EthioGold
import com.example.ui.theme.StatusSuccess
import com.example.util.AppStrings

@Composable
fun BranchesScreen(
    branches: List<CompanyBranch>,
    selectedBranch: CompanyBranch?,
    currentLocation: UserLocation?,
    onSelectBranch: (CompanyBranch) -> Unit,
    onAddBranch: (name: String, city: String, lat: Double, lon: Double, radius: Int, address: String) -> Unit,
    onDeleteBranch: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = EthioEmerald,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_branch_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = AppStrings.addBranchTitle)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("branches_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = AppStrings.branchesTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = AppStrings.branchesSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Summary Card
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
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${branches.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = EthioEmerald
                            )
                            Text(
                                text = AppStrings.configuredSites,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val inZone = branches.any { branch ->
                                if (currentLocation != null) {
                                    val dist = GeofenceCalculator.calculateDistanceMeters(
                                        currentLocation.latitude,
                                        currentLocation.longitude,
                                        branch.latitude,
                                        branch.longitude
                                    )
                                    dist <= branch.radiusMeters
                                } else false
                            }

                            Text(
                                text = if (inZone) AppStrings.onSiteBadge else AppStrings.remoteBadge,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (inZone) StatusSuccess else EthioGold
                            )
                            Text(
                                text = AppStrings.currentProximity,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // Branch List
            items(branches, key = { it.id }) { branch ->
                val isSelected = branch.id == selectedBranch?.id
                val distance = if (currentLocation != null) {
                    GeofenceCalculator.calculateDistanceMeters(
                        currentLocation.latitude,
                        currentLocation.longitude,
                        branch.latitude,
                        branch.longitude
                    )
                } else null

                val isInside = distance != null && distance <= branch.radiusMeters

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectBranch(branch) }
                        .testTag("branch_card_${branch.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isSelected) EthioEmerald.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) EthioEmerald.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = null,
                                        tint = if (isSelected) EthioEmerald else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = branch.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) EthioEmerald else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${branch.city} • ክልል፡ ${branch.radiusMeters} ሜትር",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            if (isSelected) {
                                Surface(
                                    color = EthioEmerald,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = AppStrings.activeSiteBadge,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else if (!branch.isDefault) {
                                IconButton(
                                    onClick = { onDeleteBranch(branch.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (branch.address.isNotBlank()) {
                            Text(
                                text = branch.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "%.4f° N, %.4f° E".format(branch.latitude, branch.longitude),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.outline
                            )

                            if (distance != null) {
                                Surface(
                                    color = if (isInside) StatusSuccess.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isInside) "በቢሮ ውስጥ (${distance.toInt()} ሜትር)"
                                        else "${GeofenceCalculator.formatDistance(distance)} ርቀት ላይ",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isInside) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Add Branch Dialog
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var city by remember { mutableStateOf("አዲስ አበባ") }
        var latStr by remember { mutableStateOf(currentLocation?.latitude?.toString() ?: "9.0227") }
        var lonStr by remember { mutableStateOf(currentLocation?.longitude?.toString() ?: "38.7468") }
        var radiusMeters by remember { mutableFloatStateOf(250f) }
        var address by remember { mutableStateOf(currentLocation?.address ?: "") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(AppStrings.addBranchTitle) },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(AppStrings.branchNameInput) },
                        placeholder = { Text("ለምሳሌ፡ ቦሌ መድሃኒዓለም ቅርንጫፍ") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("branch_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text(AppStrings.cityInput) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = latStr,
                            onValueChange = { latStr = it },
                            label = { Text("ላቲቱድ") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = lonStr,
                            onValueChange = { lonStr = it },
                            label = { Text("ሎንጊቱድ") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    if (currentLocation != null) {
                        OutlinedButton(
                            onClick = {
                                latStr = currentLocation.latitude.toString()
                                lonStr = currentLocation.longitude.toString()
                                address = currentLocation.address
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(AppStrings.useCurrentGps, fontSize = 12.sp)
                        }
                    }

                    Text(
                        text = AppStrings.geofenceRadiusLabel(radiusMeters.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Slider(
                        value = radiusMeters,
                        onValueChange = { radiusMeters = it },
                        valueRange = 100f..1000f,
                        steps = 8
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(AppStrings.addressInput) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lat = latStr.toDoubleOrNull() ?: 9.0227
                        val lon = lonStr.toDoubleOrNull() ?: 38.7468
                        if (name.isNotBlank()) {
                            onAddBranch(name, city, lat, lon, radiusMeters.toInt(), address)
                            showAddDialog = false
                        }
                    },
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = EthioEmerald),
                    modifier = Modifier.testTag("save_branch_button")
                ) {
                    Text(AppStrings.saveBranch)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(AppStrings.cancel)
                }
            }
        )
    }
}
