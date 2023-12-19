package com.example.ali3dassessment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem

class MapFragment : AppCompatActivity() {
    lateinit var map1: MapView
    lateinit var overlay: ItemizedIconOverlay<OverlayItem>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mapfrag)
        map1 = findViewById<MapView>(R.id.map1)
        map1.controller.setZoom(16.0)
        map1.controller.setCenter(GeoPoint(51.05, -0.72))

        overlay = ItemizedIconOverlay(this, arrayListOf<OverlayItem>(), null)
        map1.overlays.add(overlay)
    }
}

