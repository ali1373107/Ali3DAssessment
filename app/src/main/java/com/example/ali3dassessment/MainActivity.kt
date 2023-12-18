package com.example.ali3dassessment

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.ComponentActivity

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.fuel.gson.responseObject // for GSON - uncomment when needed
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import java.net.URL

class MainActivity : ComponentActivity() {
    var lon = -1.4
    var lat = 50.9


    var west = lon -0.01
    var east =lon +0.01
    var south= lat -0.01
    var north = lat +0.01
    val bbox = "$west,$south,$east,$north"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                       var url = "https://hikar.org/webapp/map?bbox=${bbox}&layers=poi&outProj=4326&format=json"
                       url.httpGet().responseObject<List<POI>> { request, response, result ->
                           when(result){
                               is Result.Success -> {
                                   val listPoi = result.get()
                                   Log.d("POIs","Res ${listPoi}")
                                   if(listPoi.isEmpty()){
                                       Toast.makeText(
                                           this@MainActivity,
                                           "No POIs exist",
                                           Toast.LENGTH_SHORT
                                       ).show()
                                   }else{
                                       for(poi in listPoi){
                                           Log.d("POI", "${poi}")
                                       }
                                   }
                               }
                               is Result.Failure -> {
                                   Toast.makeText(this@MainActivity, "Failed to Load", Toast.LENGTH_SHORT).show()
                               }
                           }
                       }
                   }
               }
            }

        }
        return false

    }
}
