package com.munireport.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.munireport.app.core.designsystem.colors.MuniActionBlue
import com.munireport.app.core.designsystem.colors.MuniBlue50
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.colors.MuniCardPink
import com.munireport.app.core.designsystem.colors.MuniCream
import com.munireport.app.core.designsystem.colors.MuniFieldPink
import com.munireport.app.core.designsystem.colors.MuniGuideBorder
import com.munireport.app.core.designsystem.colors.MuniGuideBorderStrong
import com.munireport.app.core.designsystem.colors.MuniHeroBlue
import com.munireport.app.core.designsystem.colors.MuniInk
import com.munireport.app.core.designsystem.colors.MuniInkFaint
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.colors.MuniLineSoft
import com.munireport.app.core.designsystem.colors.MuniNavy
import com.munireport.app.core.designsystem.colors.MuniPillGray
import com.munireport.app.core.designsystem.colors.MuniRed
import com.munireport.app.core.designsystem.colors.MuniSurface
import com.munireport.app.core.designsystem.colors.MuniYellow
import com.munireport.app.core.designsystem.colors.StatusPendingBg
import com.munireport.app.core.designsystem.colors.StatusPendingFg
import com.munireport.app.core.designsystem.colors.StatusResolvedBg
import com.munireport.app.core.designsystem.colors.StatusResolvedFg
import com.munireport.app.core.designsystem.colors.StatusReviewBg
import com.munireport.app.core.designsystem.colors.StatusReviewFg
import com.munireport.app.core.utils.DateFormats
import com.munireport.app.domain.model.AppNotification
import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintCategory
import com.munireport.app.domain.model.ComplaintStatus
import com.munireport.app.domain.model.NotificationType
import com.munireport.app.domain.model.UserProfile
import java.util.Calendar

// ---------------------------------------------------------------------------
// PDF-aligned civic components
//
// The screen guide uses a light canvas, white bordered cards, a thin rose rule
// on important surfaces and the Muntinlupa blue/red/yellow/green palette. These
// components deliberately avoid gradients, large shadows and extra decoration.
// ---------------------------------------------------------------------------

enum class ResidentNavTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    REPORT("Report", Icons.Rounded.AddCircleOutline),
    COMPLAINTS("Complaints", Icons.Rounded.FormatListBulleted),
    PROFILE("Profile", Icons.Rounded.PersonOutline),
}

enum class AdminNavTab(val label: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Rounded.GridView),
    COMPLAINTS("Complaints", Icons.Rounded.FormatListBulleted),
    ANALYTICS("Analytics", Icons.Rounded.BarChart),
    ACCOUNT("Account", Icons.Rounded.PersonOutline),
}

@Composable
fun ResidentBottomNavigation(
    selected: ResidentNavTab,
    onSelect: (ResidentNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    PdfBottomBar(
        items = ResidentNavTab.entries.map { it.label to it.icon },
        selectedIndex = selected.ordinal,
        onSelect = { onSelect(ResidentNavTab.entries[it]) },
        modifier = modifier,
    )
}

@Composable
fun AdminBottomNavigation(
    selected: AdminNavTab,
    onSelect: (AdminNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    PdfBottomBar(
        items = AdminNavTab.entries.map { it.label to it.icon },
        selectedIndex = selected.ordinal,
        onSelect = { onSelect(AdminNavTab.entries[it]) },
        modifier = modifier,
    )
}

@Composable
private fun PdfBottomBar(
    items: List<Pair<String, ImageVector>>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MuniCardPink),
    ) {
        HorizontalDivider(color = MuniGuideBorder.copy(alpha = 0.55f), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(62.dp),
        ) {
            items.forEachIndexed { index, (label, icon) ->
                val isSelected = index == selectedIndex
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics { selected = isSelected }
                        .clickable(role = Role.Tab) { onSelect(index) }
                        .padding(vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) MuniActionBlue else MuniInkFaint,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = label,
                        color = if (isSelected) MuniActionBlue else MuniInkMuted,
                        fontSize = 10.5.sp,
                        lineHeight = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
fun PdfCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    background: Color = MuniSurface,
    borderColor: Color = MuniLineSoft,
    corner: Int = 14,
    contentPadding: Int = 13,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(corner.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding.dp),
        content = content,
    )
}

@Composable
fun PageHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    emphasized: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    SystemBarIcons(useDarkStatusIcons = true)
    val base = modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp)
        .padding(top = 14.dp, bottom = 10.dp)
    if (emphasized) {
        Column(
            modifier = base
                .clip(RoundedCornerShape(12.dp))
                .background(MuniCardPink)
                .border(1.dp, MuniGuideBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = MuniNavy,
                        fontSize = 23.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = MuniInkMuted,
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp,
                        )
                    }
                }
                actions()
            }
        }
    } else {
        Row(modifier = base, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = MuniNavy,
                    fontSize = 25.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = MuniInkMuted,
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                    )
                }
            }
            actions()
        }
    }
}

@Composable
fun BackHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    emphasized: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
) {
    SystemBarIcons(useDarkStatusIcons = true)
    val shape = RoundedCornerShape(0.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(if (emphasized) MuniCardPink else MuniCanvas)
            .then(
                if (emphasized) {
                    Modifier.border(1.dp, MuniGuideBorder, shape)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = MuniNavy,
                modifier = Modifier.size(21.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MuniNavy,
                fontSize = 19.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = MuniInkMuted,
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp,
                )
            }
        }
        actions()
    }
}

@Composable
fun AdminHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    eyebrow: String? = "ADMIN · MUNTINLUPA CITY",
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
) {
    SystemBarIcons(useDarkStatusIcons = false)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MuniNavy)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = if (onBack != null) 6.dp else 18.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(21.dp),
                    )
                }
                Spacer(Modifier.width(2.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                if (eyebrow != null) {
                    Text(
                        text = eyebrow,
                        color = MuniYellow,
                        fontSize = 9.5.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    )
                    Spacer(Modifier.height(2.dp))
                }
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 23.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                    )
                }
            }
            actions()
        }
        content()
        MuniBrandStripe()
    }
}

@Composable
fun StatusPill(
    status: ComplaintStatus,
    modifier: Modifier = Modifier,
) {
    val (container, content) = when (status) {
        ComplaintStatus.PENDING -> StatusPendingBg to StatusPendingFg
        ComplaintStatus.ACKNOWLEDGED,
        ComplaintStatus.IN_PROGRESS,
        -> StatusReviewBg to StatusReviewFg
        ComplaintStatus.RESOLVED -> StatusResolvedBg to StatusResolvedFg
        ComplaintStatus.REJECTED -> MuniPillGray to MuniInkMuted
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(7.dp),
        color = container,
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            color = content,
            fontSize = 9.5.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
fun ComplaintCard(
    complaint: Complaint,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentUserId: String? = null,
    highlighted: Boolean = false,
) {
    PdfCard(
        modifier = modifier,
        onClick = onClick,
        borderColor = if (highlighted || complaint.followedByMe) MuniGuideBorder else MuniLineSoft,
        contentPadding = 11,
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MuniPillGray),
                contentAlignment = Alignment.Center,
            ) {
                if (complaint.photoUrls.isNotEmpty()) {
                    AsyncImage(
                        model = complaint.photoUrls.first(),
                        contentDescription = "Photo for ${complaint.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(9.dp)),
                    )
                } else {
                    Icon(
                        imageVector = complaint.category.icon(),
                        contentDescription = null,
                        tint = MuniInkFaint,
                        modifier = Modifier.size(23.dp),
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${complaint.shortTicketNumber} · ${complaint.category.shortLabel()}",
                        color = MuniInkMuted,
                        fontSize = 9.5.sp,
                        lineHeight = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(5.dp))
                    StatusPill(status = complaint.status)
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = complaint.title,
                    color = MuniNavy,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = listOf(
                        complaint.authorPurok.ifBlank { complaint.address },
                        DateFormats.relative(complaint.createdAt),
                    ).filter { it.isNotBlank() }.joinToString(" · "),
                    color = MuniInkMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val isMine = currentUserId != null && complaint.authorId == currentUserId
            Text(
                text = if (isMine) "You" else complaint.authorName,
                color = if (isMine) MuniRed else MuniInkMuted,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = if (isMine) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (complaint.followedByMe) {
                Spacer(Modifier.width(6.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, MuniGuideBorder, RoundedCornerShape(6.dp))
                        .background(MuniSurface)
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsNone,
                        contentDescription = null,
                        tint = MuniNavy,
                        modifier = Modifier.size(10.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "Following",
                        color = MuniNavy,
                        fontSize = 8.5.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Rounded.ChatBubbleOutline,
                contentDescription = null,
                tint = MuniInkMuted,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = complaint.commentCount.toString(),
                color = MuniInkMuted,
                fontSize = 10.sp,
                lineHeight = 13.sp,
            )
        }
    }
}

@Composable
fun ComplaintRow(
    complaint: Complaint,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    PdfCard(
        modifier = modifier,
        onClick = onClick,
        background = if (highlighted) MuniCardPink else MuniSurface,
        borderColor = if (highlighted) MuniGuideBorder else MuniLineSoft,
        contentPadding = 11,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "#${complaint.shortTicketNumber} · ${complaint.category.shortLabel()} · ${complaint.authorPurok}",
                color = MuniInkMuted,
                fontSize = 9.5.sp,
                lineHeight = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(5.dp))
            StatusPill(status = complaint.status)
        }
        Spacer(Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = complaint.title,
                color = MuniNavy,
                fontSize = 13.5.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "Open complaint",
                tint = MuniInkMuted,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = listOf(
                DateFormats.shortDate(complaint.createdAt),
                "${complaint.followerCount} following",
                if (complaint.commentCount == 0) "no comments" else "${complaint.commentCount} comments",
            ).joinToString(" · "),
            color = MuniInkMuted,
            fontSize = 9.5.sp,
            lineHeight = 12.sp,
        )
    }
}

@Composable
fun <T> FilterTabs(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Surface(
                onClick = { onSelect(option) },
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) MuniHeroBlue else MuniSurface,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) MuniHeroBlue else MuniGuideBorder,
                ),
            ) {
                Text(
                    text = label(option),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    color = if (isSelected) Color.White else MuniNavy,
                    fontSize = 10.5.sp,
                    lineHeight = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    helper: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)
    val borderColor = when {
        error != null -> MuniGuideBorderStrong
        focused -> MuniActionBlue
        else -> MuniGuideBorder
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = MuniNavy,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MuniFieldPink)
                .border(1.dp, borderColor, shape)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = if (singleLine) 28.dp else 64.dp)
                    .onFocusChanged { focused = it.isFocused },
                enabled = enabled,
                singleLine = singleLine,
                minLines = minLines,
                textStyle = TextStyle(
                    color = MuniInk,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
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
                cursorBrush = SolidColor(MuniActionBlue),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                color = MuniInkFaint,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                maxLines = if (singleLine) 1 else 4,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (trailingContent != null) {
                Spacer(Modifier.width(4.dp))
                trailingContent()
            }
        }
        if (error != null) {
            Text(
                text = error,
                modifier = Modifier.padding(start = 2.dp, top = 3.dp),
                color = MuniGuideBorderStrong,
                fontSize = 10.sp,
                lineHeight = 13.sp,
            )
        } else if (helper != null) {
            Text(
                text = helper,
                modifier = Modifier.padding(start = 2.dp, top = 3.dp),
                color = MuniInkFaint,
                fontSize = 9.5.sp,
                lineHeight = 12.sp,
            )
        }
    }
}

@Composable
fun <T> PdfDropdownField(
    label: String,
    selected: T?,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select an option",
    helper: String? = null,
    error: String? = null,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = selected?.let(optionLabel).orEmpty()
    val shape = RoundedCornerShape(8.dp)
    val borderColor = when {
        error != null -> MuniGuideBorderStrong
        expanded -> MuniActionBlue
        else -> MuniGuideBorder
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = MuniNavy,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(MuniFieldPink)
                    .border(1.dp, borderColor, shape)
                    .clickable(enabled = enabled) { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedLabel.ifBlank { placeholder },
                    color = if (selectedLabel.isBlank()) MuniInkFaint else MuniInk,
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = MuniInkMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = MuniSurface,
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = optionLabel(option),
                                color = MuniNavy,
                                fontSize = 12.5.sp,
                            )
                        },
                        onClick = {
                            expanded = false
                            onSelect(option)
                        },
                    )
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                modifier = Modifier.padding(start = 2.dp, top = 3.dp),
                color = MuniGuideBorderStrong,
                fontSize = 10.sp,
                lineHeight = 13.sp,
            )
        } else if (helper != null) {
            Text(
                text = helper,
                modifier = Modifier.padding(start = 2.dp, top = 3.dp),
                color = MuniInkFaint,
                fontSize = 9.5.sp,
                lineHeight = 12.sp,
            )
        }
    }
}

@Composable
fun CategorySelector(
    selected: ComplaintCategory?,
    onSelect: (ComplaintCategory) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        ComplaintCategory.entries.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                row.forEach { category ->
                    val isSelected = category == selected
                    val shape = RoundedCornerShape(9.dp)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(shape)
                            .background(if (isSelected) MuniBlue50 else MuniSurface)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MuniActionBlue else MuniGuideBorder.copy(alpha = 0.65f),
                                shape = shape,
                            )
                            .clickable(enabled = enabled, role = Role.Button) { onSelect(category) }
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = category.icon(),
                            contentDescription = null,
                            tint = if (isSelected) MuniActionBlue else MuniInkMuted,
                            modifier = Modifier.size(19.dp),
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = category.label,
                            color = if (isSelected) MuniActionBlue else MuniNavy,
                            fontSize = 9.5.sp,
                            lineHeight = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                repeat(3 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun LocationPicker(
    latitude: Double?,
    longitude: Double?,
    onCoordinateChange: (Double, Double) -> Unit,
    onUseMyLocation: () -> Unit,
    modifier: Modifier = Modifier,
    locating: Boolean = false,
    enabled: Boolean = true,
    error: String? = null,
    instruction: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Location",
                color = MuniNavy,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Surface(
                onClick = onUseMyLocation,
                enabled = enabled && !locating,
                shape = RoundedCornerShape(6.dp),
                color = MuniSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MuniGuideBorder),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = null,
                        tint = MuniActionBlue,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (locating) "Locating…" else "Use my location",
                        color = MuniActionBlue,
                        fontSize = 9.5.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(9.dp))
                .border(1.dp, MuniGuideBorder, RoundedCornerShape(9.dp)),
        ) {
            MuniMapPicker(
                latitude = latitude,
                longitude = longitude,
                onCoordinateChange = onCoordinateChange,
                interactive = enabled,
                height = 150.dp,
                instruction = instruction,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = when {
                latitude != null && longitude != null ->
                    "Pinned at ${String.format(java.util.Locale.US, "%.5f, %.5f", latitude, longitude)} · inside Muntinlupa City"
                else -> "Tap the map to pin the exact spot inside Muntinlupa City."
            },
            color = MuniInkMuted,
            fontSize = 9.5.sp,
            lineHeight = 13.sp,
        )
        if (error != null) {
            Spacer(Modifier.height(3.dp))
            Text(
                text = error,
                color = MuniGuideBorderStrong,
                fontSize = 10.sp,
                lineHeight = 13.sp,
            )
        }
    }
}

@Composable
fun NotificationCard(
    notification: AppNotification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (accent, tile, icon, kicker) = when (notification.type) {
        NotificationType.STATUS_CHANGE -> NotificationVisual(
            MuniActionBlue,
            MuniBlue50,
            Icons.Rounded.Sync,
            "FOLLOWING",
        )
        NotificationType.COMMENT -> NotificationVisual(
            Color(0xFFB08123),
            MuniCream,
            Icons.Rounded.ChatBubbleOutline,
            "FOLLOWING",
        )
        NotificationType.VERIFICATION -> NotificationVisual(
            StatusResolvedFg,
            StatusResolvedBg,
            Icons.Rounded.Shield,
            "ACCOUNT",
        )
        NotificationType.SYSTEM -> NotificationVisual(
            StatusReviewFg,
            MuniBlue50,
            Icons.Rounded.NotificationsNone,
            "ACCOUNT",
        )
    }
    PdfCard(
        modifier = modifier,
        onClick = onClick,
        background = if (notification.isRead) MuniSurface else MuniCardPink,
        borderColor = if (notification.isRead) MuniLineSoft else MuniGuideBorder,
        contentPadding = 11,
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tile),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(19.dp),
                )
            }
            Spacer(Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.textKicker(kicker),
                        color = MuniInkMuted,
                        fontSize = 8.5.sp,
                        lineHeight = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.weight(1f),
                    )
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(MuniRed, CircleShape),
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = notification.title,
                    color = MuniNavy,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.SemiBold,
                )
                if (notification.message.isNotBlank() && notification.message != notification.title) {
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = notification.message,
                        color = MuniInkMuted,
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = DateFormats.relative(notification.createdAt),
                    color = MuniInkFaint,
                    fontSize = 9.5.sp,
                    lineHeight = 12.sp,
                )
            }
        }
    }
}

private data class NotificationVisual(
    val accent: Color,
    val tile: Color,
    val icon: ImageVector,
    val kicker: String,
)

private fun AppNotification.textKicker(fallback: String): String {
    val haystack = "$title $message".lowercase()
    return when {
        haystack.contains("your report") || haystack.contains("your complaint") -> "YOUR REPORT"
        type == NotificationType.VERIFICATION -> "ACCOUNT"
        else -> fallback
    }
}

@Composable
fun ProfileSummary(
    profile: UserProfile,
    reportCount: Int,
    followingCount: Int,
    commentCount: Int,
    modifier: Modifier = Modifier,
) {
    PdfCard(modifier = modifier, contentPadding = 14) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MuniGuideBorder, RoundedCornerShape(8.dp))
                    .padding(4.dp)
                    .clip(CircleShape)
                    .border(2.dp, MuniYellow, CircleShape)
                    .padding(5.dp)
                    .background(MuniPillGray, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = profile.initials,
                    color = MuniNavy,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = profile.fullName,
                color = MuniNavy,
                fontSize = 19.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = profile.email,
                color = MuniInkMuted,
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
            )
            Spacer(Modifier.height(7.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                VerificationBadge(status = profile.verificationStatus)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MuniPillGray)
                        .padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Brgy. ${profile.purok.ifBlank { "—" }}",
                        color = MuniInkMuted,
                        fontSize = 9.5.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MuniCardPink)
                    .border(1.dp, MuniGuideBorder, RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp),
            ) {
                ProfileCount(reportCount.toString(), "Reports", Modifier.weight(1f))
                ProfileCount(followingCount.toString(), "Following", Modifier.weight(1f))
                ProfileCount(commentCount.toString(), "Comments", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ProfileCount(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = MuniNavy,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            color = MuniInkMuted,
            fontSize = 9.5.sp,
            lineHeight = 12.sp,
        )
    }
}

@Composable
fun StatisticsCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    helper: String? = null,
    containerColor: Color = MuniSurface,
    accentColor: Color = MuniNavy,
    borderColor: Color = MuniLineSoft,
    onClick: (() -> Unit)? = null,
) {
    PdfCard(
        modifier = modifier,
        onClick = onClick,
        background = containerColor,
        borderColor = borderColor,
        contentPadding = 11,
    ) {
        Text(
            text = label,
            color = MuniInkMuted,
            fontSize = 10.sp,
            lineHeight = 13.sp,
        )
        Spacer(Modifier.height(1.dp))
        Text(
            text = value,
            color = accentColor,
            fontSize = 25.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
        )
        if (helper != null) {
            Text(
                text = helper,
                color = MuniInkMuted,
                fontSize = 9.sp,
                lineHeight = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    PdfCard(
        modifier = modifier,
        background = MuniCardPink,
        borderColor = MuniGuideBorder,
        contentPadding = 13,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = MuniGuideBorderStrong,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = message,
                color = MuniInk,
                fontSize = 11.5.sp,
                lineHeight = 15.sp,
                modifier = Modifier.weight(1f),
            )
            if (onRetry != null) {
                IconButton(onClick = onRetry, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Retry",
                        tint = MuniActionBlue,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun PdfSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = MuniNavy,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                color = MuniActionBlue,
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 3.dp, vertical = 3.dp),
            )
        }
    }
}

@Composable
fun PdfPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clip(shape)
            .background(if (enabled && !loading) MuniHeroBlue else MuniHeroBlue.copy(alpha = 0.55f))
            .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(17.dp),
                strokeWidth = 2.dp,
                color = Color.White,
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp,
            )
        }
    }
}

@Composable
fun PdfOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    contentColor: Color = MuniNavy,
    borderColor: Color = MuniGuideBorder,
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 42.dp)
            .clip(shape)
            .background(MuniSurface)
            .border(1.dp, borderColor, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            color = contentColor,
            fontSize = 11.5.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun ComplaintCategory.shortLabel(): String = when (this) {
    ComplaintCategory.WASTE_MANAGEMENT -> "Garbage"
    ComplaintCategory.FLOODING -> "Flooding"
    ComplaintCategory.DRAINAGE -> "Drainage"
    ComplaintCategory.ROADS_AND_SIDEWALKS -> "Roads"
    ComplaintCategory.STREETLIGHTS -> "Streetlights"
    ComplaintCategory.OTHER -> "Others"
}
