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
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.coroutines.suspendCoroutine

class GoogleFitCommunicator(
    private val context: Context,
    private val weeklyGoal: Int = FALLBACK_WEEKLY_GOAL,
    private val daysInPeriod: Int = FALLBACK_DAYS_IN_PERIOD
) {


    /**
     * Checks if the application has access to Google fit for the defined statistics
     * @return true if access is allowed, false if otherwise
     */
    fun checkFitAccess(): Boolean {
        val options = getFitnessOptions()
        val gAccount = getGoogleAccountStatic(options)
        return (GoogleSignIn.hasPermissions(gAccount, options))
    }

    private suspend fun getStepStatistics(readRequest: DataReadRequest): StepStatisticModel = suspendCoroutine {
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
//                    successFunction(result)
                it.resumeWith(Result.success(result))
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "There was a problem reading the data.", e)
                it.resumeWith(Result.failure(e))
            }
    }

    private suspend fun getCyclingData(readRequest: DataReadRequest): List<CyclingUnit> = suspendCoroutine {
        Fitness.getHistoryClient(context, getGoogleAccountStatic())
            .readData(readRequest)
            .addOnSuccessListener { cyclingResponse ->
                Log.e(TAG, "Cycling data was read")//
                val result = parseGoogleFitCyclingResponse(cyclingResponse)
                it.resumeWith(Result.success(result))
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Cycling data was not read: $e")
                it.resumeWith(Result.failure(e))
            }
    }

    suspend fun accessGoogleFit(
        startDayOfWeek: Int,
        successFunction: (StepStatisticModel) -> Unit,
        completeFunction: () -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        Log.i(TAG, "access granted")
        val readRequest = createFitnessDataRequestStatic(startDayOfWeek)

        // Invoke the History API to fetch the data with the query
        val statisticsStepStatisticModel = async {
            getStepStatistics(readRequest)
        }.await()

        val cyclingReadRequest = createFitnessDataBicycleRequest(startDayOfWeek)

        val cyclingData = async {
            getCyclingData(cyclingReadRequest)
        }.await()

        cyclingData.forEach {
            statisticsStepStatisticModel.addCyclingDay(it.date, it.cycledDistance)
        }

        successFunction(statisticsStepStatisticModel)
    }

    /**
     * Fetch Data from Google Fit
     * @param successFunction CallbackFunction that should be executed with the resultdata
     */
    @Deprecated("Use suspend function instead")
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

        val cyclingReadRequest = createFitnessDataBicycleRequest(startDayOfWeek)

        Fitness.getHistoryClient(context, getGoogleAccountStatic())
            .readData(cyclingReadRequest)
            .addOnSuccessListener { cyclingResponse ->
                Log.e(TAG, "Cycling data was read")//
                parseGoogleFitCyclingResponse(cyclingResponse)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Cycling data was not read: $e")
            }

    }

    fun getFitnessOptions(): FitnessOptions {
        return FitnessOptions.builder()
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_ACTIVITY_SUMMARY, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .build()
        // TODO add cycling options if necessary
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

    private fun parseGoogleFitCyclingResponse(cyclingResponse: DataReadResponse): List<CyclingUnit> {
        if (cyclingResponse.buckets.isNullOrEmpty()) return emptyList()
        val result = mutableListOf<CyclingUnit>()
        //printData(cyclingResponse)
//        for (bucket in cyclingResponse.buckets) {
//            if (bucket.activity != "biking") continue
//            bucket.dataSets.forEachIndexed { index, dataSet ->
//                Log.d(CYCLING_TAG, "dataSetIndex: $index")
//                Log.d(CYCLING_TAG, "dataSetType: ${dataSet.dataType}")
//                Log.d(CYCLING_TAG, "dataSetSource: ${dataSet.dataSource}")
//                Log.d(CYCLING_TAG, "dataSetPoints: ${dataSet.dataPoints}")
//                val test = dataSet.dataPoints[0]
//                val type = test.dataType
//                Log.d(CYCLING_TAG, "DataPoint Type: $type")
//                val value = test.getValue(Field.FIELD_DISTANCE).asFloat()
//                Log.d(CYCLING_TAG, "DataPoint Value: $value")
//                val timestamp = test.getTimestamp(TimeUnit.MILLISECONDS)
//                val readableDate = timeToDateTimeString(timestamp)
//                Log.d(CYCLING_TAG, "Date for cyclingdata: $readableDate")
//            }
//        }
        for (bucket in cyclingResponse.buckets) {
            if (bucket.activity != "biking") continue
            bucket.dataSets.forEachIndexed { index, dataSet ->
                val test = dataSet.dataPoints[0]
                val type = test.dataType
                Log.d(CYCLING_TAG, "DataPoint Type: $type")
                val value = test.getValue(Field.FIELD_DISTANCE).asFloat()
                Log.d(CYCLING_TAG, "DataPoint Value: $value")
                val timestamp = test.getTimestamp(TimeUnit.MILLISECONDS)
                result.add(CyclingUnit(timestamp, value))
            }
        }
        return result
    }

    private fun createFitnessDataBicycleRequest(startDayOfWeek: Int): DataReadRequest {
        val endTime = DateHelper.getNow()
        val startTime = DateHelper.getStartOfSpecifiedDay(7)
        Log.i(
            TAG, "Range Start: ${DateHelper.timeToDateTimeString(startTime, TimeUnit.SECONDS)}"
        )
        Log.i(
            TAG,
            "Range End: ${DateHelper.timeToDateTimeString(endTime, TimeUnit.SECONDS)}"
        )

        val dataSource = DataSource.Builder()
            .setAppPackageName("com.google.android.gms")
            .setDataType(DataType.AGGREGATE_DISTANCE_DELTA)
            .setType(DataSource.TYPE_DERIVED)
            .setStreamName("cycled_distance")
            .build()

        return DataReadRequest.Builder()
            //.aggregate(dataSource)
            .aggregate(DataType.TYPE_DISTANCE_DELTA)
            //.bucketByTime(1, TimeUnit.DAYS)
            .bucketByActivitySegment(1, TimeUnit.MINUTES)
            .setTimeRange(startTime, endTime, TimeUnit.SECONDS)
            .build()
    }

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

    companion object {
        private const val TAG = "GFit"
        private const val CYCLING_TAG = "Cycling"
    }
}