package com.munireport.app.data.repository

import com.munireport.app.core.common.AppException
import com.munireport.app.core.validation.Validators
import com.munireport.app.data.local.DemoMuniStore
import com.munireport.app.domain.model.AppNotification
import com.munireport.app.domain.model.NotificationType
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.UserRole
import com.munireport.app.domain.model.VerificationStatus
import com.munireport.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class DemoAuthRepository(private val store: DemoMuniStore) : AuthRepository {

    override fun observeAuthUserId(): Flow<String?> = store.currentUserId

    override fun observeProfile(userId: String): Flow<UserProfile?> =
        store.users.map { it[userId] }

    override suspend fun getProfile(userId: String): UserProfile? = store.profile(userId)

    override suspend fun resolveAuthorizedRole(profile: UserProfile): UserRole = profile.role

    override suspend fun signIn(email: String, password: String): UserProfile {
        val normalized = Validators.normalizeEmail(email)
        val account = store.accounts.value[normalized] ?: throw AppException.InvalidCredentials()
        if (account.password != password) throw AppException.InvalidCredentials()
        store.setCurrentUser(account.profile.id)
        return account.profile
    }

    override suspend fun signUp(
        fullName: String,
        email: String,
        phone: String,
        address: String,
        purok: String,
        password: String,
    ): UserProfile {
        val normalized = Validators.normalizeEmail(email)
        if (store.accounts.value.containsKey(normalized)) throw AppException.EmailInUse()

        val now = System.currentTimeMillis()
        val profile = UserProfile(
            id = "res-${UUID.randomUUID().toString().take(8)}",
            fullName = fullName.trim(),
            email = normalized,
            phone = phone.trim(),
            address = address.trim(),
            purok = purok.trim(),
            role = UserRole.RESIDENT,
            verificationStatus = VerificationStatus.NOT_SUBMITTED,
            createdAt = now,
            updatedAt = now,
        )
        store.createAccount(profile, password)
        store.notify(
            profile.id,
            AppNotification(
                id = "ntf-${UUID.randomUUID()}",
                recipientId = profile.id,
                title = "Welcome to MuniReport",
                message = "Complete your residency verification to start filing reports.",
                type = NotificationType.SYSTEM,
                createdAt = now,
            ),
        )
        return profile
    }

    override suspend fun signOut() {
        store.setCurrentUser(null)
    }

    override suspend fun sendPasswordReset(email: String) {
        // The demo does not send email. The UI still shows the same neutral confirmation.
    }

    override suspend fun updateProfile(
        userId: String,
        fullName: String,
        phone: String,
        address: String,
        purok: String,
    ) {
        val current = store.profile(userId) ?: throw AppException.NotFound()
        store.updateProfile(
            current.copy(
                fullName = fullName.trim(),
                phone = phone.trim(),
                address = address.trim(),
                purok = purok.trim(),
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }
}
