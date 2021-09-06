package com.reply.irisstandbyduty.domain

import com.reply.irisstandbyduty.model.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Created by Reply on 03/09/21.
 */
class StandbyDutyCalendarParser @Inject constructor(
    private val monthParser: MonthParser
) {

    fun parse(input: ArrayList<ArrayList<String>>): List<Shift> {
        val monthString = input.getOrNull(3)?.getOrNull(1)
            ?: throw CalendarParseException("Month cell is empty or null")
        val monthParsing = monthParser.parse(monthString)
        val month = monthParsing.first
        val year = monthParsing.second
        val onCallCalendar: MutableList<Shift> = mutableListOf()

        // Row index of the first Android person is 6.
        // There are 5 Android people.
        for (i in 6..10) {
            // Person name.
            val name = input.getOrNull(i)?.getOrNull(0)
            if (name != null) {
                for (day in 1..31) {
                    val interventionType = input.getOrNull(i)?.getOrNull(day)?.toInterventionType()
                    if (interventionType != null) {
                        val date = GregorianCalendar(
                            year,
                            month - 1,
                            day
                        )
                        val shift = Shift(
                            date = date.time,
                            employee = name.toEmployee(),
                            interventionType = interventionType
                        )
                        // Add shift to list.
                        onCallCalendar.add(shift)
                    }
                }
            }
        }

        onCallCalendar.sortBy { it.date }

        return onCallCalendar
    }

}

class CalendarParseException(message: String) : Exception(message)