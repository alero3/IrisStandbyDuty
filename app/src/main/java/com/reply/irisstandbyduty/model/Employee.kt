package com.reply.irisstandbyduty.model

/**
 * Created by Reply on 05/09/21.
 */
data class Employee constructor(
    val name: String,
    val profilePictureUrl: String
)

fun String.toEmployee(): Employee {
    return Employee(
        name = this,
        profilePictureUrl = this.lowercase().replace(" ", "_")
    )
}