package com.munireport.app.admin.checkcomplaint

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.munireport.app.core.designsystem.colors.MuniActionBlue
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.colors.MuniCardPink
import com.munireport.app.core.designsystem.colors.MuniGuideBorder
import com.munireport.app.core.designsystem.colors.MuniGuideBorderStrong
import com.munireport.app.core.designsystem.colors.MuniInk
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.colors.MuniLineSoft
import com.munireport.app.core.designsystem.colors.MuniNavy
import com.munireport.app.core.designsystem.colors.MuniPillGray
import com.munireport.app.core.designsystem.colors.MuniSurface
import com.munireport.app.core.designsystem.colors.StatusResolvedFg
import com.munireport.app.core.designsystem.components.AdminHeader
import com.munireport.app.core.designsystem.components.ErrorState
import com.munireport.app.core.designsystem.components.FormField
import com.munireport.app.core.designsystem.components.LoadingState
import com.munireport.app.core.designsystem.components.MuniMapPicker
import com.munireport.app.core.designsystem.components.PdfCard
import com.munireport.app.core.designsystem.components.PdfPrimaryButton
import com.munireport.app.core.designsystem.components.PdfSectionTitle
import com.munireport.app.core.utils.DateFormats
import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintStatus
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.ComplaintRepository

private val adminStatusOptions = listOf(
    ComplaintStatus.PENDING,
    ComplaintStatus.IN_PROGRESS,
    ComplaintStatus.RESOLVED,
)

private fun ComplaintStatus.adminLabel(): String = when (this) {
    ComplaintStatus.PENDING -> "Pending"
    ComplaintStatus.ACKNOWLEDGED,
    ComplaintStatus.IN_PROGRESS,
    -> "Being Reviewed"
    ComplaintStatus.RESOLVED -> "Resolved"
    ComplaintStatus.REJECTED -> "Closed"
}

@Composable
fun CheckComplaintScreen(
    complaintId: String,
    adminProfile: UserProfile,
    complaintRepository: ComplaintRepository,
    onBack: () -> Unit,
) {
    val viewModel: CheckComplaintViewModel = viewModel(
        key = "check-complaint-$complaintId",
        factory = CheckComplaintViewModel.factory(
            complaintId = complaintId,
            adminProfile = adminProfile,
            complaintRepository = complaintRepository,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val complaint = state.complaint

    Scaffold(
        containerColor = MuniCanvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AdminHeader(
                title = "Check Complaint",
                subtitle = complaint?.let {
                    "#${it.shortTicketNumber} · submitted ${DateFormats.shortDateTime(it.createdAt)}"
                },
                eyebrow = null,
                onBack = onBack,
            )
        },
        bottomBar = {
            if (complaint != null) {
                SaveUpdateBar(
                    followerCount = complaint.followerCount,
                    enabled = state.note.isNotBlank() && !state.isSubmitting,
                    loading = state.isSubmitting,
                    onSave = viewModel::postUpdate,
                )
            }
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingState(
                modifier = Modifier.padding(padding),
                message = "Loading the complaint",
            )
            state.errorMessage != null -> ErrorState(
                message = requireNotNull(state.errorMessage),
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
            )
            complaint == null -> ErrorState(
                message = "This complaint is no longer available.",
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
            )
            else -> {
                val selected = state.selectedStatus ?: complaint.status
                val selectedUiStatus = when (selected) {
                    ComplaintStatus.ACKNOWLEDGED -> ComplaintStatus.IN_PROGRESS
                    else -> selected
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = 14.dp)
                        .padding(top = 10.dp, bottom = 16.dp),
                ) {
                    PhotoAndMap(complaint = complaint)

                    Spacer(Modifier.height(8.dp))
                    PdfCard(
                        background = MuniSurface,
                        borderColor = MuniGuideBorder,
                        contentPadding = 10,
                    ) {
                        Text(
                            text = complaint.title,
                            color = MuniNavy,
                            fontSize = 15.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        if (complaint.description.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = complaint.description,
                                color = MuniInk,
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp,
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    ComplaintDetailsCard(complaint = complaint)

                    Spacer(Modifier.height(10.dp))
                    PdfSectionTitle(title = "Update status")
                    Spacer(Modifier.height(6.dp))
                    StatusSelector(
                        selected = selectedUiStatus,
                        onSelect = viewModel::onStatusChange,
                    )

                    Spacer(Modifier.height(10.dp))
                    FormField(
                        value = state.note,
                        onValueChange = viewModel::onNoteChange,
                        label = "Admin comment (posted to the thread)",
                        placeholder = "Forwarded to the City Environment office for declogging.",
                        enabled = !state.isSubmitting,
                        singleLine = false,
                        minLines = 4,
                    )

                    state.actionSuccess?.let { message ->
                        Spacer(Modifier.height(8.dp))
                        PdfCard(
                            background = MuniSurface,
                            borderColor = StatusResolvedFg.copy(alpha = 0.4f),
                            contentPadding = 9,
                        ) {
                            Text(
                                text = message,
                                color = StatusResolvedFg,
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp,
                            )
                        }
                    }
                    state.actionError?.let { message ->
                        Spacer(Modifier.height(8.dp))
                        ErrorState(message = message)
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoAndMap(complaint: Complaint) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(122.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val shape = RoundedCornerShape(10.dp)
        Box(
            modifier = Modifier
                .weight(1.7f)
                .fillMaxHeight()
                .clip(shape)
                .background(MuniPillGray)
                .border(1.dp, MuniGuideBorder, shape),
            contentAlignment = Alignment.Center,
        ) {
            if (complaint.photoUrls.isNotEmpty()) {
                AsyncImage(
                    model = complaint.photoUrls.first(),
                    contentDescription = "Reporter's photo for ${complaint.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Image,
                    contentDescription = "No photo attached",
                    tint = MuniInkMuted.copy(alpha = 0.55f),
                    modifier = Modifier.size(40.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(shape)
                .border(1.dp, MuniGuideBorder, shape),
        ) {
            MuniMapPicker(
                latitude = complaint.latitude,
                longitude = complaint.longitude,
                onCoordinateChange = { _, _ -> },
                modifier = Modifier.fillMaxSize(),
                interactive = false,
                height = 122.dp,
            )
        }
    }
}

@Composable
private fun ComplaintDetailsCard(complaint: Complaint) {
    PdfCard(
        background = MuniCardPink,
        borderColor = MuniGuideBorder,
        contentPadding = 10,
    ) {
        DetailRow(label = "Category", value = complaint.category.label)
        DetailDivider()
        DetailRow(label = "Location", value = complaint.locationLabel.ifBlank { "Not provided" })
        DetailDivider()
        DetailRow(
            label = "Reported by",
            value = complaint.authorName + if (complaint.authorPurok.isNotBlank()) {
                " (verified)"
            } else {
                ""
            },
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(7.dp))
                .background(MuniSurface)
                .border(1.dp, MuniGuideBorder, RoundedCornerShape(7.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Followers / Comments",
                color = MuniInkMuted,
                fontSize = 9.5.sp,
                lineHeight = 12.sp,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${complaint.followerCount} following · ${complaint.commentCount} comments",
                color = MuniActionBlue,
                fontSize = 9.5.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            color = MuniInkMuted,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            modifier = Modifier.width(92.dp),
        )
        Text(
            text = value,
            color = MuniNavy,
            fontSize = 10.5.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(color = MuniGuideBorder.copy(alpha = 0.35f), thickness = 1.dp)
}

@Composable
private fun StatusSelector(
    selected: ComplaintStatus,
    onSelect: (ComplaintStatus) -> Unit,
) {
    PdfCard(
        background = MuniCardPink,
        borderColor = MuniGuideBorder,
        contentPadding = 7,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            adminStatusOptions.forEach { status ->
                val isSelected = status == selected
                val shape = RoundedCornerShape(7.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(shape)
                        .background(MuniSurface)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MuniActionBlue else MuniGuideBorder.copy(alpha = 0.55f),
                            shape = shape,
                        )
                        .semantics { this.selected = isSelected }
                        .clickable(role = Role.RadioButton) { onSelect(status) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = status.adminLabel(),
                        color = if (isSelected) MuniActionBlue else MuniNavy,
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveUpdateBar(
    followerCount: Int,
    enabled: Boolean,
    loading: Boolean,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MuniCanvas)
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(11.dp))
                .background(MuniSurface)
                .border(1.dp, MuniGuideBorder, RoundedCornerShape(11.dp))
                .padding(8.dp),
        ) {
            PdfPrimaryButton(
                text = "Save update",
                onClick = onSave,
                enabled = enabled,
                loading = loading,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Notifies the reporter and all $followerCount followers",
                modifier = Modifier.fillMaxWidth(),
                color = MuniInkMuted,
                fontSize = 8.5.sp,
                lineHeight = 11.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
