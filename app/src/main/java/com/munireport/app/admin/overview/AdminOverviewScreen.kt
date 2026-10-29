package com.munireport.app.admin.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.munireport.app.core.designsystem.colors.MuniActionBlue
import com.munireport.app.core.designsystem.colors.MuniBlue50
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.colors.MuniCardPink
import com.munireport.app.core.designsystem.colors.MuniCream
import com.munireport.app.core.designsystem.colors.MuniGreen50
import com.munireport.app.core.designsystem.colors.MuniGreen600
import com.munireport.app.core.designsystem.colors.MuniGuideBorder
import com.munireport.app.core.designsystem.colors.MuniHeroBlue
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.colors.MuniLineSoft
import com.munireport.app.core.designsystem.colors.MuniNavy
import com.munireport.app.core.designsystem.colors.MuniSurface
import com.munireport.app.core.designsystem.colors.MuniWarmIcon
import com.munireport.app.core.designsystem.colors.MuniYellow
import com.munireport.app.core.designsystem.components.AdminHeader
import com.munireport.app.core.designsystem.components.ErrorState
import com.munireport.app.core.designsystem.components.LoadingState
import com.munireport.app.core.designsystem.components.PdfCard
import com.munireport.app.core.designsystem.components.PdfSectionTitle
import com.munireport.app.core.utils.DateFormats
import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintCategory
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.ComplaintRepository
import com.munireport.app.domain.repository.NotificationRepository
import com.munireport.app.domain.repository.VerificationRepository

@Composable
fun AdminOverviewScreen(
    profile: UserProfile,
    complaintRepository: ComplaintRepository,
    verificationRepository: VerificationRepository,
    notificationRepository: NotificationRepository,
    onOpenNotifications: () -> Unit,
    onOpenVerificationQueue: () -> Unit,
    onOpenComplaints: () -> Unit,
    onOpenComplaint: (String) -> Unit,
) {
    val viewModel: AdminOverviewViewModel = viewModel(
        factory = AdminOverviewViewModel.factory(
            profile = profile,
            complaintRepository = complaintRepository,
            verificationRepository = verificationRepository,
            notificationRepository = notificationRepository,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MuniCanvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AdminHeader(
                title = "Overview",
                subtitle = "${DateFormats.weekdayDate(System.currentTimeMillis())} · sample data",
                actions = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MuniGuideBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(onClick = onOpenNotifications, modifier = Modifier.size(40.dp)) {
                            BadgedBox(
                                badge = {
                                    if (state.unreadNotifications > 0) {
                                        Badge(
                                            containerColor = MuniYellow,
                                            contentColor = MuniNavy,
                                        ) {
                                            Text(
                                                text = state.unreadNotifications.toString(),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsNone,
                                    contentDescription = "Notifications, ${state.unreadNotifications} unread",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OverviewMetric(
                        label = "Total reports",
                        value = state.totalReports.toString(),
                        helper = when {
                            state.weeklyChange > 0 -> "+${state.weeklyChange} this week"
                            state.weeklyChange < 0 -> "${state.weeklyChange} this week"
                            else -> "No change this week"
                        },
                        helperColor = MuniActionBlue,
                        container = MuniCardPink,
                        border = MuniGuideBorder,
                        onClick = onOpenComplaints,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    OverviewMetric(
                        label = "Pending",
                        value = state.pendingReports.toString(),
                        helper = if (state.olderPendingReports > 0) {
                            "${state.olderPendingReports} older than 3 days"
                        } else {
                            "Within service window"
                        },
                        helperColor = MuniWarmIcon,
                        container = MuniCream,
                        border = MuniCream,
                        onClick = onOpenComplaints,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OverviewMetric(
                        label = "Being reviewed",
                        value = state.reviewingReports.toString(),
                        helper = null,
                        helperColor = MuniInkMuted,
                        container = MuniBlue50,
                        border = MuniBlue50,
                        onClick = onOpenComplaints,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    OverviewMetric(
                        label = "Resolved",
                        value = state.resolvedReports.toString(),
                        helper = null,
                        helperColor = MuniGreen600,
                        container = MuniGreen50,
                        border = MuniGreen50,
                        onClick = onOpenComplaints,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }

            if (state.pendingVerifications > 0) {
                item {
                    VerificationBanner(
                        pendingCount = state.pendingVerifications,
                        onReview = onOpenVerificationQueue,
                    )
                }
            }

            if (state.isLoading) {
                item { LoadingState(message = "Loading today's operations snapshot") }
            }
            state.errorMessage?.let { message ->
                item { ErrorState(message = message) }
            }

            item {
                PdfSectionTitle(
                    title = "Needs attention",
                    actionLabel = if (state.attentionComplaints.isNotEmpty()) "View all" else null,
                    onAction = onOpenComplaints,
                )
            }

            if (state.attentionComplaints.isEmpty() && !state.isLoading) {
                item {
                    PdfCard(
                        background = MuniGreen50,
                        borderColor = MuniGreen50,
                        contentPadding = 12,
                    ) {
                        Text(
                            text = "No reports need attention right now. New concerns will appear here.",
                            color = MuniGreen600,
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp,
                        )
                    }
                }
            } else {
                itemsIndexed(
                    items = state.attentionComplaints,
                    key = { _, complaint -> "attention-${complaint.id}" },
                ) { index, complaint ->
                    AttentionRow(
                        complaint = complaint,
                        highlighted = index == 0,
                        onClick = { onOpenComplaint(complaint.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewMetric(
    label: String,
    value: String,
    helper: String?,
    helperColor: Color,
    container: Color,
    border: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PdfCard(
        modifier = modifier,
        onClick = onClick,
        background = container,
        borderColor = border,
        contentPadding = 10,
    ) {
        Text(
            text = label,
            color = MuniInkMuted,
            fontSize = 9.5.sp,
            lineHeight = 12.sp,
        )
        Spacer(Modifier.height(1.dp))
        Text(
            text = value,
            color = MuniNavy,
            fontSize = 24.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
        )
        if (helper != null) {
            Text(
                text = helper,
                color = helperColor,
                fontSize = 8.5.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun VerificationBanner(
    pendingCount: Int,
    onReview: () -> Unit,
) {
    PdfCard(
        background = MuniSurface,
        borderColor = MuniGuideBorder,
        contentPadding = 9,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color(0xFFE9ECF9)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = MuniActionBlue,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$pendingCount ${if (pendingCount == 1) "resident" else "residents"} to verify",
                    color = MuniNavy,
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Check IDs before they can post",
                    color = MuniInkMuted,
                    fontSize = 9.5.sp,
                    lineHeight = 12.sp,
                )
            }
            Spacer(Modifier.width(6.dp))
            Surface(
                onClick = onReview,
                shape = RoundedCornerShape(7.dp),
                color = MuniHeroBlue,
            ) {
                Text(
                    text = "Review",
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp),
                    color = Color.White,
                    fontSize = 10.5.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun AttentionRow(
    complaint: Complaint,
    highlighted: Boolean,
    onClick: () -> Unit,
) {
    PdfCard(
        onClick = onClick,
        background = if (highlighted) MuniCardPink else MuniSurface,
        borderColor = if (highlighted) MuniGuideBorder else MuniLineSoft,
        contentPadding = 10,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${complaint.shortTicketNumber} · " +
                        "${complaint.category.shortAdminLabel()} · ${complaint.authorPurok}",
                    color = MuniInkMuted,
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = complaint.title,
                    color = MuniNavy,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = buildString {
                        append("${complaint.followerCount} following · ")
                        append(
                            if (complaint.commentCount == 0) {
                                "no comments"
                            } else {
                                "${complaint.commentCount} comments"
                            },
                        )
                        append(" · ${DateFormats.relative(complaint.createdAt)}")
                    },
                    color = MuniInkMuted,
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "Open complaint",
                tint = MuniInkMuted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private fun ComplaintCategory.shortAdminLabel(): String = when (this) {
    ComplaintCategory.WASTE_MANAGEMENT -> "Garbage"
    ComplaintCategory.FLOODING -> "Flooding"
    ComplaintCategory.DRAINAGE -> "Drainage"
    ComplaintCategory.ROADS_AND_SIDEWALKS -> "Roads"
    ComplaintCategory.STREETLIGHTS -> "Streetlights"
    ComplaintCategory.OTHER -> "Others"
}
