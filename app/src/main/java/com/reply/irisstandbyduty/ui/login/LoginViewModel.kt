package com.reply.irisstandbyduty.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.reply.irisstandbyduty.domain.service.GoogleDriveAuthenticator
import java.lang.Exception


class LoginViewModel(
    private val googleDriveAuthenticator: GoogleDriveAuthenticator
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun checkAuthenticationStatus() {
        googleDriveAuthenticator.checkLoginStatus()
    }

    fun setAuthenticationStatus(loginState: LoginState) {
        _loginState.postValue(loginState)
    }
}

sealed class LoginState {
    object NotLoggedIn : LoginState()
    data class LoggedIn(val googleAccount: GoogleSignInAccount) : LoginState()
    data class LoginError(val exception: Exception) : LoginState()
    object Loading : LoginState()
}