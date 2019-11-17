package com.till.ui.settings

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.till.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.user_preferences, rootKey)

        val darkMode: SwitchPreference? = findPreference(getString(R.string.pref_dark_mode))

        val config = resources.configuration
        when (config.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                darkMode?.isChecked = false
            } // Night mode is not active, we're using the light theme
            Configuration.UI_MODE_NIGHT_YES -> {
                darkMode?.isChecked = true
            } // Night mode is active, we're using dark theme
        }

        darkMode?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
            }
            true
        }

    }
}
