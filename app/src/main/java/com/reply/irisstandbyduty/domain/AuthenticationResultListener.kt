package com.reply.irisstandbyduty.domain

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

/**
 * Created by Reply on 01/08/21.
 */
interface AuthenticationResultListener {
    fun onLoginSuccess(googleAccont: GoogleSignInAccount)
    fun onLoginNotPerformed()
    fun onLoginCancel()
    fun onLoginError(exception: Exception)
}