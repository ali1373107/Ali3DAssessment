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
import androidx.fragment.app.viewModels
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import com.example.ali3dassessment.geo.EastNorth
import com.example.ali3dassessment.geo.SphericalMercatorProjection
import org.osmdroid.config.Configuration

class MapFragment : Fragment(R.layout.mapfrag) {
    lateinit var map1: MapView
    lateinit var overlay: ItemizedIconOverlay<OverlayItem>
    val poiViewModel: PoiViewModel by viewModels()
    var proj = SphericalMercatorProjection()
    var lon = -1.4
    var lat = 50.9

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
      //  val filterButton = view.findViewById<Button>(R.id.filter_Button)
      //  val filterEditText = view.findViewById<EditText>(R.id.filter_Edit_Text)
       //filterButton.setOnClickListener {
           //val filterText = filterEditText.text.toString()
            poiViewModel.allPois
                .observe(viewLifecycleOwner, { pois ->
                    for (poi in pois) {
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

}
