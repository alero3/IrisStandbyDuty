package com.reply.irisstandbyduty.util

import java.util.*

/**
 * Created by Reply on 05/09/21.
 */
object DateUtils {

    fun areSameDay(d1: Date, d2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = d1
        cal2.time = d2
        return cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR] &&
                cal1[Calendar.YEAR] == cal2[Calendar.YEAR]
    }
}