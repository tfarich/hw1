package com.example.homework

import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
        val location = LatLng(65.02223468829855, 25.471850778153048)
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
    private fun switchActivities() {
        val switchActivityIntent = Intent(this, AddActivity::class.java)
        switchActivityIntent.putExtra("latitude", latitude)
        switchActivityIntent.putExtra("longitude", longitude)
        startActivity(switchActivityIntent)
    }
}
