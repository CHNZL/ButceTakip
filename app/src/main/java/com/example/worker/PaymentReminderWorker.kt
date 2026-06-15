package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.R
import com.example.data.AppDatabase
import java.util.Calendar

import com.example.data.PreferenceManager
import java.text.SimpleDateFormat
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

        val dbTxs = database.transactionDao().getAllTransactionsSync()
        
        val notifyList = dbTxs.filter {
            it.type == com.example.data.TransactionType.EXPENSE && !it.isPaid
        }.filter {
            val isOverdue = it.timestamp < today.timeInMillis
            val isDueToday = it.timestamp in today.timeInMillis..endOfToday.timeInMillis
            val isUpcoming = it.timestamp in (endOfToday.timeInMillis + 1)..upcomingLimit
            
            (isOverdue && prefManager.remindOverdue) ||
            (isDueToday && prefManager.remindDueDay) ||
            isUpcoming // Upcoming is always checked based on limit
        }.sortedBy { it.timestamp }

        if (notifyList.isNotEmpty()) {
            showNotification(notifyList)
        }

        return Result.success()
    }

    private fun showNotification(transactions: List<com.example.data.Transaction>) {
        val count = transactions.size
        val sum = transactions.sumOf { it.amount }
        val amountStr = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("tr", "TR")).format(sum)
        
        val channelId = "payment_reminder_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Ödeme Hatırlatıcıları",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Son günü gelmiş veya gecikmiş ödemeler için hatırlatmalar"
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("tr"))
        
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("$count adet bekleyen ödeme var")
            .setSummaryText("Toplam: $amountStr")

        transactions.take(5).forEach { tx ->
            val dStr = sdf.format(java.util.Date(tx.timestamp))
            val mStr = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("tr", "TR")).format(tx.amount)
            inboxStyle.addLine("$dStr | ${tx.title} | $mStr")
        }
        if (transactions.size > 5) {
            inboxStyle.addLine("... ve ${transactions.size - 5} işlem daha.")
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Ödeme Hatırlatması!")
            .setContentText("Bekleyen $count ödemeniz var. (Toplam: $amountStr)")
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

        if (androidx.core.content.ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            NotificationManagerCompat.from(applicationContext).notify(1001, builder.build())
        }
    }
}
