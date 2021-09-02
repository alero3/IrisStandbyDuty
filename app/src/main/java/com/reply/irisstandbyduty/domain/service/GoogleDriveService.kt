package com.reply.irisstandbyduty.domain.service

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.reply.irisstandbyduty.R
import com.reply.irisstandbyduty.domain.ServiceListener
import timber.log.Timber


/**
 * Created by Reply on 01/08/21.
 */
class GoogleDriveService(
    private val fragment: Fragment,
    private val activity: Activity
) {

    var serviceListener: ServiceListener? = null //1

    private var mSignInAccount: GoogleSignInAccount? = null //4

    private val googleSignInClient: GoogleSignInClient by lazy {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE))
            .build();
        GoogleSignIn.getClient(activity, signInOptions)
    }

    private val startForSignIn: ActivityResultLauncher<Intent>
    private val startForOpenItem: ActivityResultLauncher<IntentSenderRequest>

    private var mDriveServiceHelper: DriveServiceHelper? = null

    init {
        startForSignIn =
            fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                // Handle the returned result.
                onSignInResult(
                    result.resultCode,
                    result.data
                )
            }

        startForOpenItem =
            fragment.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                // Handle the returned result.
                onOpenItemResult(
                    result.resultCode,
                    result.data
                )
            }
    }

    private fun onSignInResult(resultCode: Int, data: Intent?) {
        if (data != null) {
            handleSignIn(data)
        } else {
            serviceListener?.cancelled()
        }
    }

    private fun onOpenItemResult(resultCode: Int, data: Intent?) {
        if (data != null) {
            //openItem(data)
        } else {
            serviceListener?.cancelled()
        }
    }


    fun checkLoginStatus() {
        val requiredScopes = HashSet<Scope>(2)
        requiredScopes.add(Scope(DriveScopes.DRIVE_FILE))
        mSignInAccount = GoogleSignIn.getLastSignedInAccount(activity)
        val containsScope = mSignInAccount?.grantedScopes?.containsAll(requiredScopes)
        val account = mSignInAccount
        if (account != null && containsScope == true) {
            initializeDriveClient(account)
        }
    }

    fun auth() {
        startForSignIn.launch(googleSignInClient.signInIntent)
    }

    fun logout() {
        googleSignInClient.signOut()
        mSignInAccount = null
    }

    fun createFile() {
        val task = mDriveServiceHelper?.createFile()
        task?.addOnSuccessListener {
            Timber.d("Created file with id: $it")
        }
            ?.addOnFailureListener {
                Timber.d("File creation failed")
            }
    }

    fun downloadFileWithId(id: String) {
        Timber.d("Logged in as ${mSignInAccount?.email}")
        mDriveServiceHelper?.readAllFiles()
        val task = mDriveServiceHelper?.readFile(id)
        task
            ?.addOnSuccessListener {
                Timber.d("Downloaded file with name: ${it.first}")
            }
            ?.addOnFailureListener {
                Timber.d("Error in download $it")
            }
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
                initializeDriveClient(googleAccount)
            }
            .addOnFailureListener { exception: Exception ->
                Timber.e(exception, "Unable to sign in.")
                serviceListener?.handleError(Exception("Sign-in failed.", exception))
            }
    }

    private fun initializeDriveClient(googleAccount: GoogleSignInAccount) {
        // Use the authenticated account to sign in to the Drive service.
        val credential = GoogleAccountCredential.usingOAuth2(
            activity,
            setOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = googleAccount.account
        val googleDriveService = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName(activity.resources.getString(R.string.app_name))
            .build()

        // The DriveServiceHelper encapsulates all REST API and SAF functionality.
        // Its instantiation is required before handling any onClick actions.
        mDriveServiceHelper = DriveServiceHelper(googleDriveService)

        serviceListener?.loggedIn()
    }

}