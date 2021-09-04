package com.reply.irisstandbyduty.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.reply.irisstandbyduty.domain.usecase.LoadOnCallCalendarUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import com.reply.irisstandbyduty.result.Result
import java.text.SimpleDateFormat
import java.util.*


class HomeViewModel(
    private val loadOnCallCalendarUseCase: LoadOnCallCalendarUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<UiState>().apply {
        value = UiState.Loading
    }
    val uiState: LiveData<UiState> = _uiState

    fun loadCurrentOnCallEmployee(googleAccount: GoogleSignInAccount) = viewModelScope.launch {
        try {
            loadOnCallCalendarUseCase.invoke(LoadOnCallCalendarUseCase.Input(googleAccount))
                .collect {
                    if (it is Result.Success) {
                        Timber.d("Result success")
                        val pattern = "dd-MM"
                        val simpleDateFormat = SimpleDateFormat(pattern, Locale.ITALY)
                        it.data.onCallCalendar.toSortedMap().forEach { entry ->
                            val date = entry.key
                            val person = entry.value
                            Timber.d("${simpleDateFormat.format(date)} -> $person")
                        }
                    }

                }
        } catch (e: Exception) {
            Timber.e(e, "loadCurrentOnCallEmployee failed.")
        }

    }

    fun setLoggedInUser(googleAccount: GoogleSignInAccount) {
        _uiState.value = UiState.LoggedIn(googleAccount)
    }
}

sealed class UiState {
    object NotLoggedIn : UiState()
    data class LoggedIn(val googleAccount: GoogleSignInAccount) : UiState()
    object Loading : UiState()
    data class OnCallEmployee(val name: String) : UiState()
}