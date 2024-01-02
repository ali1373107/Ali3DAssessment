package com.example.ali3dassessment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PoiViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PoiDatabase.getDatabase(application)

    // Use MutableLiveData for dynamic updates
     val allPois: MutableLiveData<LiveData<List<POI>>> = MutableLiveData()

    init {
        // Initialize with empty LiveData
        allPois.value = db.PoiDao().getAllpois()
    }

    // Return the LiveData for observation
    fun getAllPois(): LiveData<List<POI>> {
        return allPois.value ?: MutableLiveData()
    }

    fun getPoisByType(type: String): LiveData<List<POI>> {
        if (type.isEmpty()) {
            // Update LiveData with all pois
            allPois.value = db.PoiDao().getAllpois()
        } else {
            // Update LiveData with pois filtered by type
            allPois.value = db.PoiDao().getPoiByType(type)
        }
        return getAllPois()
    }
}
