package com.munireport.app.admin.complaintmanagement

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.munireport.app.core.designsystem.colors.MuniActionBlue
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.colors.MuniCardPink
import com.munireport.app.core.designsystem.colors.MuniFieldPink
import com.munireport.app.core.designsystem.colors.MuniGuideBorder
import com.munireport.app.core.designsystem.colors.MuniInk
import com.munireport.app.core.designsystem.colors.MuniInkFaint
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.colors.MuniLineSoft
import com.munireport.app.core.designsystem.colors.MuniNavy
import com.munireport.app.core.designsystem.colors.MuniSurface
import com.munireport.app.core.designsystem.components.AdminHeader
import com.munireport.app.core.designsystem.components.ComplaintRow
import com.munireport.app.core.designsystem.components.EmptyState
import com.munireport.app.core.designsystem.components.ErrorState
import com.munireport.app.core.designsystem.components.LoadingState
import com.munireport.app.domain.model.ComplaintCategory
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.ComplaintRepository

@Composable
fun ComplaintManagementScreen(
    profile: UserProfile,
    complaintRepository: ComplaintRepository,
    onOpenComplaint: (String) -> Unit,
) {
    val viewModel: ComplaintManagementViewModel = viewModel(
        factory = ComplaintManagementViewModel.factory(profile, complaintRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MuniCanvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                AdminHeader(
                    title = "Complaint Management",
                    eyebrow = null,
                    content = {
                        SearchField(
                            query = state.query,
                            onQueryChange = viewModel::onQueryChange,
                        )
                    },
                )
                ManagementTabs(
                    selected = state.statusFilter,
                    onSelect = viewModel::onStatusFilterChange,
                )
                FilterRow(
                    category = state.category,
                    onCategoryChange = viewModel::onCategoryChange,
                    barangay = state.barangay,
                    barangays = state.barangays,
                    onBarangayChange = viewModel::onBarangayChange,
                    sort = state.sort,
                    onSortChange = viewModel::onSortChange,
                )
                ResultCount(count = state.complaints.size)
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.isLoading) {
                item { LoadingState(message = "Loading complaints") }
            }
            state.errorMessage?.let { message ->
                item { ErrorState(message = message) }
            }
            if (!state.isLoading && state.complaints.isEmpty()) {
                item {
                    EmptyState(
                        title = "No complaints match these filters",
                        message = "Clear a filter or search for another ticket number.",
                        actionLabel = if (state.hasActiveFilters) "Clear filters" else null,
                        onAction = if (state.hasActiveFilters) viewModel::clearFilters else null,
                    )
                }
            }
            itemsIndexed(
                items = state.complaints,
                key = { _, complaint -> complaint.id },
            ) { index, complaint ->
                ComplaintRow(
                    complaint = complaint,
                    onClick = { onOpenComplaint(complaint.id) },
                    highlighted = index == 0,
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val shape = RoundedCornerShape(9.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 11.dp)
            .clip(shape)
            .background(MuniFieldPink)
            .border(1.dp, MuniGuideBorder, shape)
            .padding(horizontal = 11.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = MuniInkMuted,
            modifier = Modifier.size(17.dp),
        )
        Spacer(Modifier.width(7.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    text = "Search ID, title, or street",
                    color = MuniInkFaint,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = TextStyle(
                    color = MuniNavy,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                ),
                cursorBrush = SolidColor(MuniActionBlue),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )
        }
        if (query.isNotEmpty()) {
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Clear search",
                tint = MuniInkMuted,
                modifier = Modifier
                    .size(17.dp)
                    .clickable { onQueryChange("") },
            )
        }
    }
}

@Composable
private fun ManagementTabs(
    selected: AdminComplaintStatusFilter,
    onSelect: (AdminComplaintStatusFilter) -> Unit,
) {
    val tabs = AdminComplaintStatusFilter.entries
    Column(modifier = Modifier.fillMaxWidth().background(MuniSurface)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEach { tab ->
                val isSelected = tab == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .semantics { this.selected = isSelected }
                        .clickable(role = Role.Tab) { onSelect(tab) }
                        .padding(top = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = tab.label,
                        color = if (isSelected) MuniActionBlue else MuniInkMuted,
                        fontSize = 9.sp,
                        lineHeight = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 3.dp),
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(if (isSelected) MuniActionBlue else MuniSurface),
                    )
                }
            }
        }
        HorizontalDivider(color = MuniGuideBorder.copy(alpha = 0.45f), thickness = 1.dp)
    }
}

@Composable
private fun FilterRow(
    category: ComplaintCategory?,
    onCategoryChange: (ComplaintCategory?) -> Unit,
    barangay: String?,
    barangays: List<String>,
    onBarangayChange: (String?) -> Unit,
    sort: AdminComplaintSort,
    onSortChange: (AdminComplaintSort) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MuniSurface)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        DropdownFilter(
            label = category?.label ?: "Category",
            isPlaceholder = category == null,
            options = listOf<ComplaintCategory?>(null) + ComplaintCategory.entries,
            optionLabel = { it?.label ?: "All categories" },
            onSelect = onCategoryChange,
            modifier = Modifier.weight(1f),
        )
        DropdownFilter(
            label = barangay ?: "Barangay",
            isPlaceholder = barangay == null,
            options = listOf<String?>(null) + barangays,
            optionLabel = { it ?: "All barangays" },
            onSelect = onBarangayChange,
            modifier = Modifier.weight(1f),
        )
        DropdownFilter(
            label = sort.label,
            isPlaceholder = false,
            options = AdminComplaintSort.entries,
            optionLabel = { it.label },
            onSelect = onSortChange,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun <T> DropdownFilter(
    label: String,
    isPlaceholder: Boolean,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(7.dp)
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MuniSurface)
                .border(1.dp, MuniGuideBorder, shape)
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                color = if (isPlaceholder) MuniInkMuted else MuniNavy,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(2.dp))
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = MuniInkMuted,
                modifier = Modifier.size(15.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MuniSurface,
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = optionLabel(option),
                            color = MuniNavy,
                            fontSize = 12.sp,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

@Composable
private fun ResultCount(count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MuniCanvas)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(MuniCardPink)
                .border(1.dp, MuniGuideBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Showing $count ${if (count == 1) "complaint" else "complaints"}",
                color = MuniInk,
                fontSize = 9.5.sp,
                lineHeight = 12.sp,
            )
        }
    }
}
