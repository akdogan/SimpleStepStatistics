package com.akdogan.simplestepstatistics.repository

import com.akdogan.simplestepstatistics.helper.DateHelper
import com.akdogan.simplestepstatistics.helper.DateHelper.isSameDay
import kotlin.math.roundToInt

fun createSampleData(goal: Int, daysInPeriod: Int, days: Int = 4): StepStatisticModel{
    val result = StepStatisticModel(goal, daysInPeriod)
    repeat(days){
        val day = DateHelper.getStartOfPastDay(it + 1)
        val steps = (4500..9500).random()
        val cycledDistance = if ((0..2).random() == 2){
            (1..7).random().toFloat()
        } else null
        result.addDay(day, steps, cycledDistance)
    }
    return result
}

class StepStatisticModel(
    private val goal: Int,
    private val daysInPeriod: Int
) {
    private val dataSet = mutableListOf<StepStatisticDay>()

    val dataList: List<StepStatisticDay>
        get() = dataSet.toList()

    private val dailyGoal = goal.toDouble() / daysInPeriod.toDouble()

    /**
     * Adds a day to the statistics
     * @param date Date of the day in Milliseconds since Epoch
     * @param stepCount Number of Steps taken that day
     */
    fun addDay(date: Long, stepCount: Int, cycledDistance: Float? = null){
        dataSet.add(StepStatisticDay(date, stepCount, cycledDistance))
        dataSet.sortBy { it.date }
    }

    fun addCyclingDay(date: Long, cycledDistance: Float?) {
        val existingItem = dataSet.find { date.isSameDay(it.date)}
        if (existingItem == null){
            dataSet.add(StepStatisticDay(date, 0, cycledDistance))
        } else {
            dataSet.remove(existingItem)
            dataSet.add(existingItem.copy(cycledDistance = cycledDistance))
        }
        dataSet.sortBy { it.date }
    }

    fun getTotalStepCount(): Int {
        return dataSet.sumBy { it.totalSteps() }
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

data class CyclingUnit(
    val date: Long,
    val cycledDistance: Float
)

data class StepStatisticDay(
    val date: Long,
    val steps: Int,
    val cycledDistance: Float? = null
) {

    override fun toString(): String{
        return "${DateHelper.timeToDateString(date)} Stepcount $steps :: CycledDistance $cycledDistance"
    }
}

fun StepStatisticDay.totalSteps(): Int {
    var result = steps
    if (cycledDistance != null) {
        result += (cycledDistance * 1.5).toInt()
    }
    return result //if (cycledDistance == null) steps else (cycledDistance * 1500).toInt() + steps
}