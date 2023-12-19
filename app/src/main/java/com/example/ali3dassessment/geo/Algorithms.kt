package com.example.ali3dassessment.geo


object Algorithms {
    fun haversineDist(lon1: Double, lat1: Double, lon2: Double, lat2: Double): Double {
        val R = 6371000.0
        val dlon = (lon2 - lon1) * (Math.PI / 180)
        val dlat = (lat2 - lat1) * (Math.PI / 180)
        val slat = Math.sin(dlat / 2)
        val slon = Math.sin(dlon / 2)
        val a =
            slat * slat + Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) * slon * slon
        val c = 2 * Math.asin(Math.min(1.0, Math.sqrt(a)))
        return R * c
    }
}

