package com.example.ali3dassessment

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import com.example.ali3dassessment.geo.EastNorth
import com.example.ali3dassessment.geo.SphericalMercatorProjection
import org.json.JSONObject
import org.osmdroid.config.Configuration
import kotlin.math.log

class MapFragment : Fragment(R.layout.mapfrag) {
    lateinit var map1: MapView
    lateinit var overlay: ItemizedIconOverlay<OverlayItem>
    val poiViewModel: PoiViewModel by viewModels()
    var proj = SphericalMercatorProjection()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        val view = inflater.inflate(R.layout.mapfrag, container, false)
        map1 = view.findViewById(R.id.map1)
        map1.controller.setZoom(16.0)
        map1.controller.setCenter(GeoPoint(50.9, -1.4))


        poiViewModel.allPois.observe(viewLifecycleOwner) { pois ->

            val items = mutableListOf<OverlayItem>()
            items.clear()
            map1.overlays.clear()
            for (poi in pois) {
                Log.d("Button", "POIs found: $poi")
                val poinorth = poi.lat
                val poieast = poi.lon
                // unproj lat and lon
                val en2 = EastNorth(poieast, poinorth)
                val p2 = proj.unproject(en2)
                poi.lon = p2.lon
                poi.lat = p2.lat
                // log lon and lat to ensure we are getting correct data
                Log.d("lon", "${poi.lon}")
                Log.d("lat", "${poi.lat}")
                val type = poi.featureType
                val location = GeoPoint(poi.lat, poi.lon)
                val name = poi.name
                val overlayItem = OverlayItem(name, type, location)
                items.add(overlayItem)
            }
            overlay = ItemizedIconOverlay(requireContext(), items, null)
            map1.overlays.add(overlay)
            for (poi in pois) {
                Log.d("poisd", "${poi}")
            }

        }


        val filterButton = view.findViewById<Button>(R.id.filter_Button1)
        val filterEditText = view.findViewById<EditText>(R.id.filter_Edit_Text1)
        filterButton.setOnClickListener {
            Log.d("Button", "Clicked")

            var filterText = filterEditText.text.toString()
            poiViewModel.poitype = filterText
            poiViewModel.getPoisByType()

            Log.d("Button", "Filter Text VM2: ${poiViewModel.poitype}")


        }
        return view
    }













    /* filterButton.setOnClickListener {
         var filterText = filterEditText.text.toString()
         Log.d("filtertext", "${filterText}")
         filterText = poiViewModel.poitype
         Log.d("typeview","${filterText}")
         poiViewModel.allPois
             .observe(viewLifecycleOwner, { pois ->
                 allPoi = pois
                 if (filterText.isEmpty()) {
                     poiViewModel.getPoisByType(filterText)
                 } else {
                     poiViewModel.getPoisByType(filterText)
                 }

                 val items = mutableListOf<OverlayItem>()
                 for (poi in allPoi) {
                     val poinorth = poi.lat
                     val poieast = poi.lon
                     // unproj lat and lon
                     val en2 = EastNorth(poieast, poinorth)
                     val p2 = proj.unproject(en2)
                     poi.lon = p2.lon
                     poi.lat = p2.lat
                     // log lon and lat to ensure we are getting correct data
                     Log.d("lon", "${poi.lon}")
                     Log.d("lat", "${poi.lat}")
                     val type = poi.featureType
                     val location = GeoPoint(poi.lat, poi.lon)
                     val name = poi.name
                     val overlayItem = OverlayItem(name, type, location)
                     items.add(overlayItem)
                 }
                 overlay = ItemizedIconOverlay(requireContext(), items, null)
                 map1.overlays.add(overlay)
                 for (poi in pois) {
                     Log.d("poisd", "${poi}")
                 }

             })
     }
     return view
 }

 private fun updateMapWithFilteredPois(filterText: String) {

 }




*/





/*
    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        val view = inflater.inflate(R.layout.mapfrag, container, false)
        map1 = view.findViewById(R.id.map1)
        map1.controller.setZoom(16.0)
        map1.controller.setCenter(GeoPoint(lat, lon))
        val items = arrayListOf<OverlayItem>()
        val filterButton = view.findViewById<Button>(R.id.filter_Button)
        val filterEditText = view.findViewById<EditText>(R.id.filter_Edit_Text)
       //filterButton.setOnClickListener {
           //val filterText = filterEditText.text.toString()
        poiViewModel.allPois
            .observe(viewLifecycleOwner, { pois->
                for (poi in pois ){
                    val poinorth = poi.lat
                    val poieast = poi.lon
                    // unproj lat and lon
                    val en2 = EastNorth(poieast, poinorth)
                    val p2 = proj.unproject(en2)
                    poi.lon = p2.lon
                    poi.lat = p2.lat
                    // log lon and lat to ensure we are getting correct data
                    Log.d("lon", "${poi.lon}")
                    Log.d("lat", "${poi.lat}")
                    val type = poi.featureType
                    val location = GeoPoint(poi.lat, poi.lon)
                    val name = poi.name
                    val overlayItem = OverlayItem(name, type, location)
                    items.add(overlayItem)
                }
                overlay = ItemizedIconOverlay(requireContext(), items, null)
                map1.overlays.add(overlay)
            })
     //   }

            return view
        }
*/
}
