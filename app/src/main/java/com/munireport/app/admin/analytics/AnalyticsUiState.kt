package com.munireport.app.admin.analytics

import com.munireport.app.domain.model.ComplaintCategory
import com.munireport.app.domain.model.ComplaintStatus

enum class AnalyticsPeriod(val label: String, val days: Int) {
    WEEK("This week", 7),
    MONTH("This month", 30),
    QUARTER("All time", 90),
}

data class CategoryMetric(
    val category: ComplaintCategory,
    val count: Int,
)

data class StatusMetric(
    val status: ComplaintStatus,
    val count: Int,
)

data class DailyMetric(
    val label: String,
    val count: Int,
)

data class BarangayMetric(
    val name: String,
    val count: Int,
    val resolved: Int,
) {
    val resolutionRate: Int
        get() = if (count == 0) 0 else (resolved * 100) / count
}

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val period: AnalyticsPeriod = AnalyticsPeriod.QUARTER,
    val totalReports: Int = 0,
    val previousPeriodReports: Int = 0,
    val openReports: Int = 0,
    val resolvedReports: Int = 0,
    val urgentOpenReports: Int = 0,
    val resolutionRate: Int = 0,
    val averageResolutionHours: Double? = null,
    val pendingVerifications: Int = 0,
    val verifiedResidents: Int = 0,
    val categories: List<CategoryMetric> = emptyList(),
    val statuses: List<StatusMetric> = emptyList(),
    val lastSevenDays: List<DailyMetric> = emptyList(),
    val topBarangays: List<BarangayMetric> = emptyList(),
    val generatedAt: Long = System.currentTimeMillis(),
) {
    val periodChange: Int
        get() = totalReports - previousPeriodReports
}
