package com.example.ali3dassessment.geo


class SphericalMercatorProjection {
    fun project(lonLat: LonLat): EastNorth {
        return EastNorth(
            lonToSphericalMercator(lonLat.lon) - originE,
            latToSphericalMercator(lonLat.lat) - originN
        )
    }

    fun unproject(projected: EastNorth): LonLat {
        return LonLat(
            sphmercToLon(projected.easting + originE),
            sphmercToLat(projected.northing + originN)
        )
    }

    private fun lonToSphericalMercator(lon: Double): Double {
        return lon / 180 * HALF_EARTH
    }

    private fun latToSphericalMercator(lat: Double): Double {
        val y = Math.log(Math.tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180)
        return y * HALF_EARTH / 180
    }

    private fun sphmercToLon(x: Double): Double {
        return x / HALF_EARTH * 180.0
    }

    private fun sphmercToLat(y: Double): Double {
        var lat = y / HALF_EARTH * 180.0
        lat = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2)
        return lat
    }

    val iD: String
        get() = "epsg:3857"

    companion object {
        const val EARTH = 40075016.68
        const val HALF_EARTH = 20037508.34
        private const val originE = -155997.56840143888
        private const val originN = 6604997.254756545
    }
}