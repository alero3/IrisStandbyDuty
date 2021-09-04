package com.reply.irisstandbyduty.domain

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

/**
 * Created by Reply on 01/08/21.
 */
interface ServiceListener {
    fun onLoginSuccess(googleAccont: GoogleSignInAccount)
    fun onLoginCancel()
    fun onLoginError(exception: Exception)
}