package com.munireport.app.admin.verification

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.munireport.app.core.designsystem.components.BackHeader
import com.munireport.app.core.designsystem.components.EmptyState
import com.munireport.app.core.designsystem.components.ErrorBanner
import com.munireport.app.core.designsystem.components.LoadingState
import com.munireport.app.core.designsystem.components.MuniSearchField
import com.munireport.app.core.designsystem.components.ScreenIntro
import com.munireport.app.domain.repository.VerificationRepository

@Composable
fun VerificationQueueScreen(
    verificationRepository: VerificationRepository,
    onOpenVerification: (String) -> Unit,
    onBack: () -> Unit,
) {
    val viewModel: VerificationQueueViewModel = viewModel(
        factory = VerificationQueueViewModel.factory(verificationRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            BackHeader(
                title = "Verification queue",
                subtitle = "Residency checks",
                onBack = onBack,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ScreenIntro(
                    eyebrow = "Residency checks",
                    title = "Review who is reporting.",
                    subtitle = "Match each submission against the barangay residency record before approving access to report filing.",
                )
            }
            item {
                MuniSearchField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = "Search name, email, or purok",
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    VerificationQueueFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = state.filter == filter,
                            onClick = { viewModel.onFilterChange(filter) },
                            label = { Text(filter.label) },
                        )
                    }
                }
            }
            if (state.isLoading) {
                item { LoadingState(message = "Loading verification requests") }
            }
            state.errorMessage?.let { message ->
                item { ErrorBanner(message = message) }
            }
            if (!state.isLoading && state.verifications.isEmpty()) {
                item {
                    EmptyState(
                        title = when (state.filter) {
                            VerificationQueueFilter.PENDING -> "No pending verifications"
                            else -> "No verification records here"
                        },
                        message = if (state.filter == VerificationQueueFilter.PENDING) {
                            "New residency submissions will appear in this queue."
                        } else {
                            "Try another filter or search term."
                        },
                    )
                }
            }
            items(state.verifications, key = { it.id }) { verification ->
                VerificationRequestCard(
                    verification = verification,
                    onClick = { onOpenVerification(verification.id) },
                )
            }
        }
    }
}
