package com.till.workers

import android.app.NotificationManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.till.data.AppDatabase
import com.till.data.Connection
import com.till.fromTimestampToFormatMonthDayYear
import com.till.notif.NotificationHelper
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import java.util.*

class PushNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            val database = AppDatabase.getInstance(applicationContext)
            val neglectedContacts = database.connectionDao().getConnectionsByLastContact()

            // pick some contact
            val neglected = neglectedContacts.value!!.stream().filter {
                filterBetweenMonths(it)
            }.findAny().get()

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

    private fun filterBetweenMonths(connection: Connection): Boolean {
        val d = Date(connection.lastContact.toLong())
        val now = System.currentTimeMillis()
        // 12 month
        val twelveMonths = now - 31540000000
        // 3 month
        val threeMonths = now - 7884000000
        return d.after(Date(twelveMonths)) && d.before(Date(threeMonths))
    }
}