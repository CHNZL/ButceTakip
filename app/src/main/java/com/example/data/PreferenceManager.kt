package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("cloud_sync_prefs", Context.MODE_PRIVATE)

    var userId: String
        get() = prefs.getString("user_id", "") ?: ""
        set(value) = prefs.edit().putString("user_id", value).apply()

    var userName: String
        get() = prefs.getString("user_name", "") ?: ""
        set(value) = prefs.edit().putString("user_name", value).apply()

    var profilePicUrl: String
        get() = prefs.getString("profile_pic_url", "") ?: ""
        set(value) = prefs.edit().putString("profile_pic_url", value).apply()

    var remindDueDay: Boolean
        get() = prefs.getBoolean("remind_due_day", true)
        set(value) = prefs.edit().putBoolean("remind_due_day", value).apply()

    var remindOverdue: Boolean
        get() = prefs.getBoolean("remind_overdue", true)
        set(value) = prefs.edit().putBoolean("remind_overdue", value).apply()

    var remindUpcomingDays: Int
        get() = prefs.getInt("remind_upcoming_days", 3)
        set(value) = prefs.edit().putInt("remind_upcoming_days", value).apply()

    var silentHoursEnabled: Boolean
        get() = prefs.getBoolean("silent_hours_enabled", true)
        set(value) = prefs.edit().putBoolean("silent_hours_enabled", value).apply()

    var silentHoursStart: String
        get() = prefs.getString("silent_hours_start", "00:00") ?: "00:00"
        set(value) = prefs.edit().putString("silent_hours_start", value).apply()

    var silentHoursEnd: String
        get() = prefs.getString("silent_hours_end", "08:00") ?: "08:00"
        set(value) = prefs.edit().putString("silent_hours_end", value).apply()

    fun isInSilentHours(): Boolean {
        if (!silentHoursEnabled) return false
        val currentCal = java.util.Calendar.getInstance()
        val curHour = currentCal.get(java.util.Calendar.HOUR_OF_DAY)
        val curMin = currentCal.get(java.util.Calendar.MINUTE)
        val curTotal = curHour * 60 + curMin

        val startParts = silentHoursStart.split(":")
        val endParts = silentHoursEnd.split(":")
        if (startParts.size < 2 || endParts.size < 2) return false

        val startHour = startParts[0].toIntOrNull() ?: 0
        val startMin = startParts[1].toIntOrNull() ?: 0
        val startTotal = startHour * 60 + startMin

        val endHour = endParts[0].toIntOrNull() ?: 8
        val endMin = endParts[1].toIntOrNull() ?: 0
        val endTotal = endHour * 60 + endMin

        return if (startTotal <= endTotal) {
            curTotal in startTotal..endTotal
        } else {
            curTotal >= startTotal || curTotal <= endTotal
        }
    }

    fun getCustomPrice(category: String): Double? {
        val key = "custom_price_${category.trim().lowercase()}"
        if (!prefs.contains(key)) return null
        return prefs.getFloat(key, 0.0f).toDouble()
    }

    fun setCustomPrice(category: String, price: Double) {
        val key = "custom_price_${category.trim().lowercase()}"
        val timeKey = "custom_price_time_${category.trim().lowercase()}"
        prefs.edit()
            .putFloat(key, price.toFloat())
            .putLong(timeKey, System.currentTimeMillis())
            .apply()
    }

    fun getCustomPriceTime(category: String): Long {
        val timeKey = "custom_price_time_${category.trim().lowercase()}"
        return prefs.getLong(timeKey, 0L)
    }

    fun getMarketMatch(category: String): String? {
        val key = "market_match_${category.trim().lowercase(java.util.Locale("tr", "TR"))}"
        return prefs.getString(key, null)
    }

    fun setMarketMatch(category: String, marketSourceCode: String?) {
        val key = "market_match_${category.trim().lowercase(java.util.Locale("tr", "TR"))}"
        if (marketSourceCode == null) {
            prefs.edit().remove(key).apply()
        } else {
            prefs.edit().putString(key, marketSourceCode).apply()
        }
    }
}
