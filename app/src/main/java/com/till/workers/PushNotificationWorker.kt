package com.till.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

class PushNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            // Do work
            Timber.i("SCHEDULED PUSH SENT!")
            Result.success()
        } catch (ex: Exception) {
            Timber.e("Error seeding database: $ex")
            Result.failure()
        }
    }
}