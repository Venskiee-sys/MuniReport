package com.munireport.app.gettingin.session

import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.UserRole
import com.munireport.app.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object Restoring : SessionState
    data object SignedOut : SessionState
    data class Onboarding(
        val profile: UserProfile,
        val canCancel: Boolean,
    ) : SessionState
    data class SignedIn(val profile: UserProfile) : SessionState
}

/**
 * Single source of truth for authentication and the authorized role.
 *
 * The role is resolved by the repository (Firebase custom claims in production).
 * Screens never decide whether a user is an administrator.
 */
class AuthSessionManager(
    private val authRepository: AuthRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _state = MutableStateFlow<SessionState>(SessionState.Restoring)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    private var onboardingActive = false
    private var profileJob: Job? = null

    init {
        scope.launch {
            authRepository.observeAuthUserId()
                .flatMapLatest { userId ->
                    if (userId == null) {
                        flowOf(null)
                    } else {
                        authRepository.observeProfile(userId)
                    }
                }
                .catch {
                    _state.value = SessionState.SignedOut
                }
                .collect { profile ->
                    if (onboardingActive) return@collect
                    if (profile == null) {
                        _state.value = SessionState.SignedOut
                    } else {
                        publishSignedIn(profile)
                    }
                }
        }
    }

    suspend fun signIn(email: String, password: String): Result<UserProfile> = runCatching {
        val profile = authRepository.signIn(email, password)
        publishSignedIn(profile)
        _state.value.let { state ->
            if (state is SessionState.SignedIn) state.profile else profile
        }
    }.onFailure {
        onboardingActive = false
        _state.value = SessionState.SignedOut
    }

    suspend fun signUp(
        fullName: String,
        email: String,
        phone: String,
        address: String,
        purok: String,
        password: String,
    ): Result<UserProfile> = runCatching {
        onboardingActive = true
        val profile = authRepository.signUp(fullName, email, phone, address, purok, password)
        val resident = profile.copy(role = UserRole.RESIDENT)
        _state.value = SessionState.Onboarding(profile = resident, canCancel = false)
        resident
    }.onFailure {
        onboardingActive = false
        _state.value = SessionState.SignedOut
    }

    fun beginVerification() {
        val signedIn = _state.value as? SessionState.SignedIn ?: return
        if (signedIn.profile.role != UserRole.RESIDENT) return
        onboardingActive = true
        _state.value = SessionState.Onboarding(
            profile = signedIn.profile,
            canCancel = true,
        )
    }

    fun completeOnboarding() {
        val onboarding = _state.value as? SessionState.Onboarding ?: return
        profileJob?.cancel()
        profileJob = scope.launch {
            val profile = authRepository.getProfile(onboarding.profile.id) ?: onboarding.profile
            onboardingActive = false
            publishSignedIn(profile)
        }
    }

    fun cancelOnboarding() {
        val onboarding = _state.value as? SessionState.Onboarding ?: return
        if (!onboarding.canCancel) {
            signOut()
            return
        }
        profileJob?.cancel()
        profileJob = scope.launch {
            val profile = authRepository.getProfile(onboarding.profile.id) ?: onboarding.profile
            onboardingActive = false
            publishSignedIn(profile)
        }
    }

    fun signOut() {
        onboardingActive = false
        _state.value = SessionState.SignedOut
        scope.launch {
            runCatching { authRepository.signOut() }
        }
    }

    private suspend fun publishSignedIn(profile: UserProfile) {
        val authorizedRole = authRepository.resolveAuthorizedRole(profile)
        _state.value = SessionState.SignedIn(profile.copy(role = authorizedRole))
    }
}
