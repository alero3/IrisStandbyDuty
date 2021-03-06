package com.reply.irisstandbyduty.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.reply.irisstandbyduty.domain.usecase.LoadOnCallCalendarUseCase
import com.reply.irisstandbyduty.model.Employee
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import com.reply.irisstandbyduty.result.Result
import com.reply.irisstandbyduty.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val loadOnCallCalendarUseCase: LoadOnCallCalendarUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    fun loadCurrentOnCallEmployee(googleAccount: GoogleSignInAccount) = viewModelScope.launch {
        _uiState.value = UiState.Loading
        try {
            loadOnCallCalendarUseCase.invoke(LoadOnCallCalendarUseCase.Input(googleAccount))
                .collect {
                    if (it is Result.Success) {
                        // Get employee which is on call right now.
                        val now = GregorianCalendar().time
                        val currentOnCallEmployee = it.data.firstOrNull { shift ->
                            DateUtils.areSameDay(shift.date, now)
                        }?.employee
                        if (currentOnCallEmployee != null) {
                            _uiState.value = UiState.OnCallEmployeeAvailable(currentOnCallEmployee)
                        } else {
                            // Employee not found. Something wrong in the data source.
                            _uiState.value = UiState.Error(OnCallEmployeeNotFound())
                        }
                    }
                }
        } catch (e: Exception) {
            Timber.e(e, "loadCurrentOnCallEmployee failed.")
        }
    }

}

sealed class UiState {
    object Loading : UiState()
    data class OnCallEmployeeAvailable(val employee: Employee) : UiState()
    data class Error(val exception: Exception): UiState()
}

class OnCallEmployeeNotFound: Exception()