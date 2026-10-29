package com.munireport.app.admin.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.munireport.app.core.designsystem.colors.MuniActionBlue
import com.munireport.app.core.designsystem.colors.MuniBlue50
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.colors.MuniCardPink
import com.munireport.app.core.designsystem.colors.MuniGuideBorder
import com.munireport.app.core.designsystem.colors.MuniGuideBorderStrong
import com.munireport.app.core.designsystem.colors.MuniInk
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.colors.MuniLineSoft
import com.munireport.app.core.designsystem.colors.MuniNavy
import com.munireport.app.core.designsystem.colors.MuniSurface
import com.munireport.app.core.designsystem.colors.StatusResolvedFg
import com.munireport.app.core.designsystem.components.AdminHeader
import com.munireport.app.core.designsystem.components.ErrorState
import com.munireport.app.core.designsystem.components.FormField
import com.munireport.app.core.designsystem.components.PdfCard
import com.munireport.app.core.designsystem.components.PdfOutlinedButton
import com.munireport.app.core.designsystem.components.PdfPrimaryButton
import com.munireport.app.core.designsystem.components.PdfSectionTitle
import com.munireport.app.core.utils.DateFormats
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.UserRole
import com.munireport.app.domain.repository.AuthRepository

@Composable
fun AdminAccountScreen(
    profile: UserProfile,
    authRepository: AuthRepository,
    onSignOut: () -> Unit,
) {
    val viewModel: AdminAccountViewModel = viewModel(
        factory = AdminAccountViewModel.factory(profile, authRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSignOutDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MuniCanvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AdminHeader(
                title = "Account",
                subtitle = "Administrator settings · Muntinlupa City",
            )
        },
    ) { padding ->
        if (profile.role != UserRole.ADMIN) {
            ErrorState(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                message = "This account is not authorized for the staff console.",
            )
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
            AdminProfileCard(profile = profile)

            Spacer(Modifier.height(12.dp))
            PdfSectionTitle(title = "Contact details")
            Spacer(Modifier.height(6.dp))
            state.saveError?.let { message ->
                ErrorState(message = message)
                Spacer(Modifier.height(8.dp))
            }
            state.saveMessage?.let { message ->
                PdfCard(
                    background = MuniSurface,
                    borderColor = StatusResolvedFg.copy(alpha = 0.35f),
                    contentPadding = 9,
                ) {
                    Text(text = message, color = StatusResolvedFg, fontSize = 10.5.sp)
                }
                Spacer(Modifier.height(8.dp))
            }
            FormField(
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = "Full name",
                error = state.fullNameError,
                enabled = !state.isSaving,
            )
            Spacer(Modifier.height(9.dp))
            FormField(
                value = profile.email,
                onValueChange = {},
                label = "Email",
                enabled = false,
                helper = "Contact the system owner to change a staff email.",
            )
            Spacer(Modifier.height(9.dp))
            FormField(
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                label = "Mobile no.",
                error = state.phoneError,
                enabled = !state.isSaving,
            )
            Spacer(Modifier.height(9.dp))
            FormField(
                value = state.address,
                onValueChange = viewModel::onAddressChange,
                label = "Office or street address",
                error = state.addressError,
                enabled = !state.isSaving,
            )
            Spacer(Modifier.height(9.dp))
            FormField(
                value = state.purok,
                onValueChange = viewModel::onPurokChange,
                label = "Purok or assignment",
                error = state.purokError,
                enabled = !state.isSaving,
            )
            Spacer(Modifier.height(11.dp))
            PdfPrimaryButton(
                text = "Save account details",
                onClick = viewModel::save,
                enabled = !state.isSaving,
                loading = state.isSaving,
            )

            Spacer(Modifier.height(14.dp))
            PdfSectionTitle(title = "Access and security")
            Spacer(Modifier.height(6.dp))
            PdfCard(
                background = MuniSurface,
                borderColor = MuniGuideBorder,
                contentPadding = 10,
            ) {
                Text(
                    text = "Administrator access is assigned outside the app. Signing up from the resident form can never create or upgrade an admin role.",
                    color = MuniInkMuted,
                    fontSize = 9.5.sp,
                    lineHeight = 13.sp,
                )
                Spacer(Modifier.height(8.dp))
                state.resetError?.let { message ->
                    ErrorState(message = message)
                    Spacer(Modifier.height(7.dp))
                }
                state.resetMessage?.let { message ->
                    Text(
                        text = message,
                        color = StatusResolvedFg,
                        fontSize = 9.5.sp,
                        lineHeight = 13.sp,
                    )
                    Spacer(Modifier.height(7.dp))
                }
                PdfOutlinedButton(
                    text = if (state.isSendingReset) "Sending…" else "Send password reset email",
                    onClick = viewModel::sendPasswordReset,
                    enabled = !state.isSendingReset,
                    icon = Icons.Rounded.LockReset,
                )
            }

            Spacer(Modifier.height(12.dp))
            PdfOutlinedButton(
                text = "Sign out",
                onClick = { showSignOutDialog = true },
                icon = Icons.AutoMirrored.Rounded.Logout,
                contentColor = MuniGuideBorderStrong,
                borderColor = MuniGuideBorder,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = "Actions performed here are attributed to this administrator account.",
                color = MuniInkMuted,
                fontSize = 9.sp,
                lineHeight = 12.sp,
            )
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = MuniSurface,
            title = {
                Text("Sign out of the staff console?", color = MuniNavy, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "You will need your administrator credentials to sign back in.",
                    color = MuniInk,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    },
                ) {
                    Text("Sign out", color = MuniGuideBorderStrong)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = MuniActionBlue)
                }
            },
        )
    }
}

@Composable
private fun AdminProfileCard(profile: UserProfile) {
    PdfCard(
        background = MuniCardPink,
        borderColor = MuniGuideBorder,
        contentPadding = 12,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(MuniBlue50)
                    .border(1.dp, MuniGuideBorder, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = profile.initials,
                    color = MuniNavy,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.fullName,
                    color = MuniNavy,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = profile.email,
                    color = MuniInkMuted,
                    fontSize = 9.5.sp,
                    lineHeight = 12.sp,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(MuniNavy)
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AdminPanelSettings,
                        contentDescription = null,
                        tint = MuniSurface,
                        modifier = Modifier.size(11.dp),
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "ADMINISTRATOR",
                        color = MuniSurface,
                        fontSize = 7.5.sp,
                        lineHeight = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                    )
                }
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(
            text = "Barangay staff account · Member since ${
                if (profile.createdAt > 0) DateFormats.monthYear(profile.createdAt) else "recently"
            }",
            color = MuniInkMuted,
            fontSize = 9.sp,
            lineHeight = 12.sp,
        )
    }
}
