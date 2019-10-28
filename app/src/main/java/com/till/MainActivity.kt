package com.till

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.till.notif.NotificationHelper.createNotificationChannel
import com.till.workers.PushNotificationWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Set up Logger
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Create Notification Channel
        createNotificationChannel(
            this,
            NotificationManagerCompat.IMPORTANCE_DEFAULT, false,
            getString(R.string.app_name), "App notification channel."
        )

        // Start push notification worker
        val request =
            PeriodicWorkRequestBuilder<PushNotificationWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(applicationContext).enqueue(request)

        setUpNavigation()
    }

    private fun setUpNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navHostFragment = findNavController(R.id.nav_host_fragment)
        bottomNavigationView.setupWithNavController(navHostFragment)
        bottomNavigationView.itemIconTintList = null
    }
}
