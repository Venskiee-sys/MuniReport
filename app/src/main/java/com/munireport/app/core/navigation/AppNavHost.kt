package com.munireport.app.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.munireport.app.admin.navigation.AdminNavGraph
import com.munireport.app.core.designsystem.components.MuniLogo
import com.munireport.app.di.AppContainer
import com.munireport.app.gettingin.navigation.GettingInNavGraph
import com.munireport.app.gettingin.navigation.GettingInRoutes
import com.munireport.app.gettingin.session.SessionState
import com.munireport.app.domain.model.UserRole
import com.munireport.app.resident.navigation.ResidentNavGraph

/**
 * The only top-level navigation switch in MuniReport.
 *
 * A role graph is composed only when the signed-in session has that authorized
 * role. Resident sessions never mount the administrator destinations, so there
 * is no route to guess or deep-link into. Firestore and Storage rules enforce the
 * same boundary on the server.
 */
@Composable
fun AppNavHost(container: AppContainer) {
    val session by container.sessionManager.state.collectAsStateWithLifecycle()

    when (val current = session) {
        SessionState.Restoring -> SplashScreen()

        SessionState.SignedOut -> {
            GettingInNavGraph(
                startDestination = GettingInRoutes.LOGIN,
                sessionManager = container.sessionManager,
                authRepository = container.authRepository,
                verificationRepository = container.verificationRepository,
                isDemoMode = container.isDemoMode,
                onboardingProfile = null,
                canCancelOnboarding = false,
            )
        }

        is SessionState.Onboarding -> {
            GettingInNavGraph(
                startDestination = GettingInRoutes.RESIDENCY_FORM,
                sessionManager = container.sessionManager,
                authRepository = container.authRepository,
                verificationRepository = container.verificationRepository,
                isDemoMode = container.isDemoMode,
                onboardingProfile = current.profile,
                canCancelOnboarding = current.canCancel,
            )
        }

        is SessionState.SignedIn -> {
            when (current.profile.role) {
                UserRole.ADMIN -> {
                    AdminNavGraph(
                        profile = current.profile,
                        authRepository = container.authRepository,
                        complaintRepository = container.complaintRepository,
                        notificationRepository = container.notificationRepository,
                        verificationRepository = container.verificationRepository,
                        reviewVerificationUseCase = container.reviewVerificationUseCase,
                        onSignOut = container.sessionManager::signOut,
                    )
                }
                UserRole.RESIDENT -> {
                    ResidentNavGraph(
                        profile = current.profile,
                        authRepository = container.authRepository,
                        complaintRepository = container.complaintRepository,
                        notificationRepository = container.notificationRepository,
                        verificationRepository = container.verificationRepository,
                        createComplaintUseCase = container.createComplaintUseCase,
                        onSignOut = container.sessionManager::signOut,
                        onStartVerification = container.sessionManager::beginVerification,
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            MuniLogo(showWordmark = true)
            Spacer(Modifier.height(28.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Checking your session",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
