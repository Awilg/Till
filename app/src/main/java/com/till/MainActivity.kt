package com.till

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.till.data.AppDatabase
import com.till.ui.main.MainFragment
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Set up Logger
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }

}
