package com.munireport.app.resident.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.munireport.app.core.designsystem.colors.MuniActionBlue
import com.munireport.app.core.designsystem.colors.MuniGuideBorder
import com.munireport.app.core.designsystem.colors.MuniHeroBlue
import com.munireport.app.core.designsystem.colors.MuniInkFaint
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.colors.MuniLineSoft
import com.munireport.app.core.designsystem.colors.MuniNavy
import com.munireport.app.core.designsystem.colors.MuniSurface
import com.munireport.app.core.designsystem.components.PdfSectionTitle
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.ComplaintRepository

/**
 * Renders the comment thread only. Complaint details keep the composer pinned
 * above the system navigation bar while the thread scrolls with the report.
 */
@Composable
fun CommentsList(
    state: CommentsUiState,
    modifier: Modifier = Modifier,
    commentCount: Int? = null,
) {
    val visibleCount = maxOf(commentCount ?: 0, state.comments.size)
    Column(modifier = modifier.fillMaxWidth()) {
        PdfSectionTitle(title = "Comments ($visibleCount)")
        Spacer(Modifier.height(8.dp))
        when {
            state.isLoading -> Text(
                text = "Loading comments…",
                color = MuniInkMuted,
                fontSize = 11.5.sp,
                lineHeight = 15.sp,
            )
            state.comments.isEmpty() -> Text(
                text = "No comments yet. Add the first useful update.",
                color = MuniInkMuted,
                fontSize = 11.5.sp,
                lineHeight = 15.sp,
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                state.comments.forEach { comment ->
                    CommentItem(comment = comment)
                }
            }
        }
    }
}

/**
 * Fixed comment composer used by the complaint details scaffold. It shares the
 * same [CommentsViewModel] instance as the thread above so a sent comment shows
 * up immediately without creating a second repository stream.
 */
@Composable
fun CommentsComposer(
    state: CommentsUiState,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MuniSurface)
            .imePadding()
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = MuniGuideBorder.copy(alpha = 0.55f), thickness = 1.dp)
        state.errorMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 7.dp),
                color = MuniGuideBorder,
                fontSize = 10.sp,
                lineHeight = 13.sp,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 42.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(Color(0xFFFCF6F7))
                    .border(1.dp, MuniGuideBorder, RoundedCornerShape(21.dp))
                    .padding(horizontal = 15.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (state.draft.isEmpty()) {
                    Text(
                        text = "Write a comment…",
                        color = MuniInkFaint,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                }
                BasicTextField(
                    value = state.draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSending,
                    maxLines = 4,
                    textStyle = TextStyle(
                        color = MuniNavy,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                    ),
                    cursorBrush = SolidColor(MuniActionBlue),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                )
            }
            Spacer(Modifier.size(8.dp))
            Surface(
                onClick = onSend,
                enabled = state.draft.isNotBlank() && !state.isSending,
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = if (state.draft.isNotBlank()) MuniHeroBlue else MuniLineSoft,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (state.isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(17.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send comment",
                            tint = if (state.draft.isNotBlank()) Color.White else MuniInkMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Self-contained comments block kept for screens that scroll the composer with
 * their content. Complaint details uses [CommentsList] + [CommentsComposer].
 */
@Composable
fun CommentsSection(
    complaintId: String,
    profile: UserProfile,
    complaintRepository: ComplaintRepository,
    modifier: Modifier = Modifier,
    commentCount: Int? = null,
) {
    val viewModel: CommentsViewModel = viewModel(
        factory = CommentsViewModel.factory(complaintId, profile, complaintRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxWidth()) {
        CommentsList(state = state, commentCount = commentCount)
        Spacer(Modifier.height(10.dp))
        CommentsComposer(
            state = state,
            onDraftChange = viewModel::onDraftChange,
            onSend = viewModel::sendComment,
        )
    }
}
