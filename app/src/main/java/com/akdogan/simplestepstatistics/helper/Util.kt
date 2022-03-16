package com.akdogan.simplestepstatistics.helper

import android.content.res.Resources
import android.util.Log
import com.akdogan.simplestepstatistics.R
import com.akdogan.simplestepstatistics.repository.StepStatisticDay
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.result.DataReadResponse
import java.util.concurrent.TimeUnit
import kotlin.math.round


fun Float.toKmRounded() = round(this / 1000)


fun formatTotalWithGoal(goal: Int, total: Int, res: Resources): String{
    return res.getString(R.string.total_goal, total, goal)
}

fun formatDays(dataSet: List<StepStatisticDay>, res: Resources): String{
    val builder = StringBuilder()
    dataSet.forEach {
        builder.append(res.getString(
            R.string.one_day,
            it.date,
            it.date,
            it.date,
            it.steps))
        builder.append("\n")
    }
    return builder.toString()
}

fun formatStats(stepsLeftToday: Int, stepsPerDay: Int, res: Resources): String{
    return StringBuilder()
        .append(res.getString(R.string.today_break_even, stepsLeftToday))
        .append("\n")
        .append(res.getString(R.string.steps_left_per_day, stepsPerDay))
        .toString()
}

fun printData(dataReadResult: DataReadResponse) {
    val TAG = "GFit Parsing"
    // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
    // as buckets containing DataSets, instead of just DataSets.
    var total = 0
    if (dataReadResult.buckets.isNotEmpty()) {
        Log.i(TAG, "Number of returned buckets of DataSets is: " + dataReadResult.buckets.size)
        for (bucket in dataReadResult.buckets) {
            bucket.dataSets.forEach { total += dumpDataSet(it) }
            Log.i(TAG, "Running Total: $total")
        }
    } else if (dataReadResult.dataSets.isNotEmpty()) {
        Log.i(TAG, "Number of returned DataSets is: " + dataReadResult.dataSets.size)
        dataReadResult.dataSets.forEach { dumpDataSet(it) }
    }
    Log.i(TAG, "FINAL TOTAL STEP COUNT: $total")

}

fun dumpDataSet(dataSet: DataSet): Int {
    val TAG = "GFit Parsing"
    Log.i(TAG, "Data returned for Data type: ${dataSet.dataType.name}")

    var total = 0
    for (dp in dataSet.dataPoints) {
        Log.i(TAG, "Data point:")
        Log.i(TAG, "\tType: ${dp.dataType.name}")
        Log.i(
            TAG, "\tStart: ${
                DateHelper.timeToDateTimeString(dp.getStartTime(
                    TimeUnit.MILLISECONDS))} -- (${dp.getStartTime(TimeUnit.MILLISECONDS)})")
        Log.i(
            TAG, "\tEnd: ${
                DateHelper.timeToDateTimeString(dp.getEndTime(
                    TimeUnit.MILLISECONDS))} -- (${dp.getEndTime(TimeUnit.MILLISECONDS)})")
        dp.dataType.fields.forEach {
            Log.i(TAG, "\tField: ${it.name} Value: ${dp.getValue(it)}")
            val va = dp.getValue(it).asInt()
            total += va
        }
    }
    return total
}