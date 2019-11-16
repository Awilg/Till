package com.till.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.till.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.user_preferences, rootKey)
    }
}
