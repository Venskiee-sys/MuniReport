package com.munireport.app.core.designsystem.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Traffic
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Water
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.ui.graphics.vector.ImageVector
import com.munireport.app.domain.model.ComplaintCategory
import com.munireport.app.domain.model.NotificationType

fun ComplaintCategory.icon(): ImageVector = when (this) {
    ComplaintCategory.WASTE_MANAGEMENT -> Icons.Rounded.DeleteOutline
    ComplaintCategory.FLOODING -> Icons.Rounded.Water
    ComplaintCategory.DRAINAGE -> Icons.Rounded.Waves
    ComplaintCategory.ROADS_AND_SIDEWALKS -> Icons.Rounded.Traffic
    ComplaintCategory.STREETLIGHTS -> Icons.Rounded.Lightbulb
    ComplaintCategory.OTHER -> Icons.Rounded.Construction
}

fun NotificationType.icon(): ImageVector = when (this) {
    NotificationType.STATUS_CHANGE -> Icons.Rounded.ReportProblem
    NotificationType.COMMENT -> Icons.Rounded.ChatBubbleOutline
    NotificationType.VERIFICATION -> Icons.Rounded.VerifiedUser
    NotificationType.SYSTEM -> Icons.Rounded.NotificationsNone
}
