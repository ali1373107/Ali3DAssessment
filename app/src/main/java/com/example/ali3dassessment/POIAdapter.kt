package com.example.ali3dassessment
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ali3dassessment.geo.Algorithms
import com.example.ali3dassessment.geo.EastNorth
import com.example.ali3dassessment.geo.SphericalMercatorProjection
class POIAdapter(private var pois: List<POI>, private val context: Context,private val poiViewModel: PoiViewModel) : RecyclerView.Adapter<POIAdapter.ViewHolder>() {
    lateinit var channel: NotificationChannel
    var notificationId = 1
    val channelID = "POI"
    var proj = SphericalMercatorProjection()

    init {
// Initialize notification channel in the constructor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(channelID, "POI", NotificationManager.IMPORTANCE_DEFAULT)
            val nMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nMgr.createNotificationChannel(channel)
        }
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val typeTextView: TextView = itemView.findViewById(R.id.typeTextView)
        val distancetextView:TextView = itemView.findViewById(R.id.distance)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.poi_item, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val poi = pois[position]
        val lon = poiViewModel.lon
        val lat = poiViewModel.lat
        val poieast = poi.lon
        val poinorth = poi.lat
//unproj lat and lon
        val en2 = EastNorth(poieast,poinorth)
        val p2 = proj.unproject(en2)
        poi.lon = p2.lon
        poi.lat = p2.lat
//log lon and lat to ee we getting correct data
        Log.d("lon","${poi.lon}")
        Log.d("lat","${poi.lat}")
        val dist = Algorithms.haversineDist(poi.lon,poi.lat,lon,lat)
        val distInKm = dist/1000
        val formattedDist = String.format("%.2f", distInKm) // Format to 2 decimal places

        holder.nameTextView.text = poi.name
        holder.typeTextView.text = poi.featureType
        holder.distancetextView.text = formattedDist
        if(dist < 50000) {
            sendNotification(poi.name)
        }
// Bind other properties as needed
    }
    override fun getItemCount(): Int {
        return pois.size
    }
    fun updateData(newPois: List<POI>) {
        pois = newPois
        notifyDataSetChanged()
    }
    private fun sendNotification(pName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification = Notification.Builder(context, channelID)
                .setContentTitle("Nearby POIs")
                .setContentText("New Location is now ${pName}")
                .setSmallIcon(R.drawable.peak)
                .build()
            val nMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nMgr.notify(notificationId++, notification) // uniqueId is a unique ID for this notification
        }
    }
}