package com.prathamesh.womensafetyapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class PoliceStationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var currentLatLng: LatLng? = null
    private var currentRoutePolyline: Polyline? = null

    private val LOCATION_PERMISSION_REQUEST = 1001
    private val TAG_DIRECTIONS = "DirectionsAPI"

    //
    private val policeStationLatLng = LatLng(19.1998183, 72.8616118)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_police_station)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateDistanceMeters(5f)
            .setMinUpdateIntervalMillis(2000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                onLocationObtained(loc)
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        try {
            mMap.isMyLocationEnabled = true
        } catch (_: SecurityException) {}


        mMap.addMarker(MarkerOptions().position(policeStationLatLng).title("Police Station"))


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    onLocationObtained(loc)
                } else {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                }
            }
        }
    }

    private fun onLocationObtained(location: Location) {
        currentLatLng = LatLng(location.latitude, location.longitude)


        val boundsBuilder = LatLngBounds.Builder()
        boundsBuilder.include(currentLatLng!!)
        boundsBuilder.include(policeStationLatLng)
        val bounds = boundsBuilder.build()
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))

        // Draw route
        drawRoute(currentLatLng!!, policeStationLatLng)
    }

    // ---------- DIRECTIONS API ----------
    private fun drawRoute(origin: LatLng, destination: LatLng) {
        val url = "https://router.project-osrm.org/route/v1/driving/" +
                "${origin.longitude},${origin.latitude};" +
                "${destination.longitude},${destination.latitude}" +
                "?overview=full&geometries=geojson"   // ✅ use geojson

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@PoliceStationActivity, "OSRM request failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return

                try {
                    val json = JSONObject(body)
                    val routes = json.getJSONArray("routes")
                    if (routes.length() == 0) return

                    // ✅ Extract coordinates from GeoJSON
                    val coordinates = routes.getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates")

                    val path = ArrayList<LatLng>()
                    for (i in 0 until coordinates.length()) {
                        val coord = coordinates.getJSONArray(i)
                        val lng = coord.getDouble(0)
                        val lat = coord.getDouble(1)
                        path.add(LatLng(lat, lng)) // Google Maps needs (lat, lng)
                    }

                    runOnUiThread {
                        currentRoutePolyline?.remove()
                        currentRoutePolyline = mMap.addPolyline(
                            PolylineOptions()
                                .addAll(path)
                                .color(Color.BLUE)
                                .width(10f)
                        )
                        // 👇 Auto zoom to fit the entire route
                        val boundsBuilder = LatLngBounds.Builder()
                        path.forEach { boundsBuilder.include(it) }
                        val bounds = boundsBuilder.build()
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
                    }
                } catch (e: Exception) {
                    Log.e("OSRM", "Parse error", e)
                }
            }
        })
    }





    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (::mMap.isInitialized) {
                    try {
                        mMap.isMyLocationEnabled = true
                    } catch (_: SecurityException) {}
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (_: Exception) {}
    }
}
