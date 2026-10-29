package com.munireport.app.core.common

sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class InvalidCredentials : AppException("The email or password is incorrect.")
    class EmailInUse : AppException("An account already uses this email address.")
    class WeakPassword : AppException("Choose a stronger password.")
    class Network : AppException("No connection. Check your internet and try again.")
    class PermissionDenied : AppException("You do not have permission to do that.")
    class NotFound : AppException("That record is no longer available.")
    class Conflict : AppException("This record was changed by someone else. Refresh and try again.")
    class InvalidInput(message: String) : AppException(message)
    class Unknown(message: String = "Something went wrong. Please try again.") : AppException(message)
}

fun Throwable.userMessage(): String = when (this) {
    is AppException -> message ?: "Something went wrong. Please try again."
    else -> message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
}
