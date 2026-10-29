package com.munireport.app.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateFormats {
    private fun formatter(pattern: String): SimpleDateFormat =
        SimpleDateFormat(pattern, Locale.getDefault())

    fun date(millis: Long): String = formatter("MMM d, yyyy").format(Date(millis))

    fun dateTime(millis: Long): String = formatter("MMM d, yyyy · h:mm a").format(Date(millis))

    fun shortDate(millis: Long): String = formatter("MMM d").format(Date(millis))

    fun shortDateTime(millis: Long): String = formatter("MMM d, h:mm a").format(Date(millis))

    fun monthYear(millis: Long): String = formatter("MMMM yyyy").format(Date(millis))

    fun weekdayDate(millis: Long): String = formatter("EEEE, MMMM d").format(Date(millis))

    fun reportStamp(millis: Long): String = formatter("yyyy-MM-dd-HHmm").format(Date(millis))

    fun relative(millis: Long, now: Long = System.currentTimeMillis()): String {
        val difference = (now - millis).coerceAtLeast(0L)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(difference)
        val hours = TimeUnit.MILLISECONDS.toHours(difference)
        val days = TimeUnit.MILLISECONDS.toDays(difference)
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            days < 365 -> date(millis)
            else -> date(millis)
        }
    }

    fun hoursBetween(from: Long, to: Long): Double =
        TimeUnit.MILLISECONDS.toMinutes((to - from).coerceAtLeast(0L)) / 60.0

    fun durationLabel(hours: Double): String {
        if (hours <= 0.0) return "Not available"
        return if (hours < 24) {
            val rounded = (hours * 10).toInt() / 10.0
            "$rounded hours"
        } else {
            val days = (hours / 24 * 10).toInt() / 10.0
            "$days days"
        }
    }
}
