package com.munireport.app.gettingin.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.munireport.app.core.designsystem.components.DocumentPreview
import com.munireport.app.core.designsystem.components.EmptyState
import com.munireport.app.core.designsystem.components.ErrorBanner
import com.munireport.app.core.designsystem.components.InlineNotice
import com.munireport.app.core.designsystem.components.LoadingState
import com.munireport.app.core.designsystem.components.MuniCard
import com.munireport.app.core.designsystem.components.MuniDivider
import com.munireport.app.core.designsystem.components.MuniTextButton
import com.munireport.app.core.designsystem.components.MuniTopBar
import com.munireport.app.core.designsystem.components.NoticeTone
import com.munireport.app.core.designsystem.components.PrimaryButton
import com.munireport.app.core.designsystem.components.SecondaryButton
import com.munireport.app.core.designsystem.components.VerificationBadge
import com.munireport.app.core.designsystem.components.verificationAccent
import com.munireport.app.core.utils.DateFormats
import com.munireport.app.domain.model.ResidencyVerification
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.VerificationStatus
import com.munireport.app.domain.repository.VerificationRepository

@Composable
fun ResidencyVerificationScreen(
    profile: UserProfile,
    verificationRepository: VerificationRepository,
    canCancel: Boolean,
    onCancel: () -> Unit,
    onContinue: () -> Unit,
    onSignOut: () -> Unit,
) {
    val viewModel: VerificationStatusViewModel = viewModel(
        factory = VerificationStatusViewModel.factory(profile, verificationRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MuniTopBar(
                title = "Verification status",
                onBack = if (canCancel) onCancel else null,
                actions = {
                    MuniTextButton(text = "Sign out", onClick = onSignOut)
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 26.dp),
        ) {
            when {
                state.isLoading -> LoadingState(message = "Checking your verification")
                state.errorMessage != null -> ErrorBanner(message = requireNotNull(state.errorMessage))
                state.verification == null -> {
                    EmptyState(
                        title = "No verification on file",
                        message = "Submit your residency details so the barangay can review your account.",
                    )
                    SecondaryButton(
                        text = "Back",
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                else -> {
                    val verification = requireNotNull(state.verification)
                    VerificationSummary(verification)
                    Spacer(Modifier.height(18.dp))
                    VerificationTimeline(verification)
                    Spacer(Modifier.height(18.dp))
                    if (verification.status == VerificationStatus.REJECTED) {
                        InlineNotice(
                            title = "What to do next",
                            message = verification.reviewNote ?: "Submit a clearer photo of your ID from your profile.",
                            tone = NoticeTone.WARNING,
                        )
                        Spacer(Modifier.height(14.dp))
                    }
                    if (!verification.documentUrl.isNullOrBlank()) {
                        MuniCard {
                            Text(
                                text = "Submitted document",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Spacer(Modifier.height(10.dp))
                            DocumentPreview(
                                uri = verification.documentUrl,
                                fileName = verification.documentName,
                            )
                            if (verification.documentName != null) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = verification.documentName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                    }
                    PrimaryButton(
                        text = "Continue to MuniReport",
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    MuniTextButton(
                        text = "Sign out",
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun VerificationSummary(verification: ResidencyVerification) {
    val accent = verificationAccent(verification.status)
    val icon = when (verification.status) {
        VerificationStatus.APPROVED -> Icons.Rounded.TaskAlt
        VerificationStatus.REJECTED -> Icons.Rounded.WarningAmber
        else -> Icons.Rounded.HourglassTop
    }
    val heading = when (verification.status) {
        VerificationStatus.APPROVED -> "Your residency is verified."
        VerificationStatus.REJECTED -> "Your documents need another look."
        else -> "Your documents are with the barangay."
    }
    val message = when (verification.status) {
        VerificationStatus.APPROVED -> "You can now file reports and comment on community concerns."
        VerificationStatus.REJECTED -> "Review the note below, then submit a corrected document from your profile."
        else -> "The barangay office usually reviews residency submissions within two working days. You can continue to the app while you wait."
    }
    MuniCard {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = heading,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                VerificationBadge(status = verification.status)
            }
        }
        Spacer(Modifier.height(16.dp))
        MuniDivider()
        Spacer(Modifier.height(10.dp))
        SummaryRow("Applicant", verification.applicantName)
        SummaryRow("Address", "${verification.address}, ${verification.purok}")
        SummaryRow("ID presented", verification.idType.label)
        SummaryRow("Submitted", DateFormats.dateTime(verification.submittedAt))
        if (verification.reviewedAt != null) {
            SummaryRow("Reviewed", DateFormats.dateTime(verification.reviewedAt))
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Composable
private fun VerificationTimeline(verification: ResidencyVerification) {
    MuniCard {
        Text(
            text = "Verification progress",
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(Modifier.height(12.dp))
        TimelineStep(
            title = "Documents submitted",
            detail = DateFormats.dateTime(verification.submittedAt),
            done = true,
            active = false,
        )
        TimelineStep(
            title = "Barangay review",
            detail = if (verification.status == VerificationStatus.PENDING) {
                "In review"
            } else {
                verification.reviewerName ?: "Reviewed by barangay staff"
            },
            done = verification.status != VerificationStatus.PENDING,
            active = verification.status == VerificationStatus.PENDING,
        )
        TimelineStep(
            title = "Decision",
            detail = when (verification.status) {
                VerificationStatus.APPROVED -> "Approved"
                VerificationStatus.REJECTED -> "Needs correction"
                else -> "Waiting for review"
            },
            done = verification.status in listOf(VerificationStatus.APPROVED, VerificationStatus.REJECTED),
            active = false,
            last = true,
        )
    }
}

@Composable
private fun TimelineStep(
    title: String,
    detail: String,
    done: Boolean,
    active: Boolean,
    last: Boolean = false,
) {
    val color = when {
        done -> MaterialTheme.colorScheme.primary
        active -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
    Row {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (done) Icons.Rounded.TaskAlt else Icons.Rounded.HourglassTop,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp),
            )
            if (!last) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(width = 1.dp, height = 34.dp)
                        .then(Modifier.padding(vertical = 2.dp))
                        .background(color.copy(alpha = 0.35f)),
                )
            }
        }
        Spacer(Modifier.size(10.dp))
        Column(modifier = Modifier.padding(top = 1.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
