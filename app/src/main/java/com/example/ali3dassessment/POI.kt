package com.example.ali3dassessment

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName="Poi")
data class POI (
    @PrimaryKey val osm_id: Long,
    var name: String,
    var featureType: String,
    val lon:Double,
    val lat:Double
)