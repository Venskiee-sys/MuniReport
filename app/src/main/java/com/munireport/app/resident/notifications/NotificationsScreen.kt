package com.munireport.app.resident.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.munireport.app.core.designsystem.colors.MuniActionBlue
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.components.BackHeader
import com.munireport.app.core.designsystem.components.EmptyState
import com.munireport.app.core.designsystem.components.ErrorState
import com.munireport.app.core.designsystem.components.LoadingState
import com.munireport.app.core.designsystem.components.NotificationCard
import com.munireport.app.domain.model.AppNotification
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.NotificationRepository
import java.util.Calendar

@Composable
fun NotificationsScreen(
    profile: UserProfile,
    notificationRepository: NotificationRepository,
    onOpenComplaint: (String) -> Unit,
    onOpenAccount: () -> Unit,
    onBack: () -> Unit,
) {
    val viewModel: NotificationsViewModel = viewModel(
        factory = NotificationsViewModel.factory(profile, notificationRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val hasUnread = state.notifications.any { !it.isRead }
    val startOfToday = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val today = state.notifications.filter { it.createdAt >= startOfToday }
    val earlier = state.notifications.filter { it.createdAt < startOfToday }

    Scaffold(
        containerColor = MuniCanvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            BackHeader(
                title = "Notifications",
                onBack = onBack,
                actions = {
                    if (hasUnread) {
                        Text(
                            text = if (state.isMarkingAll) "Marking…" else "Mark all read",
                            color = MuniActionBlue,
                            fontSize = 10.5.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable(enabled = !state.isMarkingAll) {
                                    viewModel.markAllRead()
                                }
                                .padding(horizontal = 8.dp, vertical = 7.dp),
                        )
                    }
                },
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 26.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.isLoading) {
                    item { LoadingState(message = "Loading notifications") }
                }
                state.errorMessage?.let { message ->
                    item { ErrorState(message = message, onRetry = viewModel::dismissError) }
                }
                if (!state.isLoading && state.notifications.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No notifications yet",
                            message = "Updates on the reports you file or follow will appear here.",
                        )
                    }
                }
                if (today.isNotEmpty()) {
                    item { GroupLabel("Today") }
                    items(today, key = { it.id }) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = {
                                onNotificationClick(
                                    notification = notification,
                                    markRead = viewModel::markRead,
                                    onOpenComplaint = onOpenComplaint,
                                    onOpenAccount = onOpenAccount,
                                )
                            },
                        )
                    }
                }
                if (earlier.isNotEmpty()) {
                    item { GroupLabel("Earlier") }
                    items(earlier, key = { it.id }) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = {
                                onNotificationClick(
                                    notification = notification,
                                    markRead = viewModel::markRead,
                                    onOpenComplaint = onOpenComplaint,
                                    onOpenAccount = onOpenAccount,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun onNotificationClick(
    notification: AppNotification,
    markRead: (String) -> Unit,
    onOpenComplaint: (String) -> Unit,
    onOpenAccount: () -> Unit,
) {
    if (!notification.isRead) markRead(notification.id)
    notification.complaintId?.let(onOpenComplaint) ?: onOpenAccount()
}

@Composable
private fun GroupLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = MuniInkMuted,
        fontSize = 9.5.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 1.dp, start = 2.dp),
    )
}
