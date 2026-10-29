package com.munireport.app.admin.analytics

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.munireport.app.core.common.userMessage
import com.munireport.app.core.designsystem.colors.MuniActionBlue
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.colors.MuniCardPink
import com.munireport.app.core.designsystem.colors.MuniFieldPink
import com.munireport.app.core.designsystem.colors.MuniGuideBorder
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.colors.MuniLineSoft
import com.munireport.app.core.designsystem.colors.MuniNavy
import com.munireport.app.core.designsystem.colors.MuniPillGray
import com.munireport.app.core.designsystem.colors.MuniSurface
import com.munireport.app.core.designsystem.colors.MuniYellow
import com.munireport.app.core.designsystem.colors.StatusPendingFg
import com.munireport.app.core.designsystem.colors.StatusResolvedFg
import com.munireport.app.core.designsystem.components.AdminHeader
import com.munireport.app.core.designsystem.components.ErrorState
import com.munireport.app.core.designsystem.components.LoadingState
import com.munireport.app.core.designsystem.components.PdfCard
import com.munireport.app.core.utils.DateFormats
import com.munireport.app.domain.model.ComplaintStatus
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.ComplaintRepository
import com.munireport.app.domain.repository.VerificationRepository
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AnalyticsScreen(
    profile: UserProfile,
    complaintRepository: ComplaintRepository,
    verificationRepository: VerificationRepository,
) {
    val viewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModel.factory(
            profile = profile,
            complaintRepository = complaintRepository,
            verificationRepository = verificationRepository,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val exporter = remember(context) { PdfReportExporter(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var exporting by remember { mutableStateOf(false) }
    var exportError by remember { mutableStateOf<String?>(null) }

    fun exportSnapshot() {
        if (exporting) return
        scope.launch {
            exporting = true
            exportError = null
            runCatching { exporter.export(profile, state) }
                .onSuccess { file ->
                    runCatching { sharePdf(context, file) }
                        .onFailure { exportError = it.userMessage() }
                }
                .onFailure { exportError = it.userMessage() }
            exporting = false
        }
    }

    Scaffold(
        containerColor = MuniCanvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AdminHeader(
                title = "Reports and Analytics",
                subtitle = "Sample data",
                eyebrow = null,
                actions = {
                    ExportPdfButton(
                        exporting = exporting,
                        onClick = ::exportSnapshot,
                    )
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                PeriodSelector(
                    selected = state.period,
                    onSelect = viewModel::onPeriodChange,
                )
            }

            exportError?.let { message ->
                item { ErrorState(message = message) }
            }
            state.errorMessage?.let { message ->
                item { ErrorState(message = message) }
            }

            if (state.isLoading) {
                item { LoadingState(message = "Building the analytics snapshot") }
            } else {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AnalyticsStatCard(
                            label = "Resolution rate",
                            value = "${state.resolutionRate}%",
                            helper = "${state.resolvedReports} of ${state.totalReports} resolved",
                            valueColor = StatusResolvedFg,
                            container = MuniCardPink,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                        AnalyticsStatCard(
                            label = "Avg. time to resolve",
                            value = state.averageResolutionHours
                                ?.let(DateFormats::durationLabel)
                                ?: "—",
                            helper = "From submit to resolved",
                            valueColor = MuniNavy,
                            container = MuniCardPink,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }

                item { CategoryBreakdownCard(categories = state.categories) }
                item { StatusBreakdownCard(statuses = state.statuses) }
                item { TopBarangaysCard(barangays = state.topBarangays) }
            }
        }
    }
}

@Composable
private fun ExportPdfButton(
    exporting: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = !exporting,
        shape = RoundedCornerShape(8.dp),
        color = MuniYellow,
        border = androidx.compose.foundation.BorderStroke(1.dp, MuniGuideBorder),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (exporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MuniNavy,
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = null,
                    tint = MuniNavy,
                    modifier = Modifier.size(15.dp),
                )
            }
            Spacer(Modifier.width(5.dp))
            Text(
                text = "Export PDF",
                color = MuniNavy,
                fontSize = 10.5.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun PeriodSelector(
    selected: AnalyticsPeriod,
    onSelect: (AnalyticsPeriod) -> Unit,
) {
    PdfCard(
        background = MuniSurface,
        borderColor = MuniGuideBorder,
        contentPadding = 4,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            AnalyticsPeriod.entries.forEach { period ->
                val isSelected = period == selected
                val shape = RoundedCornerShape(6.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(shape)
                        .background(if (isSelected) MuniCardPink else Color.Transparent)
                        .then(
                            if (isSelected) {
                                Modifier.border(1.dp, MuniGuideBorder, shape)
                            } else {
                                Modifier
                            },
                        )
                        .semantics { this.selected = isSelected }
                        .clickable(role = Role.Tab) { onSelect(period) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = period.label,
                        color = if (isSelected) MuniNavy else MuniInkMuted,
                        fontSize = 9.5.sp,
                        lineHeight = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsStatCard(
    label: String,
    value: String,
    helper: String,
    valueColor: Color,
    container: Color,
    modifier: Modifier = Modifier,
) {
    PdfCard(
        modifier = modifier,
        background = container,
        borderColor = MuniGuideBorder,
        contentPadding = 10,
    ) {
        Text(
            text = label,
            color = MuniInkMuted,
            fontSize = 9.5.sp,
            lineHeight = 12.sp,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = 22.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = helper,
            color = MuniInkMuted,
            fontSize = 8.5.sp,
            lineHeight = 11.sp,
            maxLines = 2,
        )
    }
}

@Composable
private fun CategoryBreakdownCard(categories: List<CategoryMetric>) {
    val max = categories.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    PdfCard(
        background = MuniCardPink,
        borderColor = MuniGuideBorder,
        contentPadding = 10,
    ) {
        Text(
            text = "Reports by category",
            color = MuniNavy,
            fontSize = 12.5.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        if (categories.isEmpty()) {
            Text(
                text = "No reports filed in this period.",
                color = MuniInkMuted,
                fontSize = 10.sp,
            )
        } else {
            categories.forEachIndexed { index, metric ->
                if (index > 0) Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = metric.category.label,
                        color = MuniNavy,
                        fontSize = 9.sp,
                        lineHeight = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(84.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MuniPillGray),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(metric.count.toFloat() / max)
                                .fillMaxHeight()
                                .background(MuniActionBlue),
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = metric.count.toString(),
                        color = MuniNavy,
                        fontSize = 9.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBreakdownCard(statuses: List<StatusMetric>) {
    val countFor: (ComplaintStatus) -> Int = { status ->
        statuses.firstOrNull { it.status == status }?.count ?: 0
    }
    val pending = countFor(ComplaintStatus.PENDING)
    val reviewing = countFor(ComplaintStatus.ACKNOWLEDGED)
    val resolved = countFor(ComplaintStatus.RESOLVED)
    val total = pending + reviewing + resolved

    PdfCard(
        background = MuniCardPink,
        borderColor = MuniGuideBorder,
        contentPadding = 10,
    ) {
        Text(
            text = "Status breakdown",
            color = MuniNavy,
            fontSize = 12.5.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(7.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MuniPillGray),
        ) {
            if (total > 0) {
                if (pending > 0) {
                    Box(
                        modifier = Modifier
                            .weight(pending.toFloat())
                            .fillMaxHeight()
                            .background(MuniYellow),
                    )
                }
                if (reviewing > 0) {
                    Box(
                        modifier = Modifier
                            .weight(reviewing.toFloat())
                            .fillMaxHeight()
                            .background(MuniActionBlue),
                    )
                }
                if (resolved > 0) {
                    Box(
                        modifier = Modifier
                            .weight(resolved.toFloat())
                            .fillMaxHeight()
                            .background(Color(0xFF2E8B57)),
                    )
                }
            }
        }
        Spacer(Modifier.height(7.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendItem(color = MuniYellow, label = "Pending $pending")
            LegendItem(color = MuniActionBlue, label = "Reviewing $reviewing")
            LegendItem(color = Color(0xFF2E8B57), label = "Resolved $resolved")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, RoundedCornerShape(2.dp)),
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text = label,
            color = MuniNavy,
            fontSize = 8.5.sp,
            lineHeight = 11.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun TopBarangaysCard(barangays: List<BarangayMetric>) {
    PdfCard(
        background = MuniCardPink,
        borderColor = MuniGuideBorder,
        contentPadding = 10,
    ) {
        Text(
            text = "Top barangays",
            color = MuniNavy,
            fontSize = 12.5.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(5.dp))
        if (barangays.isEmpty()) {
            Text(
                text = "No barangay activity in this period.",
                color = MuniInkMuted,
                fontSize = 10.sp,
            )
        } else {
            barangays.take(3).forEachIndexed { index, barangay ->
                if (index > 0) Spacer(Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${index + 1}. ${barangay.name}",
                        color = MuniNavy,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${barangay.count} reports",
                        color = MuniNavy,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

private fun sharePdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(send, "Share analytics snapshot")
    if (context !is Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}
