package com.example.ali3dassessment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.ali3dassessment.geo.EastNorth
import com.example.ali3dassessment.geo.LonLat
import com.example.ali3dassessment.geo.SphericalMercatorProjection
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    var lon = -1.4
    var lat = 50.9

    private lateinit var db: PoiDatabase
    var west = lon -0.01
    var east =lon +0.01
    var south= lat -0.01
    var north = lat +0.01
    val bbox = "$west,$south,$east,$north"
    var proj = SphericalMercatorProjection()
    val myList = mutableListOf<POI>()
    val listPoi1 = mutableListOf<POI>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = PoiDatabase.getDatabase(application)
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

    val DispalyPoiLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

        }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.upload_from_url ->{
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
                                           myList.add(Poi)
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
                DispalyPoiLauncher.launch(intent)
                return true
            }
            R.id.deletePOI ->{
                lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    // Perform the delete operation on a background thread
                    db.PoiDao().deleteAll()
                }
            }
                return true
            }

        }
        return false

    }
}
