package com.munireport.app.resident.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.munireport.app.core.designsystem.colors.MuniActionBlue
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.colors.MuniCardPink
import com.munireport.app.core.designsystem.colors.MuniGuideBorder
import com.munireport.app.core.designsystem.colors.MuniHeroBlue
import com.munireport.app.core.designsystem.colors.MuniInk
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.colors.MuniNavy
import com.munireport.app.core.designsystem.colors.MuniRed
import com.munireport.app.core.designsystem.colors.MuniSurface
import com.munireport.app.core.designsystem.colors.MuniYellow
import com.munireport.app.core.designsystem.components.ComplaintCard
import com.munireport.app.core.designsystem.components.EmptyState
import com.munireport.app.core.designsystem.components.ErrorState
import com.munireport.app.core.designsystem.components.LoadingState
import com.munireport.app.core.designsystem.components.PdfCard
import com.munireport.app.core.designsystem.components.PdfSectionTitle
import com.munireport.app.core.designsystem.components.SystemBarIcons
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.VerificationStatus
import com.munireport.app.domain.repository.ComplaintRepository
import com.munireport.app.domain.repository.NotificationRepository
import java.util.Calendar

@Composable
fun HomeScreen(
    profile: UserProfile,
    complaintRepository: ComplaintRepository,
    notificationRepository: NotificationRepository,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenReport: () -> Unit,
    onBrowseComplaints: () -> Unit,
    onOpenComplaint: (String) -> Unit,
    onStartVerification: () -> Unit,
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(profile, complaintRepository, notificationRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MuniCanvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                HomeHeader(
                    profile = profile,
                    unreadCount = state.unreadNotifications,
                    onOpenNotifications = onOpenNotifications,
                    onOpenProfile = onOpenProfile,
                )
            }

            if (profile.verificationStatus != VerificationStatus.APPROVED) {
                item {
                    UnverifiedNotice(
                        status = profile.verificationStatus,
                        onStartVerification = onStartVerification,
                    )
                }
            }

            item {
                ReportHero(
                    canReport = profile.isVerified,
                    onReport = if (profile.isVerified) onOpenReport else onStartVerification,
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    HomeStat(
                        value = state.myReportsCount.toString(),
                        label = "My reports",
                        modifier = Modifier.weight(1f),
                    )
                    HomeStat(
                        value = state.followingCount.toString(),
                        label = "Following",
                        modifier = Modifier.weight(1f),
                    )
                    HomeStat(
                        value = state.resolvedCount.toString(),
                        label = "Resolved",
                        valueColor = com.munireport.app.core.designsystem.colors.StatusResolvedFg,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (state.isLoading) {
                item { LoadingState(message = "Loading barangay activity") }
            }
            state.errorMessage?.let { message ->
                item { ErrorState(message = message) }
            }

            item {
                Spacer(Modifier.height(2.dp))
                PdfSectionTitle(
                    title = "Happening near you",
                    actionLabel = "See all",
                    onAction = onBrowseComplaints,
                )
            }

            if (!state.isLoading && state.recentComplaints.isEmpty()) {
                item {
                    EmptyState(
                        title = "No community reports yet",
                        message = "When neighbors file concerns, they will appear here.",
                        actionLabel = "Browse reports",
                        onAction = onBrowseComplaints,
                    )
                }
            } else {
                items(state.recentComplaints, key = { it.id }) { complaint ->
                    ComplaintCard(
                        complaint = complaint,
                        onClick = { onOpenComplaint(complaint.id) },
                        currentUserId = profile.id,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    profile: UserProfile,
    unreadCount: Int,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    SystemBarIcons(useDarkStatusIcons = true)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(11.dp))
                .background(MuniSurface)
                .border(1.dp, MuniGuideBorder, RoundedCornerShape(11.dp))
                .padding(start = 13.dp, end = 9.dp, top = 9.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenProfile),
            ) {
                Text(
                    text = "${greeting()},",
                    color = MuniInk,
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp,
                )
                Text(
                    text = profile.firstName,
                    color = MuniNavy,
                    fontSize = 24.sp,
                    lineHeight = 29.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Box {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MuniCardPink)
                        .border(1.dp, MuniGuideBorder, RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenNotifications)
                        .semantics {
                            contentDescription = if (unreadCount > 0) {
                                "Notifications, $unreadCount unread"
                            } else {
                                "Notifications"
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsNone,
                        contentDescription = null,
                        tint = MuniNavy,
                        modifier = Modifier.size(22.dp),
                    )
                }
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 5.dp, end = 5.dp)
                            .size(8.dp)
                            .background(MuniRed, CircleShape),
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MuniCardPink)
                .border(1.dp, MuniGuideBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = MuniNavy,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (profile.isVerified) "Verified resident" else profile.verificationLabel(),
                color = MuniNavy,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(13.dp)
                    .background(MuniGuideBorder.copy(alpha = 0.7f)),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Brgy. ${profile.purok.ifBlank { "—" }}",
                color = MuniNavy,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun ReportHero(
    canReport: Boolean,
    onReport: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MuniHeroBlue)
            .padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        Text(
            text = if (canReport) {
                "See a problem in your barangay?"
            } else {
                "Verify your address to start reporting."
            },
            color = Color.White,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (canReport) {
                "Snap a photo, pin the spot, and send it straight to the city."
            } else {
                "Only verified Muntinlupa residents can post reports."
            },
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 11.5.sp,
            lineHeight = 16.sp,
        )
        Spacer(Modifier.height(11.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(7.dp))
                .background(MuniYellow)
                .clickable(onClick = onReport)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (canReport) Icons.Rounded.Add else Icons.Rounded.Lock,
                contentDescription = null,
                tint = MuniNavy,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = if (canReport) "Report a concern" else "Verify residency",
                color = MuniNavy,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun HomeStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MuniNavy,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(MuniCardPink)
            .border(1.dp, MuniGuideBorder.copy(alpha = 0.8f), RoundedCornerShape(9.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
    ) {
        Text(
            text = value,
            color = valueColor,
            fontSize = 23.sp,
            lineHeight = 27.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            color = MuniInkMuted,
            fontSize = 9.5.sp,
            lineHeight = 13.sp,
        )
    }
}

@Composable
private fun UnverifiedNotice(
    status: VerificationStatus,
    onStartVerification: () -> Unit,
) {
    PdfCard(
        background = com.munireport.app.core.designsystem.colors.MuniCream,
        borderColor = MuniGuideBorder,
        contentPadding = 11,
        onClick = onStartVerification,
    ) {
        Text(
            text = if (status == VerificationStatus.PENDING) {
                "Residency under review. Posting unlocks once approved."
            } else {
                "Complete your residency check to post reports."
            },
            color = MuniInk,
            fontSize = 10.5.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun UserProfile.verificationLabel(): String = when (verificationStatus) {
    VerificationStatus.APPROVED -> "Verified resident"
    VerificationStatus.PENDING -> "Residency under review"
    VerificationStatus.REJECTED -> "Verification needs attention"
    VerificationStatus.NOT_SUBMITTED -> "Unverified resident"
}

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Magandang araw"
        hour < 18 -> "Magandang hapon"
        else -> "Magandang gabi"
    }
}
