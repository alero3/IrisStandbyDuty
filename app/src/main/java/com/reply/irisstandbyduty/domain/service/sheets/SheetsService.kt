package com.reply.irisstandbyduty.domain.service.sheets

import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.Sheets
import com.reply.irisstandbyduty.R
import com.reply.irisstandbyduty.domain.service.GoogleDriveAuthenticator
import timber.log.Timber
import java.lang.Exception


/**
 * Created by Reply on 01/08/21.
 */
class SheetsService(
    private val fragment: Fragment
) : GoogleDriveAuthenticator(fragment) {

    private var mSpreadsheetServiceHelper: SheetsServiceHelper? = null

    fun readSheet(
        id: String,
        range: String,
        sheetName: String? = null,
        callback: ReadSheetCallback) {
        Timber.d("Logged in as ${mSignInAccount?.email}")
        val task = mSpreadsheetServiceHelper?.readSheet(id, "A1:AF16")
        task?.addOnSuccessListener {
            Timber.d("Sheet read success.")
            callback.onSuccess(it)
        }
        task?.addOnFailureListener {
            Timber.e(it, "Sheet read failed.")
            callback.onFailure(it)
        }
        task?.addOnCanceledListener {
            Timber.d("Sheet read canceled.")
            callback.onCancel()
        }
    }

    override fun initializeClient(googleAccount: GoogleSignInAccount) {
        // Use the authenticated account to sign in to the Drive service.
        val credential = GoogleAccountCredential.usingOAuth2(
            fragment.requireContext(),
            setOf(mScope)
        )
        credential.selectedAccount = googleAccount.account

        val sheetsService = Sheets.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential)
            .setApplicationName(fragment.requireContext().resources.getString(R.string.app_name))
            .build()

        // The DriveServiceHelper encapsulates all REST API and SAF functionality.
        // Its instantiation is required before handling any onClick actions.
        mSpreadsheetServiceHelper = SheetsServiceHelper(sheetsService)

        serviceListener?.onLoginSuccess()
    }

}

interface ReadSheetCallback {
    fun onSuccess(data: ArrayList<*>)
    fun onFailure(exception: Exception)
    fun onCancel()
}