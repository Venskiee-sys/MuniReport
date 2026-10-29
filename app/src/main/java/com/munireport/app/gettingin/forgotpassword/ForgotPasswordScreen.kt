package com.munireport.app.gettingin.forgotpassword

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.colors.MuniGreen50
import com.munireport.app.core.designsystem.colors.MuniGreen600
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.colors.MuniNavy
import com.munireport.app.core.designsystem.components.BackHeader
import com.munireport.app.core.designsystem.components.ErrorState
import com.munireport.app.core.designsystem.components.FormField
import com.munireport.app.core.designsystem.components.PdfCard
import com.munireport.app.core.designsystem.components.PdfOutlinedButton
import com.munireport.app.core.designsystem.components.PdfPrimaryButton
import com.munireport.app.domain.repository.AuthRepository

@Composable
fun ForgotPasswordScreen(
    authRepository: AuthRepository,
    onBackToLogin: () -> Unit,
) {
    val viewModel: ForgotPasswordViewModel = viewModel(
        factory = ForgotPasswordViewModel.factory(authRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ForgotPasswordContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onSend = viewModel::sendResetLink,
        onBackToLogin = onBackToLogin,
    )
}

@Composable
private fun ForgotPasswordContent(
    state: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSend: () -> Unit,
    onBackToLogin: () -> Unit,
) {
    Scaffold(
        containerColor = MuniCanvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            BackHeader(
                title = "Forgot password",
                subtitle = "Account recovery",
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
                .padding(top = 14.dp, bottom = 22.dp),
        ) {
            Text(
                text = "Reset your password",
                color = MuniNavy,
                fontSize = 18.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "Enter the email address linked to your MuniReport account. We will send a reset link if it matches a registered account.",
                color = MuniInkMuted,
                fontSize = 10.5.sp,
                lineHeight = 15.sp,
            )
            Spacer(Modifier.height(14.dp))

            if (state.isSent) {
                PdfCard(
                    background = MuniGreen50,
                    borderColor = MuniGreen600.copy(alpha = 0.35f),
                    contentPadding = 11,
                ) {
                    Text(
                        text = "Check your email",
                        color = MuniGreen600,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "If ${state.email.trim()} is registered, a password reset link is on its way. Check the spam folder if it does not arrive within a few minutes.",
                        color = MuniInkMuted,
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp,
                    )
                }
                Spacer(Modifier.height(12.dp))
                PdfPrimaryButton(
                    text = "Back to sign in",
                    onClick = onBackToLogin,
                )
            } else {
                state.errorMessage?.let { message ->
                    ErrorState(message = message)
                    Spacer(Modifier.height(9.dp))
                }
                FormField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    placeholder = "you@email.com",
                    keyboardType = KeyboardType.Email,
                    error = state.emailError,
                    enabled = !state.isSubmitting,
                )
                Spacer(Modifier.height(13.dp))
                PdfPrimaryButton(
                    text = "Send reset link",
                    onClick = onSend,
                    enabled = !state.isSubmitting,
                    loading = state.isSubmitting,
                )
                Spacer(Modifier.height(8.dp))
                PdfOutlinedButton(
                    text = "Back to sign in",
                    onClick = onBackToLogin,
                    enabled = !state.isSubmitting,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "For security, we never confirm whether an email address is registered.",
                modifier = Modifier.fillMaxWidth(),
                color = MuniInkMuted,
                fontSize = 9.sp,
                lineHeight = 12.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
