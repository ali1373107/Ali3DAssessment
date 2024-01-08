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
    private val _lat = MutableLiveData<Double>()
    var lat1: LiveData<Double> = _lat

    private val _lon = MutableLiveData<Double>()
    var lon1: LiveData<Double> = _lon
    init {
        getPoisByType()
    }
    public fun updateLatLon(newLat: Double, newLon: Double) {
        // logic to set lat and lon
        _lat.postValue(newLat)
        _lon.postValue(newLon)

    }
    public fun getPoisByType() {
        viewModelScope.launch(Dispatchers.IO) {
            // we use view model poitype to search by type
           val updatedList = if (poitype==""||poitype=="all") {
                db.PoiDao().getAllpois()
            } else {
                Log.d("POI Type2:", "$poitype")
                db.PoiDao().getPoiByType(poitype)
            }
// Use 'value' to update LiveData
            _poiList.postValue(updatedList) }

    }
}
