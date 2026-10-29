package com.munireport.app.gettingin.verification

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.munireport.app.core.constants.AppConstants
import com.munireport.app.core.designsystem.colors.MuniActionBlue
import com.munireport.app.core.designsystem.colors.MuniBlue50
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.colors.MuniCardPink
import com.munireport.app.core.designsystem.colors.MuniGreen50
import com.munireport.app.core.designsystem.colors.MuniGreen600
import com.munireport.app.core.designsystem.colors.MuniGuideBorder
import com.munireport.app.core.designsystem.colors.MuniGuideBorderStrong
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.colors.MuniNavy
import com.munireport.app.core.designsystem.colors.MuniSurface
import com.munireport.app.core.designsystem.colors.MuniWarmIcon
import com.munireport.app.core.designsystem.components.BackHeader
import com.munireport.app.core.designsystem.components.ErrorState
import com.munireport.app.core.designsystem.components.FormField
import com.munireport.app.core.designsystem.components.LoadingState
import com.munireport.app.core.designsystem.components.PdfCard
import com.munireport.app.core.designsystem.components.PdfDropdownField
import com.munireport.app.core.designsystem.components.PdfPrimaryButton
import com.munireport.app.core.utils.displayName
import com.munireport.app.domain.model.IdType
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.VerificationStatus
import com.munireport.app.domain.repository.VerificationRepository

@Composable
fun ResidencyFormScreen(
    profile: UserProfile,
    verificationRepository: VerificationRepository,
    canCancel: Boolean,
    onCancel: () -> Unit,
    onSubmitted: () -> Unit,
    onSignOut: () -> Unit,
) {
    val viewModel: ResidencyFormViewModel = viewModel(
        factory = ResidencyFormViewModel.factory(profile, verificationRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            viewModel.onDocumentSelected(uri.toString(), context.displayName(uri))
        }
    }

    LaunchedEffect(state.submitted) {
        if (state.submitted) onSubmitted()
    }

    Scaffold(
        containerColor = MuniCanvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            BackHeader(
                title = "Residency check",
                subtitle = "Step 2 of 2: Confirm your address",
                onBack = if (canCancel) onCancel else onSignOut,
                actions = {
                    if (!canCancel) {
                        Text(
                            text = "Sign out",
                            color = MuniActionBlue,
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable(onClick = onSignOut)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingState(modifier = Modifier.padding(padding), message = "Loading your details")
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 14.dp)
                .padding(top = 10.dp, bottom = 22.dp),
        ) {
            state.loadError?.let { message ->
                ErrorState(message = message)
                Spacer(Modifier.height(9.dp))
            }

            when (state.existingStatus) {
                VerificationStatus.PENDING -> {
                    PdfCard(
                        background = MuniCardPink,
                        borderColor = MuniGuideBorder,
                        contentPadding = 11,
                    ) {
                        Text(
                            text = "Already under review",
                            color = MuniNavy,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Your residency documents are being reviewed. You do not need to submit them again.",
                            color = MuniInkMuted,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    PdfPrimaryButton(text = "Back", onClick = onCancel)
                    return@Column
                }
                VerificationStatus.APPROVED -> {
                    PdfCard(
                        background = MuniGreen50,
                        borderColor = MuniGreen600.copy(alpha = 0.35f),
                        contentPadding = 11,
                    ) {
                        Text(
                            text = "Verified resident",
                            color = MuniGreen600,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Your residency has already been approved.",
                            color = MuniInkMuted,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                        )
                    }
                    return@Column
                }
                VerificationStatus.REJECTED -> {
                    PdfCard(
                        background = MuniCardPink,
                        borderColor = MuniGuideBorderStrong,
                        contentPadding = 11,
                    ) {
                        Text(
                            text = "Please correct your submission",
                            color = MuniGuideBorderStrong,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = state.existingNote
                                ?: "The barangay office asked for a clearer document.",
                            color = MuniInkMuted,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
                else -> Unit
            }

            state.submitError?.let { message ->
                ErrorState(message = message)
                Spacer(Modifier.height(9.dp))
            }

            FormField(
                value = state.address,
                onValueChange = viewModel::onAddressChange,
                label = "House no. and street",
                placeholder = "e.g. 12 Mabini St.",
                error = state.addressError,
                enabled = !state.isSubmitting,
            )
            Spacer(Modifier.height(9.dp))
            PdfDropdownField(
                label = "Barangay",
                selected = state.purok.takeIf { it.isNotBlank() },
                options = AppConstants.MUNTINLUPA_BARANGAYS,
                optionLabel = { it },
                onSelect = viewModel::onPurokChange,
                placeholder = "Select your barangay",
                error = state.purokError,
                enabled = !state.isSubmitting,
            )
            Spacer(Modifier.height(9.dp))
            FormField(
                value = state.yearsOfResidency,
                onValueChange = viewModel::onYearsChange,
                label = "Years of residence in Muntinlupa",
                placeholder = "5",
                keyboardType = KeyboardType.Number,
                error = state.yearsError,
                enabled = !state.isSubmitting,
                helper = "Whole years only",
            )
            Spacer(Modifier.height(9.dp))
            PdfDropdownField(
                label = "Government ID",
                selected = state.idType,
                options = IdType.entries,
                optionLabel = { it.label },
                onSelect = viewModel::onIdTypeChange,
                enabled = !state.isSubmitting,
            )
            Spacer(Modifier.height(9.dp))
            FormField(
                value = state.idNumber,
                onValueChange = viewModel::onIdNumberChange,
                label = "ID number",
                placeholder = "Enter the number on your ID",
                error = state.idNumberError,
                enabled = !state.isSubmitting,
            )

            Spacer(Modifier.height(10.dp))
            UploadProofCard(
                fileName = state.documentName,
                hasDocument = state.documentUri != null,
                error = state.documentError,
                enabled = !state.isSubmitting,
                onPick = { documentPicker.launch(arrayOf("image/*", "application/pdf")) },
            )

            Spacer(Modifier.height(12.dp))
            PdfPrimaryButton(
                text = "Submit for verification",
                onClick = viewModel::submit,
                enabled = !state.isSubmitting,
                loading = state.isSubmitting,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = "An admin reviews your ID first. You can browse right away, but posting unlocks once you are verified.",
                modifier = Modifier.fillMaxWidth(),
                color = MuniInkMuted,
                fontSize = 9.sp,
                lineHeight = 12.sp,
                textAlign = TextAlign.Center,
            )
            if (canCancel) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Cancel and finish later",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !state.isSubmitting, onClick = onCancel)
                        .padding(vertical = 6.dp),
                    color = MuniActionBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun UploadProofCard(
    fileName: String?,
    hasDocument: Boolean,
    error: String?,
    enabled: Boolean,
    onPick: () -> Unit,
) {
    val shape = RoundedCornerShape(9.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MuniSurface)
            .border(
                width = 1.dp,
                color = if (error != null) MuniGuideBorderStrong else MuniGuideBorder,
                shape = shape,
            )
            .clickable(enabled = enabled, onClick = onPick)
            .padding(10.dp),
    ) {
        Text(
            text = "Proof of residency",
            color = MuniNavy,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(5.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MuniBlue50),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (hasDocument) Icons.Rounded.Badge else Icons.Rounded.UploadFile,
                    contentDescription = null,
                    tint = MuniActionBlue,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName ?: "Upload a valid ID",
                    color = MuniNavy,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    text = if (hasDocument) {
                        "Tap to replace this document."
                    } else {
                        "Must show a Muntinlupa address. Barangay ID or certificate also accepted."
                    },
                    color = MuniInkMuted,
                    fontSize = 9.5.sp,
                    lineHeight = 12.sp,
                    maxLines = 2,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (hasDocument) "Replace" else "Upload",
                color = MuniActionBlue,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        if (error != null) {
            Spacer(Modifier.height(3.dp))
            Text(
                text = error,
                color = MuniGuideBorderStrong,
                fontSize = 9.5.sp,
                lineHeight = 12.sp,
            )
        }
    }
}
