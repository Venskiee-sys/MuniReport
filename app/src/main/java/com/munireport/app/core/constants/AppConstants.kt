package com.munireport.app.core.constants

object AppConstants {
    const val APP_NAME = "MuniReport"
    const val MAX_PHOTOS = 1
    const val MAX_PDF_RECORDS = 150
    const val DEFAULT_BARANGAY = "Poblacion"
    const val DEMO_RESIDENT_EMAIL = "resident@munireport.app"
    const val DEMO_RESIDENT_PASSWORD = "Resident123!"
    const val DEMO_ADMIN_EMAIL = "admin@munireport.app"
    const val DEMO_ADMIN_PASSWORD = "Admin123!"

    val MUNTINLUPA_BARANGAYS = listOf(
        "Alabang",
        "Ayala Alabang",
        "Bayanan",
        "Buli",
        "Cupang",
        "Poblacion",
        "Putatan",
        "Sucat",
        "Tunasan",
    )
}

object FirestoreCollections {
    const val USERS = "users"
    const val COMPLAINTS = "complaints"
    const val COMMENTS = "comments"
    const val UPDATES = "updates"
    const val FOLLOWS = "follows"
    const val NOTIFICATIONS = "notifications"
    const val VERIFICATIONS = "verifications"
}
