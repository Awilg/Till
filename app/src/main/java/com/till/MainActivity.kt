package com.till

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.till.notif.NotificationHelper.createNotificationChannel
import timber.log.Timber


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        //TODO: remove before release
        Timber.plant(Timber.DebugTree())

        // Create Notification Channel
        createNotificationChannel(
            this,
            NotificationManagerCompat.IMPORTANCE_DEFAULT, false,
            getString(R.string.app_name), "App notification channel."
        )

        // draw behind status and nav bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        //View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        setUpNavigation()
    }

    private fun setUpNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navHostFragment = findNavController(R.id.nav_host_fragment)
        bottomNavigationView.setupWithNavController(navHostFragment)
        bottomNavigationView.itemIconTintList = null
    }
}
