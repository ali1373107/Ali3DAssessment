package com.example.ali3dassessment

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName="Poi")
data class POI (
    @PrimaryKey val osm_Id: String,
    var name: String,
    var featureType: String,
    val lat:Double,
    val lon:Double
)