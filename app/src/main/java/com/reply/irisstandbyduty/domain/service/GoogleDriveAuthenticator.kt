package com.reply.irisstandbyduty.domain.service

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.reply.irisstandbyduty.domain.AuthenticationListener
import timber.log.Timber
import java.lang.RuntimeException

/**
 * Created by Reply on 03/09/21.
 */
class GoogleDriveAuthenticator constructor(
    private val context: Context
) {

    var authenticationListener: AuthenticationListener? = null

    private var activityResultCaller: ActivityResultCaller? = null

    private var mSignInAccount: GoogleSignInAccount? = null

    private val mScope = DriveScopes.DRIVE

    private val googleSignInClient: GoogleSignInClient by lazy {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(mScope))
            .build()
        GoogleSignIn.getClient(context, signInOptions)
    }

    private val startForSignIn: ActivityResultLauncher<Intent>

    init {
        if (context is ActivityResultCaller) {
            activityResultCaller = context
            startForSignIn =
                activityResultCaller!!.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    // Handle the returned result.
                    onSignInResult(
                        result.resultCode,
                        result.data
                    )
                }
        } else {
            throw RuntimeException("context passed to ${GoogleDriveAuthenticator::class.java.simpleName} must implement ${ActivityResultCaller::class.java.simpleName}")
        }
    }

    fun checkLoginStatus() {
        val requiredScopes = HashSet<Scope>(2)
        requiredScopes.add(Scope(mScope))
        mSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
        val containsScope = mSignInAccount?.grantedScopes?.containsAll(requiredScopes)
        val account = mSignInAccount
        if (account != null && containsScope == true) {
            authenticationListener?.onLoginSuccess(account)
        } else {
            authenticationListener?.onLoginNotPerformed()
        }
    }

    fun auth() {
        startForSignIn.launch(googleSignInClient.signInIntent)
    }

    fun logout() {
        googleSignInClient.signOut()
        mSignInAccount = null
    }

    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     * #requestSignIn()}.
     */
    private fun handleSignIn(data: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnSuccessListener { googleAccount: GoogleSignInAccount ->
                Timber.d("Signed in as %s", googleAccount.email)
                mSignInAccount = googleAccount
                authenticationListener?.onLoginSuccess(googleAccount)
            }
            .addOnFailureListener { exception: Exception ->
                Timber.e(exception, "Unable to sign in.")
                authenticationListener?.onLoginError(Exception("Sign-in failed.", exception))
            }
    }

    private fun onSignInResult(resultCode: Int, data: Intent?) {
        if (data != null) {
            handleSignIn(data)
        } else {
            authenticationListener?.onLoginCancel()
        }
    }
}