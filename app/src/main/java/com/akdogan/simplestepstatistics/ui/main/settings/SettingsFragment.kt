package com.akdogan.simplestepstatistics.ui.main.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.akdogan.simplestepstatistics.R
import com.akdogan.simplestepstatistics.repository.DataStoreRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private var currentDay = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = DataStoreRepository()

        view.findViewById<Button>(R.id.test_button).setOnClickListener {
            var day = currentDay
            day++
            if (day > 7) day = 1
            viewLifecycleOwner.lifecycleScope.launch {
                repository.setData(
                    requireContext(),
                    day
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            repository.getDataFLow(requireContext()).collect {
                view.findViewById<TextView>(R.id.test_textview).text = "From Settings: $it"
                currentDay = it
            }
        }


    }

    companion object {
        @JvmStatic
        fun newInstance() = SettingsFragment()
    }
}