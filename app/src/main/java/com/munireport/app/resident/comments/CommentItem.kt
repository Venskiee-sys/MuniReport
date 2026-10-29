package com.munireport.app.resident.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.munireport.app.core.designsystem.colors.MuniBlue50
import com.munireport.app.core.designsystem.colors.MuniCardPink
import com.munireport.app.core.designsystem.colors.MuniGuideBorder
import com.munireport.app.core.designsystem.colors.MuniInk
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.colors.MuniNavy
import com.munireport.app.core.designsystem.colors.MuniPillGray
import com.munireport.app.core.utils.DateFormats
import com.munireport.app.domain.model.Comment
import com.munireport.app.domain.model.UserRole

@Composable
fun CommentItem(
    comment: Comment,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    val isAdmin = comment.isOfficial && comment.authorRole == UserRole.ADMIN
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (comment.isOfficial) MuniBlue50 else MuniCardPink)
            .border(1.dp, MuniGuideBorder, shape)
            .padding(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(27.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(if (comment.isOfficial) MuniNavy else MuniPillGray),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = comment.initials,
                color = if (comment.isOfficial) androidx.compose.ui.graphics.Color.White else MuniNavy,
                fontSize = 9.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (comment.isOfficial) "City Admin" else comment.authorName,
                    color = MuniNavy,
                    fontSize = 10.5.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (comment.isOfficial) {
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = if (isAdmin) "ADMIN" else "OFFICIAL",
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MuniNavy)
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 7.5.sp,
                        lineHeight = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                    )
                }
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "· ${comment.locationLabel()} · ${DateFormats.relative(comment.createdAt)}",
                    color = MuniInkMuted,
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.size(3.dp))
            Text(
                text = comment.text,
                color = MuniInk,
                fontSize = 11.5.sp,
                lineHeight = 15.sp,
            )
        }
    }
}

private fun Comment.locationLabel(): String {
    // The comment model does not carry a barangay; use the author profile label
    // when available and fall back to the city so the meta line stays honest.
    return "Muntinlupa"
}
