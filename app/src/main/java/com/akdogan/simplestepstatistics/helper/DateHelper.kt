package com.akdogan.simplestepstatistics.helper

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit

object DateHelper {

    private fun getSystemZone(): ZoneId? {
        return ZoneId.systemDefault()
    }

    private fun nowZoned(): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.now(), getSystemZone())
    }

    fun getNow(): Long{
        return nowZoned().toEpochSecond()
    }

    fun getStartOfToday(): Long{
        val zdtStart = nowZoned().toLocalDate().atStartOfDay(getSystemZone())
        return zdtStart.toEpochSecond()
    }

    /**
     * Returns the start of a day in the past.
     * @param days Number of days to go into the past from today. Example: If today is Wednesday, days 2 will return
     * the start of the last monday
     * @return start of the specified day in epoch seconds
     */
    fun getStartOfPastDay(days: Int): Long {
        val zdtStartOfBefore = nowZoned().toLocalDate().atStartOfDay(getSystemZone()).minusDays(days.toLong())
        return zdtStartOfBefore.toEpochSecond()
    }


    fun Long.isSameDay(other: Long): Boolean{
        val thisDate = Date(this).toInstant()
            .atZone(getSystemZone())
            .toLocalDate()
        val otherDate = Date(other).toInstant()
            .atZone(getSystemZone())
            .toLocalDate()
        return thisDate.isEqual(otherDate)
    }

    /**
     * Returns the start of the last specified day of the Week. For example, if 3 (Wednesday) is supplied as an
     * argument, then the start of the last Wedensday is returned. If today is Wendesday, start of today is returned
     * @param day day of the week as int, value from 1 - 7 (translates to Mon - Sun)
     * @return returns the start of the last specified day of the week in epoch seconds
     * @throws IllegalArgumentException if the parameter specified is out of bounds (must be between 1 and 7 inclusive)
     */
    @Throws(IllegalArgumentException::class)
    fun getStartOfSpecifiedDay(day: Int): Long {
        if ( day < 1 || day > 7){
            throw IllegalArgumentException("Specified Day is not in Range")
        } else {
            val today = nowZoned().dayOfWeek.value
            println("Today: $today")
            val offSet = if (today < day){
                7 - (day - today)
            } else {
                today - day
            }
            return getStartOfPastDay(offSet)
        }
    }

    fun timeToDateString(time: Long, tu: TimeUnit = TimeUnit.MILLISECONDS): String{
        return convertTimeStampToDateString(time, "dd.MM.yyy", tu)
    }

    fun timeToTimeString(time:Long, tu: TimeUnit = TimeUnit.MILLISECONDS): String{
        return convertTimeStampToDateString(time, "HH:mm:ss", tu)
    }

    fun timeToDateTimeString(time: Long, tu: TimeUnit = TimeUnit.MILLISECONDS): String{
        return convertTimeStampToDateString(time, "dd.MM.yyy HH:mm:ss", tu)
    }

    fun timeToSimpleDateString(time: Long, tu: TimeUnit = TimeUnit.MILLISECONDS): String {
        return convertTimeStampToDateString(time, "E dd.MM", tu)
    }

    private fun convertTimeStampToDateString(time: Long, pattern: String, tu: TimeUnit): String{
        val targetTime = TimeUnit.MILLISECONDS.convert(time, tu)
        val date = Date(time)
        val formatter: DateFormat = SimpleDateFormat(pattern)
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(date)
    }

}