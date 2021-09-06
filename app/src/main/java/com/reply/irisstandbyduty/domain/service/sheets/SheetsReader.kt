package com.reply.irisstandbyduty.domain.service.sheets

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.Sheets
import com.reply.irisstandbyduty.R
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

/**
 * Created by Reply on 04/09/21.
 */
class SheetsReader @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val mScope = DriveScopes.DRIVE

    private var mReadSheetCallback: ReadSheetCallback? = null

    fun readSheet(
        googleAccount: GoogleSignInAccount,
        id: String,
        range: String,
        sheetName: String? = null,
        callback: ReadSheetCallback) {
        Timber.d("Logged in as ${googleAccount.email}")

        mReadSheetCallback = callback

        // Use the authenticated account to sign in to the Drive service.
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            setOf(mScope)
        )
        credential.selectedAccount = googleAccount.account

        val sheetsService = Sheets.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential)
            .setApplicationName(context.resources.getString(R.string.app_name))
            .build()

        // The DriveServiceHelper encapsulates all REST API and SAF functionality.
        // Its instantiation is required before handling any onClick actions.
        val mSpreadsheetServiceHelper = SheetsServiceHelper(sheetsService)

        val task = mSpreadsheetServiceHelper.readSheet(id, range)
        task.addOnSuccessListener {
            Timber.d("Sheet read success.")
            mReadSheetCallback?.onSuccess(it)
        }
        task.addOnFailureListener {
            Timber.e(it, "Sheet read failed.")
            mReadSheetCallback?.onFailure(it)
        }
        task.addOnCanceledListener {
            Timber.d("Sheet read canceled.")
            mReadSheetCallback?.onCancel()
        }
    }

    fun unregisterReadSheetCallback() {
        mReadSheetCallback = null
    }
}

interface ReadSheetCallback {
    fun onSuccess(data: ArrayList<*>)
    fun onFailure(exception: Exception)
    fun onCancel()
}