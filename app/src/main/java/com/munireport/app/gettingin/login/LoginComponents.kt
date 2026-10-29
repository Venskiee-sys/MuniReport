package com.munireport.app.gettingin.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.munireport.app.core.designsystem.colors.MuniBlue
import com.munireport.app.core.designsystem.colors.MuniBrandPink
import com.munireport.app.core.designsystem.colors.MuniInputPink
import com.munireport.app.core.designsystem.colors.MuniLoginCardBorder
import com.munireport.app.core.designsystem.colors.MuniLoginErrorSurface
import com.munireport.app.core.designsystem.colors.MuniLoginFrameSurface
import com.munireport.app.core.designsystem.colors.MuniLoginInk
import com.munireport.app.core.designsystem.colors.MuniLoginInkMuted
import com.munireport.app.core.designsystem.colors.MuniLoginNoticeIcon
import com.munireport.app.core.designsystem.colors.MuniLoginSurface
import com.munireport.app.core.designsystem.colors.MuniNavy
import com.munireport.app.core.designsystem.colors.MuniNoticeYellow
import com.munireport.app.core.designsystem.colors.MuniRed
import com.munireport.app.core.designsystem.colors.MuniYellow

/**
 * Shared measurements for the framed GettingIn login reference.
 *
 * The frame is a visual device, not a fixed screen size. Content below it uses
 * these dp values so every screen scales together.
 */
internal object LoginTokens {
    val FrameMaxWidth = 430.dp
    val FrameCorner = 28.dp
    val FrameBorderWidth = 3.dp
    val StripeHeight = 4.dp
    val FrameHorizontalPadding = 16.dp
    val FrameVerticalPadding = 12.dp

    val BrandingCorner = 8.dp
    val CardCorner = 12.dp
    val ControlCorner = 7.dp

    val FieldHeight = 40.dp
    val FieldHorizontalPadding = 13.dp
    val ButtonHeight = 40.dp
    val CardPadding = 10.dp

    val LabelSize = 11.sp
    val FieldTextSize = 12.5.sp
    val NoticeToAccountGapTall = 84.dp
    val NoticeToAccountGapCompact = 36.dp
}

/**
 * The reference screen's rounded handset frame and the blue/red/yellow civic
 * stripe. The frame wraps its content instead of forcing a fixed image size, so
 * the same composition works on short and tall devices.
 */
@Composable
internal fun LoginPhoneFrame(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(LoginTokens.FrameCorner)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MuniLoginFrameSurface)
            .border(
                width = LoginTokens.FrameBorderWidth,
                color = MuniNavy,
                shape = shape,
            )
            .padding(LoginTokens.FrameBorderWidth),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(LoginTokens.StripeHeight),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MuniBlue),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MuniRed),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MuniYellow),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = LoginTokens.FrameHorizontalPadding,
                    vertical = LoginTokens.FrameVerticalPadding,
                ),
            content = content,
        )
    }
}

@Composable
internal fun MuniReportBrandingPanel(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(LoginTokens.BrandingCorner)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MuniBrandPink)
            .border(1.dp, MuniRed, shape)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MuniReportLogo()
        Spacer(Modifier.height(6.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = MuniNavy)) { append("Muni") }
                withStyle(SpanStyle(color = MuniRed)) { append("Report") }
            },
            color = MuniNavy,
            fontSize = 25.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = "Report community concerns in Muntinlupa City and track them until they're resolved.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            color = MuniLoginInkMuted,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Blue civic tile with a white pin-and-shield mark and the small yellow accent.
 * The mark is a custom vector so it reads as one symbol rather than emoji.
 */
@Composable
internal fun MuniReportLogo(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(48.dp)) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(12.dp))
                .background(MuniBlue),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MuniPinShieldMark,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
        Box(
            modifier = Modifier
                .size(13.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(MuniYellow)
                .border(2.dp, MuniBrandPink, CircleShape),
        )
    }
}

@Composable
internal fun LoginFormCard(
    email: String,
    password: String,
    emailError: String?,
    passwordError: String?,
    authError: String?,
    isSubmitting: Boolean,
    showAdminPreview: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onForgotPassword: () -> Unit,
    onAdminPreview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(LoginTokens.CardCorner)
    val passwordFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        keyboard?.hide()
        onSignIn()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MuniLoginSurface)
            .border(1.dp, MuniLoginCardBorder, shape)
            .padding(LoginTokens.CardPadding),
    ) {
        Column(Modifier.fillMaxWidth()) {
            if (authError != null) {
                LoginErrorBanner(message = authError)
                Spacer(Modifier.height(10.dp))
            }

            LoginTextField(
                value = email,
                onValueChange = onEmailChange,
                label = "Email",
                placeholder = "juan.delacruz@email.com",
                enabled = !isSubmitting,
                error = emailError,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocus.requestFocus() },
                ),
            )
            Spacer(Modifier.height(7.dp))
            LoginTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Password",
                placeholder = "Enter your password",
                enabled = !isSubmitting,
                isPassword = true,
                error = passwordError,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { submit() }),
                focusRequester = passwordFocus,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "Forgot password?",
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(role = Role.Button, onClick = onForgotPassword)
                        .padding(start = 6.dp, end = 2.dp, top = 4.dp, bottom = 3.dp),
                    color = MuniNavy,
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(2.dp))

            LoginButton(
                text = "Log In",
                loading = isSubmitting,
                onClick = submit,
            )

            if (showAdminPreview) {
                Spacer(Modifier.height(8.dp))
                AdminPreviewControl(
                    enabled = !isSubmitting,
                    onClick = {
                        focusManager.clearFocus()
                        keyboard?.hide()
                        onAdminPreview()
                    },
                )
            }
        }
    }
}

@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    enabled: Boolean,
    error: String?,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    keyboardActions: KeyboardActions,
    isPassword: Boolean = false,
    focusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = MuniNavy,
            fontSize = LoginTokens.LabelSize,
            lineHeight = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))

        val shape = RoundedCornerShape(LoginTokens.ControlCorner)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = LoginTokens.FieldHeight)
                .clip(shape)
                .background(MuniInputPink)
                .border(1.dp, MuniRed, shape)
                .then(
                    if (focusRequester != null) {
                        Modifier.focusRequester(focusRequester)
                    } else {
                        Modifier
                    },
                ),
            enabled = enabled,
            singleLine = true,
            textStyle = TextStyle(
                color = MuniLoginInk,
                fontFamily = FontFamily.SansSerif,
                fontSize = LoginTokens.FieldTextSize,
                lineHeight = 17.sp,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
            ),
            keyboardActions = keyboardActions,
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            cursorBrush = SolidColor(MuniBlue),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = LoginTokens.FieldHorizontalPadding,
                            vertical = 11.dp,
                        ),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = MuniLoginInkMuted,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            maxLines = 1,
                        )
                    }
                    innerTextField()
                }
            },
        )

        if (error != null) {
            Text(
                text = error,
                modifier = Modifier.padding(start = 2.dp, top = 3.dp),
                color = MuniRed,
                fontSize = 10.sp,
                lineHeight = 13.sp,
            )
        }
    }
}

@Composable
private fun LoginErrorBanner(message: String) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MuniLoginErrorSurface)
            .border(1.dp, MuniRed.copy(alpha = 0.45f), shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(
            text = message,
            color = MuniRed,
            fontSize = 10.5.sp,
            lineHeight = 14.sp,
        )
    }
}

@Composable
private fun LoginButton(
    text: String,
    loading: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(LoginTokens.ControlCorner)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = LoginTokens.ButtonHeight)
            .clip(shape)
            .background(MuniBlue)
            .border(1.dp, MuniRed, shape)
            .clickable(
                enabled = !loading,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp,
            )
        }
    }
}

@Composable
private fun AdminPreviewControl(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(LoginTokens.ControlCorner)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 26.dp)
            .clip(shape)
            .background(MuniLoginSurface)
            .border(1.dp, MuniRed.copy(alpha = 0.6f), shape)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Preview: log in as Admin",
            color = MuniLoginInkMuted,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun ResidencyNotice(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(LoginTokens.ControlCorner)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MuniNoticeYellow)
            .border(1.dp, MuniRed, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.VerifiedUser,
            contentDescription = null,
            tint = MuniLoginNoticeIcon,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Only verified Muntinlupa residents can post reports.",
            color = MuniLoginInk,
            fontSize = 10.5.sp,
            lineHeight = 14.sp,
        )
    }
}

@Composable
internal fun CreateAccountLink(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(LoginTokens.ControlCorner)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MuniBrandPink)
            .border(1.dp, MuniRed, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = MuniLoginInkMuted, fontWeight = FontWeight.Normal)) {
                    append("New here? ")
                }
                withStyle(SpanStyle(color = MuniNavy, fontWeight = FontWeight.Bold)) {
                    append("Create an account")
                }
            },
            fontSize = 11.5.sp,
            lineHeight = 15.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun SdgFooter(modifier: Modifier = Modifier) {
    Text(
        text = "Supports SDG 11: Sustainable Cities and Communities",
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        color = MuniLoginInkMuted,
        fontSize = 9.5.sp,
        lineHeight = 12.sp,
        textAlign = TextAlign.Center,
    )
}

private val MuniPinShieldMark: ImageVector by lazy {
    ImageVector.Builder(
        name = "MuniPinShield",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.White),
            pathFillType = PathFillType.EvenOdd,
        ) {
            // Location pin.
            moveTo(12f, 2f)
            curveTo(8.13f, 2f, 5f, 5.13f, 5f, 9f)
            curveTo(5f, 14.25f, 12f, 22f, 12f, 22f)
            curveTo(12f, 22f, 19f, 14.25f, 19f, 9f)
            curveTo(19f, 5.13f, 15.87f, 2f, 12f, 2f)
            close()

            // Shield cut-out: the blue tile shows through the pin.
            moveTo(12f, 5.1f)
            lineTo(15.1f, 6.35f)
            lineTo(15.1f, 9.35f)
            curveTo(15.1f, 11.35f, 13.8f, 13.05f, 12f, 13.9f)
            curveTo(10.2f, 13.05f, 8.9f, 11.35f, 8.9f, 9.35f)
            lineTo(8.9f, 6.35f)
            close()

            // Check mark inside the shield.
            moveTo(10.65f, 8.8f)
            lineTo(11.55f, 9.7f)
            lineTo(13.25f, 7.95f)
            lineTo(13.95f, 8.65f)
            lineTo(11.55f, 11.05f)
            lineTo(9.95f, 9.45f)
            close()
        }
    }.build()
}
