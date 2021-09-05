package com.reply.irisstandbyduty.model

import java.util.*

/**
 * Created by Reply on 03/09/21.
 */

/**
 * @param month
 * @param onCallCalendar map that associates each day to the name of the person on call for that day.
 */
data class StandbyDutyMonth(
    val month: Int,
    val onCallCalendar: SortedMap<Date, String>
)
