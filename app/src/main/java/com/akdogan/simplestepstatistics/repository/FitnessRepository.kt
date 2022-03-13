package com.akdogan.simplestepstatistics.repository

import android.content.Context
import android.util.Log
import com.akdogan.simplestepstatistics.FALLBACK_DAYS_IN_PERIOD
import com.akdogan.simplestepstatistics.FALLBACK_WEEKLY_GOAL
import com.akdogan.simplestepstatistics.helper.DateHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import java.util.concurrent.TimeUnit

class GoogleFitCommunicator(
    private val context: Context,
    private val weeklyGoal: Int = FALLBACK_WEEKLY_GOAL,
    private val daysInPeriod: Int = FALLBACK_DAYS_IN_PERIOD
) {

    val TAG = "GFit"

    /**
     * Checks if the application has access to Google fit for the defined statistics
     * @return true if access is allowed, false if otherwise
     */
    fun checkFitAccess(): Boolean {
        val options = getFitnessOptions()
        val gAccount = getGoogleAccountStatic(options)
        return (GoogleSignIn.hasPermissions(gAccount, options))
    }

    /**
     * Fetch Data from Google Fit
     * @param successFunction CallbackFunction that should be executed with the resultdata
     */
    fun accessGoogleFitStatic(
        startDayOfWeek: Int,
        successFunction: (StepStatisticModel) -> Unit,
        completeFunction: () -> Unit = {}
    ) {
        Log.i(TAG, "access granted")
        val readRequest = createFitnessDataRequestStatic(startDayOfWeek)

        // Invoke the History API to fetch the data with the query
        Fitness.getHistoryClient(context, getGoogleAccountStatic())
            .readData(readRequest)
            .addOnSuccessListener { dataReadResponse ->
                // When parsing fails, sample data is used instead
                val result = try {
                    parseGoogleFitResponseStatic(dataReadResponse)
                } catch (e: Exception) {
                    Log.e("GOOGLE FIT PARSER", e.stackTraceToString())
                    createSampleData(weeklyGoal, daysInPeriod)
                }
                successFunction(result)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "There was a problem reading the data.", e)
            }
            .addOnCompleteListener {
                completeFunction()
            }

    }

    fun getFitnessOptions(): FitnessOptions {
        return FitnessOptions.builder()
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build()
    }

    fun getGoogleAccountStatic(
        options: FitnessOptions = getFitnessOptions()
    ): GoogleSignInAccount {
        return GoogleSignIn.getAccountForExtension(context, options)
    }


    private fun parseGoogleFitResponseStatic(dataReadResult: DataReadResponse): StepStatisticModel {
        val statistics = StepStatisticModel(weeklyGoal, daysInPeriod)
        // TODO Bug: If there are no steps in a day, an empty bucket is retrieved and the app crashes
        if (dataReadResult.buckets.isNotEmpty()) {
            for (bucket in dataReadResult.buckets) {
                bucket.dataSets.firstOrNull()?.let {
                    it.dataPoints.firstOrNull()?.let {
                        val date = it.getStartTime(TimeUnit.MILLISECONDS)
                        val steps = it.getValue(it.dataType.fields[0]).asInt()
                        statistics.addDay(date, steps)
                    }
                }
                /*bucket.dataSets[0]?.dataPoints?.get(0)?.let{
                    val date = it.getStartTime(TimeUnit.MILLISECONDS)
                    val steps = it.getValue(it.dataType.fields[0]).asInt()
                    statistics.addDay(date, steps)
                }*/
            }
        }
        return statistics
    }

//    private fun createFitnessDataBicycleRequest(): DataReadRequest {
//        val endTime = DateHelper.getNow()
//        val startTime = DateHelper.getStartOfSpecifiedDay(6)
//        Log.i(
//            TAG, "Range Start: ${DateHelper.timeToDateTimeString(startTime, TimeUnit.SECONDS)}"
//        )
//        Log.i(
//            TAG,
//            "Range End: ${DateHelper.timeToDateTimeString(endTime, TimeUnit.SECONDS)}"
//        )
//
//        val dataSource = DataSource.Builder()
//            .setAppPackageName("com.google.android.gms")
//            .setDataType(DataType.AGGREGATE_DISTANCE_DELTA)
//    }

    private fun createFitnessDataRequestStatic(startDayOfWeek: Int): DataReadRequest {
        val endTime = DateHelper.getNow()
        val startTime = DateHelper.getStartOfSpecifiedDay(startDayOfWeek)
        Log.i(
            TAG, "Range Start: ${DateHelper.timeToDateTimeString(startTime, TimeUnit.SECONDS)}"
        )
        Log.i(
            TAG,
            "Range End: ${DateHelper.timeToDateTimeString(endTime, TimeUnit.SECONDS)}"
        )

        val dataSource = DataSource.Builder()
            .setAppPackageName("com.google.android.gms")
            .setDataType(DataType.AGGREGATE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_DERIVED)
            .setStreamName("estimated_steps")
            .build()

        return DataReadRequest.Builder()
            // The data request can specify multiple data types to return, effectively
            // combining multiple data queries into one call.
            .aggregate(dataSource/*, DataType.AGGREGATE_STEP_COUNT_DELTA*/)
            // Analogous to a "Group By" in SQL, defines how data should be aggregated.
            // bucketByTime allows for a time span, whereas bucketBySession would allow
            // bucketing by "sessions", which would need to be defined in code.
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.SECONDS)
            .build()
    }
}