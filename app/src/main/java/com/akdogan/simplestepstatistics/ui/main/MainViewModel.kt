package com.akdogan.simplestepstatistics.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.akdogan.simplestepstatistics.repository.GoogleFitCommunicator
import com.akdogan.simplestepstatistics.repository.StepStatisticDay
import com.akdogan.simplestepstatistics.repository.StepStatisticModel
import com.akdogan.simplestepstatistics.widget.callBackCreator

const val weeklyGoal = 60000
const val daysInPeriod = 7
const val TAG = "MainViewModel"


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var statistics = StepStatisticModel(weeklyGoal, daysInPeriod)
    val goal = weeklyGoal

    private val _loadingDone = MutableLiveData<Boolean>()
    val loadingDone: LiveData<Boolean>
        get() = _loadingDone

    private val googleFitCommunicator = GoogleFitCommunicator(application)

    // account verification needed

    fun getData() {
        googleFitCommunicator.accessGoogleFitStatic(
            successFunction = {
                statistics = it
                _loadingDone.value = true
                // TODO add function to update app Widgets
                callBackCreator(getApplication()).invoke(it)
            })
    }

    fun getTotal(): Int {
        return statistics.getTotalStepCount()
    }

    fun getDaysAsList(): List<StepStatisticDay> {
        return statistics.getDaysAsList()
    }

    fun getBreakEvenToday(): Int {
        return statistics.getRequiredTodayForBreakEven()
    }

    fun getLeftDaily(): Int {
        return statistics.getRequiredPerDayUpcoming()
    }


}