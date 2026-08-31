package com.example.prayer

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.NetworkModule
import com.example.data.remote.AladhanData
import com.example.data.remote.Timings
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PrayerViewModel(application: Application) : AndroidViewModel(application) {
    private val _aladhanData = MutableStateFlow<AladhanData?>(null)
    val aladhanData: StateFlow<AladhanData?> = _aladhanData
    
    private val _timings = MutableStateFlow<Timings?>(null)
    val timings: StateFlow<Timings?> = _timings
    
    private val _locationName = MutableStateFlow<String>("Locating...")
    val locationName: StateFlow<String> = _locationName

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    @SuppressLint("MissingPermission")
    fun fetchPrayerTimes() {
        viewModelScope.launch {
            if (_timings.value != null) return@launch // Already fetched
            _isLoading.value = true
            _error.value = null
            try {
                // Fetch last known location
                val location = try {
                    fusedLocationClient.lastLocation.await()
                } catch (e: Exception) {
                    null
                }
                
                val lat: Double
                val lon: Double
                
                if (location != null) {
                    lat = location.latitude
                    lon = location.longitude
                    
                    try {
                        val geocoder = android.location.Geocoder(getApplication(), java.util.Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val city = address.locality ?: address.subAdminArea ?: address.adminArea
                            val country = address.countryName
                            if (city != null && country != null) {
                                _locationName.value = "$city, $country"
                            } else {
                                _locationName.value = "Current Location"
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    lat = 21.4225
                    lon = 39.8262
                    _locationName.value = "Mecca, Saudi Arabia (Default)"
                }

                val response = NetworkModule.api.getTimings(lat, lon)
                _aladhanData.value = response.data
                _timings.value = response.data?.timings
                
                _timings.value?.let { 
                    com.example.settings.AlarmScheduler.scheduleAlarmsForTimings(getApplication(), it)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
