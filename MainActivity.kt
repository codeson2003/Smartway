package com.example.softwareengineerrouteproject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


class MainActivity : AppCompatActivity(), OnMapReadyCallback, CoroutineScope by CoroutineScope(Dispatchers.Main) {
    private lateinit var editTextContainer: LinearLayout
    private var editTextCount = 1
    private var destinationList: MutableList<String> = mutableListOf()
    private lateinit var mMap: GoogleMap
    private val client = OkHttpClient()

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
            destinationList.clear()

            // Iterate through all EditText views and add their content to destinationList
            for (i in 0 until editTextContainer.childCount) {
                val childView = editTextContainer.getChildAt(i)
                if (childView is EditText) {
                    val destination = childView.text.toString().trim()
                    // Only add non-empty destinations
                    if (destination.isNotEmpty()) {
                        destinationList.add(destination)
                    }
                }
            }
            if (destinationList.size >= 2) {
                pairEachDestination(destinationList)
            }
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

    private fun pairEachDestination(destinationList: MutableList<String>) {
        val pairs = mutableListOf<Pair<String, String>>()
        for (i in 0 until destinationList.size) {
            for (j in i + 1 until destinationList.size) {
                pairs.add(Pair(destinationList[i], destinationList[j]))
            }
        }
        pairs.forEach { (origin, dest) ->
            getRoute(origin, dest)
        }
    }
    private fun getRoute(origin: String, dest: String) = launch {
        withContext(Dispatchers.IO) {

            val requestUrl = "https://maps.googleapis.com/maps/api/directions/json?origin=$origin&destination=$dest&key=API_KEY" // change youre api key here

            val request = Request.Builder()
                .url(requestUrl)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val responseData = response.body?.string()
                    if (responseData != null) {
                    // Parse JSON response
                    val directionsResponse = directionsAdapter.fromJson(responseData)
                    val duration = directionsResponse?.routes?.firstOrNull()
                        ?.legs?.firstOrNull()
                        ?.duration
                    if (duration != null) {
                        // Use withContext(Dispatchers.Main) if you need to update UI components
                        println("From: $origin ,To: $dest Duration: ${duration.text} ")
                    } else {
                        println("Could not parse the duration.")
                    }
                }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Handle any exceptions, e.g., by showing an error message
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val initialLocation = LatLng(40.71830841160277, -74.01058004547683)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(initialLocation))
    }
}

// Data classes to match the JSON structure
data class DirectionsResponse(
    val routes: List<Route>
)

data class Route(
    val legs: List<Leg>
)

data class Leg(
    val duration: Duration
)

data class Duration(
    val text: String,
    val value: Int // Duration in seconds
)

// Initialize Moshi
val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

val directionsAdapter = moshi.adapter(DirectionsResponse::class.java)

