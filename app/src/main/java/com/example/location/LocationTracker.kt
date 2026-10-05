package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double = 0.0,
    val speed: Float = 0.0f,
    val timestamp: Long = System.currentTimeMillis(),
    val address: String = "Detecting location...",
    val isSimulated: Boolean = false
)

sealed interface LocationStatus {
    object Idle : LocationStatus
    object RequestingPermission : LocationStatus
    object Locating : LocationStatus
    data class Success(val location: UserLocation) : LocationStatus
    data class Error(val message: String) : LocationStatus
}

class LocationTracker(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationStatus = MutableStateFlow<LocationStatus>(LocationStatus.Idle)
    val locationStatus: StateFlow<LocationStatus> = _locationStatus.asStateFlow()

    private val _currentLocation = MutableStateFlow<UserLocation?>(null)
    val currentLocation: StateFlow<UserLocation?> = _currentLocation.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var isTracking = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                updateLocation(location, isSimulated = false)
            }
        }
    }

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    fun isGpsEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (!hasLocationPermission()) {
            _locationStatus.value = LocationStatus.RequestingPermission
            return
        }

        if (!isGpsEnabled()) {
            _locationStatus.value = LocationStatus.Error("GPS / Location services are disabled on device.")
            return
        }

        _locationStatus.value = LocationStatus.Locating
        isTracking = true

        // Try getting last known location first for immediate UI responsiveness
        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
            if (lastLoc != null) {
                updateLocation(lastLoc, isSimulated = false)
            }
        }

        // Request real-time periodic updates
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(1500L)
            .setMinUpdateDistanceMeters(2.0f)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            ).addOnFailureListener { exception ->
                fallbackToSystemLocationManager()
            }
        } catch (e: Exception) {
            fallbackToSystemLocationManager()
        }
    }

    fun stopTracking() {
        isTracking = false
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            // ignore
        }
    }

    @SuppressLint("MissingPermission")
    private fun fallbackToSystemLocationManager() {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
        try {
            val gpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val best = gpsLoc ?: netLoc
            if (best != null) {
                updateLocation(best, isSimulated = false)
            } else {
                _locationStatus.value = LocationStatus.Error("Waiting for GPS satellite fix...")
            }
        } catch (e: Exception) {
            _locationStatus.value = LocationStatus.Error("Could not access device location: ${e.message}")
        }
    }

    private fun updateLocation(loc: Location, isSimulated: Boolean) {
        val userLoc = UserLocation(
            latitude = loc.latitude,
            longitude = loc.longitude,
            accuracy = if (loc.hasAccuracy()) loc.accuracy else 8.0f,
            altitude = loc.altitude,
            speed = loc.speed,
            timestamp = loc.time,
            address = "Lat: %.5f, Lon: %.5f".format(Locale.US, loc.latitude, loc.longitude),
            isSimulated = isSimulated
        )

        _currentLocation.value = userLoc
        _locationStatus.value = LocationStatus.Success(userLoc)

        // Resolve reverse geocode in background
        scope.launch {
            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        val thoroughfare = addr.thoroughfare ?: addr.subLocality ?: ""
                        val locality = addr.locality ?: addr.adminArea ?: "Ethiopia"
                        val fullText = if (thoroughfare.isNotBlank()) "$thoroughfare, $locality" else locality
                        _currentLocation.value = userLoc.copy(address = fullText)
                        _locationStatus.value = LocationStatus.Success(userLoc.copy(address = fullText))
                    }
                }
            } catch (e: Exception) {
                // Keep the coordinate string
            }
        }
    }

    /**
     * Testing / Demo helper: Allows simulating checking in from the Ethio Company Branch
     * (e.g. Churchill Ave HQ Addis Ababa or Bole Branch) for indoor emulator testing.
     */
    fun setSimulatedLocation(latitude: Double, longitude: Double, label: String) {
        val simLoc = UserLocation(
            latitude = latitude,
            longitude = longitude,
            accuracy = 4.5f,
            altitude = 2355.0, // Addis elevation ~2355m
            speed = 0f,
            timestamp = System.currentTimeMillis(),
            address = label,
            isSimulated = true
        )
        _currentLocation.value = simLoc
        _locationStatus.value = LocationStatus.Success(simLoc)
    }
}
