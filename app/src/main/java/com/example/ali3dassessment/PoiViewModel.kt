package com.example.ali3dassessment
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class PoiViewModel(application: Application) : AndroidViewModel(application) {
    val db = PoiDatabase.getDatabase(application)

    var poitype : String = ""
    // Use MutableLiveData for dynamic updates
    private val _poiList = MutableLiveData<List<POI>>()
    val allPois: LiveData<List<POI>> = _poiList
    private val editTextContent = MutableLiveData<String>()
    var lat: Double =  0.0
    var lon: Double = 0.0
    init {
        getPoisByType()
    }

// Return the LiveData for observation

    public fun getPoisByType() {
        viewModelScope.launch(Dispatchers.IO) {
           val updatedList = if (poitype=="") {
                db.PoiDao().getAllpois()
            } else {
                Log.d("POI Type2:", "$poitype")
                db.PoiDao().getPoiByType(poitype)
            }
// Use 'value' to update LiveData

            _poiList.postValue(updatedList) }
// Method to delete all POIs
    }
}
