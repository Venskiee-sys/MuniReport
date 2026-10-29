package com.munireport.app.resident.complaints

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PeopleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.munireport.app.core.designsystem.colors.MuniRed600
import com.munireport.app.core.designsystem.components.MuniCard
import com.munireport.app.core.designsystem.components.StatusBadge
import com.munireport.app.core.designsystem.components.icon
import com.munireport.app.core.designsystem.components.statusAccent
import com.munireport.app.core.utils.DateFormats
import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintStatus

@Composable
fun ResidentComplaintCard(
    complaint: Complaint,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentUserId: String? = null,
    compact: Boolean = false,
) {
    val thumbnailSize = if (compact) 62.dp else 74.dp
    MuniCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = 12,
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(thumbnailSize)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (complaint.photoUrls.isNotEmpty()) {
                    AsyncImage(
                        model = complaint.photoUrls.first(),
                        contentDescription = "Photo for ${complaint.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(thumbnailSize)
                            .clip(RoundedCornerShape(13.dp)),
                    )
                } else {
                    Icon(
                        imageVector = complaint.category.icon(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(25.dp),
                    )
                }
            }
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "#${complaint.shortTicketNumber} · ${complaint.category.shortLabel()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    StatusBadge(status = complaint.status)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = complaint.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "${complaint.locationLabel} · ${DateFormats.relative(complaint.createdAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        StatusProgressBar(status = complaint.status)
        Spacer(Modifier.height(9.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val isMine = currentUserId != null && complaint.authorId == currentUserId
            Text(
                text = if (isMine) "You" else "by ${complaint.authorName}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isMine) MuniRed600 else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isMine) FontWeight.SemiBold else FontWeight.Medium,
            )
            if (complaint.followedByMe) {
                Spacer(Modifier.width(7.dp))
                Icon(
                    imageVector = Icons.Rounded.Bookmark,
                    contentDescription = "You follow this report",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = "Following",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.weight(1f))
            CountLabel(
                icon = Icons.Rounded.PeopleOutline,
                count = complaint.followerCount,
                description = "${complaint.followerCount} followers",
            )
            Spacer(Modifier.width(10.dp))
            CountLabel(
                icon = Icons.Rounded.ChatBubbleOutline,
                count = complaint.commentCount,
                description = "${complaint.commentCount} comments",
            )
        }
    }
}

@Composable
private fun CountLabel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    description: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics { contentDescription = description },
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(13.dp),
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun StatusProgressBar(
    status: ComplaintStatus,
    modifier: Modifier = Modifier,
) {
    val completed = when (status) {
        ComplaintStatus.PENDING -> 1
        ComplaintStatus.ACKNOWLEDGED -> 2
        ComplaintStatus.IN_PROGRESS -> 2
        ComplaintStatus.RESOLVED -> 3
        ComplaintStatus.REJECTED -> 0
    }
    val labels = when (status) {
        ComplaintStatus.REJECTED -> "Closed"
        else -> "${completed.coerceAtLeast(1)} of 3 steps"
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Report progress: ${status.label}, $labels"
            },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            index < completed -> statusAccent(status)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
            )
        }
    }
}

private fun com.munireport.app.domain.model.ComplaintCategory.shortLabel(): String = when (this) {
    com.munireport.app.domain.model.ComplaintCategory.WASTE_MANAGEMENT -> "Garbage"
    com.munireport.app.domain.model.ComplaintCategory.FLOODING -> "Flooding"
    com.munireport.app.domain.model.ComplaintCategory.DRAINAGE -> "Drainage"
    com.munireport.app.domain.model.ComplaintCategory.ROADS_AND_SIDEWALKS -> "Roads"
    com.munireport.app.domain.model.ComplaintCategory.STREETLIGHTS -> "Streetlights"
    com.munireport.app.domain.model.ComplaintCategory.OTHER -> "Others"
}
