package com.munireport.app.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class FirebaseModule(context: Context) {
    private val app: FirebaseApp? = runCatching {
        FirebaseApp.getApps(context).firstOrNull() ?: FirebaseApp.initializeApp(context)
    }.getOrNull()

    val isConfigured: Boolean = app != null

    fun auth(): FirebaseAuth = FirebaseAuth.getInstance(requireApp())

    fun firestore(): FirebaseFirestore = FirebaseFirestore.getInstance(requireApp())

    fun storage(): FirebaseStorage = FirebaseStorage.getInstance(requireApp())

    private fun requireApp(): FirebaseApp =
        app ?: error("Firebase is not configured. Add app/google-services.json first.")
}
