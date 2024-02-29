package com.example.softwareengineerrouteproject

import android.annotation.SuppressLint
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import android.widget.LinearLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var editTextContainer: LinearLayout
    private var editTextCount = 1
    private var destinationList: MutableList<String> = mutableListOf()
    private var latLngList: MutableList<LatLng> = mutableListOf()
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextContainer = findViewById(R.id.editTextContainer)
        val btnShow: Button = findViewById(R.id.btnShow)
        val btnSort: Button = findViewById(R.id.btnSort)

        btnShow.setOnClickListener {
            addNewEditText()
        }


        btnSort.setOnClickListener {
            saveAllDestinationsToList()
            setMapMarkers()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun addNewEditText() {
        val newEditText = EditText(this).apply {
            id = editTextCount++
            hint = "Enter a destination"
            setPadding(5, 5, 5, 5)
            textSize = 22f
        }
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        editTextContainer.addView(newEditText, layoutParams)
    }

    private fun saveAllDestinationsToList() {
        destinationList.clear()
        latLngList.clear()
        for (i in 0 until editTextContainer.childCount) {
            val view = editTextContainer.getChildAt(i)
            if (view is EditText) {
                val destination = view.text.toString()
                destinationList.add(destination)
            }
        }
        getLatLong()
    }

    private fun getLatLong() {
        val geocoder = Geocoder(this, Locale.getDefault())
        for (destination in destinationList) {
            try {
                val addressList = geocoder.getFromLocationName(destination, 1)
                if (addressList != null) {
                    if (addressList.isNotEmpty()) {
                        val address = addressList?.get(0)
                        if (address != null) {
                            latLngList.add(LatLng(address.latitude, address.longitude))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setMapMarkers() {
        mMap.clear()
        latLngList.forEach { latLng ->
            mMap.addMarker(MarkerOptions().position(latLng))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val initialLocation = LatLng(40.71830841160277, -74.01058004547683)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(initialLocation))
    }
}



//AIzaSyC1h3oQs3G96pIwlF_IVzOzS5iwtE0N2ac