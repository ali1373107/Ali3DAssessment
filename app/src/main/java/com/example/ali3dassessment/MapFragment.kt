package com.example.ali3dassessment

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.ali3dassessment.geo.Algorithms
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

    lateinit var channel: NotificationChannel
    var notificationId = 1
    val channelID = "POI"
     var lat: Double = 0.0
     var lon: Double = 0.0
    // Declaring our sensor variables as attributes of the Main Activity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        poiViewModel.lat1.observeForever(latObserver)
        poiViewModel.lon1.observeForever(lonObserver)
        val view = inflater.inflate(R.layout.mapfrag, container, false)
        map1 = view.findViewById(R.id.map1)
        map1.controller.setZoom(16.0)

        map1.controller.setCenter(GeoPoint(50.9, -1.4))


            val filterButton = view.findViewById<Button>(R.id.filter_Button1)
            val filterEditText = view.findViewById<EditText>(R.id.filter_Edit_Text1)
            filterButton.setOnClickListener {
                Log.d("Button", "Clicked")

                var filterText = filterEditText.text.toString()
                poiViewModel.poitype = filterText
                poiViewModel.getPoisByType()

                Log.d("Button", "Filter Text VM2: ${poiViewModel.poitype}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    channel = NotificationChannel(
                        channelID,
                        "POIs nearby",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    val nMgr = requireContext().getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager

                    nMgr.createNotificationChannel(channel)
                }

            }
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
            /*
            for (poi in pois) {

                val poieast = poi.lon
                val poinorth = poi.lat
                Log.d("MYtagy1", "NotiLat1 and Lon1: ${poi.lon} ,,${poi.lat}")

                val en2 = EastNorth(poieast, poinorth)
                val p2 = proj.unproject(en2)
                poi.lon = p2.lon
                poi.lat = p2.lat
                val dist =
                    // bevcouse of shorting in time i didnt get live lat and lon i hard coded but in all other activities and fragments I used live data from viewmodel
                    Algorithms.haversineDist( poi.lon, poi.lat,1.4037617, 50.90806832852336)
                Log.d("MYtagy1", "Lon1: ${poi.lon} ,,${poi.lat},,${-1.4037617},,${50.90806832852336}")

                if (dist < 50000) {
                    sendNotification(poi.name, poi.featureType)
                }


            }

             */
            overlay = ItemizedIconOverlay(requireContext(), items, null)
            map1.overlays.add(overlay)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel = NotificationChannel(channelID, "POI", NotificationManager.IMPORTANCE_DEFAULT)
                val nMgr = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nMgr.createNotificationChannel(channel)
            }

        }
            return view
        }
    private val latObserver = Observer<Double> { newLat ->
        lat = newLat
        Log.d("MyTag11", "Updated Latitude in Activity: $lat")
    }

    private val lonObserver = Observer<Double> { newLon ->
        lon = newLon
        Log.d("MyTag11", "Updated Longitude in Activity: $lon")
    }

    private fun sendNotification(poiName: String, type: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification = Notification.Builder(requireContext(), channelID)
                .setContentTitle("Pois Nearby")
                .setContentText("Name - ${poiName} Distance - ${type}")
                .setSmallIcon(R.drawable.peak)
                .build()
            val nMgr = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nMgr.notify(
                notificationId++,
                notification
            ) // uniqueId is a unique ID for this notification
        }
    }

}
