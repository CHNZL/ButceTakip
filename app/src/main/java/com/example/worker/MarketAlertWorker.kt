package com.example.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.data.LiveMarketSnapshot
import com.example.data.MarketAssetOption
import com.example.data.MarketDataFetcher
import com.example.data.MarketPriceAlarm
import com.example.data.MarketPriceParser
import com.example.data.MarketScheduleConfig
import com.example.data.PreferenceManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object MarketAlertScheduler {
    private const val WORK_NAME = "MarketAlertPeriodicWork"

    /**
     * Synchronizes background WorkManager periodic schedule according to active price alarms
     * and scheduled summary settings. Cancels work completely when both are inactive.
     */
    fun syncSchedule(context: Context) {
        val prefs = PreferenceManager(context)
        val schedule = prefs.marketScheduleConfig
        val activeAlarms = prefs.marketPriceAlarms.filter { it.isEnabled }
        val wm = WorkManager.getInstance(context)

        if (!schedule.enabled && activeAlarms.isEmpty()) {
            wm.cancelUniqueWork(WORK_NAME)
            return
        }

        // Price alarms check every 15 min (minimum Android WorkManager periodic interval).
        // Periodic summaries alone use the user's chosen interval.
        val intervalMinutes = if (activeAlarms.isNotEmpty()) {
            15L
        } else {
            schedule.intervalMinutes.coerceAtLeast(15).toLong()
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<MarketAlertWorker>(
            intervalMinutes,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        wm.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}

object MarketNotificationHelper {
    private const val SUMMARY_CHANNEL_ID = "market_summary_channel"
    private const val LIMIT_CHANNEL_ID = "market_limit_channel"
    private const val SUMMARY_NOTIFICATION_ID = 2001
    private const val COOLDOWN_MILLIS = 60L * 60L * 1000L // 60 minutes cooldown for recurring alarms

    private fun hasNotificationPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val summaryChannel = NotificationChannel(
                SUMMARY_CHANNEL_ID,
                "Canlı Piyasa Özeti",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Belirlediğiniz saatlerde ve sıklıkta canlı altın ve döviz kurları"
            }

            val limitChannel = NotificationChannel(
                LIMIT_CHANNEL_ID,
                "Piyasa Fiyat Alarmları",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canlı piyasa fiyatları hedef limitlerinize ulaştığında anlık uyarılar"
            }

            nm.createNotificationChannel(summaryChannel)
            nm.createNotificationChannel(limitChannel)
        }
    }

    private fun buildContentIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            200,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun isWithinTimeWindow(
        startTime: String,
        endTime: String,
        weekdaysOnly: Boolean,
        nowCal: Calendar = Calendar.getInstance()
    ): Boolean {
        if (weekdaysOnly) {
            val dow = nowCal.get(Calendar.DAY_OF_WEEK)
            if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) return false
        }
        val curTotal = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
        val startParts = startTime.split(":")
        val endParts = endTime.split(":")
        if (startParts.size < 2 || endParts.size < 2) return true

        val startTotal = (startParts[0].toIntOrNull() ?: 9) * 60 + (startParts[1].toIntOrNull() ?: 0)
        val endTotal = (endParts[0].toIntOrNull() ?: 18) * 60 + (endParts[1].toIntOrNull() ?: 0)

        return if (startTotal <= endTotal) {
            curTotal in startTotal..endTotal
        } else {
            curTotal >= startTotal || curTotal <= endTotal
        }
    }

    /**
     * Evaluates active price alarms against a LiveMarketSnapshot and dispatches notifications
     * for any triggered limits. Returns the updated list if any alarm state changed.
     */
    fun evaluatePriceAlarms(
        context: Context,
        prefs: PreferenceManager,
        snapshot: LiveMarketSnapshot
    ): List<MarketPriceAlarm> {
        val alarms = prefs.marketPriceAlarms
        if (alarms.none { it.isEnabled }) return alarms

        val catalog = MarketPriceParser.buildCatalog(
            snapshot.goldPrices,
            snapshot.yapiKrediRates,
            snapshot.ziraatRates
        ).associateBy { it.key }

        val now = System.currentTimeMillis()
        var changed = false
        val updated = alarms.map { alarm ->
            if (!alarm.isEnabled) return@map alarm
            val option = catalog[alarm.assetKey] ?: return@map alarm
            val liveValue = if (alarm.isSell) option.sellValue else option.buyValue
            if (liveValue == null || liveValue <= 0.0) return@map alarm

            val conditionMet = if (alarm.isAbove) {
                liveValue >= alarm.targetPrice
            } else {
                liveValue <= alarm.targetPrice
            }

            if (conditionMet && (now - alarm.lastTriggeredAt >= COOLDOWN_MILLIS)) {
                sendPriceAlarmNotification(context, alarm, liveValue)
                changed = true
                alarm.copy(
                    isEnabled = if (alarm.oneTime) false else true,
                    lastTriggeredAt = now
                )
            } else {
                alarm
            }
        }

        if (changed) {
            prefs.marketPriceAlarms = updated
            MarketAlertScheduler.syncSchedule(context)
        }
        return updated
    }

    fun sendPriceAlarmNotification(
        context: Context,
        alarm: MarketPriceAlarm,
        currentPrice: Double
    ): Boolean {
        if (!hasNotificationPermission(context)) return false
        ensureChannels(context)

        val fieldLabel = if (alarm.isSell) "Satış" else "Alış"
        val dirSymbol = if (alarm.isAbove) "≥" else "≤"
        val dirWord = if (alarm.isAbove) "üzerine çıktı" else "altına düştü"
        val curText = MarketPriceParser.formatPrice(currentPrice)
        val targetText = MarketPriceParser.formatPrice(alarm.targetPrice)

        val title = "Fiyat Alarmı: ${alarm.assetLabel}"
        val body = "$fieldLabel $curText ile hedefinizin ($dirSymbol $targetText) $dirWord."

        val builder = NotificationCompat.Builder(context, LIMIT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(buildContentIntent(context))
            .setAutoCancel(true)

        val notifId = 3000 + (alarm.id.hashCode() and 0x7FFF)
        NotificationManagerCompat.from(context).notify(notifId, builder.build())
        return true
    }

    fun sendMarketSummaryNotification(
        context: Context,
        selectedKeys: Set<String>,
        snapshot: LiveMarketSnapshot
    ): Boolean {
        if (!hasNotificationPermission(context)) return false
        ensureChannels(context)

        val catalog = MarketPriceParser.buildCatalog(
            snapshot.goldPrices,
            snapshot.yapiKrediRates,
            snapshot.ziraatRates
        ).associateBy { it.key }

        val lines = mutableListOf<String>()
        selectedKeys.forEach { key ->
            val opt: MarketAssetOption = catalog[key] ?: return@forEach
            val hasBuy = opt.buyValue != null
            val hasSell = opt.sellValue != null
            if (!hasBuy && !hasSell) return@forEach
            val pricePart = when {
                hasBuy && hasSell -> "Alış ${opt.buyText} • Satış ${opt.sellText}"
                hasSell -> "Satış ${opt.sellText}"
                else -> "Alış ${opt.buyText}"
            }
            lines.add("${opt.label}: $pricePart")
        }

        if (lines.isEmpty()) return false

        val timeStr = SimpleDateFormat("HH:mm", Locale("tr", "TR")).format(Date())
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("Canlı Piyasa Özeti ($timeStr)")
            .setSummaryText("${lines.size} enstrüman güncel")

        lines.take(6).forEach { inboxStyle.addLine(it) }
        if (lines.size > 6) {
            inboxStyle.addLine("... ve ${lines.size - 6} kur daha")
        }

        val builder = NotificationCompat.Builder(context, SUMMARY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Canlı Piyasa Özeti ($timeStr)")
            .setContentText(lines.first())
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(buildContentIntent(context))
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(SUMMARY_NOTIFICATION_ID, builder.build())
        return true
    }
}

class MarketAlertWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = PreferenceManager(applicationContext)
        val schedule: MarketScheduleConfig = prefs.marketScheduleConfig
        val activeAlarms = prefs.marketPriceAlarms.filter { it.isEnabled }

        if (!schedule.enabled && activeAlarms.isEmpty()) {
            return Result.success()
        }

        val now = System.currentTimeMillis()
        val intervalMs = schedule.intervalMinutes.coerceAtLeast(15) * 60L * 1000L
        val toleranceMs = 2L * 60L * 1000L // allow small WorkManager flex window
        val lastSent = prefs.lastMarketSummarySentAt

        val shouldSendSummary = schedule.enabled &&
            !prefs.isInSilentHours() &&
            MarketNotificationHelper.isWithinTimeWindow(
                startTime = schedule.startTime,
                endTime = schedule.endTime,
                weekdaysOnly = schedule.weekdaysOnly
            ) &&
            (now - lastSent >= (intervalMs - toleranceMs).coerceAtLeast(0L)) &&
            schedule.selectedAssetKeys.isNotEmpty()

        val keysToFetch = mutableSetOf<String>()
        if (shouldSendSummary) {
            keysToFetch.addAll(schedule.selectedAssetKeys)
        }
        activeAlarms.forEach { keysToFetch.add(it.assetKey) }

        if (keysToFetch.isEmpty()) {
            return Result.success()
        }

        val snapshot = MarketDataFetcher.fetchRequiredSources(keysToFetch)

        if (shouldSendSummary) {
            val sent = MarketNotificationHelper.sendMarketSummaryNotification(
                context = applicationContext,
                selectedKeys = schedule.selectedAssetKeys,
                snapshot = snapshot
            )
            if (sent) {
                prefs.lastMarketSummarySentAt = now
            }
        }

        if (activeAlarms.isNotEmpty()) {
            MarketNotificationHelper.evaluatePriceAlarms(
                context = applicationContext,
                prefs = prefs,
                snapshot = snapshot
            )
        }

        return Result.success()
    }
}
