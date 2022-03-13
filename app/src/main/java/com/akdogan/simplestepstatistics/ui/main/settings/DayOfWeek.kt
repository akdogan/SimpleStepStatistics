package com.akdogan.simplestepstatistics.ui.main.settings

import com.akdogan.simplestepstatistics.R

enum class DayOfWeek {
    MONDAY{
        override val label = R.string.weekday_monday
        override val number = 1
    },
    TUESDAY{
        override val label = R.string.weekday_tuesday
        override val number = 2
    },
    WEDNESDAY{
        override val label = R.string.weekday_wednesday
        override val number = 3
    },
    THURSDAY{
        override val label = R.string.weekday_thursday
        override val number = 4
    },
    FRIDAY{
        override val label = R.string.weekday_friday
        override val number = 5
    },
    SATURDAY{
        override val label = R.string.weekday_saturday
        override val number = 6
    },
    SUNDAY{
        override val label = R.string.weekday_sunday
        override val number = 7
    };


    abstract val label: Int

    abstract val number: Int
}