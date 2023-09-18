package io.castled.android.notifications.commons

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class DateTimeUtils {
    companion object {
        private const val defaultPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

        fun getCurrentTimeFormatted(customPattern: String? = null): String {
            val patternToUse = customPattern ?: defaultPattern
            val dateFormat = SimpleDateFormat(patternToUse, Locale.US)
            return dateFormat.format(Date())
        }

        fun  getDateFromEpochTime(timestamp: Long): Date{
             return Date(timestamp)
        }
    }

}