package com.folta.todoapp.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
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
                SettingsFragment(this)
            )
            ?.commit()
        activity?.actionBar?.setDisplayHomeAsUpEnabled(true)

        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    class SettingsFragment(private val settingsFragment: com.folta.todoapp.setting.SettingsFragment) :
        PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val tagButton = findPreference<Preference>(getString(R.string.tagSettingButton))

            tagButton?.setOnPreferenceClickListener {
                settingsFragment.findNavController()
                    .navigate(R.id.action_navigation_setting_to_navigation_tag)
                return@setOnPreferenceClickListener true
            }

        }
    }
}