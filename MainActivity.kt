package com.example.softwareengineerrouteproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MainActivity : AppCompatActivity(), CoroutineScope by CoroutineScope(Dispatchers.Main) {
    private lateinit var editTextContainer: LinearLayout
    private lateinit var destinationRecyclerView: RecyclerView
    private lateinit var destinationAdapter: DestinationAdapter
    private var destinationList: MutableList<String> = mutableListOf()
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextContainer = findViewById(R.id.editTextContainer)
        destinationRecyclerView = findViewById(R.id.destinationRecyclerView)
        destinationAdapter = DestinationAdapter(destinationList)
        destinationRecyclerView.adapter = destinationAdapter
        destinationRecyclerView.layoutManager = LinearLayoutManager(this)
        val btnShow: Button = findViewById(R.id.btnShow)
        val btnSort: Button = findViewById(R.id.btnSort)

        btnShow.setOnClickListener {
            addNewAddress()
        }

        btnSort.setOnClickListener {
            pairEachDestination(destinationList)
        }
    }

    private fun addNewAddress() {
        val editText = findViewById<EditText>(R.id.etMessage)
        val address = editText.text.toString().trim()
        if (address.isNotEmpty()) {
            println("Added: $address")
            destinationList.add(address)
            destinationAdapter.notifyDataSetChanged() // Notify adapter of data change
            editText.setText("") // Clear the text field
        }
    }

    private fun pairEachDestination(destinationList: MutableList<String>) {
        println(destinationList)
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
            val requestUrl = "https://maps.googleapis.com/maps/api/directions/json?origin=$origin&destination=$dest&key=AIzaSyC1h3oQs3G96pIwlF_IVzOzS5iwtE0N2ac"

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
}



class DestinationAdapter(private val destinations: MutableList<String>) : RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_destination, parent, false)
        return DestinationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        holder.bind(destinations[position])
    }

    override fun getItemCount() = destinations.size

    inner class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val destinationTextView: TextView = itemView.findViewById(R.id.destinationItemTextView)
        private val removeButton: TextView = itemView.findViewById(R.id.removeButton)

        init {
            removeButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    destinations.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
        }

        fun bind(destination: String) {
            destinationTextView.text = destination
        }
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


//AIzaSyC1h3oQs3G96pIwlF_IVzOzS5iwtE0N2ac

//package com.example.softwareengineerrouteproject
//
//import android.os.Bundle
//import android.widget.Button
//import android.widget.EditText
//import android.widget.LinearLayout
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.LatLng
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import java.io.IOException
//import com.squareup.moshi.Moshi
//import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
//
//
//class MainActivity : AppCompatActivity(), CoroutineScope by CoroutineScope(Dispatchers.Main) {
//    private lateinit var editTextContainer: LinearLayout
//    private var editTextCount = 1
//    private var destinationList: MutableList<String> = mutableListOf()
//    private lateinit var mMap: GoogleMap
//    private val client = OkHttpClient()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        editTextContainer = findViewById(R.id.editTextContainer)
//        val btnShow: Button = findViewById(R.id.btnShow)
//        val btnSort: Button = findViewById(R.id.btnSort)
//
//        btnShow.setOnClickListener {
//            addNewEditText()
//        }
//
//        btnSort.setOnClickListener {
//            destinationList.clear()
//
//            // Iterate through all EditText views and add their content to destinationList
//            for (i in 0 until editTextContainer.childCount) {
//                val childView = editTextContainer.getChildAt(i)
//                if (childView is EditText) {
//                    val destination = childView.text.toString().trim()
//                    // Only add non-empty destinations
//                    if (destination.isNotEmpty()) {
//                        destinationList.add(destination)
//                    }
//                }
//            }
//            if (destinationList.size >= 2) {
//                pairEachDestination(destinationList)
//            }
//        }
//    }
//
//    private fun addNewEditText() {
//        val newEditText = EditText(this).apply {
//            id = editTextCount++
//            hint = "Enter a destination"
//            setPadding(5, 5, 5, 5)
//            textSize = 22f
//        }
//        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//        editTextContainer.addView(newEditText, layoutParams)
//    }
//
//    private fun pairEachDestination(destinationList: MutableList<String>) {
//        val pairs = mutableListOf<Pair<String, String>>()
//        for (i in 0 until destinationList.size) {
//            for (j in i + 1 until destinationList.size) {
//                pairs.add(Pair(destinationList[i], destinationList[j]))
//            }
//        }
//        pairs.forEach { (origin, dest) ->
//            getRoute(origin, dest)
//        }
//    }
//    private fun getRoute(origin: String, dest: String) = launch {
//        withContext(Dispatchers.IO) {
////            val origin = origin
////
////            val dest = pairs[1]
//
//            val requestUrl = "https://maps.googleapis.com/maps/api/directions/json?origin=$origin&destination=$dest&key=AIzaSyC1h3oQs3G96pIwlF_IVzOzS5iwtE0N2ac"
//
//            val request = Request.Builder()
//                .url(requestUrl)
//                .build()
//
//            try {
//                client.newCall(request).execute().use { response ->
//                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
//
//                    val responseData = response.body?.string()
//                    if (responseData != null) {
//                        // Parse JSON response
//                        val directionsResponse = directionsAdapter.fromJson(responseData)
//                        val duration = directionsResponse?.routes?.firstOrNull()
//                            ?.legs?.firstOrNull()
//                            ?.duration
//                        if (duration != null) {
//                            // Use withContext(Dispatchers.Main) if you need to update UI components
//                            println("From: $origin ,To: $dest Duration: ${duration.text} ")
//                        } else {
//                            println("Could not parse the duration.")
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    // Handle any exceptions, e.g., by showing an error message
//                    e.printStackTrace()
//                }
//            }
//        }
//    }
//}
//
//// Data classes to match the JSON structure
//data class DirectionsResponse(
//    val routes: List<Route>
//)
//
//data class Route(
//    val legs: List<Leg>
//)
//
//data class Leg(
//    val duration: Duration
//)
//
//data class Duration(
//    val text: String,
//    val value: Int // Duration in seconds
//)
//
//// Initialize Moshi
//val moshi = Moshi.Builder()
//    .add(KotlinJsonAdapterFactory())
//    .build()
//
//val directionsAdapter = moshi.adapter(DirectionsResponse::class.java)