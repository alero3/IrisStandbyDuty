package com.reply.irisstandbyduty.domain

import com.reply.irisstandbyduty.model.StandbyDutyMonth
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Created by Reply on 03/09/21.
 */
class StandbyDutyCalendarParser @Inject constructor(
    private val monthParser: MonthParser
) {

    fun parse(input: ArrayList<ArrayList<String>>): StandbyDutyMonth {
        val monthString = input.getOrNull(3)?.getOrNull(1)
            ?: throw CalendarParseException("Month cell is empty or null")
        val monthParsing = monthParser.parse(monthString)
        val month = monthParsing.first
        val year = monthParsing.second
        val onCallCalendarMap: SortedMap<Date, String> = sortedMapOf()

        // Row index of the first Android person is 6.
        // There are 5 Android people.
        for (i in 6..10) {
            // Person name.
            val name = input.getOrNull(i)?.getOrNull(0)
            if (name != null) {
                for (day in 1..31) {
                    if (input.getOrNull(i)?.getOrNull(day)?.isMarkedAsOnCall() == true) {
                        val date = GregorianCalendar(
                            year,
                            month - 1,
                            day
                        )
                        // Add [date - person] association to the map.
                        onCallCalendarMap[date.time] = name
                    }
                }
            }
        }

        return StandbyDutyMonth(
            month = month,
            onCallCalendar = onCallCalendarMap
        )
    }

    private fun String.isMarkedAsOnCall(): Boolean {
        return when (this) {
            "R", "RL1", "RL2", "STR" -> true
            else -> false
        }
    }


}

class CalendarParseException(message: String) : Exception(message)