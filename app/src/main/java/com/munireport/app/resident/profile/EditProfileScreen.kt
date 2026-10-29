package com.munireport.app.resident.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.munireport.app.core.constants.AppConstants
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.components.BackHeader
import com.munireport.app.core.designsystem.components.ErrorState
import com.munireport.app.core.designsystem.components.FormField
import com.munireport.app.core.designsystem.components.PdfCard
import com.munireport.app.core.designsystem.components.PdfDropdownField
import com.munireport.app.core.designsystem.components.PdfPrimaryButton
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.AuthRepository

@Composable
fun EditProfileScreen(
    profile: UserProfile,
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val viewModel: EditProfileViewModel = viewModel(
        factory = EditProfileViewModel.factory(profile, authRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Scaffold(
        containerColor = MuniCanvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            BackHeader(
                title = "Edit profile",
                subtitle = "Personal information",
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 14.dp)
                .padding(top = 10.dp, bottom = 22.dp),
        ) {
            state.errorMessage?.let { message ->
                ErrorState(message = message)
                Spacer(Modifier.height(9.dp))
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
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                label = "Mobile no.",
                placeholder = "09XX XXX XXXX",
                keyboardType = KeyboardType.Phone,
                error = state.phoneError,
                enabled = !state.isSaving,
            )
            Spacer(Modifier.height(9.dp))
            FormField(
                value = state.address,
                onValueChange = viewModel::onAddressChange,
                label = "House no. and street",
                placeholder = "e.g. 12 Mabini St.",
                error = state.addressError,
                enabled = !state.isSaving,
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
                enabled = !state.isSaving,
            )
            Spacer(Modifier.height(9.dp))
            FormField(
                value = profile.email,
                onValueChange = {},
                label = "Email",
                enabled = false,
                helper = "Email changes require barangay office assistance.",
            )
            Spacer(Modifier.height(10.dp))
            PdfCard(
                background = MuniCanvas,
                borderColor = MuniInkMuted.copy(alpha = 0.22f),
                contentPadding = 10,
            ) {
                Text(
                    text = "Changing your address does not automatically change your verification record. The barangay office may ask you to verify again.",
                    color = MuniInkMuted,
                    fontSize = 9.5.sp,
                    lineHeight = 13.sp,
                )
            }
            Spacer(Modifier.height(12.dp))
            PdfPrimaryButton(
                text = "Save changes",
                onClick = viewModel::save,
                enabled = !state.isSaving,
                loading = state.isSaving,
            )
        }
    }
}
