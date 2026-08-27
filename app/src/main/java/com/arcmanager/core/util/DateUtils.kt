package com.arcmanager.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {

    private val defaultZone: ZoneId = ZoneId.of("Asia/Kolkata")

    private val displayDateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    private val shortDateFormatter = DateTimeFormatter.ofPattern("MMM dd")
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    private val isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    fun today(): LocalDate = LocalDate.now(defaultZone)

    fun now(): ZonedDateTime = ZonedDateTime.now(defaultZone)

    /**
     * Format date for display: "Aug 27, 2026"
     */
    fun formatDisplayDate(date: LocalDate): String = date.format(displayDateFormatter)

    /**
     * Short date: "Aug 27"
     */
    fun formatShortDate(date: LocalDate): String = date.format(shortDateFormatter)

    /**
     * Month year: "August 2026"
     */
    fun formatMonthYear(date: LocalDate): String = date.format(monthYearFormatter)

    /**
     * Format timestamp for display with relative time.
     * Returns "Today, 4:32 PM", "Yesterday", "Aug 20", etc.
     */
    fun formatRelativeDateTime(instant: Instant): String {
        val dateTime = instant.atZone(defaultZone)
        val today = today()
        val date = dateTime.toLocalDate()

        return when {
            date == today -> "Today, ${dateTime.format(timeFormatter)}"
            date == today.minusDays(1) -> "Yesterday"
            date.isAfter(today.minusDays(7)) -> "${daysAgo(date, today)} days ago"
            date.year == today.year -> formatShortDate(date)
            else -> formatDisplayDate(date)
        }
    }

    /**
     * Calculate days until a due date.
     * Positive = future, negative = overdue.
     */
    fun daysUntil(dueDate: LocalDate): Long {
        return ChronoUnit.DAYS.between(today(), dueDate)
    }

    /**
     * Human-readable "due" text.
     */
    fun formatDueText(dueDate: LocalDate): String {
        val days = daysUntil(dueDate)
        return when {
            days < -1 -> "Overdue by ${-days} days"
            days == -1L -> "Overdue by 1 day"
            days == 0L -> "Due today"
            days == 1L -> "Due tomorrow"
            days <= 7 -> "$days days left"
            days <= 30 -> "${days / 7} weeks left"
            else -> "Due ${formatShortDate(dueDate)}"
        }
    }

    /**
     * Check if a date is overdue (before today).
     */
    fun isOverdue(dueDate: LocalDate): Boolean = dueDate.isBefore(today())

    /**
     * Parse ISO timestamp from Supabase.
     */
    fun parseTimestamp(isoString: String?): Instant? {
        if (isoString == null) return null
        return try {
            Instant.parse(isoString)
        } catch (e: Exception) {
            try {
                ZonedDateTime.parse(isoString, isoFormatter).toInstant()
            } catch (e2: Exception) {
                null
            }
        }
    }

    /**
     * Parse local date from string "yyyy-MM-dd".
     */
    fun parseLocalDate(dateString: String?): LocalDate? {
        if (dateString == null) return null
        return try {
            LocalDate.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    private fun daysAgo(date: LocalDate, today: LocalDate): Long {
        return ChronoUnit.DAYS.between(date, today)
    }
}
