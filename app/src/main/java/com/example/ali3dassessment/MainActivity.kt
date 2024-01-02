package com.example.ali3dassessment

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver

import android.content.Intent
import android.content.pm.PackageManager
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
    val latLon = LatLon()
    var lon = -1.401
    var lat = 50.908
    val poiViewModel: PoiViewModel by viewModels()
    lateinit var poiAdapter: POIAdapter
    lateinit var db: PoiDatabase
    var west = lon -0.01
    var east =lon +0.01
    var south= lat -0.01
    var north = lat +0.01
    val bbox = "$west,$south,$east,$north"
    var proj = SphericalMercatorProjection()
    val myList = mutableListOf<POI>()
    val listPoi1 = mutableListOf<POI>()
    var service: MyGPSService? = null
    val gpsViewModel: LocationViewModel by viewModels()
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fragmentContainer: FrameLayout
    private val poiFragment = PoiFragment()
    private val mapFragment = MapFragment()
    lateinit var receiver: BroadcastReceiver



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = PoiDatabase.getDatabase(application)
       // bottomNavigationView = findViewById(R.id.bottom_navigation)
        //displayRecycleView()
        requestPermissions()
        /*val filter = IntentFilter().apply {
            addAction("sendLocationCoords")
        }
        receiver= object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    "sendLocationCoords" -> {
                        val newLatitude = intent.getDoubleExtra("Servicelatitude", 0.0)
                        val newLongitude = intent.getDoubleExtra("Servicelongitude", 0.0)
                        Log.d("service", "Servicelatitude: $newLatitude, Lon: $lon")
                        Log.d("service", "Servicelongitude : $newLongitude, Lon: $lon")

                        // Update your variables with the new location
                        lat = newLatitude
                        lon = newLongitude

                        // Now, you can use lat and lon as needed
                        Log.d("MainActivity", "New Location - Lat: $lat, Lon: $lon")
                    }
                }
            }
        }
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        val gpsStartBroadcast = Intent().apply {
            action = "StartSignal"
        }
        sendBroadcast(gpsStartBroadcast)
        service?.latLon?.apply {
            gpsViewModel.latlon = this
        }*/
        //registerReceiver(locationUpdateReceiver, filter)
        gpsViewModel.liveLatLon.observe(this, { newLatLon ->
            lat = newLatLon.lat
            lon = newLatLon.lon
            Log.d("MainActivity", "New Location - Lat: $lat, Lon: $lon")
        })


        var url1 = "https://hikar.org/webapp/map?bbox=${bbox}&layers=poi&outProj=4326&format=json"
        url1.httpGet().responseObject<List<POI>> { _, _, result ->
            result.fold(
                success = { listPoi ->
                    Log.d("POIs", "Res $listPoi")
                    // Handle the list of POIs here
                    for (poi in listPoi) {
                        Log.d("POI", "${poi.osm_id}")
                        listPoi1.add(poi)
                        // Process each POI as needed
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
                                       for(poi in listPoi1){
                                           Log.d("POI", "${poi}")
                                           val poiLon = poi.lon
                                           val poiLat = poi.lat
                                           val p = LonLat(poiLon,poiLat)
                                           val en = proj.project(p)
                                           val Poi = POI(
                                               osm_id = poi.osm_id,
                                               name = poi.name,
                                               featureType = poi.featureType,
                                               lon = en.easting,
                                               lat = en.northing
                                           )
                                           Log.d("es en","converted: ${Poi}")
                                           db.PoiDao().insert(Poi)
                                       }
                                       //unproject the lat and lon
                                       for(poi in myList){
                                           val poieast = poi.lon
                                           val poinorth = poi.lat
                                           val en2 = EastNorth(poieast,poinorth)
                                           val p2 = proj.unproject(en2)
                                           val Poi1 = POI(
                                               osm_id = poi.osm_id,
                                               name = poi.name,
                                               featureType = poi.featureType,
                                               lon = p2.lon,
                                               lat = p2.lat
                                           )
                                           Log.d("es en","unconverted: ${Poi1}")
                                       }
                                   }



                       }


            }
            R.id.displayPoionMap ->{
                val intent = Intent(this, MapFragment::class.java)
                //DispalyPoiLauncher.launch(intent)
               // return true
                val pois: LiveData<List<POI>> = poiViewModel.getAllPois()
                pois.observe(this, { pois ->
                    for (i in pois.indices) {
                        val poi = pois[i]
                        Log.d("PoiData", " type: ${poi.featureType}")
                    }
                })
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

            //initService()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 0 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

           // initService()
        } else {

            AlertDialog.Builder(this).setPositiveButton("OK", null).setMessage("GPS permission denied").show()
        }
    }
    /*private fun updateLocation(location: Location) {
        lat = location.latitude
        lon = location.longitude
        Log.d("MainActivity", "New Location - Lat: $lat, Lon: $lon")

        // Update the liveLatLon LiveData in the LocationViewModel
        gpsViewModel.liveLatLon.value = LatLon(lat, lon)
    }*/
    /*override fun onLocationChanged(loc: Location) {
        lat = loc.latitude
        lon = loc.longitude
    }*/
    override fun onDestroy() {
        super.onDestroy()
        // Unregister the BroadcastReceiver when the activity is destroyed
       // unregisterReceiver(receiver)
    }
    /*fun initService() {
        // Start the service only if it's not running
        if (service == null) {
            val startIntent = Intent(this, MyGPSService::class.java)
            startService(startIntent)
        }
    }
*/
}
