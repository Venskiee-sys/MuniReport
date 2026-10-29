package com.munireport.app.admin.complaintmanagement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.munireport.app.core.designsystem.components.MuniDropdownField
import com.munireport.app.core.designsystem.components.MuniFilterRow
import com.munireport.app.core.designsystem.components.MuniFilterTab
import com.munireport.app.core.designsystem.components.MuniSearchField
import com.munireport.app.domain.model.ComplaintCategory

@Composable
fun AdminComplaintFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    statusFilter: AdminComplaintStatusFilter,
    onStatusFilterChange: (AdminComplaintStatusFilter) -> Unit,
    category: ComplaintCategory?,
    onCategoryChange: (ComplaintCategory?) -> Unit,
    sort: AdminComplaintSort,
    onSortChange: (AdminComplaintSort) -> Unit,
    pendingCount: Int,
    reviewingCount: Int,
    resolvedCount: Int,
    totalCount: Int,
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        MuniSearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Search ticket, title, or address",
        )
        Spacer(Modifier.height(10.dp))
        MuniFilterRow(modifier = Modifier.fillMaxWidth()) {
            MuniFilterTab(
                label = "All",
                selected = statusFilter == AdminComplaintStatusFilter.ALL,
                onClick = { onStatusFilterChange(AdminComplaintStatusFilter.ALL) },
                count = totalCount,
            )
            MuniFilterTab(
                label = "Pending",
                selected = statusFilter == AdminComplaintStatusFilter.PENDING,
                onClick = { onStatusFilterChange(AdminComplaintStatusFilter.PENDING) },
                count = pendingCount,
            )
            MuniFilterTab(
                label = "Being Reviewed",
                selected = statusFilter == AdminComplaintStatusFilter.REVIEWING,
                onClick = { onStatusFilterChange(AdminComplaintStatusFilter.REVIEWING) },
                count = reviewingCount,
            )
            MuniFilterTab(
                label = "Resolved",
                selected = statusFilter == AdminComplaintStatusFilter.RESOLVED,
                onClick = { onStatusFilterChange(AdminComplaintStatusFilter.RESOLVED) },
                count = resolvedCount,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MuniDropdownField<ComplaintCategory?>(
                label = "Category",
                selected = category,
                options = listOf<ComplaintCategory?>(null) + ComplaintCategory.entries,
                optionLabel = { it?.label ?: "All categories" },
                onSelect = onCategoryChange,
                modifier = Modifier.weight(1f),
            )
            MuniDropdownField<AdminComplaintSort>(
                label = "Sort by",
                selected = sort,
                options = AdminComplaintSort.entries,
                optionLabel = { it.label },
                onSelect = onSortChange,
                modifier = Modifier.weight(1f),
            )
        }
        if (hasActiveFilters) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Filters applied",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onClearFilters) {
                    Text(
                        text = "Clear filters",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}
