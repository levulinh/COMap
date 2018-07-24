package com.andrew.studio.comap.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.andrew.studio.comap.R
import com.andrew.studio.comap.config.Config
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import org.joda.time.DateTime
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var sessionCode: Int = 0
    private val config = Config
    private lateinit var queue: RequestQueue
    private lateinit var jsonArrayRequest: JsonArrayRequest
    private var firstLat: Double = 0.0
    private var firstLong: Double = 0.0
    private var mode = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mode = intent.getBooleanExtra("mode", false)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        sessionCode = intent.getIntExtra("code", 0)
        queue = Volley.newRequestQueue(this)

        jsonArrayRequest = JsonArrayRequest(Request.Method.GET,
                if (!mode) "${config.GET_ALL_DATA}/$sessionCode" else "${config.GET_ALL_DATA}/admin",
                null,
                Response.Listener { response ->
                    firstLat = response.getJSONObject(0).getDouble("latitude")
                    firstLong = response.getJSONObject(0).getDouble("longitude")

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(firstLat, firstLong), 18.0f))
                    var list: MutableList<LatLng> = mutableListOf()

                    for (i in 0 until response.length()) {

                        val resObj = response.getJSONObject(i)

                        val dateTime = DateTime(resObj.getString("createdAt"))
                        val date = dateTime.toDate()
                        mMap.addMarker(MarkerOptions()
                                .position(LatLng(
                                        resObj.getDouble("latitude"),
                                        resObj.getDouble("longitude")))
                                .title("${resObj.getInt("covalue")} ppm"))
                                .snippet = SimpleDateFormat("HH:mm dd-MMM-yyyy").format(date)

                        list.add(LatLng(
                                resObj.getDouble("latitude"),
                                resObj.getDouble("longitude")))
                    }
                if (!mode) {
                    val polyline: Polyline = mMap.addPolyline(PolylineOptions()
                            .addAll(list)
                            .width(5f)
                            .color(Color.RED))

                    polyline.tag = "A"
                }
                },
                Response.ErrorListener { error ->
                    if (!error.message.isNullOrBlank()) Log.e("LOCATION ERROR", error.message)
                    Toast.makeText(this@MapsActivity, "Something didn't work as expected, try again later!", Toast.LENGTH_LONG).show()
                })
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.isMyLocationEnabled = true

        queue.add(jsonArrayRequest)
    }
}
