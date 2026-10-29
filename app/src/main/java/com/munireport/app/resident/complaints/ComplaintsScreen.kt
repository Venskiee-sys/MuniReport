package com.munireport.app.resident.complaints

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.colors.MuniCardPink
import com.munireport.app.core.designsystem.colors.MuniGuideBorder
import com.munireport.app.core.designsystem.colors.MuniHeroBlue
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.colors.MuniNavy
import com.munireport.app.core.designsystem.colors.MuniSurface
import com.munireport.app.core.designsystem.components.ComplaintCard
import com.munireport.app.core.designsystem.components.EmptyState
import com.munireport.app.core.designsystem.components.ErrorState
import com.munireport.app.core.designsystem.components.LoadingState
import com.munireport.app.core.designsystem.components.PageHeader
import com.munireport.app.core.designsystem.components.PdfCard
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.ComplaintRepository

@Composable
fun ComplaintsScreen(
    profile: UserProfile,
    complaintRepository: ComplaintRepository,
    onOpenComplaint: (String) -> Unit,
    initialFilter: ResidentComplaintFilter = ResidentComplaintFilter.ALL,
    title: String = "Complaints",
    subtitle: String = "Reports from you and your neighbors",
    onBack: (() -> Unit)? = null,
) {
    val viewModel: ComplaintsViewModel = viewModel(
        factory = ComplaintsViewModel.factory(profile, complaintRepository, initialFilter),
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                PageHeader(
                    title = title,
                    subtitle = subtitle,
                    emphasized = true,
                )
            }
            item {
                ComplaintFilterPanel(
                    selected = state.filter,
                    onSelect = viewModel::onFilterChange,
                )
            }

            if (state.isLoading) {
                item { LoadingState(message = "Loading community reports") }
            }
            state.errorMessage?.let { message ->
                item { ErrorState(message = message) }
            }
            if (!state.isLoading && state.complaints.isEmpty()) {
                item {
                    EmptyState(
                        title = "No reports in this view",
                        message = "Try another filter, or be the first to file a concern on your street.",
                        actionLabel = if (state.filter != ResidentComplaintFilter.ALL) {
                            "Show all reports"
                        } else {
                            null
                        },
                        onAction = if (state.filter != ResidentComplaintFilter.ALL) {
                            { viewModel.onFilterChange(ResidentComplaintFilter.ALL) }
                        } else {
                            null
                        },
                    )
                }
            }
            items(state.complaints, key = { it.id }) { complaint ->
                ComplaintCard(
                    complaint = complaint,
                    onClick = { onOpenComplaint(complaint.id) },
                    currentUserId = profile.id,
                )
            }
        }
    }
}

@Composable
private fun ComplaintFilterPanel(
    selected: ResidentComplaintFilter,
    onSelect: (ResidentComplaintFilter) -> Unit,
) {
    val filters = listOf(
        ResidentComplaintFilter.ALL,
        ResidentComplaintFilter.PENDING,
        ResidentComplaintFilter.REVIEWING,
        ResidentComplaintFilter.RESOLVED,
        ResidentComplaintFilter.MINE,
    )
    PdfCard(
        background = MuniCardPink,
        borderColor = MuniGuideBorder,
        contentPadding = 10,
    ) {
        filters.chunked(3).forEachIndexed { index, row ->
            if (index > 0) Spacer(Modifier.height(7.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { filter ->
                    FilterPill(
                        filter = filter,
                        selected = selected == filter,
                        onClick = { onSelect(filter) },
                    )
                }
                repeat(3 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    filter: ResidentComplaintFilter,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) MuniHeroBlue else MuniSurface)
            .border(
                width = 1.dp,
                color = if (selected) MuniHeroBlue else MuniGuideBorder,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = filter.label,
            color = if (selected) MuniSurface else MuniNavy,
            fontSize = 10.5.sp,
            lineHeight = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
        )
    }
}
