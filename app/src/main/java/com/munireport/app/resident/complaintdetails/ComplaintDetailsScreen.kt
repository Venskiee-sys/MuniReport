package com.munireport.app.resident.complaintdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.munireport.app.core.designsystem.colors.MuniActionBlue
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.colors.MuniCardPink
import com.munireport.app.core.designsystem.colors.MuniGuideBorder
import com.munireport.app.core.designsystem.colors.MuniInk
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.colors.MuniLineSoft
import com.munireport.app.core.designsystem.colors.MuniNavy
import com.munireport.app.core.designsystem.colors.MuniPillGray
import com.munireport.app.core.designsystem.colors.MuniSurface
import com.munireport.app.core.designsystem.colors.MuniYellow
import com.munireport.app.core.designsystem.components.BackHeader
import com.munireport.app.core.designsystem.components.ErrorState
import com.munireport.app.core.designsystem.components.LoadingState
import com.munireport.app.core.designsystem.components.PdfCard
import com.munireport.app.core.designsystem.components.StatusPill
import com.munireport.app.core.utils.DateFormats
import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintStatus
import com.munireport.app.domain.model.ComplaintUpdate
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.ComplaintRepository
import com.munireport.app.resident.comments.CommentsComposer
import com.munireport.app.resident.comments.CommentsList
import com.munireport.app.resident.comments.CommentsViewModel

@Composable
fun ComplaintDetailsScreen(
    complaintId: String,
    profile: UserProfile,
    complaintRepository: ComplaintRepository,
    onBack: () -> Unit,
) {
    val viewModel: ComplaintDetailsViewModel = viewModel(
        key = "complaint-$complaintId",
        factory = ComplaintDetailsViewModel.factory(complaintId, profile, complaintRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val commentsViewModel: CommentsViewModel = viewModel(
        key = "comments-$complaintId",
        factory = CommentsViewModel.factory(complaintId, profile, complaintRepository),
    )
    val commentsState by commentsViewModel.uiState.collectAsStateWithLifecycle()
    val complaint = state.complaint

    Scaffold(
        containerColor = MuniCanvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            BackHeader(
                title = "Complaint",
                subtitle = complaint?.let {
                    "#${it.shortTicketNumber} · ${it.category.label}"
                },
                onBack = onBack,
                actions = {
                    complaint?.let { StatusPill(status = it.status) }
                },
            )
        },
        bottomBar = {
            if (complaint != null) {
                CommentsComposer(
                    state = commentsState,
                    onDraftChange = commentsViewModel::onDraftChange,
                    onSend = commentsViewModel::sendComment,
                )
            }
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingState(
                modifier = Modifier.padding(padding),
                message = "Loading the report",
            )
            state.errorMessage != null -> ErrorState(
                message = requireNotNull(state.errorMessage),
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
            )
            complaint == null -> ErrorState(
                message = "This report is not available. It may have been removed or you no longer have access.",
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp)
                    .padding(top = 10.dp, bottom = 18.dp),
            ) {
                ComplaintPhoto(complaint = complaint)

                Spacer(Modifier.height(10.dp))
                Text(
                    text = complaint.title,
                    color = MuniNavy,
                    fontSize = 18.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = listOf(
                        complaint.locationLabel,
                        "by ${complaint.authorName}",
                        DateFormats.shortDate(complaint.createdAt),
                    ).filter { it.isNotBlank() }.joinToString(" · "),
                    color = MuniInkMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                )
                if (complaint.description.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = complaint.description,
                        color = MuniInk,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                    )
                }

                Spacer(Modifier.height(10.dp))
                StatusTimeline(
                    complaint = complaint,
                    updates = state.updates,
                )

                state.actionError?.let { message ->
                    Spacer(Modifier.height(8.dp))
                    ErrorState(message = message)
                }

                Spacer(Modifier.height(10.dp))
                FollowCard(
                    complaint = complaint,
                    isToggling = state.isTogglingFollow,
                    onToggle = viewModel::toggleFollow,
                )

                Spacer(Modifier.height(12.dp))
                CommentsList(
                    state = commentsState,
                    commentCount = complaint.commentCount,
                )
            }
        }
    }
}

@Composable
private fun ComplaintPhoto(complaint: Complaint) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .clip(shape)
            .background(MuniPillGray)
            .border(1.dp, MuniGuideBorder, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (complaint.photoUrls.isNotEmpty()) {
            AsyncImage(
                model = complaint.photoUrls.first(),
                contentDescription = "Photo attached to ${complaint.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Image,
                contentDescription = "No photo attached",
                tint = MuniInkMuted.copy(alpha = 0.55f),
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

@Composable
private fun StatusTimeline(
    complaint: Complaint,
    updates: List<ComplaintUpdate>,
) {
    val stage = when (complaint.status) {
        ComplaintStatus.PENDING -> 0
        ComplaintStatus.ACKNOWLEDGED,
        ComplaintStatus.IN_PROGRESS,
        -> 1
        ComplaintStatus.RESOLVED,
        ComplaintStatus.REJECTED,
        -> 2
    }
    val submittedAt = updates.firstOrNull { it.status == ComplaintStatus.PENDING }?.createdAt
        ?: complaint.createdAt
    val reviewAt = updates.firstOrNull {
        it.status == ComplaintStatus.ACKNOWLEDGED || it.status == ComplaintStatus.IN_PROGRESS
    }?.createdAt
    val closedAt = updates.firstOrNull {
        it.status == ComplaintStatus.RESOLVED || it.status == ComplaintStatus.REJECTED
    }?.createdAt
    val finalLabel = if (complaint.status == ComplaintStatus.REJECTED) "Closed" else "Resolved"

    PdfCard(
        background = MuniCardPink,
        borderColor = MuniGuideBorder,
        contentPadding = 10,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Status is ${complaint.status.label}" },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TimelineStage(
                label = "Submitted",
                detail = DateFormats.shortDateTime(submittedAt),
                reached = true,
                modifier = Modifier.weight(1f),
            )
            TimelineStage(
                label = "Being Reviewed",
                detail = reviewAt?.let(DateFormats::shortDateTime) ?: "Waiting",
                reached = stage >= 1,
                modifier = Modifier.weight(1f),
            )
            TimelineStage(
                label = finalLabel,
                detail = closedAt?.let(DateFormats::shortDateTime) ?: "Waiting",
                reached = stage >= 2,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TimelineStage(
    label: String,
    detail: String,
    reached: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    if (reached) MuniActionBlue else MuniLineSoft,
                ),
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = label,
            color = if (reached) MuniNavy else MuniInkMuted,
            fontSize = 9.5.sp,
            lineHeight = 12.sp,
            fontWeight = if (reached) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
        )
        Text(
            text = detail,
            color = MuniInkMuted,
            fontSize = 8.5.sp,
            lineHeight = 11.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun FollowCard(
    complaint: Complaint,
    isToggling: Boolean,
    onToggle: () -> Unit,
) {
    PdfCard(
        background = MuniCardPink,
        borderColor = MuniGuideBorder,
        contentPadding = 10,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Follow this complaint",
                    color = MuniNavy,
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Get notified on status changes and new comments. " +
                        "${complaint.followerCount} residents following.",
                    color = MuniInkMuted,
                    fontSize = 9.5.sp,
                    lineHeight = 13.sp,
                )
            }
            Spacer(Modifier.width(8.dp))
            val following = complaint.followedByMe
            Surface(
                onClick = onToggle,
                enabled = !isToggling,
                shape = RoundedCornerShape(7.dp),
                color = if (following) MuniSurface else MuniYellow,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (following) MuniGuideBorder else MuniYellow,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (following) {
                            Icons.Rounded.NotificationsActive
                        } else {
                            Icons.Rounded.NotificationsNone
                        },
                        contentDescription = null,
                        tint = MuniNavy,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (following) "Following" else "Follow",
                        color = MuniNavy,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
