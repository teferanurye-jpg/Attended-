package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CompanyBranch
import com.example.location.GeofenceCalculator
import com.example.location.LocationStatus
import com.example.location.UserLocation
import com.example.ui.theme.EthioEmerald
import com.example.ui.theme.EthioGold
import com.example.ui.theme.StatusSuccess
import com.example.util.AppStrings

@Composable
fun LocationRadarCard(
    currentLocation: UserLocation?,
    locationStatus: LocationStatus,
    selectedBranch: CompanyBranch?,
    distanceToBranch: Float?,
    isWithinGeofence: Boolean,
    onRefreshLocation: () -> Unit,
    onSimulateHq: () -> Unit,
    onChangeBranchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("location_radar_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Live GPS status & Refresh button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(36.dp)
                    ) {
                        // Pulsing outer ring
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    if (isWithinGeofence) EthioEmerald.copy(alpha = 0.2f)
                                    else EthioGold.copy(alpha = 0.25f)
                                )
                        )
                        // Core GPS pin
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isWithinGeofence) EthioEmerald else EthioGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "GPS Radar",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = AppStrings.locationTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (currentLocation?.isSimulated == true) AppStrings.locationSimSubtitle
                            else AppStrings.locationLiveSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshLocation,
                    modifier = Modifier.testTag("refresh_gps_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = AppStrings.refreshGps,
                        tint = EthioEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Coordinates & Accuracy Row
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppStrings.coordinatesLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (currentLocation != null) {
                            Text(
                                text = "%.5f° N, %.5f° E".format(currentLocation.latitude, currentLocation.longitude),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        } else {
                            Text(
                                text = AppStrings.acquiringGps,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = AppStrings.accuracyLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (currentLocation != null) "± ${currentLocation.accuracy.toInt()}m" else "--",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if ((currentLocation?.accuracy ?: 99f) <= 15f) StatusSuccess else EthioGold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Assigned Branch & Geofence Verification Status
            selectedBranch?.let { branch ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Branch",
                            tint = EthioEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = branch.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Text(
                                text = "${branch.city} • Radius: ${branch.radiusMeters}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 11.sp
                            )
                        }
                    }

                    AssistChip(
                        onClick = onChangeBranchClick,
                        label = { Text(AppStrings.changeBranch, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("change_branch_chip")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Geofence status banner
                val bannerBg = if (isWithinGeofence) StatusSuccess.copy(alpha = 0.12f) else EthioGold.copy(alpha = 0.14f)
                val bannerBorder = if (isWithinGeofence) StatusSuccess.copy(alpha = 0.4f) else EthioGold.copy(alpha = 0.4f)
                val statusText = if (isWithinGeofence) {
                    if (distanceToBranch != null) AppStrings.insideWorkZone(distanceToBranch.toInt())
                    else AppStrings.insideWorkZoneGeneric
                } else {
                    if (distanceToBranch != null) AppStrings.outsideWorkZone(GeofenceCalculator.formatDistance(distanceToBranch))
                    else AppStrings.outsideWorkZone("")
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(bannerBg)
                        .border(1.dp, bannerBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isWithinGeofence) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isWithinGeofence) StatusSuccess else EthioGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isWithinGeofence) StatusSuccess else Color(0xFF8C5E00),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Testing / Demo Helpers Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = onSimulateHq,
                    label = { Text(AppStrings.simulateHq, fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = EthioEmerald
                        )
                    },
                    modifier = Modifier.testTag("simulate_hq_chip")
                )
            }
        }
    }
}
