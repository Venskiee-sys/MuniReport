package com.munireport.app.resident.navigation

import android.net.Uri
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.components.EmptyState
import com.munireport.app.core.designsystem.components.ResidentBottomNavigation
import com.munireport.app.core.designsystem.components.ResidentNavTab
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.AuthRepository
import com.munireport.app.domain.repository.ComplaintRepository
import com.munireport.app.domain.repository.NotificationRepository
import com.munireport.app.domain.repository.VerificationRepository
import com.munireport.app.domain.usecase.CreateComplaintUseCase
import com.munireport.app.resident.complaintdetails.ComplaintDetailsScreen
import com.munireport.app.resident.complaints.ComplaintsScreen
import com.munireport.app.resident.home.HomeScreen
import com.munireport.app.resident.notifications.NotificationsScreen
import com.munireport.app.resident.profile.EditProfileScreen
import com.munireport.app.resident.profile.ProfileScreen
import com.munireport.app.resident.report.ReportScreen

object ResidentRoutes {
    const val HOME = "resident/home"
    const val COMPLAINTS = "resident/complaints"
    const val REPORT = "resident/report"
    const val NOTIFICATIONS = "resident/notifications"
    const val PROFILE = "resident/profile"
    const val EDIT_PROFILE = "resident/profile/edit"
    const val COMPLAINT_DETAILS = "resident/complaint/{complaintId}"

    fun complaintDetails(complaintId: String): String =
        "resident/complaint/${Uri.encode(complaintId)}"
}

private val tabRoutes = setOf(
    ResidentRoutes.HOME,
    ResidentRoutes.COMPLAINTS,
    ResidentRoutes.PROFILE,
)

@Composable
fun ResidentNavGraph(
    profile: UserProfile,
    authRepository: AuthRepository,
    complaintRepository: ComplaintRepository,
    notificationRepository: NotificationRepository,
    verificationRepository: VerificationRepository,
    createComplaintUseCase: CreateComplaintUseCase,
    onSignOut: () -> Unit,
    onStartVerification: () -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in tabRoutes
    val selectedTab = when (currentRoute) {
        ResidentRoutes.COMPLAINTS -> ResidentNavTab.COMPLAINTS
        ResidentRoutes.PROFILE -> ResidentNavTab.PROFILE
        else -> ResidentNavTab.HOME
    }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(ResidentRoutes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = MuniCanvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                ResidentBottomNavigation(
                    selected = selectedTab,
                    onSelect = { tab ->
                        navigateToTab(
                            when (tab) {
                                ResidentNavTab.HOME -> ResidentRoutes.HOME
                                ResidentNavTab.REPORT -> ResidentRoutes.REPORT
                                ResidentNavTab.COMPLAINTS -> ResidentRoutes.COMPLAINTS
                                ResidentNavTab.PROFILE -> ResidentRoutes.PROFILE
                            },
                        )
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ResidentRoutes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
        ) {
            composable(ResidentRoutes.HOME) {
                HomeScreen(
                    profile = profile,
                    complaintRepository = complaintRepository,
                    notificationRepository = notificationRepository,
                    onOpenNotifications = { navController.navigate(ResidentRoutes.NOTIFICATIONS) },
                    onOpenProfile = { navigateToTab(ResidentRoutes.PROFILE) },
                    onOpenReport = { navController.navigate(ResidentRoutes.REPORT) },
                    onBrowseComplaints = { navigateToTab(ResidentRoutes.COMPLAINTS) },
                    onOpenComplaint = { complaintId ->
                        navController.navigate(ResidentRoutes.complaintDetails(complaintId))
                    },
                    onStartVerification = onStartVerification,
                )
            }
            composable(ResidentRoutes.COMPLAINTS) {
                ComplaintsScreen(
                    profile = profile,
                    complaintRepository = complaintRepository,
                    onOpenComplaint = { complaintId ->
                        navController.navigate(ResidentRoutes.complaintDetails(complaintId))
                    },
                )
            }
            composable(ResidentRoutes.REPORT) {
                ReportScreen(
                    profile = profile,
                    createComplaintUseCase = createComplaintUseCase,
                    onBack = { navController.popBackStack() },
                    onCreated = { complaintId ->
                        navController.navigate(ResidentRoutes.complaintDetails(complaintId)) {
                            popUpTo(ResidentRoutes.REPORT) { inclusive = true }
                        }
                    },
                    onStartVerification = onStartVerification,
                )
            }
            composable(ResidentRoutes.NOTIFICATIONS) {
                NotificationsScreen(
                    profile = profile,
                    notificationRepository = notificationRepository,
                    onOpenComplaint = { complaintId ->
                        navController.navigate(ResidentRoutes.complaintDetails(complaintId))
                    },
                    onOpenAccount = { navigateToTab(ResidentRoutes.PROFILE) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(ResidentRoutes.PROFILE) {
                ProfileScreen(
                    profile = profile,
                    authRepository = authRepository,
                    complaintRepository = complaintRepository,
                    onEditProfile = { navController.navigate(ResidentRoutes.EDIT_PROFILE) },
                    onOpenNotifications = { navController.navigate(ResidentRoutes.NOTIFICATIONS) },
                    onOpenComplaint = { complaintId ->
                        navController.navigate(ResidentRoutes.complaintDetails(complaintId))
                    },
                    onStartVerification = onStartVerification,
                    onSignOut = onSignOut,
                )
            }
            composable(ResidentRoutes.EDIT_PROFILE) {
                EditProfileScreen(
                    profile = profile,
                    authRepository = authRepository,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
            composable(
                route = ResidentRoutes.COMPLAINT_DETAILS,
                arguments = listOf(
                    navArgument("complaintId") { type = NavType.StringType },
                ),
            ) { entry ->
                val complaintId = entry.arguments?.getString("complaintId")
                if (complaintId == null) {
                    EmptyState(
                        title = "Report not found",
                        message = "The selected report is no longer available.",
                    )
                } else {
                    ComplaintDetailsScreen(
                        complaintId = complaintId,
                        profile = profile,
                        complaintRepository = complaintRepository,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
