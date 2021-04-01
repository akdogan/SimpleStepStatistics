package com.akdogan.simplestepstatistics.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.akdogan.simplestepstatistics.R
import com.akdogan.simplestepstatistics.helper.formatDays
import com.akdogan.simplestepstatistics.helper.formatStats
import com.akdogan.simplestepstatistics.repository.GoogleFitCommunicator
import com.akdogan.simplestepstatistics.ui.StepProgressView
import com.akdogan.simplestepstatistics.ui.runAnimation
import com.google.android.gms.auth.api.signin.GoogleSignIn

class MainFragment : Fragment() {


    companion object {
        fun newInstance() = MainFragment()
        private const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 12345
        private const val TAG = "MainFragment"
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    fun intArrayToString(array: IntArray): String{
        val builder = StringBuilder()
        builder.append("Start Of Array: ")
        for (item in array){
            builder.append("[Entry: $item] ")
        }
        return builder.toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Create ViewModel
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(MainViewModel::class.java)
        // Start Google Fit Auth Flow
        requestOauthFit()
        // Initialize SwipeRefresh Layout
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            getGoogleFitData()
        }


        viewModel.loadingDone.observe(viewLifecycleOwner, {
            if (it == true) {
                //val total = view.findViewById<TextView>(R.id.text_view_total)
                val list = view.findViewById<TextView>(R.id.text_view_days_list)
                val stats = view.findViewById<TextView>(R.id.text_view_stats)
                val statsView = view.findViewById<StepProgressView>(R.id.step_progress_view)
                val res = requireContext().resources
                //total.text = formatTotalWithGoal(viewModel.goal, viewModel.getTotal(), res)
                list.text = formatDays(viewModel.getDaysAsList(), res)
                stats.text = formatStats(
                    viewModel.getBreakEvenToday(),
                    viewModel.getLeftDaily(),
                    res
                )
                with(statsView){
                    setGoal(viewModel.goal)
                    setProgress(viewModel.getTotal())
                    visibility = View.VISIBLE
                    runAnimation()
                }

                // Stop Swipe To Refresh Animation if it is runnig
                if (swipeRefreshLayout.isRefreshing){
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        })
        //statsView.setBackgroundColor(retrieveThemeColor(requireActivity()))

    }



    private fun requestOauthFit() {
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
                getGoogleFitData()
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
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> getGoogleFitData()
                else -> {
                    Log.i(TAG, "Result not from Fit")
                }
            }
            else -> {
                Log.i(TAG, "Permission denied")
            }
        }
    }


    private fun getGoogleFitData() {
        viewModel.getData()
    }
}