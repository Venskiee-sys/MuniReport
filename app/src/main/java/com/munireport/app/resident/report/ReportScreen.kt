package com.munireport.app.resident.report

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.colors.MuniNavy
import com.munireport.app.core.designsystem.colors.MuniSurface
import com.munireport.app.core.designsystem.components.BackHeader
import com.munireport.app.core.designsystem.components.CategorySelector
import com.munireport.app.core.designsystem.components.ErrorState
import com.munireport.app.core.designsystem.components.FormField
import com.munireport.app.core.designsystem.components.LocationPicker
import com.munireport.app.core.designsystem.components.PdfCard
import com.munireport.app.core.designsystem.components.PdfOutlinedButton
import com.munireport.app.core.designsystem.components.PdfPrimaryButton
import com.munireport.app.core.permissions.rememberLocationPermissionRequest
import com.munireport.app.core.utils.LocationProvider
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.usecase.CreateComplaintUseCase
import kotlinx.coroutines.launch

@Composable
fun ReportScreen(
    profile: UserProfile,
    createComplaintUseCase: CreateComplaintUseCase,
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    onStartVerification: () -> Unit,
) {
    val viewModel: ReportViewModel = viewModel(
        factory = ReportViewModel.factory(profile, createComplaintUseCase),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val locationProvider = remember(context) { LocationProvider(context) }
    val scope = rememberCoroutineScope()
    var locating by remember { mutableStateOf(false) }
    var locationMessage by remember { mutableStateOf<String?>(null) }

    val requestLocation = rememberLocationPermissionRequest { granted ->
        if (!granted) {
            locationMessage = "Location permission was not granted. You can still drop the pin on the map."
        } else {
            scope.launch {
                locating = true
                val coordinates = locationProvider.lastKnownCoordinates()
                locating = false
                if (coordinates != null) {
                    viewModel.onCoordinates(coordinates.first, coordinates.second)
                    locationMessage = "Current location added to the pin."
                } else {
                    locationMessage = "No recent GPS location was available. Drop the pin on the map instead."
                }
            }
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.onPhotosAdded(listOf(uri.toString()))
    }

    Scaffold(
        containerColor = MuniCanvas,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            BackHeader(
                title = "Report a concern",
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 10.dp, bottom = 26.dp),
            ) {
                if (!profile.isVerified) {
                    PdfCard(contentPadding = 16) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MuniCardPink),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = MuniNavy,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Verify before you report.",
                            color = MuniNavy,
                            fontSize = 19.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "MuniReport accepts reports from verified Muntinlupa residents. Complete the residency check, then return here to file your concern.",
                            color = MuniInkMuted,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                        )
                        Spacer(Modifier.height(14.dp))
                        PdfPrimaryButton(
                            text = "Open residency verification",
                            onClick = onStartVerification,
                        )
                    }
                    return@Column
                }

                state.submitError?.let { message ->
                    ErrorState(message = message)
                    Spacer(Modifier.height(10.dp))
                }

                PdfCard(
                    background = MuniSurface,
                    borderColor = MuniGuideBorder,
                    contentPadding = 10,
                ) {
                    SectionLabel("Category")
                    CategorySelector(
                        selected = state.category,
                        onSelect = viewModel::onCategoryChange,
                        enabled = !state.isSubmitting,
                    )
                    state.categoryError?.let { message ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = message,
                            color = MuniGuideBorderStrong,
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                PdfCard(
                    background = MuniSurface,
                    borderColor = MuniGuideBorder,
                    contentPadding = 10,
                ) {
                    FormField(
                        value = state.title,
                        onValueChange = viewModel::onTitleChange,
                        label = "Short title",
                        placeholder = "Example: Clogged canal beside the market",
                        error = state.titleError,
                        helper = "${state.title.length}/90",
                        enabled = !state.isSubmitting,
                    )
                }

                Spacer(Modifier.height(10.dp))
                PdfCard(
                    background = MuniSurface,
                    borderColor = MuniGuideBorder,
                    contentPadding = 10,
                ) {
                    FormField(
                        value = state.description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = "What's happening?",
                        placeholder = "Describe what you saw, how long it has been there, and who it affects.",
                        error = state.descriptionError,
                        helper = "${state.description.length}/1,200",
                        enabled = !state.isSubmitting,
                        singleLine = false,
                        minLines = 4,
                    )
                }

                Spacer(Modifier.height(10.dp))
                PhotoSection(
                    uri = state.photoUris.firstOrNull(),
                    enabled = !state.isSubmitting,
                    onPick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                )

                Spacer(Modifier.height(12.dp))
                LocationPicker(
                    latitude = state.latitude,
                    longitude = state.longitude,
                    onCoordinateChange = { latitude, longitude ->
                        locationMessage = null
                        viewModel.onCoordinates(latitude, longitude)
                    },
                    onUseMyLocation = requestLocation,
                    locating = locating,
                    enabled = !state.isSubmitting,
                    error = state.locationError,
                )
                locationMessage?.let { message ->
                    if (state.locationError == null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = message,
                            color = MuniInkMuted,
                            fontSize = 9.5.sp,
                            lineHeight = 13.sp,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                PdfPrimaryButton(
                    text = "Submit report",
                    onClick = viewModel::submit,
                    enabled = !state.isSubmitting,
                    loading = state.isSubmitting,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Reports start as Pending and enter the barangay review queue.",
                    modifier = Modifier.fillMaxWidth(),
                    color = MuniInkMuted,
                    fontSize = 9.5.sp,
                    lineHeight = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    state.createdComplaint?.let { complaint ->
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = com.munireport.app.core.designsystem.colors.StatusResolvedFg,
                    modifier = Modifier.size(36.dp),
                )
            },
            title = { Text("Report submitted") },
            text = {
                Text(
                    "Ticket ${complaint.ticketNumber} is now in the barangay queue. You will receive an alert when its status changes.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCreatedComplaint()
                        onCreated(complaint.id)
                    },
                ) {
                    Text("View my report")
                }
            },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = MuniNavy,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 1.dp, bottom = 6.dp),
    )
}

@Composable
private fun PhotoSection(
    uri: String?,
    enabled: Boolean,
    onPick: () -> Unit,
) {
    PdfCard(
        background = MuniSurface,
        borderColor = MuniGuideBorder,
        contentPadding = 10,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Photo",
                color = MuniNavy,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "(1 photo only)",
                color = MuniInkMuted,
                fontSize = 9.5.sp,
                lineHeight = 12.sp,
            )
        }
        Spacer(Modifier.height(7.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(7.dp))
                .background(MuniCardPink)
                .border(1.dp, MuniGuideBorder.copy(alpha = 0.65f), RoundedCornerShape(7.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MuniSurface)
                    .border(1.dp, MuniGuideBorder.copy(alpha = 0.55f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (uri != null) {
                    AsyncImage(
                        model = Uri.parse(uri),
                        contentDescription = "Attached report photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(6.dp)),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = null,
                        tint = MuniInkMuted,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uri?.let { it.substringAfterLast('/').take(24) } ?: "No photo added",
                    color = MuniNavy,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    text = if (uri != null) "1 of 1 added" else "Add one clear photo",
                    color = MuniInkMuted,
                    fontSize = 9.5.sp,
                    lineHeight = 12.sp,
                )
            }
            Spacer(Modifier.width(6.dp))
            PdfOutlinedButton(
                text = if (uri == null) "Add photo" else "Replace",
                onClick = onPick,
                enabled = enabled,
                modifier = Modifier.width(92.dp),
            )
        }
    }
}
