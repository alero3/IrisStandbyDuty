package com.reply.irisstandbyduty.model

import java.util.*

/**
 * Created by Reply on 03/09/21.
 */

/**
 * @param monthIndex
 * @param onCallCalendar map that associates each day to the name of the person on call for that day.
 */
data class StandbyDutyMonth(
    val monthIndex: Int,
    val onCallCalendar: List<Shift>
)

/**
 * On call shift.
 *
 * @param employee
 * @param interventionType
 */
data class Shift(
    val date: Date,
    val employee: Employee,
    val interventionType: InterventionType
)

/**
 * [InterventionType.R] no intervention.
 * [InterventionType.RL1] intervention over 30 minutes.
 * [InterventionType.RL2] important intervention.
 * [InterventionType.STR] overtime work.
 */
enum class InterventionType {
    R,
    RL1,
    RL2,
    STR
}

fun String.toInterventionType(): InterventionType? {
    return when (this) {
        "R" -> InterventionType.R
        "RL1" -> InterventionType.RL1
        "RL2" -> InterventionType.RL2
        "STR" -> InterventionType.STR
        else -> null
    }
}