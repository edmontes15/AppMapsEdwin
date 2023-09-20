package com.example.appmapsedwin

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.appmapsedwin.mvvm.repository.ApiService
import com.example.appmapsedwin.mvvm.model.LocationData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener,
    LocationListener {
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createMapFragment()
        setupNotificationChannel()

        //sendNotification()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        createMarker()
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        enabledLocation()
    }

    private fun createMarker() {
        if (isLocationPermissionGranted()) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        currentLatitude = it.latitude
                        currentLongitude = it.longitude
                        val currentLatLng = LatLng(currentLatitude, currentLongitude)
                        val motorcycleIcon =
                            BitmapDescriptorFactory.fromResource(R.drawable.ic_moto)
                        map.addMarker(
                            MarkerOptions().position(currentLatLng).title("Mi ubicación actual")
                                .icon(motorcycleIcon)
                        )
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(currentLatLng, 50f),
                            4000,
                            null
                        )
                    }
                }
        } else {
            //requestLocationPermission()
        }
    }

    private fun createMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED


    private fun enabledLocation() {
        if (!::map.isInitialized) return
        if (isLocationPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermision()
        }
    }

    private fun requestLocationPermision() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            Toast.makeText(this, "ve a ajusted y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
            } else {
                Toast.makeText(
                    this,
                    "Para activar la localizacion ve a ajusted y acepta los permisos",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {}
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::map.isInitialized) return
        if (!isLocationPermissionGranted()) {
            map.isMyLocationEnabled = false
            Toast.makeText(
                this,
                "Para activar la localizacion ve a ajusted y acepta los permisos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(
            this,
            "Para activar la localizacion ve a ajusted y acepta los permisos",
            Toast.LENGTH_SHORT
        ).show()
        return false
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Estas en ${p0.latitude}, ${p0.altitude}", Toast.LENGTH_SHORT).show()

    }

    override fun onLocationChanged(location: Location) {
    }

    fun setupNotificationChannel() {
        val channelID = "chat"
        val channelName = "chat"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelID, channelName, importance)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendLocationNotification() {
        val channelID = "chat"

        val notification = NotificationCompat.Builder(this, channelID).apply {
            setContentTitle("Ubicación")
            setContentText("Latitud: $currentLatitude, Longitud: $currentLongitude")
            setSmallIcon(R.drawable.ic_moto)
        }.build()
        print("Jalando padre")
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(1, notification)
        GlobalScope.launch(Dispatchers.IO) {
            val response = ApiService.locationApi.sendLocation(LocationData(currentLatitude, currentLongitude))
            if (response.isSuccessful) {
                Log.d("Si agarro", "Si agarro")
            // La solicitud se realizó correctamente
            val responseBody = response.body()
            // Puedes usar 'responseBody' para obtener más información de la respuesta
        } else {
            // Hubo un error en la solicitud
                Log.d("No agarro", "No agarro")
            val errorBody = response.errorBody()?.string()
            // 'errorBody' contiene el cuerpo de la respuesta de error del servidor
        }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val locationNotificationRunnable = object : Runnable {
        override fun run() {
            sendLocationNotification()
            handler.postDelayed(this, 5000) // 5 segundos
            print("Jalando padre")
        }
    }

    override fun onStart() {
        super.onStart()
        handler.post(locationNotificationRunnable)

    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(locationNotificationRunnable)
        //stopLocationUpdates()
    }

}