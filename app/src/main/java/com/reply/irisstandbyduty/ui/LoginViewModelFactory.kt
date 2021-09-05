package com.reply.irisstandbyduty.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.reply.irisstandbyduty.domain.service.GoogleDriveAuthenticator
import com.reply.irisstandbyduty.ui.login.LoginViewModel

class LoginViewModelFactory constructor(private val googleDriveAuthenticator: GoogleDriveAuthenticator): ViewModelProvider.Factory {

     override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            LoginViewModel(
                googleDriveAuthenticator = googleDriveAuthenticator
            ) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}