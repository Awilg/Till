package com.till.workers

import android.app.NotificationManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.till.data.AppDatabase
import com.till.fromTimestampToFormatMonthDayYear
import com.till.notif.NotificationHelper
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

class PushNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            val database = AppDatabase.getInstance(applicationContext)
            val neglected = database.connectionDao().getNeglectedConnection()

            NotificationHelper.createNotificationChannel(
                applicationContext,
                NotificationManager.IMPORTANCE_MAX, // HIGH
                false,
                "Test Channel",
                "This is a test channel description!"
            )

            val builder = NotificationHelper.createSampleDataNotification(
                applicationContext,
                neglected.name,
                "15m: You last spoke with them on ${neglected.lastContact.fromTimestampToFormatMonthDayYear()}! Want to reach out and catch up?",
                neglected.number,
                "",
                autoCancel = true
            )

            NotificationHelper.showNotification(applicationContext, builder)

            Result.success()
        } catch (ex: Exception) {
            Timber.e("Error seeding database: $ex")
            Result.failure()
        }
    }
}