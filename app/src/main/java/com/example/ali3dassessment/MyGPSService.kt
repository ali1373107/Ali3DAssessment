package com.example.ali3dassessment


import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import android.content.BroadcastReceiver // For part b of the 2nd question
import android.content.IntentFilter // For part b of the 2nd question
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat

class MyGPSService: Service(), LocationListener {


    var mgr: LocationManager? = null

    // Need this for storing the new location when the user's location changes
    inner class GPSServiceBinder(val GPS_Service: MyGPSService): android.os.Binder()
    var lat = 0.0
    var lon = 0.0
    // The following code is related to binding the service to the main activity
    var checkPermission = false
    // Start handler
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MyTag", "onStartCommand")


        mgr = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        checkPermission = true
        // start GPS,
        startGps()
        return START_STICKY // --> Important if you want the service to be restarted
    }


    override fun onBind(intent: Intent?): IBinder? {
        //return null // This is temporary, but needs to be changed so that the service can be bound to the Activity
        return GPSServiceBinder(this)
    }

    override fun onDestroy() {
        stopGps()
        super.onDestroy()
    }
    fun startGps() {
        if (checkPermission==true) {
            mgr?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, this)
        }
    }


    fun stopGps() {
        // Remove all updates from the location listener; this is done when the service is stopped.
        mgr?.removeUpdates(this)
    }

    override fun onLocationChanged(newLoc: Location) {
        lat = newLoc.latitude
        lon = newLoc.longitude
        Log.d("MyTag", "onLocationChanged${newLoc.latitude} ${newLoc.longitude}")

        // Send the above data in a broadcast instead
        val broadcast = Intent().apply {
            action = "sendLocation"
            putExtra("Servicelat", newLoc.latitude)
            putExtra("Servicelon", newLoc.longitude)


        }
        sendBroadcast(broadcast)
    }

    override fun onProviderDisabled(provider: String) {
        Toast.makeText(this, "Provider is disabled", Toast.LENGTH_LONG).show()
    }

    override fun onProviderEnabled(provider: String) {
        Toast.makeText(this, "Provider has been enabled", Toast.LENGTH_LONG).show()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }
}