package com.reply.irisstandbyduty.util

import android.content.Context
import com.reply.irisstandbyduty.R
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

    fun getShortDayOfWeekName(context: Context, date: Date): String {
        val cal: Calendar = Calendar.getInstance()
        cal.time = date
        val dayOfWeek: Int = cal.get(Calendar.DAY_OF_WEEK)
        return context.resources.getStringArray(R.array.daysOfWeek)[dayOfWeek-1]
    }

    fun getMonthName(context: Context, date: Date): String {
        val cal: Calendar = Calendar.getInstance()
        cal.time = date
        val month: Int = cal.get(Calendar.MONTH)
        return context.resources.getStringArray(R.array.months)[month]
    }

    fun getYear(context: Context, date: Date): Int {
        val cal: Calendar = Calendar.getInstance()
        cal.time = date
        return cal.get(Calendar.YEAR)
    }
}