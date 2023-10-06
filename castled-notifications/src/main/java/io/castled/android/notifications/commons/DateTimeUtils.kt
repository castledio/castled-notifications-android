package io.castled.android.notifications.commons

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal class DateTimeUtils {
    companion object {
        private const val defaultPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

        internal fun getCurrentTimeFormatted(customPattern: String? = null): String {
            val patternToUse = customPattern ?: defaultPattern
            val dateFormat = SimpleDateFormat(patternToUse, Locale.US)
            return dateFormat.format(Date())
        }

        internal fun getDateFromEpochTime(timestamp: Long): Date {
            return Date(timestamp)
        }

        internal fun timeAgo(date: Date, defaultFormat: String? = "MMM d, yyyy"): String {
            val now = Date()
            val timeDifference = now.time - date.time

            val seconds = timeDifference / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            return when {
                days >= 7 -> {
                    val dateFormatter = SimpleDateFormat(defaultFormat, Locale.getDefault())
                    dateFormatter.timeZone = TimeZone.getDefault()
                    dateFormatter.format(date)
                }

                days >= 1 -> "$days day${if (days > 1) "s" else ""} ago"
                hours >= 1 -> "$hours hour${if (hours > 1) "s" else ""} ago"
                minutes >= 1 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
                else -> "Just now"
            }
        }
    }
}