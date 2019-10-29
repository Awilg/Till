package com.till.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.till.data.AppDatabase
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
            Timber.i("NEGLECTED: ${neglected}")

            Result.success()
        } catch (ex: Exception) {
            Timber.e("Error seeding database: $ex")
            Result.failure()
        }
    }
}