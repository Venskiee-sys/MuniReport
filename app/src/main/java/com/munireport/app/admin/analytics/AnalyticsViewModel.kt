package com.munireport.app.admin.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.munireport.app.core.common.userMessage
import com.munireport.app.core.utils.DateFormats
import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintCategory
import com.munireport.app.domain.model.ComplaintPriority
import com.munireport.app.domain.model.ComplaintStatus
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.model.VerificationStatus
import com.munireport.app.domain.repository.ComplaintRepository
import com.munireport.app.domain.repository.VerificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AnalyticsViewModel(
    profile: UserProfile,
    complaintRepository: ComplaintRepository,
    verificationRepository: VerificationRepository,
) : ViewModel() {

    private val period = MutableStateFlow(AnalyticsPeriod.QUARTER)

    val uiState = combine(
        complaintRepository.observeComplaints(profile.id),
        verificationRepository.observeQueue(),
        period,
    ) { complaints, verifications, selectedPeriod ->
        val dayMillis = TimeUnit.DAYS.toMillis(1)
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val bucketCount = when (selectedPeriod) {
            AnalyticsPeriod.WEEK -> 7
            AnalyticsPeriod.MONTH -> 10
            AnalyticsPeriod.QUARTER -> 9
        }
        val allTime = selectedPeriod == AnalyticsPeriod.QUARTER
        val bucketMillis = if (allTime) {
            val earliest = complaints.minOfOrNull { it.createdAt } ?: todayStart
            ((todayStart - earliest) / bucketCount).coerceAtLeast(dayMillis)
        } else {
            (selectedPeriod.days / bucketCount).coerceAtLeast(1) * dayMillis
        }
        val rangeStart = if (allTime) {
            complaints.minOfOrNull { it.createdAt } ?: todayStart
        } else {
            todayStart - ((bucketCount - 1) * bucketMillis)
        }
        val previousStart = rangeStart - (bucketCount * bucketMillis)

        val scoped = complaints.filter { it.createdAt >= rangeStart }
        val previous = if (allTime) {
            0
        } else {
            complaints.count { it.createdAt in previousStart until rangeStart }
        }
        val resolved = scoped.filter { it.status == ComplaintStatus.RESOLVED }
        val resolutionHours = resolved
            .map { DateFormats.hoursBetween(it.createdAt, it.updatedAt) }
            .filter { it > 0.0 }
        val activity = (0 until bucketCount).map { index ->
            val start = rangeStart + (index * bucketMillis)
            val end = start + bucketMillis
            DailyMetric(
                label = DateFormats.shortDate(start),
                count = scoped.count { it.createdAt in start until end },
            )
        }
        val statusMetrics = listOf(
            StatusMetric(
                status = ComplaintStatus.PENDING,
                count = scoped.count { it.status == ComplaintStatus.PENDING },
            ),
            StatusMetric(
                status = ComplaintStatus.ACKNOWLEDGED,
                count = scoped.count { it.status.isBeingReviewed },
            ),
            StatusMetric(
                status = ComplaintStatus.RESOLVED,
                count = scoped.count { it.status == ComplaintStatus.RESOLVED },
            ),
        )
        val byBarangay = scoped
            .groupBy { it.authorPurok.ifBlank { "Unassigned" } }
            .map { (name, reports) ->
                BarangayMetric(
                    name = name,
                    count = reports.size,
                    resolved = reports.count { it.status == ComplaintStatus.RESOLVED },
                )
            }
            .sortedByDescending { it.count }
            .take(5)

        AnalyticsUiState(
            isLoading = false,
            period = selectedPeriod,
            totalReports = scoped.size,
            previousPeriodReports = previous,
            openReports = scoped.count { it.status.isOpen },
            resolvedReports = resolved.size,
            urgentOpenReports = scoped.count {
                it.status.isOpen && it.priority == ComplaintPriority.URGENT
            },
            resolutionRate = if (scoped.isEmpty()) 0 else (resolved.size * 100) / scoped.size,
            averageResolutionHours = resolutionHours.takeIf { it.isNotEmpty() }?.average(),
            pendingVerifications = verifications.count { it.status == VerificationStatus.PENDING },
            verifiedResidents = verifications.count { it.status == VerificationStatus.APPROVED },
            categories = ComplaintCategory.entries
                .map { category ->
                    CategoryMetric(
                        category = category,
                        count = scoped.count { it.category == category },
                    )
                }
                .filter { it.count > 0 }
                .sortedByDescending { it.count },
            statuses = statusMetrics,
            lastSevenDays = activity,
            topBarangays = byBarangay,
            generatedAt = System.currentTimeMillis(),
        )
    }.catch { failure ->
        emit(AnalyticsUiState(isLoading = false, errorMessage = failure.userMessage()))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnalyticsUiState(),
    )

    fun onPeriodChange(value: AnalyticsPeriod) {
        period.value = value
    }

    companion object {
        fun factory(
            profile: UserProfile,
            complaintRepository: ComplaintRepository,
            verificationRepository: VerificationRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AnalyticsViewModel(profile, complaintRepository, verificationRepository)
            }
        }
    }
}
