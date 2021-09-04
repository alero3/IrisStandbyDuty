package com.reply.irisstandbyduty.domain.service

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.reply.irisstandbyduty.domain.ServiceListener
import timber.log.Timber

/**
 * Created by Reply on 03/09/21.
 */
class GoogleDriveAuthenticator(
    private val fragment: Fragment) {

    var serviceListener: ServiceListener? = null

    private val context: Context
        get() = fragment.requireContext()

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
        startForSignIn =
            fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                // Handle the returned result.
                onSignInResult(
                    result.resultCode,
                    result.data
                )
            }
    }

    fun checkLoginStatus() {
        val requiredScopes = HashSet<Scope>(2)
        requiredScopes.add(Scope(mScope))
        mSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
        val containsScope = mSignInAccount?.grantedScopes?.containsAll(requiredScopes)
        val account = mSignInAccount
        if (account != null && containsScope == true) {
            serviceListener?.onLoginSuccess(account)
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
                serviceListener?.onLoginSuccess(googleAccount)
            }
            .addOnFailureListener { exception: Exception ->
                Timber.e(exception, "Unable to sign in.")
                serviceListener?.onLoginError(Exception("Sign-in failed.", exception))
            }
    }

    private fun onSignInResult(resultCode: Int, data: Intent?) {
        if (data != null) {
            handleSignIn(data)
        } else {
            serviceListener?.onLoginCancel()
        }
    }
}