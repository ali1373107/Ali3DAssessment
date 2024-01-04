package com.example.ali3dassessment

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import android.widget.FrameLayout
import android.widget.Toast

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.example.ali3dassessment.geo.Algorithms
import com.example.ali3dassessment.geo.EastNorth
import com.example.ali3dassessment.geo.LonLat
import com.example.ali3dassessment.geo.SphericalMercatorProjection
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    lateinit var poiAdapter: POIAdapter
    lateinit var db: PoiDatabase
    var proj = SphericalMercatorProjection()
    val listPoi1 = mutableListOf<POI>()
    var service: MyGPSService? = null
    lateinit var receiver: BroadcastReceiver
    val poiViewModel: PoiViewModel by viewModels()
    var permissionsGranted = false
    var lon= 0.0
    var lat= 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = PoiDatabase.getDatabase(application)

        requestPermissions()


        val filter = IntentFilter().apply {
            addAction("sendLocation")
        }

        receiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    "sendLocation" -> {
                        val newlat = intent.getDoubleExtra("Servicelat", 0.0)
                        val newlon = intent.getDoubleExtra("Servicelon", 0.0)
                        poiViewModel.lat = newlat
                        poiViewModel.lon= newlon
                        Log.d("MyTag","latvm${poiViewModel.lat}")
                        Log.d("MyTag","lonvm${poiViewModel.lon}")
                       // updateLocationOperations(newlat, newlon)


                    }
                }
            }
        }
        registerReceiver(receiver, filter)

    }
    private fun updateLocationOperations() {

        lon = poiViewModel.lon
        lat = poiViewModel.lat
        Log.d("MyTag", "lonbbox${lon}")
        Log.d("MyTag", "latbbox${lat}")
        var west = lon - 0.01
        var east = lon + 0.01
        var south = lat - 0.01
        var north = lat + 0.01
        var bbox = "$west,$south,$east,$north"

        var url1 = "https://hikar.org/webapp/map?bbox=${bbox}&layers=poi&outProj=4326&format=json"
        url1.httpGet().responseObject<List<POI>> { _, _, result ->
            result.fold(
                success = { listPoi ->
                    Log.d("POIs", "Res $listPoi")
                    if (listPoi.isEmpty()) {
                        Toast.makeText(
                            this@MainActivity,
                            "NO POIs Nearby",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else {
                        // Handle the list of POIs here
                        for (poi in listPoi) {
                            Log.d("POI", "${poi.osm_id}")
                            listPoi1.add(poi)
                            // Process each POI as needed
                        }
                    }
                },
                failure = { error ->
                    Toast.makeText(this@MainActivity, "Failed to Load", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.upload_from_url ->{

               lifecycleScope.launch{
                   withContext(Dispatchers.IO){
                       updateLocationOperations()
                                       for(poi in listPoi1){
                                           val existingPoi = db.PoiDao().getPoiById(poi.osm_id)
                                           if(existingPoi == null) {
                                               Log.d("POI", "${poi}")
                                               val poiLon = poi.lon
                                               val poiLat = poi.lat
                                               val p = LonLat(poiLon, poiLat)
                                               val en = proj.project(p)
                                               val Poi = POI(
                                                   osm_id = poi.osm_id,
                                                   name = poi.name,
                                                   featureType = poi.featureType,
                                                   lon = en.easting,
                                                   lat = en.northing
                                               )
                                               Log.d("es en", "converted: ${Poi}")
                                               db.PoiDao().insert(Poi)
                                           }else {
                                               // Handle the case where the POI already exists (update or skip)
                                               // You can update the existing record or handle it according to your requirements
                                               Log.d("POI", "existedID:${existingPoi.osm_id}")

                                           }

                                       }

                                   }



                       }


            }

            R.id.deletePOI -> {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        // Perform the delete operation on a background thread
                        db.PoiDao().deleteAll()
                    }
                }
            }
            R.id.action_map -> {
                val mapFrag = MapFragment()
                supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, mapFrag)
                    .addToBackStack(null)
                    .commit()
                return true
                }
            R.id.action_poi ->{
                val recycleFrag = PoiFragment()
                supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, recycleFrag)
                    .addToBackStack(null)
                    .commit()
                return true
            }

            }


        return false

    }

    fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, LOCATION_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        } else {
            permissionsGranted = true
            initService()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 0 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionsGranted = true
            initService()
        } else {

            AlertDialog.Builder(this).setPositiveButton("OK", null).setMessage("GPS permission denied").show()
        }
    }


    @SuppressLint("MissingPermission")

    fun initService() {
        val startIntent = Intent(this, MyGPSService::class.java)
        startService(startIntent)

    }


}
