package com.akdogan.simplestepstatistics.repository

import com.akdogan.simplestepstatistics.helper.DateHelper
import kotlin.math.roundToInt

fun createSampleData(goal: Int, daysInPeriod: Int, days: Int = 4): StepStatisticModel{
    val result = StepStatisticModel(goal, daysInPeriod)
    repeat(days){
        val day = DateHelper.getStartOfPastDay(it + 1)
        val steps = (4500..9500).random()
        result.addDay(day, steps)
    }
    return result
}

class StepStatisticModel(
    private val goal: Int,
    private val daysInPeriod: Int
) {
    private val dataSet = mutableListOf<StepStatisticDay>()
    private val dailyGoal = goal.toDouble() / daysInPeriod.toDouble()

    /**
     * Adds a day to the statistics
     * @param date Date of the day in Milliseconds since Epoch
     * @param stepCount Number of Steps taken that day
     */
    fun addDay(date: Long, stepCount: Int){
        dataSet.add(StepStatisticDay(date, stepCount))
        dataSet.sortBy { it.date }
    }

    fun getTotalStepCount(): Int {
        return dataSet.sumBy { it.steps }
    }

    private fun getDiffToWeeklyGoal(): Int {
        return goal - getTotalStepCount()
    }

    fun getRequiredTodayForBreakEven(): Int {
        val required = dataSet.size * dailyGoal
        return required.roundToInt() - getTotalStepCount()
    }

    fun getRequiredPerDayUpcoming(): Int {
        val daysLeft = daysInPeriod - dataSet.size
        return if (daysLeft <= 0){
            getDiffToWeeklyGoal()
        } else {
            (getDiffToWeeklyGoal().toDouble() / daysLeft).roundToInt()
        }
    }

    fun diffLatestDay(): Int {
        return (dataSet.lastOrNull()?.let{
            dailyGoal - it.steps
        } ?: dailyGoal).roundToInt()
    }

    fun clear(){
        dataSet.clear()
    }

    override fun toString(): String {
        val result = StringBuilder()
        dataSet.forEach {
            result.append(it.toString())
            result.append("\n")
        }
        return result.toString()
    }

    fun getDaysAsList(): List<StepStatisticDay> {
        return dataSet.toList()
    }

}


data class StepStatisticDay(
    val date: Long,
    val steps: Int
) {

    override fun toString(): String{
        return "${DateHelper.timeToDateString(date)} Stepcount $steps"
    }
}