package com.munireport.app.gettingin.login

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.munireport.app.core.designsystem.colors.MuniBlue
import com.munireport.app.core.designsystem.colors.MuniLoginCanvas
import com.munireport.app.core.designsystem.colors.MuniLoginInkMuted
import com.munireport.app.core.designsystem.colors.MuniRed
import com.munireport.app.core.designsystem.colors.MuniYellow
import com.munireport.app.domain.usecase.SignInUseCase

@Composable
fun LoginScreen(
    signInUseCase: SignInUseCase,
    isDemoMode: Boolean,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
) {
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.factory(
            signInUseCase = signInUseCase,
            allowDemoAdminPreview = isDemoMode,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LoginContent(
        state = state,
        showAdminPreview = isDemoMode,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSignIn = viewModel::signIn,
        onAdminPreview = viewModel::previewAdminSignIn,
        onNavigateToSignUp = onNavigateToSignUp,
        onNavigateToForgotPassword = onNavigateToForgotPassword,
    )
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    showAdminPreview: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onAdminPreview: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
) {
    LoginSystemBars()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MuniLoginCanvas),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 430.dp)
                    .fillMaxWidth(),
            ) {
                Spacer(Modifier.height(20.dp))
                LoginCityStripe()
                Spacer(Modifier.height(18.dp))
                MuniReportBrandingPanel()
                Spacer(Modifier.height(16.dp))
                LoginFormCard(
                    email = state.email,
                    password = state.password,
                    emailError = state.emailError,
                    passwordError = state.passwordError,
                    authError = state.errorMessage,
                    isSubmitting = state.isSubmitting,
                    showAdminPreview = showAdminPreview,
                    onEmailChange = onEmailChange,
                    onPasswordChange = onPasswordChange,
                    onSignIn = onSignIn,
                    onForgotPassword = onNavigateToForgotPassword,
                    onAdminPreview = onAdminPreview,
                )
                Spacer(Modifier.height(36.dp))
                ResidencyNotice()
                Spacer(Modifier.height(26.dp))
                CreateAccountLink(onClick = onNavigateToSignUp)
                SdgFooter()
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Muntinlupa City · SDG 11",
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = MuniLoginInkMuted,
                    fontSize = 9.sp,
                    lineHeight = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun LoginCityStripe() {
    val shape = RoundedCornerShape(4.dp)
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(shape),
    ) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxSize()
                .background(MuniBlue),
        )
        Box(
            Modifier
                .weight(1f)
                .fillMaxSize()
                .background(MuniRed),
        )
        Box(
            Modifier
                .weight(1f)
                .fillMaxSize()
                .background(MuniYellow),
        )
    }
}

/**
 * The sign-in reference stays light regardless of the device theme, so the
 * system bar icons use their dark variant while this screen is mounted.
 */
@Composable
private fun LoginSystemBars() {
    val view = LocalView.current
    val activity = rememberViewActivity(view)
    if (activity != null) {
        DisposableEffect(activity, view) {
            val controller = WindowCompat.getInsetsController(activity.window, view)
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
            onDispose {
                controller.isAppearanceLightStatusBars = true
                controller.isAppearanceLightNavigationBars = true
            }
        }
    }
}

@Composable
private fun rememberViewActivity(view: android.view.View): Activity? {
    return androidx.compose.runtime.remember(view) { view.context.findActivity() }
}

private fun Context.findActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return current as? Activity
}
