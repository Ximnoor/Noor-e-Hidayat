package com.example.essentials

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.location.Location
import android.os.CancellationSignal
import android.view.Surface
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.math.*

@Composable
fun QiblaScreen() {
    val context = LocalContext.current
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var coords by remember { mutableStateOf<Pair<Double,Double>?>(null) }
    var heading by remember { mutableStateOf<Float?>(null) }
    var message by remember { mutableStateOf("Use your current location or enter coordinates. No location is assumed.") }
    var inaccurate by remember { mutableStateOf(false) }
    var locationRequest by remember { mutableStateOf<CancellationTokenSource?>(null) }
    fun locate() {
        if (ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            message = "Location permission is required, or enter coordinates manually."; return
        }
        message = "Finding your current location…"
        locationRequest?.cancel()
        val token = CancellationTokenSource(); locationRequest = token
        try {
            LocationServices.getFusedLocationProviderClient(context).getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, token.token)
                .addOnSuccessListener { location: Location? ->
                    if (token.token.isCancellationRequested) return@addOnSuccessListener
                    if (location == null) { message = "No location fix. Enable location or enter coordinates." }
                    else {
                        latitude = location.latitude.toString(); longitude = location.longitude.toString()
                        coords = location.latitude to location.longitude
                        message = "Current location • accuracy ${location.accuracy.toInt()} metres"
                    }
                }.addOnFailureListener { message = "Location unavailable. Enter latitude and longitude manually." }
        } catch (_: SecurityException) { message = "Location permission was denied." }
    }
    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted.values.any { it }) locate() else message = "Permission denied. Manual coordinates still work."
    }
    DisposableEffect(Unit) { onDispose { locationRequest?.cancel() } }
    val sensors = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { sensors.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }
    DisposableEffect(coords) {
        val listener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { inaccurate = accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE || accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW }
            override fun onSensorChanged(event: SensorEvent) {
                val point = coords ?: return
                val matrix = FloatArray(9); val remapped = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(matrix, event.values)
                @Suppress("DEPRECATION")
                val rotation = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
                val axes = when(rotation) {
                    Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
                    Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
                    Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
                    else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
                }
                SensorManager.remapCoordinateSystem(matrix, axes.first, axes.second, remapped)
                val orientation = SensorManager.getOrientation(remapped, FloatArray(3))
                val declination = GeomagneticField(point.first.toFloat(), point.second.toFloat(), 0f, System.currentTimeMillis()).declination
                heading = ((Math.toDegrees(orientation[0].toDouble()) + declination + 360) % 360).toFloat()
            }
        }
        if(sensor != null) sensors.registerListener(listener,sensor,SensorManager.SENSOR_DELAY_UI)
        onDispose { sensors.unregisterListener(listener) }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("قبلہ • Qibla compass", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = { permissions.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)) }) { Text("Use my location") }
        OutlinedTextField(latitude,{latitude=it},label={Text("Latitude (−90 to 90)")},singleLine=true)
        OutlinedTextField(longitude,{longitude=it},label={Text("Longitude (−180 to 180)")},singleLine=true)
        OutlinedButton(onClick={
            val lat=latitude.toDoubleOrNull(); val lon=longitude.toDoubleOrNull()
            if(lat==null || lon==null || !lat.isFinite() || !lon.isFinite() || lat !in -90.0..90.0 || lon !in -180.0..180.0) message="Enter valid coordinates."
            else { coords=lat to lon; message="Using entered coordinates. They must match where you are standing." }
        }) { Text("Use coordinates") }
        Text(message)
        coords?.let { (lat,lon) ->
            if (abs(lat-21.422487)<0.0001 && abs(lon-39.826206)<0.0001) Text("You are at the Kaaba coordinates; a single bearing is undefined.")
            else {
                val bearing = QiblaMath.bearing(lat,lon)
                Text("Qibla: %.1f° clockwise from true north".format(bearing))
                if(sensor == null) Text("This phone has no compass sensor. Use the true-north bearing above with an external compass.")
                else if(heading == null) Text("Waiting for compass… Hold the phone flat.")
                else {
                    Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment=androidx.compose.ui.Alignment.Center) {
                        Text("↑", fontSize=120.sp, modifier=Modifier.rotate((bearing-heading!!).toFloat()), color=MaterialTheme.colorScheme.primary)
                    }
                    Text("Turn until the arrow points to the top of the phone. Hold it flat, away from metal and magnets.")
                    if(inaccurate) Text("Low compass accuracy. Move your phone in a figure-eight to calibrate.",color=MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
