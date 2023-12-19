package com.example.ali3dassessment

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities =  [POI::class],version = 1, exportSchema = false)
public abstract class PoiDatabase: RoomDatabase(){
    abstract fun PoiDao():PoiDao

    companion object {
        private var instance: PoiDatabase? = null
        fun getDatabase(ctx:Context) : PoiDatabase{
            var tmpInstance = instance
            if(tmpInstance == null) {
                tmpInstance = Room.databaseBuilder(
                    ctx.applicationContext,
                    PoiDatabase::class.java,
                    "poiDatabase"
                ).build()
                instance = tmpInstance

            }
            return tmpInstance
        }
    }
}