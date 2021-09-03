package com.reply.irisstandbyduty.domain

/**
 * Created by Reply on 01/08/21.
 */
interface ServiceListener {
    fun onLoginSuccess()
    fun onLoginCancel()
    fun onLoginError(exception: Exception)
}