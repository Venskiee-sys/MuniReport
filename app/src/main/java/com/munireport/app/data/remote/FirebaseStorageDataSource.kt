package com.munireport.app.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseStorageDataSource(private val storage: FirebaseStorage) {

    suspend fun uploadComplaintPhotos(userId: String, uris: List<String>): List<String> =
        uris.map { uri ->
            upload(
                path = "complaints/$userId/${UUID.randomUUID()}.jpg",
                uri = Uri.parse(uri),
            )
        }

    suspend fun uploadVerificationDocument(userId: String, uri: String): String =
        upload(
            path = "verifications/$userId/${UUID.randomUUID()}.jpg",
            uri = Uri.parse(uri),
        )

    private suspend fun upload(path: String, uri: Uri): String {
        val reference = storage.reference.child(path)
        reference.putFile(uri).await()
        return reference.downloadUrl.await().toString()
    }
}
