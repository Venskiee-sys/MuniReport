package com.munireport.app.domain.repository

import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthUserId(): Flow<String?>
    fun observeProfile(userId: String): Flow<UserProfile?>
    suspend fun getProfile(userId: String): UserProfile?
    suspend fun resolveAuthorizedRole(profile: UserProfile): UserRole
    suspend fun signIn(email: String, password: String): UserProfile
    suspend fun signUp(
        fullName: String,
        email: String,
        phone: String,
        address: String,
        purok: String,
        password: String,
    ): UserProfile
    suspend fun signOut()
    suspend fun sendPasswordReset(email: String)
    suspend fun updateProfile(
        userId: String,
        fullName: String,
        phone: String,
        address: String,
        purok: String,
    )
}
