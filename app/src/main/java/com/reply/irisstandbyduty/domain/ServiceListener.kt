package com.reply.irisstandbyduty.domain

import java.io.File

/**
 * Created by Reply on 01/08/21.
 */
interface ServiceListener {
    fun loggedIn() //1
    fun fileDownloaded(file: File) //2
    fun cancelled() //3
    fun handleError(exception: Exception) //4
}