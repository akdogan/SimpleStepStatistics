package com.akdogan.simplestepstatistics.ui.main.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.akdogan.simplestepstatistics.databinding.FragmentSettingsBinding
import com.akdogan.simplestepstatistics.repository.DataStoreRepository
import com.akdogan.simplestepstatistics.ui.main.settings.selectionlist.DayOfWeekSelectionItem
import com.akdogan.simplestepstatistics.ui.main.settings.selectionlist.DayOfWeekSelectionListAdapter
import kotlinx.coroutines.flow.collect

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {

    private var binding: FragmentSettingsBinding? = null
    private var dataList: List<DayOfWeekSelectionItem>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false)


        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        dataList = generateDaysList()

        val adapter = DayOfWeekSelectionListAdapter(){
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                DataStoreRepository.setData(requireContext(), it.number)
            }
        }

        binding?.daySelection?.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            DataStoreRepository.getDataFLow(requireContext()).collect { dataRepoDayNumber ->
                Toast.makeText(activity, "DataStore changed to $dataRepoDayNumber", Toast.LENGTH_LONG).show()

                var list = dataList ?: return@collect
                list = list.map {
                    if (it.day.number == dataRepoDayNumber){
                        it.copy(selected = true)
                    } else {
                        it.copy(selected = false)
                    }
                }
                dataList = list
                adapter.submitList(list)
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun generateDaysList(): List<DayOfWeekSelectionItem> {
        val result = mutableListOf<DayOfWeekSelectionItem>()
        DayOfWeek.values().forEach {
            result.add(DayOfWeekSelectionItem(it))
        }
        return result
    }

    companion object {
        @JvmStatic
        fun newInstance() = SettingsFragment()
    }
}