package com.example.homework

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import java.io.IOException
import java.security.AccessController.getContext
import java.util.*


class LocationPicker : AppCompatActivity(), OnMapReadyCallback {

    var longitude = -200.0
    var latitude = -200.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

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
        LocationServices.getFusedLocationProviderClient(this@LocationPicker)
            .requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    LocationServices.getFusedLocationProviderClient(this@LocationPicker)
                        .removeLocationUpdates(this)
                    if (locationResult != null && locationResult.locations.size > 0) {
                        var locIndex = locationResult.locations.size - 1
                        latitude = locationResult.locations[locIndex].latitude
                        longitude = locationResult.locations[locIndex].longitude

                        val location = LatLng(latitude, longitude)
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(location)
                                .title("Location")
                                .draggable(true)
                        )
                        /*googleMap.addCircle(
                            CircleOptions().center(location).radius(100.0).strokeWidth(3f).strokeColor(Color.RED).fillColor(
                                Color.argb(70, 150, 50, 50))
                        )*/
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14f))
                        googleMap.setOnMarkerDragListener(object : OnMarkerDragListener {
                            override fun onMarkerDragStart(marker: Marker) {
                                latitude = marker.position.latitude
                                longitude = marker.position.longitude
                            }

                            override fun onMarkerDrag(marker: Marker) {}
                            override fun onMarkerDragEnd(marker: Marker) {
                                latitude = marker.position.latitude
                                longitude = marker.position.longitude
                                Log.v("latitude", latitude.toString())
                                Log.v("longitude", longitude.toString())
                            }
                        })
                    }
                }
            }, Looper.getMainLooper())
    }

    private fun switchActivities() {
        val switchActivityIntent = Intent(this, AddActivity::class.java)
        switchActivityIntent.putExtra("latitude", latitude)
        switchActivityIntent.putExtra("longitude", longitude)
        startActivity(switchActivityIntent)
    }
}
