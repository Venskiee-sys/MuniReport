package com.munireport.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.munireport.app.core.common.AppException
import com.munireport.app.core.validation.Validators
import com.munireport.app.data.mapper.toFirestoreMap
import com.munireport.app.data.mapper.toUserProfile
import com.munireport.app.data.remote.toAppException
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.UserRole
import com.munireport.app.domain.model.VerificationStatus
import com.munireport.app.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRepository {

    override fun observeAuthUserId(): Flow<String?> = callbackFlow {
        trySend(auth.currentUser?.uid)
        val listener = FirebaseAuth.AuthStateListener { current ->
            trySend(current.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun observeProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val registration = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error.toAppException())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toUserProfile())
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getProfile(userId: String): UserProfile? = runCatching {
        firestore.collection("users").document(userId).get().await().toUserProfile()
    }.getOrElse { throw it.toAppException() }

    override suspend fun resolveAuthorizedRole(profile: UserProfile): UserRole {
        val claim = runCatching {
            auth.currentUser?.getIdToken(false)?.await()?.claims?.get("role") as? String
        }.getOrNull()
        return if (claim == UserRole.ADMIN.name && profile.role == UserRole.ADMIN) {
            UserRole.ADMIN
        } else {
            UserRole.RESIDENT
        }
    }

    override suspend fun signIn(email: String, password: String): UserProfile {
        return runCatching {
            val result = auth.signInWithEmailAndPassword(
                Validators.normalizeEmail(email),
                password,
            ).await()
            val user = result.user ?: throw AppException.InvalidCredentials()
            val profile = firestore.collection("users").document(user.uid).get().await().toUserProfile()
                ?: throw AppException.NotFound()
            profile
        }.getOrElse { throw it.toAppException() }
    }

    override suspend fun signUp(
        fullName: String,
        email: String,
        phone: String,
        address: String,
        purok: String,
        password: String,
    ): UserProfile {
        return runCatching {
            val result = auth.createUserWithEmailAndPassword(
                Validators.normalizeEmail(email),
                password,
            ).await()
            val user = result.user ?: throw AppException.Unknown()
            try {
                user.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName.trim())
                        .build(),
                ).await()
                val now = System.currentTimeMillis()
                val profile = UserProfile(
                    id = user.uid,
                    fullName = fullName.trim(),
                    email = Validators.normalizeEmail(email),
                    phone = phone.trim(),
                    address = address.trim(),
                    purok = purok.trim(),
                    role = UserRole.RESIDENT,
                    verificationStatus = VerificationStatus.NOT_SUBMITTED,
                    createdAt = now,
                    updatedAt = now,
                )
                firestore.collection("users").document(user.uid)
                    .set(profile.toFirestoreMap())
                    .await()
                profile
            } catch (failure: Throwable) {
                runCatching { user.delete().await() }
                runCatching { auth.signOut() }
                throw failure
            }
        }.getOrElse { throw it.toAppException() }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun sendPasswordReset(email: String) {
        runCatching {
            auth.sendPasswordResetEmail(Validators.normalizeEmail(email)).await()
        }.getOrElse { throw it.toAppException() }
    }

    override suspend fun updateProfile(
        userId: String,
        fullName: String,
        phone: String,
        address: String,
        purok: String,
    ) {
        runCatching {
            firestore.collection("users").document(userId)
                .set(
                    mapOf(
                        "fullName" to fullName.trim(),
                        "phone" to phone.trim(),
                        "address" to address.trim(),
                        "purok" to purok.trim(),
                        "updatedAt" to com.google.firebase.Timestamp(Date(System.currentTimeMillis())),
                    ),
                    SetOptions.merge(),
                )
                .await()
        }.getOrElse { throw it.toAppException() }
    }
}
