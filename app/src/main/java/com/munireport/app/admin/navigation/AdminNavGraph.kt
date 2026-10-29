package com.munireport.app.admin.navigation

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
import com.munireport.app.admin.account.AdminAccountScreen
import com.munireport.app.admin.analytics.AnalyticsScreen
import com.munireport.app.admin.checkcomplaint.CheckComplaintScreen
import com.munireport.app.admin.complaintmanagement.ComplaintManagementScreen
import com.munireport.app.admin.overview.AdminOverviewScreen
import com.munireport.app.admin.verification.VerificationDetailsScreen
import com.munireport.app.admin.verification.VerificationQueueScreen
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.components.AdminBottomNavigation
import com.munireport.app.core.designsystem.components.AdminNavTab
import com.munireport.app.core.designsystem.components.EmptyState
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.AuthRepository
import com.munireport.app.domain.repository.ComplaintRepository
import com.munireport.app.domain.repository.NotificationRepository
import com.munireport.app.domain.repository.VerificationRepository
import com.munireport.app.domain.usecase.ReviewVerificationUseCase
import com.munireport.app.resident.notifications.NotificationsScreen

object AdminRoutes {
    const val OVERVIEW = "admin/overview"
    const val VERIFICATIONS = "admin/verifications"
    const val VERIFICATION_DETAILS = "admin/verification/{verificationId}"
    const val COMPLAINTS = "admin/complaints"
    const val COMPLAINT_DETAILS = "admin/complaint/{complaintId}"
    const val ANALYTICS = "admin/analytics"
    const val ACCOUNT = "admin/account"
    const val NOTIFICATIONS = "admin/notifications"

    fun verificationDetails(verificationId: String): String =
        "admin/verification/${Uri.encode(verificationId)}"

    fun complaintDetails(complaintId: String): String =
        "admin/complaint/${Uri.encode(complaintId)}"
}

private val tabRoutes = setOf(
    AdminRoutes.OVERVIEW,
    AdminRoutes.COMPLAINTS,
    AdminRoutes.ANALYTICS,
    AdminRoutes.ACCOUNT,
)

@Composable
fun AdminNavGraph(
    profile: UserProfile,
    authRepository: AuthRepository,
    complaintRepository: ComplaintRepository,
    notificationRepository: NotificationRepository,
    verificationRepository: VerificationRepository,
    reviewVerificationUseCase: ReviewVerificationUseCase,
    onSignOut: () -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in tabRoutes
    val selectedTab = when (currentRoute) {
        AdminRoutes.COMPLAINTS -> AdminNavTab.COMPLAINTS
        AdminRoutes.ANALYTICS -> AdminNavTab.ANALYTICS
        AdminRoutes.ACCOUNT -> AdminNavTab.ACCOUNT
        else -> AdminNavTab.OVERVIEW
    }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(AdminRoutes.OVERVIEW) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = MuniCanvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                AdminBottomNavigation(
                    selected = selectedTab,
                    onSelect = { tab ->
                        navigateToTab(
                            when (tab) {
                                AdminNavTab.OVERVIEW -> AdminRoutes.OVERVIEW
                                AdminNavTab.COMPLAINTS -> AdminRoutes.COMPLAINTS
                                AdminNavTab.ANALYTICS -> AdminRoutes.ANALYTICS
                                AdminNavTab.ACCOUNT -> AdminRoutes.ACCOUNT
                            },
                        )
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AdminRoutes.OVERVIEW,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
        ) {
            composable(AdminRoutes.OVERVIEW) {
                AdminOverviewScreen(
                    profile = profile,
                    complaintRepository = complaintRepository,
                    verificationRepository = verificationRepository,
                    notificationRepository = notificationRepository,
                    onOpenNotifications = {
                        navController.navigate(AdminRoutes.NOTIFICATIONS)
                    },
                    onOpenVerificationQueue = { navController.navigate(AdminRoutes.VERIFICATIONS) },
                    onOpenComplaints = { navigateToTab(AdminRoutes.COMPLAINTS) },
                    onOpenComplaint = { complaintId ->
                        navController.navigate(AdminRoutes.complaintDetails(complaintId))
                    },
                )
            }
            composable(AdminRoutes.NOTIFICATIONS) {
                NotificationsScreen(
                    profile = profile,
                    notificationRepository = notificationRepository,
                    onOpenComplaint = { complaintId ->
                        navController.navigate(AdminRoutes.complaintDetails(complaintId))
                    },
                    onOpenAccount = { navigateToTab(AdminRoutes.ACCOUNT) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(AdminRoutes.VERIFICATIONS) {
                VerificationQueueScreen(
                    verificationRepository = verificationRepository,
                    onOpenVerification = { verificationId ->
                        navController.navigate(AdminRoutes.verificationDetails(verificationId))
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = AdminRoutes.VERIFICATION_DETAILS,
                arguments = listOf(
                    navArgument("verificationId") { type = NavType.StringType },
                ),
            ) { entry ->
                val verificationId = entry.arguments?.getString("verificationId")
                if (verificationId == null) {
                    EmptyState(
                        title = "Verification not found",
                        message = "The selected verification record is no longer available.",
                    )
                } else {
                    VerificationDetailsScreen(
                        verificationId = verificationId,
                        adminProfile = profile,
                        verificationRepository = verificationRepository,
                        reviewVerificationUseCase = reviewVerificationUseCase,
                        onBack = { navController.popBackStack() },
                        onCompleted = { navController.popBackStack() },
                    )
                }
            }
            composable(AdminRoutes.COMPLAINTS) {
                ComplaintManagementScreen(
                    profile = profile,
                    complaintRepository = complaintRepository,
                    onOpenComplaint = { complaintId ->
                        navController.navigate(AdminRoutes.complaintDetails(complaintId))
                    },
                )
            }
            composable(
                route = AdminRoutes.COMPLAINT_DETAILS,
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
                    CheckComplaintScreen(
                        complaintId = complaintId,
                        adminProfile = profile,
                        complaintRepository = complaintRepository,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
            composable(AdminRoutes.ANALYTICS) {
                AnalyticsScreen(
                    profile = profile,
                    complaintRepository = complaintRepository,
                    verificationRepository = verificationRepository,
                )
            }
            composable(AdminRoutes.ACCOUNT) {
                AdminAccountScreen(
                    profile = profile,
                    authRepository = authRepository,
                    onSignOut = onSignOut,
                )
            }
        }
    }
}
