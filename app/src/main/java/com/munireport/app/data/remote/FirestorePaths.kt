package com.munireport.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore

internal fun followDocumentId(userId: String, complaintId: String): String = "${userId}_$complaintId"

internal fun complaintCollection(db: FirebaseFirestore) =
    db.collection("complaints")

internal fun verificationCollection(db: FirebaseFirestore) =
    db.collection("verifications")
