package com.munireport.app.admin.verification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.HomeWork
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.munireport.app.core.designsystem.components.Avatar
import com.munireport.app.core.designsystem.components.DestructiveButton
import com.munireport.app.core.designsystem.components.DocumentPreview
import com.munireport.app.core.designsystem.components.EmptyState
import com.munireport.app.core.designsystem.components.ErrorBanner
import com.munireport.app.core.designsystem.components.InlineNotice
import com.munireport.app.core.designsystem.components.LoadingState
import com.munireport.app.core.designsystem.components.MuniCard
import com.munireport.app.core.designsystem.components.MuniDivider
import com.munireport.app.core.designsystem.components.MuniInfoRow
import com.munireport.app.core.designsystem.components.MuniTextField
import com.munireport.app.core.designsystem.components.MuniTopBar
import com.munireport.app.core.designsystem.components.NoticeTone
import com.munireport.app.core.designsystem.components.PrimaryButton
import com.munireport.app.core.designsystem.components.SectionHeader
import com.munireport.app.core.designsystem.components.VerificationBadge
import com.munireport.app.core.utils.DateFormats
import com.munireport.app.domain.model.ResidencyVerification
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.VerificationStatus
import com.munireport.app.domain.repository.VerificationRepository
import com.munireport.app.domain.usecase.ReviewVerificationUseCase

private enum class ReviewDecision {
    APPROVE,
    REJECT,
}

@Composable
fun VerificationDetailsScreen(
    verificationId: String,
    adminProfile: UserProfile,
    verificationRepository: VerificationRepository,
    reviewVerificationUseCase: ReviewVerificationUseCase,
    onBack: () -> Unit,
    onCompleted: () -> Unit,
) {
    val viewModel: VerificationDetailsViewModel = viewModel(
        factory = VerificationDetailsViewModel.factory(
            verificationId = verificationId,
            adminProfile = adminProfile,
            verificationRepository = verificationRepository,
            reviewVerificationUseCase = reviewVerificationUseCase,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var decision by remember { mutableStateOf<ReviewDecision?>(null) }
    var note by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.completed) {
        if (state.completed) onCompleted()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { MuniTopBar(title = "Verification review", onBack = onBack) },
    ) { padding ->
        when {
            state.isLoading -> LoadingState(
                modifier = Modifier.padding(padding),
                message = "Loading the verification record",
            )
            state.errorMessage != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
            ) {
                ErrorBanner(message = requireNotNull(state.errorMessage))
            }
            state.verification == null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                EmptyState(
                    title = "Verification not found",
                    message = "This submission may have been removed.",
                )
            }
            else -> {
                val verification = requireNotNull(state.verification)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 26.dp),
                ) {
                    VerificationHeader(verification)
                    Spacer(Modifier.height(14.dp))
                    SectionHeader(title = "Applicant details", eyebrow = "Residency record")
                    Spacer(Modifier.height(10.dp))
                    MuniCard {
                        MuniInfoRow(
                            icon = Icons.Rounded.Email,
                            label = "Email",
                            value = verification.applicantEmail,
                        )
                        Spacer(Modifier.height(10.dp))
                        MuniInfoRow(
                            icon = Icons.Rounded.Phone,
                            label = "Mobile",
                            value = verification.applicantPhone.ifBlank { "Not provided" },
                        )
                        Spacer(Modifier.height(10.dp))
                        MuniInfoRow(
                            icon = Icons.Rounded.HomeWork,
                            label = "Address",
                            value = "${verification.address}, ${verification.purok}",
                        )
                        Spacer(Modifier.height(10.dp))
                        MuniInfoRow(
                            icon = Icons.Rounded.Badge,
                            label = "Presented ID",
                            value = "${verification.idType.label} · ${verification.idNumber}",
                        )
                        Spacer(Modifier.height(10.dp))
                        MuniInfoRow(
                            icon = Icons.Rounded.Schedule,
                            label = "Residency",
                            value = "${verification.yearsOfResidency} years",
                        )
                        Spacer(Modifier.height(10.dp))
                        MuniInfoRow(
                            icon = Icons.Rounded.CalendarToday,
                            label = "Submitted",
                            value = DateFormats.dateTime(verification.submittedAt),
                        )
                    }

                    Spacer(Modifier.height(18.dp))
                    SectionHeader(title = "Submitted document", eyebrow = "Identity check")
                    Spacer(Modifier.height(10.dp))
                    MuniCard {
                        DocumentPreview(
                            uri = verification.documentUrl,
                            fileName = verification.documentName,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = verification.documentName ?: "No document attached",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(Modifier.height(18.dp))
                    state.actionError?.let { message ->
                        ErrorBanner(message = message)
                        Spacer(Modifier.height(10.dp))
                    }
                    if (verification.status == VerificationStatus.PENDING) {
                        SectionHeader(title = "Decision", eyebrow = "Barangay review")
                        Spacer(Modifier.height(10.dp))
                        MuniCard {
                            Text(
                                text = "Approve only after the name and address match the barangay record. Rejection notes are shown to the resident.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                PrimaryButton(
                                    text = "Approve",
                                    onClick = {
                                        note = ""
                                        decision = ReviewDecision.APPROVE
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !state.isSubmitting,
                                    loading = state.isSubmitting,
                                )
                                DestructiveButton(
                                    text = "Reject",
                                    onClick = {
                                        note = ""
                                        decision = ReviewDecision.REJECT
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !state.isSubmitting,
                                )
                            }
                        }
                    } else {
                        ReviewedOutcome(verification)
                    }
                }
            }
        }
    }

    val activeDecision = decision
    if (activeDecision != null) {
        val isApprove = activeDecision == ReviewDecision.APPROVE
        AlertDialog(
            onDismissRequest = { if (!state.isSubmitting) decision = null },
            title = {
                Text(if (isApprove) "Approve this resident?" else "Reject this submission?")
            },
            text = {
                Column {
                    Text(
                        text = if (isApprove) {
                            "The resident will be able to file reports for the barangay."
                        } else {
                            "Add a short note explaining what the resident needs to correct. They will see this note."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    MuniTextField(
                        value = note,
                        onValueChange = { note = it.take(300) },
                        label = if (isApprove) "Review note (optional)" else "Reason for rejection",
                        placeholder = if (isApprove) "Details matched the residency record" else "Explain what needs correction",
                        singleLine = false,
                        minLines = 3,
                        enabled = !state.isSubmitting,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        decision = null
                        if (isApprove) viewModel.approve(note) else viewModel.reject(note)
                    },
                    enabled = !state.isSubmitting && (isApprove || note.isNotBlank()),
                ) {
                    Text(
                        text = if (isApprove) "Approve" else "Reject",
                        color = if (isApprove) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { decision = null },
                    enabled = !state.isSubmitting,
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun VerificationHeader(verification: ResidencyVerification) {
    val initials = verification.applicantName
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "R" }
    MuniCard {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Avatar(initials = initials, size = 54.dp)
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = verification.applicantName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${verification.purok} · ${verification.yearsOfResidency} years",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(7.dp))
                VerificationBadge(status = verification.status)
            }
        }
        Spacer(Modifier.height(12.dp))
        MuniDivider()
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Reference ${verification.id.take(12).uppercase()}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ReviewedOutcome(verification: ResidencyVerification) {
    SectionHeader(title = "Review outcome", eyebrow = "Decision recorded")
    Spacer(Modifier.height(10.dp))
    if (verification.status == VerificationStatus.APPROVED) {
        InlineNotice(
            title = "Residency approved",
            message = "This resident can now file reports and comment in the community.",
            tone = NoticeTone.SUCCESS,
        )
    } else if (verification.status == VerificationStatus.REJECTED) {
        InlineNotice(
            title = "Submission rejected",
            message = verification.reviewNote ?: "No rejection note was recorded.",
            tone = NoticeTone.WARNING,
        )
    }
    Spacer(Modifier.height(10.dp))
    MuniCard {
        MuniInfoRow(
            icon = Icons.Rounded.CalendarToday,
            label = "Reviewed",
            value = verification.reviewedAt?.let(DateFormats::dateTime) ?: "Date not recorded",
        )
        Spacer(Modifier.height(10.dp))
        MuniInfoRow(
            icon = Icons.Rounded.Badge,
            label = "Reviewer",
            value = verification.reviewerName ?: "Barangay staff",
        )
        if (!verification.reviewNote.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            MuniInfoRow(
                icon = Icons.Rounded.Email,
                label = "Note",
                value = verification.reviewNote,
            )
        }
    }
}
