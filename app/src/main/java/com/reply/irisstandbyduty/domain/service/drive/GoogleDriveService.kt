package com.reply.irisstandbyduty.domain.service.drive

import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.reply.irisstandbyduty.R
import com.reply.irisstandbyduty.domain.service.GoogleDriveAuthenticator
import timber.log.Timber


/**
 * Created by Reply on 01/08/21.
 */
class GoogleDriveService(
    private val fragment: Fragment
) : GoogleDriveAuthenticator(fragment) {

    private var mDriveServiceHelper: DriveServiceHelper? = null

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
        val task = mDriveServiceHelper?.exportFile(id)
        task
            ?.addOnSuccessListener {
                Timber.d("Downloaded file with name: ${it.first}")
            }
            ?.addOnFailureListener {
                Timber.d("Error in download $it")
            }
    }


    override fun initializeClient(googleAccount: GoogleSignInAccount) {
        // Use the authenticated account to sign in to the Drive service.
        val credential = GoogleAccountCredential.usingOAuth2(
            fragment.requireContext(),
            setOf(mScope)
        )
        credential.selectedAccount = googleAccount.account
        val googleDriveService = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName(fragment.requireContext().resources.getString(R.string.app_name))
            .build()

        // The DriveServiceHelper encapsulates all REST API and SAF functionality.
        // Its instantiation is required before handling any onClick actions.
        mDriveServiceHelper = DriveServiceHelper(googleDriveService)

        serviceListener?.onLoginSuccess()
    }

}