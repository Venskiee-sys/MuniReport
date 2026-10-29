package com.munireport.app.resident.profile

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PeopleOutline
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.munireport.app.core.designsystem.colors.MuniActionBlue
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.colors.MuniGuideBorder
import com.munireport.app.core.designsystem.colors.MuniGuideBorderStrong
import com.munireport.app.core.designsystem.colors.MuniInk
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.colors.MuniLineSoft
import com.munireport.app.core.designsystem.colors.MuniNavy
import com.munireport.app.core.designsystem.colors.MuniSurface
import com.munireport.app.core.designsystem.colors.StatusResolvedFg
import com.munireport.app.core.designsystem.components.ComplaintRow
import com.munireport.app.core.designsystem.components.ErrorState
import com.munireport.app.core.designsystem.components.PageHeader
import com.munireport.app.core.designsystem.components.PdfCard
import com.munireport.app.core.designsystem.components.ProfileSummary
import com.munireport.app.core.designsystem.components.icon
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.VerificationStatus
import com.munireport.app.domain.repository.AuthRepository
import com.munireport.app.domain.repository.ComplaintRepository
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    profile: UserProfile,
    authRepository: AuthRepository,
    complaintRepository: ComplaintRepository,
    onEditProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenComplaint: (String) -> Unit,
    onStartVerification: () -> Unit,
    onSignOut: () -> Unit,
) {
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(profile, authRepository, complaintRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val current = state.profile ?: profile
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showFollowedDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var resetSending by remember { mutableStateOf(false) }
    var resetMessage by remember { mutableStateOf<String?>(null) }
    var resetError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun sendPasswordReset() {
        if (resetSending) return
        resetSending = true
        resetError = null
        resetMessage = null
        scope.launch {
            runCatching { authRepository.sendPasswordReset(current.email) }
                .onSuccess {
                    resetSending = false
                    showPasswordDialog = false
                    resetMessage = "A password reset link was sent to ${current.email}."
                }
                .onFailure {
                    resetSending = false
                    resetError = "We could not send the reset link. Try again."
                }
        }
    }

    Scaffold(
        containerColor = MuniCanvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            PageHeader(title = "Profile")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 26.dp),
            ) {
                state.errorMessage?.let { message ->
                    ErrorState(message = message)
                    Spacer(Modifier.height(10.dp))
                }
                resetMessage?.let { message ->
                    PdfCard(
                        background = MuniSurface,
                        borderColor = StatusResolvedFg.copy(alpha = 0.35f),
                        contentPadding = 10,
                    ) {
                        Text(
                            text = message,
                            color = StatusResolvedFg,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }

                ProfileSummary(
                    profile = current,
                    reportCount = state.reportCount,
                    followingCount = state.followingCount,
                    commentCount = state.commentCount,
                )

                Spacer(Modifier.height(14.dp))
                ProfileMenuRow(
                    icon = Icons.Rounded.Edit,
                    title = "Personal information",
                    onClick = onEditProfile,
                )
                Spacer(Modifier.height(6.dp))
                ProfileMenuRow(
                    icon = Icons.Rounded.LocationOn,
                    title = "Barangay and address",
                    onClick = onEditProfile,
                )
                Spacer(Modifier.height(6.dp))
                ProfileMenuRow(
                    icon = Icons.Rounded.VerifiedUser,
                    title = "Verification status",
                    onClick = onStartVerification,
                    trailingText = current.verificationLabel(),
                    trailingColor = current.verificationColor(),
                )
                Spacer(Modifier.height(6.dp))
                ProfileMenuRow(
                    icon = Icons.Rounded.PeopleOutline,
                    title = "Complaints I follow",
                    onClick = { showFollowedDialog = true },
                    trailingText = state.followingCount.toString(),
                    showChevron = true,
                )
                Spacer(Modifier.height(6.dp))
                ProfileMenuRow(
                    icon = Icons.Rounded.NotificationsNone,
                    title = "Notification settings",
                    onClick = { showNotificationDialog = true },
                )
                Spacer(Modifier.height(6.dp))
                ProfileMenuRow(
                    icon = Icons.Rounded.LockReset,
                    title = "Change password",
                    onClick = { showPasswordDialog = true },
                )

                Spacer(Modifier.height(18.dp))
                ProfileMenuRow(
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    title = "Log out",
                    onClick = { showSignOutDialog = true },
                    tint = MuniGuideBorderStrong,
                    titleColor = MuniGuideBorderStrong,
                    showChevron = false,
                    centered = true,
                )
                Spacer(Modifier.height(9.dp))
                Text(
                    text = "Administrator access is assigned by the barangay office and cannot be selected from a resident account.",
                    color = MuniInkMuted,
                    fontSize = 9.5.sp,
                    lineHeight = 13.sp,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }

    if (showFollowedDialog) {
        Dialog(onDismissRequest = { showFollowedDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .background(MuniSurface, RoundedCornerShape(14.dp))
                    .border(1.dp, MuniGuideBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Complaints I follow",
                            color = MuniNavy,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "Close",
                            color = MuniActionBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable { showFollowedDialog = false }
                                .padding(6.dp),
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    if (state.followedComplaints.isEmpty()) {
                        Text(
                            text = "You are not following any complaints yet. Open a report and tap Follow to get updates.",
                            color = MuniInkMuted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            items(state.followedComplaints, key = { it.id }) { complaint ->
                                ComplaintRow(
                                    complaint = complaint,
                                    onClick = {
                                        showFollowedDialog = false
                                        onOpenComplaint(complaint.id)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNotificationDialog) {
        NotificationSettingsDialog(onDismiss = { showNotificationDialog = false })
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { if (!resetSending) showPasswordDialog = false },
            title = { Text("Change password?") },
            text = {
                Text(
                    "We will email a secure password reset link to ${current.email}. Follow the link to choose a new password.",
                )
            },
            confirmButton = {
                TextButton(onClick = ::sendPasswordReset, enabled = !resetSending) {
                    Text(if (resetSending) "Sending…" else "Send reset link")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPasswordDialog = false },
                    enabled = !resetSending,
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Log out of MuniReport?") },
            text = { Text("You can sign back in with your email address at any time.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    },
                ) {
                    Text("Log out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    trailingText: String? = null,
    trailingColor: androidx.compose.ui.graphics.Color = MuniActionBlue,
    showChevron: Boolean = true,
    tint: androidx.compose.ui.graphics.Color = MuniNavy,
    titleColor: androidx.compose.ui.graphics.Color = MuniInk,
    centered: Boolean = false,
) {
    val shape = RoundedCornerShape(9.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MuniSurface, shape)
            .border(1.dp, MuniGuideBorder.copy(alpha = 0.85f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(19.dp),
        )
        Spacer(Modifier.width(11.dp))
        Text(
            text = title,
            color = titleColor,
            fontSize = 12.5.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = if (centered) Modifier.weight(1f) else Modifier.weight(1f),
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                color = trailingColor,
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.width(5.dp))
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MuniInkMuted,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun NotificationSettingsDialog(onDismiss: () -> Unit) {
    var statusUpdates by remember { mutableStateOf(true) }
    var comments by remember { mutableStateOf(true) }
    var accountAlerts by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MuniSurface, RoundedCornerShape(14.dp))
                .border(1.dp, MuniGuideBorder, RoundedCornerShape(14.dp))
                .padding(16.dp),
        ) {
            Text(
                text = "Notification settings",
                color = MuniNavy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "Choose which alerts appear in your notifications feed.",
                color = MuniInkMuted,
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
            )
            Spacer(Modifier.height(10.dp))
            NotificationToggle("Status changes on followed reports", statusUpdates) {
                statusUpdates = it
            }
            NotificationToggle("New comments", comments) { comments = it }
            NotificationToggle("Account and verification alerts", accountAlerts) {
                accountAlerts = it
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Saved on this device for this demo account.",
                color = MuniInkMuted,
                fontSize = 9.sp,
                lineHeight = 12.sp,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Done",
                color = MuniActionBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun NotificationToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = MuniNavy,
            fontSize = 11.5.sp,
            lineHeight = 15.sp,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MuniSurface,
                checkedTrackColor = MuniActionBlue,
            ),
        )
    }
}

private fun UserProfile.verificationLabel(): String = when (verificationStatus) {
    VerificationStatus.APPROVED -> "Approved"
    VerificationStatus.PENDING -> "Pending"
    VerificationStatus.REJECTED -> "Needs attention"
    VerificationStatus.NOT_SUBMITTED -> "Not verified"
}

private fun UserProfile.verificationColor(): androidx.compose.ui.graphics.Color = when (verificationStatus) {
    VerificationStatus.APPROVED -> StatusResolvedFg
    VerificationStatus.PENDING -> com.munireport.app.core.designsystem.colors.StatusPendingFg
    VerificationStatus.REJECTED -> MuniGuideBorderStrong
    VerificationStatus.NOT_SUBMITTED -> MuniInkMuted
}
