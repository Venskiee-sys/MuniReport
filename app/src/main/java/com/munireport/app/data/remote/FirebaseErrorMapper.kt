package com.munireport.app.data.remote

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.munireport.app.core.common.AppException
import java.io.IOException

fun Throwable.toAppException(): Throwable = when (this) {
    is AppException -> this
    is FirebaseAuthInvalidCredentialsException -> AppException.InvalidCredentials()
    is FirebaseAuthInvalidUserException -> AppException.InvalidCredentials()
    is FirebaseAuthUserCollisionException -> AppException.EmailInUse()
    is FirebaseAuthWeakPasswordException -> AppException.WeakPassword()
    is FirebaseNetworkException -> AppException.Network()
    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> AppException.PermissionDenied()
        FirebaseFirestoreException.Code.NOT_FOUND -> AppException.NotFound()
        FirebaseFirestoreException.Code.ABORTED -> AppException.Conflict()
        FirebaseFirestoreException.Code.UNAVAILABLE -> AppException.Network()
        FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> AppException.Unknown("Too many requests. Try again shortly.")
        else -> AppException.Unknown(message ?: "The database request failed.")
    }
    is IOException -> AppException.Network()
    else -> AppException.Unknown(message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again.")
}
