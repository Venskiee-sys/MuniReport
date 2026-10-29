package com.munireport.app.domain.usecase

import com.munireport.app.core.common.AppException
import com.munireport.app.core.validation.Validators
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.AuthRepository

class SignInUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<UserProfile> = runCatching {
        if (Validators.email(email) != null || password.isBlank()) {
            throw AppException.InvalidInput("Check your email address and password.")
        }
        authRepository.signIn(Validators.normalizeEmail(email), password)
    }
}
