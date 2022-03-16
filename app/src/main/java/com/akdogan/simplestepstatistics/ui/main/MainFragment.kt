package com.akdogan.simplestepstatistics.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.akdogan.simplestepstatistics.databinding.MainFragmentBinding
import com.akdogan.simplestepstatistics.helper.DateHelper.timeToSimpleDateString
import com.akdogan.simplestepstatistics.helper.formatDays
import com.akdogan.simplestepstatistics.helper.formatStats
import com.akdogan.simplestepstatistics.helper.toKmRounded
import com.akdogan.simplestepstatistics.repository.DataStoreRepository
import com.akdogan.simplestepstatistics.repository.GoogleFitCommunicator
import com.akdogan.simplestepstatistics.repository.StepStatisticDay
import com.akdogan.simplestepstatistics.repository.totalSteps
import com.google.android.gms.auth.api.signin.GoogleSignIn

class MainFragment : Fragment() {


    companion object {
        fun newInstance() = MainFragment()
        private const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 12345
        private const val TAG = "MainFragment"
    }

    private val viewModel: MainViewModel by viewModels()

    private var startDayOfWeek: Int = 1

    private var binding: MainFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val localBinding = MainFragmentBinding.inflate(inflater, container, false)
        binding = localBinding

        localBinding.composeList.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    StepsItemList(viewModel)
                }
            }
        }

        return localBinding.root
    }

    @Composable
    fun StepsItemList(
        viewModel: MainViewModel
//        dataSet: List<StepStatisticDay>
    ){
        val data: List<StepStatisticDay> by viewModel.liveStatistics.observeAsState(listOf<StepStatisticDay>())
        Column{
            data.forEach {
                if (it.cycledDistance == null) {
                    StepsListElement(item = it)
                } else {
                    StepsListElementWithCycling(item = it)
                }
            }
        }
    }


    @Preview("Simple Item")
    @Composable
    fun ItemPreview() {
        val element = StepStatisticDay(
            date = System.currentTimeMillis(),
            steps = 1234,
            cycledDistance = 7.2f
        )
        StepsListElement(item = element)
    }

    @Preview("Item with cycling")
    @Composable
    fun ItemPreviewCycling() {
        val element = StepStatisticDay(
            date = System.currentTimeMillis(),
            steps = 1234,
            cycledDistance = 7.2f
        )
        StepsListElementWithCycling(item = element)
    }

    @Composable
    fun StepsListElement(item: StepStatisticDay) {
        Row(modifier = Modifier.padding(start = 32.dp, top = 16.dp)) {
            Column {
                Text(timeToSimpleDateString(item.date))
            }
            Column(modifier = Modifier.padding(start = 18.dp)) {
                Text(item.totalSteps().toString())
            }
        }
    }


    @Composable
    fun StepsListElementWithCycling(item: StepStatisticDay) {

        Row(modifier = Modifier.padding(start = 32.dp, top = 16.dp)) {
            Column {
                Row {
                    Text(timeToSimpleDateString(item.date))
                }
                Row(modifier = Modifier.padding(start = 12.dp)) {
                    Text("Steps")
                }
                Row(modifier = Modifier.padding(start = 12.dp)) {
                    Text("Biked km")
                }
            }
            Column {
                Row(modifier = Modifier.padding(start = 18.dp)) {
                    Text(item.totalSteps().toString())
                }
                Row(modifier = Modifier.padding(start = 18.dp)) {
                    Text(item.steps.toString())
                }
                Row(modifier = Modifier.padding(start = 18.dp)) {
                    Text(item.cycledDistance?.toKmRounded().toString())
                }
            }
        }
    }

    fun intArrayToString(array: IntArray): String {
        val builder = StringBuilder()
        builder.append("Start Of Array: ")
        for (item in array) {
            builder.append("[Entry: $item] ")
        }
        return builder.toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenResumed {
            DataStoreRepository.getDataFLow(requireContext()).collect {
                startDayOfWeek = it
                requestOauthFit(it)
            }
        }
        // Start Google Fit Auth Flow
        // requestOauthFit()
        // Initialize SwipeRefresh Layout
        binding?.swipeRefreshLayout?.setOnRefreshListener {
            getGoogleFitData(startDayOfWeek)
        }


        viewModel.loadingDone.observe(viewLifecycleOwner, {
            if (it == true) {
                val res = requireContext().resources
                binding?.apply {
                    textViewDaysList.text = formatDays(viewModel.getDaysAsList(), res)
                    textViewStats.text = formatStats(
                        viewModel.getBreakEvenToday(),
                        viewModel.getLeftDaily(),
                        res
                    )
                    with(stepProgressView) {
                        setGoal(viewModel.goal)
                        setProgress(viewModel.getTotal())
                        visibility = View.VISIBLE
//                        runAnimation()
                    }

                    // Stop Swipe To Refresh Animation if it is running
                    if (swipeRefreshLayout.isRefreshing) {
                        swipeRefreshLayout.isRefreshing = false
                    }
                }
            }
        })
        //statsView.setBackgroundColor(retrieveThemeColor(requireActivity()))

    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }


    private fun requestOauthFit(startDayOfWeek: Int) {
        GoogleFitCommunicator(requireContext()).also {
            if (!it.checkFitAccess()) {
                Log.i(TAG, "Before signin reqest")
                GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, // e.g. 1
                    it.getGoogleAccountStatic(),
                    it.getFitnessOptions()
                )
                Log.i(TAG, "After signin reqest")
            } else {
                getGoogleFitData(startDayOfWeek)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(
            TAG,
            "OnActivityResult called with ResultCode: $resultCode and RequestCode: $requestCode"
        )
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> getGoogleFitData(startDayOfWeek)
                else -> {
                    Log.i(TAG, "Result not from Fit")
                }
            }
            else -> {
                Log.i(TAG, "Permission denied")
            }
        }
    }


    private fun getGoogleFitData(startDayOfWeek: Int) {
        viewModel.getData(startDayOfWeek)
    }
}