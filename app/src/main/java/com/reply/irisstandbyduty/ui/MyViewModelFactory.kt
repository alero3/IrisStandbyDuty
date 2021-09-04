package com.reply.irisstandbyduty.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.reply.irisstandbyduty.domain.usecase.LoadOnCallCalendarUseCase
import com.reply.irisstandbyduty.ui.home.HomeViewModel

class MyViewModelFactory constructor(private val loadOnCallCalendarUseCase: LoadOnCallCalendarUseCase): ViewModelProvider.Factory {

     override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            HomeViewModel(
                loadOnCallCalendarUseCase = loadOnCallCalendarUseCase
            ) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}