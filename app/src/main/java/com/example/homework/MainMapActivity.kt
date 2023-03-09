package com.example.homework

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton


class MainMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var longitude = 0.0
    var latitude = 0.0
    var location = LatLng(0.0,0.0)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_map)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainMapActivity,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),REQUEST_PERMISSION_REQUEST_CODE)
        } else {
            location = getCurrentLocation()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val backButton = findViewById<MaterialButton>(R.id.backbtn)
        backButton.setOnClickListener {
            switchActivities()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        var locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
        }
        LocationServices.getFusedLocationProviderClient(this@MainMapActivity)
            .requestLocationUpdates(locationRequest, object:LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    LocationServices.getFusedLocationProviderClient(this@MainMapActivity)
                        .removeLocationUpdates(this)
                    if (locationResult != null && locationResult.locations.size > 0) {
                        var locIndex = locationResult.locations.size-1
                        latitude = locationResult.locations[locIndex].latitude
                        longitude = locationResult.locations[locIndex].longitude
                        Log.v("testing 1", latitude.toString())
                        Log.v("testing 1", longitude.toString())
                        var context = this
                        var db = DatabaseHandler(this@MainMapActivity)
                        val data = db.readData()!!

                        location = LatLng(latitude, longitude)

                        var start = googleMap.addMarker(
                            MarkerOptions()
                                .position(location)
                                .title("Location")
                                .draggable(true)
                        )
                        var circle = googleMap.addCircle(
                            CircleOptions().center(location).radius(500.0).strokeWidth(2f).strokeColor(Color.RED)
                                .fillColor(
                                    Color.argb(20, 150, 50, 50)
                                )
                        )

                        for (d in data) {
                            val locationX = d.location_x
                            val locationY = d.location_y
                            val coordinateX = locationX.toDouble() / 1000000.0
                            val coordinateY = locationY.toDouble() / 1000000.0
                            val coordinates = LatLng(coordinateY, coordinateX)
                            val diff = calculateLocationDifference(location, coordinates)
                            if (diff <= 500) {
                                googleMap.addMarker(
                                    MarkerOptions()
                                        .position(coordinates)
                                        .title(d.message.toString())
                                        .draggable(false)
                                )
                            }
                        }

                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14f))
                        googleMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                            override fun onMarkerDragStart(marker: Marker) {
                            }

                            override fun onMarkerDrag(marker: Marker) {}
                            override fun onMarkerDragEnd(marker: Marker) {
                                googleMap.clear()
                                latitude = marker.position.latitude
                                longitude = marker.position.longitude
                                val location = LatLng(latitude, longitude)
                                var start = googleMap.addMarker(
                                    MarkerOptions()
                                        .position(location)
                                        .title("Location")
                                        .draggable(true)
                                )
                                var circle = googleMap.addCircle(
                                    CircleOptions().center(location).radius(500.0).strokeWidth(2f)
                                        .strokeColor(Color.RED).fillColor(
                                            Color.argb(20, 150, 50, 50)
                                        )
                                )
                                val firstLocation = LatLng(latitude, longitude)
                                for (d in data) {
                                    val locationX = d.location_x
                                    val locationY = d.location_y
                                    val coordinateX = locationX.toDouble() / 1000000.0
                                    val coordinateY = locationY.toDouble() / 1000000.0
                                    val coordinates = LatLng(coordinateY, coordinateX)
                                    val diff = calculateLocationDifference(firstLocation, coordinates)
                                    if (diff <= 500) {
                                        googleMap.addMarker(
                                            MarkerOptions()
                                                .position(coordinates)
                                                .title(d.message.toString())
                                                .draggable(false)
                                        )
                                    }
                                }
                            }
                        })
                    }
                }
            }, Looper.getMainLooper())
    }

    private fun calculateLocationDifference(lastLocation: LatLng, firstLocation: LatLng): Float {
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

    private fun switchActivities() {
        val switchActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(switchActivityIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_REQUEST_CODE && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this@MainMapActivity, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
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
        ) { return LatLng(0.0, 0.0)
        }
        LocationServices.getFusedLocationProviderClient(this@MainMapActivity)
            .requestLocationUpdates(locationRequest, object:LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    LocationServices.getFusedLocationProviderClient(this@MainMapActivity)
                        .removeLocationUpdates(this)
                    if (locationResult != null && locationResult.locations.size > 0) {
                        var locIndex = locationResult.locations.size-1
                        latitude = locationResult.locations[locIndex].latitude
                        longitude = locationResult.locations[locIndex].longitude
                    }
                }
            }, Looper.getMainLooper())
        return location
    }

    companion object {
    private val REQUEST_PERMISSION_REQUEST_CODE = 2023
    }
}