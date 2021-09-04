package com.reply.irisstandbyduty.domain

/**
 * Created by Reply on 03/09/21.
 */
class MonthParser {

    /**
     * @param input month in format "MonthName"-"YY". e.g. "September-21"
     * @return pair of [Int] corresponding to that month and year.
     */
    fun parse(input: String): Pair<Int, Int> {
        val pieces = input.split("-")
        if (pieces.size == 2) {
            val monthInt = when (pieces[0]) {
                "January" -> 1
                "February" -> 2
                "March" -> 3
                "April" -> 4
                "May" -> 5
                "June" -> 6
                "July" -> 7
                "August" -> 8
                "September" -> 9
                "October" -> 10
                "November" -> 11
                "December" -> 12
                else -> throw MonthParsingException("Month ${pieces[0]} not recognized.")
            }
            val yearInt = "20${pieces[1]}".toIntOrNull() ?: throw MonthParsingException("Year ${pieces[1]} not recognized.")

            return Pair(monthInt, yearInt)

        } else throw MonthParsingException("Month $input not recognized.")

    }
}

class MonthParsingException(message: String): Exception(message)