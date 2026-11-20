package com.example.worker

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
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.MonthlyPaymentEvaluator
import com.example.data.MonthlyRuleChecklistItem
import com.example.data.PreferenceManager
import com.example.data.Transaction
import com.example.data.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PaymentReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefManager = PreferenceManager(applicationContext)
        if (prefManager.isInSilentHours()) {
            return Result.success()
        }

        val database = AppDatabase.getDatabase(applicationContext)
        val dbTxs = database.transactionDao().getAllTransactionsSync()

        // 1. Existing unpaid bills reminder (due today, overdue, upcoming)
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val upcomingLimit = Calendar.getInstance().apply {
            timeInMillis = endOfToday.timeInMillis
            add(Calendar.DAY_OF_MONTH, prefManager.remindUpcomingDays)
        }.timeInMillis

        val notifyList = dbTxs.filter {
            it.type == TransactionType.EXPENSE && !it.isPaid
        }.filter {
            val isOverdue = it.timestamp < today.timeInMillis
            val isDueToday = it.timestamp in today.timeInMillis..endOfToday.timeInMillis
            val isUpcoming = it.timestamp in (endOfToday.timeInMillis + 1)..upcomingLimit

            (isOverdue && prefManager.remindOverdue) ||
                (isDueToday && prefManager.remindDueDay) ||
                isUpcoming
        }.sortedBy { it.timestamp }

        if (notifyList.isNotEmpty()) {
            showNotification(notifyList)
        }

        // 2. Missing expected monthly payment items reminder (at most once per day)
        if (prefManager.remindMissingMonthlyPayments) {
            val rules = prefManager.monthlyPaymentRules
            val todayKey = MonthlyPaymentEvaluator.currentDateKey()
            if (rules.isNotEmpty() && prefManager.lastMissingReminderDate != todayKey) {
                val missingNow = MonthlyPaymentEvaluator.getMissingDueNow(
                    rules = rules,
                    transactions = dbTxs
                )
                if (missingNow.isNotEmpty()) {
                    val sent = MissingPaymentNotificationHelper.sendMissingPaymentsNotification(
                        context = applicationContext,
                        missingItems = missingNow,
                        isTest = false
                    )
                    if (sent) {
                        prefManager.lastMissingReminderDate = todayKey
                    }
                }
            }
        }

        return Result.success()
    }

    private fun showNotification(transactions: List<Transaction>) {
        val count = transactions.size
        val sum = transactions.sumOf { it.amount }
        val amountStr = NumberFormat.getCurrencyInstance(Locale("tr", "TR")).format(sum)

        ensurePaymentChannel(applicationContext)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("tr"))

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("$count adet bekleyen ödeme var")
            .setSummaryText("Toplam: $amountStr")

        transactions.take(5).forEach { tx ->
            val dStr = sdf.format(Date(tx.timestamp))
            val mStr = NumberFormat.getCurrencyInstance(Locale("tr", "TR")).format(tx.amount)
            inboxStyle.addLine("$dStr | ${tx.title} | $mStr")
        }
        if (transactions.size > 5) {
            inboxStyle.addLine("... ve ${transactions.size - 5} işlem daha.")
        }

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Ödeme Hatırlatması!")
            .setContentText("Bekleyen $count ödemeniz var. (Toplam: $amountStr)")
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (hasNotificationPermission(applicationContext)) {
            NotificationManagerCompat.from(applicationContext).notify(1001, builder.build())
        }
    }

    companion object {
        const val CHANNEL_ID = "payment_reminder_channel"

        fun ensurePaymentChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Ödeme Hatırlatıcıları",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Son günü gelmiş, gecikmiş veya bu ay girilmemiş ödemeler için hatırlatmalar"
                }
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun hasNotificationPermission(context: Context): Boolean {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        }
    }
}

object MissingPaymentNotificationHelper {
    private const val NOTIFICATION_ID = 1002

    fun sendMissingPaymentsNotification(
        context: Context,
        missingItems: List<MonthlyRuleChecklistItem>,
        isTest: Boolean = false
    ): Boolean {
        if (!PaymentReminderWorker.hasNotificationPermission(context)) return false
        PaymentReminderWorker.ensurePaymentChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("tr", "TR")).apply {
            maximumFractionDigits = 0
        }

        val count = missingItems.size
        val title = if (isTest) {
            "Test · Eksik Aylık Ödeme Hatırlatıcısı"
        } else {
            "Bu Ay Girilmemiş $count Ödeme Var"
        }

        val previewNames = missingItems.take(3).joinToString(", ") { it.displayTitle }
        val contentText = if (missingItems.isEmpty()) {
            "Takip edilen tüm aylık ödemeleriniz bu ay için girilmiş görünüyor."
        } else if (count > 3) {
            "$previewNames ve ${count - 3} kalem daha henüz girilmedi."
        } else {
            "$previewNames henüz bu ay girilmedi."
        }

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
            .setSummaryText(
                if (missingItems.isEmpty()) "Aylık Ödeme Kontrolü"
                else "Ödemeler sekmesinden tek dokunuşla ekleyin"
            )

        if (missingItems.isEmpty()) {
            inboxStyle.addLine("Bu ay için eksik aylık ödeme bulunmuyor.")
        } else {
            missingItems.take(5).forEach { item ->
                val amountPart = item.suggestedAmount?.let { " · ~${currencyFormat.format(it)}" } ?: ""
                inboxStyle.addLine("${item.displayTitle} (Ayın ${item.remindAfterDay}. günü)$amountPart")
            }
            if (missingItems.size > 5) {
                inboxStyle.addLine("... ve ${missingItems.size - 5} kalem daha.")
            }
        }

        val builder = NotificationCompat.Builder(context, PaymentReminderWorker.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        return true
    }
}
