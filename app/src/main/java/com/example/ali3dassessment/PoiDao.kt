package com.example.ali3dassessment

import androidx.lifecycle.LiveData
import androidx.room.*
@Dao
interface PoiDao {
    @Query("SELECT * FROM Poi")
    fun getAllpois():List<POI>

    @Query("SELECT * FROM Poi WHERE featureType=:type")
    fun getPoiByType(type:String?):List<POI>


    @Insert
   fun insert(vararg poi: POI): LongArray
    @Update
    fun update(poi:POI):Int

    @Query("DELETE FROM Poi")
    fun deleteAll()
    @Query("SELECT * FROM Poi WHERE osm_id = :osmId")
    fun getPoiById(osmId: Long): POI?
}