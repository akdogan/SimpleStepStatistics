package com.akdogan.simplestepstatistics.ui.main.settings.selectionlist

import com.akdogan.simplestepstatistics.ui.main.settings.DayOfWeek

data class DayOfWeekSelectionItem(
    val day: DayOfWeek,
    val selected: Boolean = false
)
