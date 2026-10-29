package com.munireport.app.gettingin.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.munireport.app.core.constants.AppConstants
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.colors.MuniFieldPink
import com.munireport.app.core.designsystem.colors.MuniGuideBorder
import com.munireport.app.core.designsystem.colors.MuniGuideBorderStrong
import com.munireport.app.core.designsystem.colors.MuniHeroBlue
import com.munireport.app.core.designsystem.colors.MuniInk
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.colors.MuniNavy
import com.munireport.app.core.designsystem.colors.MuniSurface
import com.munireport.app.core.designsystem.components.BackHeader
import com.munireport.app.core.designsystem.components.ErrorState
import com.munireport.app.core.designsystem.components.FormField
import com.munireport.app.core.designsystem.components.PdfDropdownField
import com.munireport.app.core.designsystem.components.PdfPrimaryButton
import com.munireport.app.gettingin.session.AuthSessionManager

@Composable
fun SignUpScreen(
    sessionManager: AuthSessionManager,
    onBackToLogin: () -> Unit,
) {
    val viewModel: SignUpViewModel = viewModel(
        factory = SignUpViewModel.factory(sessionManager),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SignUpContent(
        state = state,
        onFullNameChange = viewModel::onFullNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPhoneChange = viewModel::onPhoneChange,
        onAddressChange = viewModel::onAddressChange,
        onPurokChange = viewModel::onPurokChange,
        onPasswordChange = viewModel::onPasswordChange,
        onAcceptTermsChange = viewModel::onAcceptTermsChange,
        onSubmit = viewModel::submit,
        onDismissError = viewModel::dismissError,
        onBackToLogin = onBackToLogin,
    )
}

@Composable
private fun SignUpContent(
    state: SignUpUiState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPurokChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAcceptTermsChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onDismissError: () -> Unit,
    onBackToLogin: () -> Unit,
) {
    Scaffold(
        containerColor = MuniCanvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            BackHeader(
                title = "Create your account",
                subtitle = "Step 1 of 2: Residency check",
                onBack = onBackToLogin,
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
                ErrorState(message = message, onRetry = onDismissError)
                Spacer(Modifier.height(10.dp))
            }

            FormField(
                value = state.fullName,
                onValueChange = onFullNameChange,
                label = "Full name",
                placeholder = "First name and last name",
                error = state.fullNameError,
                enabled = !state.isSubmitting,
            )
            Spacer(Modifier.height(9.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FormField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    placeholder = "you@email.com",
                    keyboardType = KeyboardType.Email,
                    error = state.emailError,
                    enabled = !state.isSubmitting,
                    modifier = Modifier.weight(1.15f),
                )
                FormField(
                    value = state.phone,
                    onValueChange = onPhoneChange,
                    label = "Mobile no.",
                    placeholder = "09XX XXX XXXX",
                    keyboardType = KeyboardType.Phone,
                    error = state.phoneError,
                    enabled = !state.isSubmitting,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(9.dp))
            PdfDropdownField(
                label = "Barangay (Muntinlupa only)",
                selected = state.purok.takeIf { it.isNotBlank() },
                options = AppConstants.MUNTINLUPA_BARANGAYS,
                optionLabel = { it },
                onSelect = onPurokChange,
                placeholder = "Select your barangay",
                helper = AppConstants.MUNTINLUPA_BARANGAYS.joinToString(", "),
                error = state.purokError,
                enabled = !state.isSubmitting,
            )
            Spacer(Modifier.height(9.dp))
            FormField(
                value = state.address,
                onValueChange = onAddressChange,
                label = "House no. and street",
                placeholder = "e.g. 12 Mabini St.",
                error = state.addressError,
                enabled = !state.isSubmitting,
            )
            Spacer(Modifier.height(9.dp))
            FormField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = "Password",
                placeholder = "At least 8 characters",
                isPassword = true,
                error = state.passwordError,
                enabled = !state.isSubmitting,
            )

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MuniFieldPink)
                    .border(1.dp, MuniGuideBorder, RoundedCornerShape(8.dp))
                    .clickable(enabled = !state.isSubmitting) {
                        onAcceptTermsChange(!state.acceptedTerms)
                    }
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Checkbox(
                    checked = state.acceptedTerms,
                    onCheckedChange = onAcceptTermsChange,
                    enabled = !state.isSubmitting,
                    modifier = Modifier.size(28.dp),
                    colors = CheckboxDefaults.colors(
                        checkedColor = MuniHeroBlue,
                        uncheckedColor = MuniGuideBorder,
                        checkmarkColor = MuniSurface,
                    ),
                )
                Spacer(Modifier.size(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "I agree to the Terms and Privacy Policy (Data Privacy Act of 2012).",
                        color = MuniInk,
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp,
                    )
                    if (state.termsError != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = requireNotNull(state.termsError),
                            color = MuniGuideBorderStrong,
                            fontSize = 9.5.sp,
                            lineHeight = 12.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            PdfPrimaryButton(
                text = "Continue to residency check",
                onClick = onSubmit,
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
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Already have an account? Sign in",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !state.isSubmitting, onClick = onBackToLogin)
                    .padding(vertical = 6.dp),
                color = MuniHeroBlue,
                fontSize = 10.5.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
