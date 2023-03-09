package com.example.homework

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import coil.compose.rememberImagePainter
import coil.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import androidx.databinding.DataBindingUtil
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import java.util.concurrent.TimeUnit

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "MainActivity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

class MainActivity : ComponentActivity() {

    var currentLongitude = 0.0
    var currentLatitude = 0.0

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var viewModel: GeofenceViewModel

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "HuntMainActivity.treasureHunt.action.ACTION_GEOFENCE_EVENT"
    }

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        //this.deleteDatabase(DATABASE_NAME)
        super.onCreate(savedInstanceState)
        val geofencingClient = LocationServices.getGeofencingClient(this)
        var locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        var location = LatLng(0.0, 0.0)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
        }
        LocationServices.getFusedLocationProviderClient(this@MainActivity)
            .requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    LocationServices.getFusedLocationProviderClient(this@MainActivity)
                        .removeLocationUpdates(this)
                    if (locationResult != null && locationResult.locations.size > 0) {
                        var locIndex = locationResult.locations.size - 1
                        currentLatitude = locationResult.locations[locIndex].latitude
                        currentLongitude = locationResult.locations[locIndex].longitude
                        val currentLocation = LatLng(currentLatitude, currentLongitude)
                        setContent {
                            val scaffoldState = rememberScaffoldState(
                                rememberDrawerState(DrawerValue.Closed)
                            )
                            Scaffold(
                                content = {
                                    ComposePlaygroundTheme {
                                        Surface(color = LightBlue) {
                                            RecyclerViewImpl(LocalContext.current, currentLocation)
                                        }
                                    }
                                },
                                bottomBar = { BottomBar() }
                            )
                        }
                    }
                }
            }, Looper.getMainLooper())
    }

    private fun getCurrentLocation(): LatLng {
        var locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        var location = LatLng(0.0, 0.0)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return LatLng(0.0, 0.0)
        }
        LocationServices.getFusedLocationProviderClient(this@MainActivity)
            .requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    LocationServices.getFusedLocationProviderClient(this@MainActivity)
                        .removeLocationUpdates(this)
                    if (locationResult != null && locationResult.locations.size > 0) {
                        var locIndex = locationResult.locations.size - 1
                        currentLatitude = locationResult.locations[locIndex].latitude
                        currentLongitude = locationResult.locations[locIndex].longitude
                    }
                }
            }, Looper.getMainLooper())
        return location
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }

    private fun removeGeofences() {
        if (!foregroundAndBackgroundLocationPermissionApproved()) {
            return
        }
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences removed
                Log.d(TAG, getString(R.string.geofences_removed))
                Toast.makeText(applicationContext, R.string.geofences_removed, Toast.LENGTH_SHORT)
                    .show()
            }
            addOnFailureListener {
                // Failed to remove geofences
                Log.d(TAG, getString(R.string.geofences_not_removed))
            }
        }
    }

    private fun addGeofenceForClue() {
        if (viewModel.geofenceIsActive()) return
        val currentGeofenceIndex = viewModel.nextGeofenceIndex()
        if (currentGeofenceIndex >= GeofencingConstants.NUM_LANDMARKS) {
            removeGeofences()
            viewModel.geofenceActivated()
            return
        }
        val currentGeofenceData = GeofencingConstants.LANDMARK_DATA[currentGeofenceIndex]

        val geofence = Geofence.Builder()
            .setRequestId(currentGeofenceData.id)
            .setCircularRegion(
                currentGeofenceData.latLong.latitude,
                currentGeofenceData.latLong.longitude,
                GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnCompleteListener {
                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@addOnCompleteListener
                }
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        Toast.makeText(
                            this@MainActivity, R.string.geofences_added,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        Log.e("Add Geofence", geofence.requestId)
                        viewModel.geofenceActivated()
                    }
                    addOnFailureListener {
                        Toast.makeText(
                            this@MainActivity, R.string.geofences_not_added,
                            Toast.LENGTH_SHORT
                        ).show()
                        if ((it.message != null)) {
                            Log.w(TAG, it.message.toString())
                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {
            Toast.makeText(this, "Do not have permissions", Toast.LENGTH_SHORT).show()
        } else {
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        this@MainActivity,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Toast.makeText(this, "Do not have permissions", Toast.LENGTH_SHORT).show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                addGeofenceForClue()
            }
        }
    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        Log.d(TAG, "Request foreground only location permission")
        ActivityCompat.requestPermissions(
            this@MainActivity,
            permissionsArray,
            resultCode
        )
    }


}

@Composable
fun BottomBar() {
    val selectedIndex = remember { mutableStateOf(0) }
    val context = LocalContext.current
    BottomNavigation(
        backgroundColor = androidx.compose.ui.graphics.Color.White,
        elevation = 10.dp
    ) {

        BottomNavigationItem(icon = {
            Icon(imageVector = Icons.Default.ArrowBack, "")
        },
            label = { Text(text = "Log Out") },
            selected = (selectedIndex.value == 0),
            onClick = {
                context.startActivity(Intent(context, LoginActivity::class.java))
            })
        BottomNavigationItem(icon = {
            Icon(imageVector = Icons.Default.Add, "")
        },
            label = { Text(text = "Add") },
            selected = (selectedIndex.value == 1),
            onClick = {
                context.startActivity(Intent(context, AddActivity::class.java))
            })
        BottomNavigationItem(icon = {
            Icon(imageVector = Icons.Default.LocationOn, "")
        },
            label = { Text(text = "Map") },
            selected = (selectedIndex.value == 2),
            onClick = {
                context.startActivity(Intent(context, MainMapActivity::class.java))
            })
        BottomNavigationItem(icon = {
            Icon(imageVector = Icons.Default.Person, "")
        },
            label = { Text(text = "Profile") },
            selected = (selectedIndex.value == 2),
            onClick = {
                context.startActivity(Intent(context, ProfileActivity::class.java))
            })
    }
}

@Composable
fun RecyclerViewImpl(context: Context, currentLocation: LatLng) {
    var data: MutableList<Reminder>
    val db: DatabaseHandler = DatabaseHandler(context)
    val context = LocalContext.current
    var dialogOpen by remember { mutableStateOf(false) }
    var id by remember { mutableStateOf(0) }

    // check location
    // on update, if near any reminders make a notification

    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = {
                dialogOpen = false
            },
            confirmButton = {
                TextButton(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White,
                        contentColor = Color.Black
                    ),
                    onClick = {
                        db.deleteData(id)
                        Log.v("ID: ", id.toString())
                        val switchActivityIntent = Intent(context, MainActivity::class.java)
                        context.startActivity(switchActivityIntent)
                    }) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White,
                        contentColor = Color.Black
                    ),
                    onClick = {
                        dialogOpen = false
                    }) {
                    Text(text = "Do not Delete")
                }
            },
            title = {
                Text(text = "Do You Wish to Delete this Reminder?")
            },
            text = {
                Text(text = "This action cannot be undone")
            },
            modifier = Modifier // Set the width and padding
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(5.dp),
            backgroundColor = Color.White,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        )
    }

    data = db.readData()!!
    var reminderData: MutableList<Reminder> = mutableListOf()

    for (d in data) {
        val currentTime = Calendar.getInstance().timeInMillis
        val reminderTime = d.reminder_time.toLong()
        val locationX = d.location_x
        val locationY = d.location_y
        val id = d.id
        Log.v("id", id.toString())
        val coordinateX = locationX.toDouble() / 1000000.0
        val coordinateY = locationY.toDouble() / 1000000.0
        val coordinates = LatLng(coordinateY, coordinateX)
        val diff = calculateLocationDifference(currentLocation, coordinates)
        val timeDiff = (reminderTime / 1000L) - (currentTime / 1000L)
        if (timeDiff < 0) {
            reminderData.add(d)
        } else if (diff <= 500) {
            reminderData.add(d)
            val myWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInputData(
                    workDataOf(
                    "title" to "Reminder",
                    "message" to d.message.toString()
                )
                ).build()
            WorkManager.getInstance(context).enqueue(myWorkRequest)
        }
    }
    if (reminderData.isEmpty()) {
        LazyColumn {
            var list = ArrayList<String>()
            list.add("No Current Reminders")
            items(list) { it ->
                Card(modifier = Modifier.padding(8.dp)) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = it, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    } else {
        LazyColumn {
            items(reminderData) { it ->
                Card(modifier = Modifier.padding(8.dp)) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Row() {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(it.reminder_icon)
                                    .build(),
                                contentDescription = "icon",
                                contentScale = ContentScale.Inside,
                                modifier = Modifier.size(30.dp)
                            )
                            /*val painter = rememberImagePainter(data = File(it.reminder_icon))
                            Image(
                                modifier = Modifier
                                    .padding(5.dp),
                                contentScale = ContentScale.FillHeight,
                                painter = painter,
                                contentDescription = "",
                            )*/
                            Text(
                                text = it.messageToString(),
                                modifier = Modifier.padding(8.dp)
                            )
                            Spacer(Modifier.weight(1f))
                            Button(
                                border = null,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(width = 40.dp, height = 35.dp),
                                onClick = {
                                    val switchActivityIntent =
                                        Intent(context, EditActivity::class.java)
                                    switchActivityIntent.putExtra("ID", it.id)
                                    switchActivityIntent.putExtra("message", it.message)
                                    switchActivityIntent.putExtra("location_x", it.location_x)
                                    switchActivityIntent.putExtra("location_y", it.location_y)
                                    switchActivityIntent.putExtra("reminder_time", it.reminder_time)
                                    switchActivityIntent.putExtra("creation_time", it.creation_time)
                                    switchActivityIntent.putExtra("creator_id", it.creator_id)
                                    switchActivityIntent.putExtra("reminder_seen", it.reminder_seen)
                                    switchActivityIntent.putExtra("reminder_icon", it.reminder_icon)
                                    context.startActivity(switchActivityIntent)
                                },
                            ) {
                                Icon(imageVector = Icons.Default.Edit, "")
                            }
                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(width = 40.dp, height = 35.dp),
                                onClick = {
                                    dialogOpen = true
                                    id = it.id
                                }) {
                                Icon(imageVector = Icons.Default.Delete, "")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun calculateLocationDifference(lastLocation: LatLng, firstLocation: LatLng): Float {
    val dist = FloatArray(1)
    Location.distanceBetween(
        lastLocation.latitude,
        lastLocation.longitude,
        firstLocation.latitude,
        firstLocation.longitude,
        dist
    )
    return dist[0]
}