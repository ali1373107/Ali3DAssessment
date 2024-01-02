package com.example.ali3dassessment

import androidx.lifecycle.LiveData
import androidx.room.*
@Dao
interface PoiDao {
    @Query("SELECT * FROM Poi")
    fun getAllpois(): LiveData<List<POI>>

    @Query("SELECT * FROM Poi WHERE featureType=:type")
    fun getPoiByType(type:String):LiveData<List<POI>>
    @Insert
   fun insert(vararg poi: POI): LongArray
    @Update
    fun update(poi:POI):Int

    @Query("DELETE FROM Poi")
    fun deleteAll()
}