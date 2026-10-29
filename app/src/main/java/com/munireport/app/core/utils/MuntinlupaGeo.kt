package com.munireport.app.core.utils

import java.util.Locale

/**
 * Coarse municipal geofence used for the in-app location picker. MuniReport
 * only accepts pins inside Muntinlupa City; the illustrated map is deliberately
 * offline-friendly so the form still works without a Maps API key.
 */
object MuntinlupaGeo {
    const val CENTER_LATITUDE = 14.4081
    const val CENTER_LONGITUDE = 121.0415

    const val MIN_LATITUDE = 14.32
    const val MAX_LATITUDE = 14.50
    const val MIN_LONGITUDE = 120.99
    const val MAX_LONGITUDE = 121.13

    fun isInside(latitude: Double, longitude: Double): Boolean =
        latitude in MIN_LATITUDE..MAX_LATITUDE && longitude in MIN_LONGITUDE..MAX_LONGITUDE

    fun format(latitude: Double, longitude: Double): String =
        String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
}
