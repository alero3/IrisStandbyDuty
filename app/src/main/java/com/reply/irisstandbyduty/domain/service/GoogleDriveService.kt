package com.reply.irisstandbyduty.domain.service

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Environment
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.drive.*
import com.reply.irisstandbyduty.domain.GoogleDriveConfig
import com.reply.irisstandbyduty.domain.ServiceListener
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.IOException

/**
 * Created by Reply on 01/08/21.
 */
class GoogleDriveService(
    private val fragment: Fragment,
    private val activity: Activity,
    private val config: GoogleDriveConfig) {

    companion object {
        private val SCOPES = setOf<Scope>(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
        val documentMimeTypes = arrayListOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel"
        )

        const val REQUEST_CODE_OPEN_ITEM = 100
        const val REQUEST_CODE_SIGN_IN = 101
        const val TAG = "GoogleDriveService"
    }

    var serviceListener: ServiceListener? = null //1
    private var driveClient: DriveClient? = null //2
    private var driveResourceClient: DriveResourceClient? = null //3
    private var signInAccount: GoogleSignInAccount? = null //4

    val googleSignInClient: GoogleSignInClient by lazy {
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        for (scope in SCOPES) {
            builder.requestScopes(scope)
        }
        val signInOptions = builder.build()
        GoogleSignIn.getClient(activity, signInOptions)
    }

    private val startForSignIn: ActivityResultLauncher<Intent>
    private val startForOpenItem: ActivityResultLauncher<IntentSenderRequest>

    init {
        startForSignIn = fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Handle the returned result.
            onSignInResult(
                result.resultCode,
                result.data)
        }

        startForOpenItem = fragment.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            // Handle the returned result.
            onOpenItemResult(
                result.resultCode,
                result.data)
        }
    }

    fun onSignInResult(resultCode: Int, data: Intent?) {
        if (data != null) {
            handleSignIn(data)
        } else {
            serviceListener?.cancelled()
        }
    }

    fun onOpenItemResult(resultCode: Int, data: Intent?) {
        if (data != null) {
            openItem(data)
        } else {
            serviceListener?.cancelled()
        }
    }

    /**
     * Prompts the user to select a text file using OpenFileActivity.
     *
     * @return Task that resolves with the selected item's ID.
     */
    fun pickFiles(driveId: DriveId?) {
        val builder = OpenFileActivityOptions.Builder()
        if (config.mimeTypes != null) {
            builder.setMimeType(config.mimeTypes)
        } else {
            builder.setMimeType(documentMimeTypes)
        }
        if (config.activityTitle != null && config.activityTitle.isNotEmpty()) {
            builder.setActivityTitle(config.activityTitle)
        }
        if (driveId != null) {
            builder.setActivityStartFolder(driveId)
        }
        val openOptions = builder.build()
        pickItem(openOptions)
    }

    fun checkLoginStatus() {
        val requiredScopes = HashSet<Scope>(2)
        requiredScopes.add(Drive.SCOPE_FILE)
        requiredScopes.add(Drive.SCOPE_APPFOLDER)
        signInAccount = GoogleSignIn.getLastSignedInAccount(activity)
        val containsScope = signInAccount?.grantedScopes?.containsAll(requiredScopes)
        val account = signInAccount
        if (account != null && containsScope == true) {
            initializeDriveClient(account)
        }
    }

    fun auth() {
        startForSignIn.launch(googleSignInClient.signInIntent)
    }

    fun logout() {
        googleSignInClient.signOut()
        signInAccount = null
    }


    private fun handleSignIn(data: Intent) {
        val getAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
        if (getAccountTask.isSuccessful) {
            initializeDriveClient(getAccountTask.result)
        } else {
            serviceListener?.handleError(Exception("Sign-in failed.", getAccountTask.exception))
        }
    }

    private fun initializeDriveClient(signInAccount: GoogleSignInAccount) {
        driveClient = Drive.getDriveClient(activity.applicationContext, signInAccount)
        driveResourceClient = Drive.getDriveResourceClient(activity.applicationContext, signInAccount)
        serviceListener?.loggedIn()
    }

    private fun openItem(data: Intent) {
        val driveId = data.getParcelableExtra<DriveId>(OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID)
        downloadFile(driveId)
    }

    private fun downloadFile(data: DriveId?) {
        if (data == null) {
            Log.e(TAG, "downloadFile data is null")
            return
        }
        val drive = data.asDriveFile()
        var fileName = "test"
        driveResourceClient?.getMetadata(drive)?.addOnSuccessListener {
            fileName = it.originalFilename
        }
        val openFileTask = driveResourceClient?.openFile(drive, DriveFile.MODE_READ_ONLY)
        openFileTask?.continueWithTask { task ->
            val contents = task.result
            contents.inputStream.use {
                try {
                    //This is the app's download directory, not the phones
                    val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    val tempFile = File(storageDir, fileName)
                    tempFile.createNewFile()
                    val sink = tempFile.sink().buffer()
                    sink.writeAll(it.source())
                    sink.close()

                    serviceListener?.fileDownloaded(tempFile)
                } catch (e: IOException) {
                    Log.e(TAG, "Problems saving file", e)
                    serviceListener?.handleError(e)
                }
            }
            driveResourceClient?.discardContents(contents)
        }?.addOnFailureListener { e ->
            // Handle failure
            Log.e(TAG, "Unable to read contents", e)
            serviceListener?.handleError(e)
        }
    }

    private fun pickItem(openOptions: OpenFileActivityOptions) {
        val openTask = driveClient?.newOpenFileActivityIntentSender(openOptions)
        openTask?.let {
            openTask.continueWith { task ->
                //startForOpenItem.launch(task.result)
            }
        }
    }

}