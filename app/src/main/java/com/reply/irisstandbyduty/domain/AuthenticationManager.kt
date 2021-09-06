package com.reply.irisstandbyduty.domain

/**
 * Created by Reply on 01/08/21.
 */
interface AuthenticationManager {

    fun login()

    fun logout()

    fun checkAuthenticationStatus()
}