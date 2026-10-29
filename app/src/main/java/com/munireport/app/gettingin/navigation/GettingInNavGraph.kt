package com.munireport.app.gettingin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.AuthRepository
import com.munireport.app.domain.repository.VerificationRepository
import com.munireport.app.gettingin.forgotpassword.ForgotPasswordScreen
import com.munireport.app.gettingin.login.LoginScreen
import com.munireport.app.gettingin.session.AuthSessionManager
import com.munireport.app.gettingin.signup.SignUpScreen
import com.munireport.app.gettingin.verification.ResidencyFormScreen
import com.munireport.app.gettingin.verification.ResidencyVerificationScreen

object GettingInRoutes {
    const val LOGIN = "gettingin/login"
    const val SIGN_UP = "gettingin/signup"
    const val FORGOT_PASSWORD = "gettingin/forgot-password"
    const val RESIDENCY_FORM = "gettingin/residency-form"
    const val VERIFICATION_STATUS = "gettingin/verification-status"
}

@Composable
fun GettingInNavGraph(
    startDestination: String,
    sessionManager: AuthSessionManager,
    authRepository: AuthRepository,
    verificationRepository: VerificationRepository,
    isDemoMode: Boolean,
    onboardingProfile: UserProfile?,
    canCancelOnboarding: Boolean,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(GettingInRoutes.LOGIN) {
            LoginScreen(
                signInUseCase = com.munireport.app.domain.usecase.SignInUseCase(authRepository),
                isDemoMode = isDemoMode,
                onNavigateToSignUp = { navController.navigate(GettingInRoutes.SIGN_UP) },
                onNavigateToForgotPassword = { navController.navigate(GettingInRoutes.FORGOT_PASSWORD) },
            )
        }
        composable(GettingInRoutes.SIGN_UP) {
            SignUpScreen(
                sessionManager = sessionManager,
                onBackToLogin = { navController.popBackStack() },
            )
        }
        composable(GettingInRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                authRepository = authRepository,
                onBackToLogin = { navController.popBackStack() },
            )
        }
        onboardingDestination(
            profile = onboardingProfile,
            verificationRepository = verificationRepository,
            canCancel = canCancelOnboarding,
            sessionManager = sessionManager,
            onSubmitted = {
                navController.navigate(GettingInRoutes.VERIFICATION_STATUS) {
                    popUpTo(GettingInRoutes.RESIDENCY_FORM) { inclusive = true }
                }
            },
        )
        composable(GettingInRoutes.VERIFICATION_STATUS) {
            val profile = onboardingProfile ?: return@composable
            ResidencyVerificationScreen(
                profile = profile,
                verificationRepository = verificationRepository,
                canCancel = canCancelOnboarding,
                onCancel = sessionManager::cancelOnboarding,
                onContinue = sessionManager::completeOnboarding,
                onSignOut = sessionManager::signOut,
            )
        }
    }
}

private fun NavGraphBuilder.onboardingDestination(
    profile: UserProfile?,
    verificationRepository: VerificationRepository,
    canCancel: Boolean,
    sessionManager: AuthSessionManager,
    onSubmitted: () -> Unit,
) {
    composable(GettingInRoutes.RESIDENCY_FORM) {
        val currentProfile = profile ?: return@composable
        ResidencyFormScreen(
            profile = currentProfile,
            verificationRepository = verificationRepository,
            canCancel = canCancel,
            onCancel = {
                if (canCancel) sessionManager.cancelOnboarding() else sessionManager.signOut()
            },
            onSubmitted = onSubmitted,
            onSignOut = sessionManager::signOut,
        )
    }
}
