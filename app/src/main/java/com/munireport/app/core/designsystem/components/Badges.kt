package com.munireport.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.munireport.app.core.designsystem.colors.Amber600
import com.munireport.app.core.designsystem.colors.Blue600
import com.munireport.app.core.designsystem.colors.Green600
import com.munireport.app.core.designsystem.colors.Indigo600
import com.munireport.app.core.designsystem.colors.Ink500
import com.munireport.app.core.designsystem.colors.MuniBlue100
import com.munireport.app.core.designsystem.colors.MuniBlue700
import com.munireport.app.core.designsystem.colors.MuniGreen100
import com.munireport.app.core.designsystem.colors.MuniRed100
import com.munireport.app.core.designsystem.colors.MuniRed600
import com.munireport.app.core.designsystem.colors.MuniYellow100
import com.munireport.app.core.designsystem.colors.Red600
import com.munireport.app.core.designsystem.colors.StatusClosedBg
import com.munireport.app.core.designsystem.colors.StatusClosedFg
import com.munireport.app.core.designsystem.colors.StatusPendingBg
import com.munireport.app.core.designsystem.colors.StatusPendingFg
import com.munireport.app.core.designsystem.colors.StatusProgressBg
import com.munireport.app.core.designsystem.colors.StatusProgressFg
import com.munireport.app.core.designsystem.colors.StatusResolvedBg
import com.munireport.app.core.designsystem.colors.StatusResolvedFg
import com.munireport.app.core.designsystem.colors.StatusReviewBg
import com.munireport.app.core.designsystem.colors.StatusReviewFg
import com.munireport.app.domain.model.ComplaintPriority
import com.munireport.app.domain.model.ComplaintStatus
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.VerificationStatus

fun statusAccent(status: ComplaintStatus): Color = when (status) {
    ComplaintStatus.PENDING -> StatusPendingFg
    ComplaintStatus.ACKNOWLEDGED,
    ComplaintStatus.IN_PROGRESS,
    -> StatusReviewFg
    ComplaintStatus.RESOLVED -> StatusResolvedFg
    ComplaintStatus.REJECTED -> StatusClosedFg
}

fun statusContainer(status: ComplaintStatus): Color = when (status) {
    ComplaintStatus.PENDING -> StatusPendingBg
    ComplaintStatus.ACKNOWLEDGED,
    ComplaintStatus.IN_PROGRESS,
    -> StatusReviewBg
    ComplaintStatus.RESOLVED -> StatusResolvedBg
    ComplaintStatus.REJECTED -> StatusClosedBg
}

fun verificationAccent(status: VerificationStatus): Color = when (status) {
    VerificationStatus.NOT_SUBMITTED -> Ink500
    VerificationStatus.PENDING -> StatusPendingFg
    VerificationStatus.APPROVED -> Green600
    VerificationStatus.REJECTED -> Red600
}

private fun verificationContainer(status: VerificationStatus): Color = when (status) {
    VerificationStatus.NOT_SUBMITTED -> StatusClosedBg
    VerificationStatus.PENDING -> StatusPendingBg
    VerificationStatus.APPROVED -> StatusResolvedBg
    VerificationStatus.REJECTED -> MuniRed100
}

fun priorityAccent(priority: ComplaintPriority): Color = when (priority) {
    ComplaintPriority.LOW -> Ink500
    ComplaintPriority.MEDIUM -> Blue600
    ComplaintPriority.HIGH -> Amber600
    ComplaintPriority.URGENT -> Red600
}

private fun statusIcon(status: ComplaintStatus) = when (status) {
    ComplaintStatus.PENDING -> Icons.Rounded.PendingActions
    ComplaintStatus.ACKNOWLEDGED -> Icons.Rounded.FactCheck
    ComplaintStatus.IN_PROGRESS -> Icons.Rounded.HourglassEmpty
    ComplaintStatus.RESOLVED -> Icons.Rounded.CheckCircle
    ComplaintStatus.REJECTED -> Icons.Rounded.Cancel
}

@Composable
fun StatusBadge(
    status: ComplaintStatus,
    modifier: Modifier = Modifier,
) {
    BadgeShell(
        modifier = modifier,
        containerColor = statusContainer(status),
        contentColor = statusAccent(status),
        icon = statusIcon(status),
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            color = statusAccent(status),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun VerificationBadge(
    status: VerificationStatus,
    modifier: Modifier = Modifier,
) {
    BadgeShell(
        modifier = modifier,
        containerColor = verificationContainer(status),
        contentColor = verificationAccent(status),
        icon = when (status) {
            VerificationStatus.APPROVED -> Icons.Rounded.Verified
            VerificationStatus.PENDING -> Icons.Rounded.HourglassEmpty
            VerificationStatus.REJECTED -> Icons.Rounded.ErrorOutline
            VerificationStatus.NOT_SUBMITTED -> Icons.Rounded.ErrorOutline
        },
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            color = verificationAccent(status),
        )
    }
}

@Composable
fun PriorityLabel(
    priority: ComplaintPriority,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(priorityAccent(priority), CircleShape),
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = "${priority.label} priority",
            style = MaterialTheme.typography.labelSmall,
            color = priorityAccent(priority),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun OfficialBadge(
    modifier: Modifier = Modifier,
    label: String = "ADMIN",
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MuniBlue100,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.TaskAlt,
                contentDescription = null,
                tint = MuniBlue700,
                modifier = Modifier.size(11.dp),
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MuniBlue700,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun CategoryPill(
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun PersonAvatar(
    profile: UserProfile,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
) {
    Avatar(initials = profile.initials, modifier = modifier, size = size)
}

@Composable
fun Avatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = if (size >= 52.dp) MaterialTheme.typography.titleLarge else MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun BadgeShell(
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(5.dp))
            content()
        }
    }
}
