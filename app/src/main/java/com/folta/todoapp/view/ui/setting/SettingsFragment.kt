package com.folta.todoapp.view.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import com.folta.todoapp.R

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(
                R.id.settings,
                SettingsFragment()
            )
            ?.commit()
        activity?.actionBar?.setDisplayHomeAsUpEnabled(true)

        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}