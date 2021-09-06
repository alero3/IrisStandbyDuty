package com.reply.irisstandbyduty.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.reply.irisstandbyduty.domain.usecase.LoadOnCallCalendarUseCase
import com.reply.irisstandbyduty.model.Shift
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import com.reply.irisstandbyduty.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val loadOnCallCalendarUseCase: LoadOnCallCalendarUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    fun loadCalendar(googleAccount: GoogleSignInAccount) = viewModelScope.launch {
        _uiState.value = UiState.Loading
        try {
            loadOnCallCalendarUseCase.invoke(LoadOnCallCalendarUseCase.Input(googleAccount))
                .collect {
                    if (it is Result.Success) {
                        _uiState.value = UiState.ShowCalendar(it.data)
                    }
                }
        } catch (e: Exception) {
            Timber.e(e, "loadCurrentOnCallEmployee failed.")
        }
    }

}

sealed class UiState {
    object Loading : UiState()
    data class ShowCalendar(val shifts: List<Shift>) : UiState()
    data class Error(val exception: Exception): UiState()
}