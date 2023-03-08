package com.example.homework

import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

    var latitude = 0.0
    var longitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val backButton = findViewById<MaterialButton>(R.id.backbtn)
        backButton.setOnClickListener {
            switchActivities()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        var context = this
        var db = DatabaseHandler(context)
        val data = db.readData()!!

        val location = LatLng(65.02223468829855, 25.471850778153048)
        var start = googleMap.addMarker(
            MarkerOptions()
                .position(location)
                .title("Location")
                .draggable(true)
        )
        var circle = googleMap.addCircle(
            CircleOptions().center(location).radius(500.0).strokeWidth(2f).strokeColor(Color.RED).fillColor(
                Color.argb(20, 150, 50, 50))
        )

        for (d in data) {
            val locationX = d.location_x
            val locationY = d.location_y
            val coordinateX = locationX.toDouble()/1000000.0
            val coordinateY = locationY.toDouble()/1000000.0
            val coordinates = LatLng(coordinateY,coordinateX)
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
                start = googleMap.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title("Location")
                        .draggable(true)
                )
                circle = googleMap.addCircle(
                    CircleOptions().center(location).radius(500.0).strokeWidth(2f).strokeColor(Color.RED).fillColor(
                        Color.argb(20, 150, 50, 50))
                )
                val firstLocation = LatLng(latitude,longitude)
                for (d in data) {
                    val locationX = d.location_x
                    val locationY = d.location_y
                    val coordinateX = locationX.toDouble()/1000000.0
                    val coordinateY = locationY.toDouble()/1000000.0
                    val coordinates = LatLng(coordinateY,coordinateX)
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
}