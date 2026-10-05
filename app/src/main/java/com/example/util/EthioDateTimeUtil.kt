package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object EthioDateTimeUtil {

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val timeWithSecondsFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatTimeWithSeconds(timestamp: Long): String {
        return timeWithSecondsFormat.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        return dateOnlyFormat.format(Date(timestamp))
    }

    /**
     * In Ethiopian culture, time is counted in a 12-hour cycle starting at dawn (6:00 AM Gregorian is 12:00 morning Ethio).
     * For example:
     * 7:00 AM -> 1:00 am (ጧት 1:00)
     * 8:30 AM -> 2:30 am (ጧት 2:30)
     * 1:00 PM -> 7:00 afternoon (ከሰዓት 7:00)
     * 6:00 PM -> 12:00 evening (ማታ 12:00)
     */
    fun getEthiopianTime(timestamp: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val minutes = cal.get(Calendar.MINUTE)

        val ethioHour = when {
            hour24 >= 6 -> (hour24 - 6) % 12
            else -> (hour24 + 6) % 12
        }.let { if (it == 0) 12 else it }

        val period = when (hour24) {
            in 6..11 -> "Morning (ጧት)"
            in 12..17 -> "Afternoon (ከሰዓት)"
            in 18..23 -> "Night (ማታ)"
            else -> "Dawn (ንጋት)"
        }

        return String.format(Locale.getDefault(), "%d:%02d %s", ethioHour, minutes, period)
    }

    /**
     * Approximate Ethiopian Calendar Date.
     * Meskerem 1 is Sept 11/12.
     */
    fun getEthiopianDateApprox(timestamp: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) // 0-based
        val day = cal.get(Calendar.DAY_OF_MONTH)

        // Gregorian to Ethiopic rough offset (Ethiopian year is 7-8 years behind)
        val ethioYear = if (month > 8 || (month == 8 && day >= 11)) year - 7 else year - 8

        val ethioMonths = arrayOf(
            "Meskerem (መስከረም)", "Tikimt (ጥቅምት)", "Hidar (ኅዳር)",
            "Tahsas (ታኅሣሥ)", "Tir (ጥር)", "Yakatit (የካቲት)",
            "Megabit (መጋቢት)", "Miazia (ሚያዝያ)", "Ginbot (ግንቦት)",
            "Sene (ሰኔ)", "Hamle (ሐምሌ)", "Nehase (ነሐሴ)", "Pagume (ጳጉሜ)"
        )

        // Simple mapping for display purposes
        val approxIndex = (month + 4) % 12
        return "${ethioMonths[approxIndex]}, $ethioYear E.C."
    }

    fun isPunctual(timestamp: Long, expectedHour: Int = 8, expectedMinute: Int = 30): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val min = cal.get(Calendar.MINUTE)
        return hour < expectedHour || (hour == expectedHour && min <= expectedMinute)
    }

    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun formatDuration(durationMillis: Long): String {
        val totalMinutes = durationMillis / (1000 * 60)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }
}
