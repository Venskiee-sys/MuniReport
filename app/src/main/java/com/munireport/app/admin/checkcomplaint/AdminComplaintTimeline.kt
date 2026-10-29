package com.munireport.app.admin.checkcomplaint

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.munireport.app.core.designsystem.components.MuniCard
import com.munireport.app.core.designsystem.components.OfficialBadge
import com.munireport.app.core.designsystem.components.statusAccent
import com.munireport.app.core.utils.DateFormats
import com.munireport.app.domain.model.ComplaintUpdate

@Composable
fun AdminComplaintTimeline(
    updates: List<ComplaintUpdate>,
    modifier: Modifier = Modifier,
) {
    if (updates.isEmpty()) return
    MuniCard(modifier = modifier) {
        Text(
            text = "Official timeline",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(12.dp))
        updates.forEachIndexed { index, update ->
            AdminTimelineEntry(update = update, isLast = index == updates.lastIndex)
        }
    }
}

@Composable
private fun AdminTimelineEntry(
    update: ComplaintUpdate,
    isLast: Boolean,
) {
    val accent = statusAccent(update.status)
    Row {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(18.dp),
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(9.dp)
                    .background(accent, CircleShape),
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(60.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
            }
        }
        Spacer(Modifier.size(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = update.status.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                )
                if (update.isOfficial) {
                    Spacer(Modifier.size(6.dp))
                    OfficialBadge()
                }
            }
            Spacer(Modifier.size(2.dp))
            Text(
                text = update.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.size(3.dp))
            Text(
                text = "${update.authorName} · ${DateFormats.dateTime(update.createdAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
        }
    }
}
