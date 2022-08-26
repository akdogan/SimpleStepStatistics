package com.akdogan.simplestepstatistics.helper

import com.akdogan.simplestepstatistics.helper.DateHelper.isSameDay
import org.junit.Test

class DateHelperTest{

    @Test
    fun sameDay(){
        val date = 1647371828000L // Tuesday, 15. March 2022 19:17:08
        val other = 1647320948000L // Tuesday, 15. March 2022 05:09:08

        assert(date.isSameDay(other))
    }

    @Test
    fun differentDay(){
        val date = 1647371828000L // Tuesday, 15. March 2022 19:17:08
        val other = 1647407348000L // Wednesday, 16. March 2022 05:09:08

        assert(!date.isSameDay(other))
    }

}