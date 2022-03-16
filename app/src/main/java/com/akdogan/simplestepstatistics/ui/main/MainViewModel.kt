package com.akdogan.simplestepstatistics.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akdogan.simplestepstatistics.FALLBACK_DAYS_IN_PERIOD
import com.akdogan.simplestepstatistics.FALLBACK_WEEKLY_GOAL
import com.akdogan.simplestepstatistics.repository.GoogleFitCommunicator
import com.akdogan.simplestepstatistics.repository.StepStatisticDay
import com.akdogan.simplestepstatistics.repository.StepStatisticModel
import com.akdogan.simplestepstatistics.widget.callBackCreator
import kotlinx.coroutines.launch

const val TAG = "MainViewModel"


class MainViewModel(application: Application) : AndroidViewModel(application) {
    // TODO Should be taken from sharedPrefs later on
    private var weeklyGoal = FALLBACK_WEEKLY_GOAL
    private var daysInPeriod = FALLBACK_DAYS_IN_PERIOD

    private var statistics = StepStatisticModel(weeklyGoal, daysInPeriod)
    val goal = weeklyGoal

    private val _liveStatistics = MutableLiveData<List<StepStatisticDay>>()
    val liveStatistics : LiveData<List<StepStatisticDay>>
        get() = _liveStatistics

    private val _loadingDone = MutableLiveData<Boolean>()
    val loadingDone: LiveData<Boolean>
        get() = _loadingDone

    private val googleFitCommunicator = GoogleFitCommunicator(application)

    // account verification needed

    fun getData(startDayOfWeek: Int) {
//        googleFitCommunicator.accessGoogleFitStatic(
//            startDayOfWeek,
//            successFunction = {
//                statistics = it
//                _loadingDone.value = true
//                // TODO add function to update app Widgets
//                callBackCreator(getApplication()).invoke(it)
//            })
        viewModelScope.launch {
            googleFitCommunicator.accessGoogleFit(
                startDayOfWeek,
                successFunction = {
                    statistics = it
                    _liveStatistics.postValue(it.dataList)
                    _loadingDone.postValue(true)
                    // TODO add function to update app Widgets
                    callBackCreator(getApplication()).invoke(it)
                })
        }
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