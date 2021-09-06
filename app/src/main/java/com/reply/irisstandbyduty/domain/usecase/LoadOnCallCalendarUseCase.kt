package com.reply.irisstandbyduty.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.reply.irisstandbyduty.domain.CalendarParseException
import com.reply.irisstandbyduty.domain.StandbyDutyCalendarParser
import com.reply.irisstandbyduty.domain.service.sheets.ReadSheetCallback
import com.reply.irisstandbyduty.domain.service.sheets.SheetsReader
import com.reply.irisstandbyduty.model.Shift
import com.reply.irisstandbyduty.result.Result
import com.reply.irisstandbyduty.shared.DispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import timber.log.Timber
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Created by Reply on 04/09/21.
 */
class LoadOnCallCalendarUseCase @Inject constructor(
    private val calendarParser: StandbyDutyCalendarParser,
    private val sheetsReader: SheetsReader,
    dispatcherProvider: DispatcherProvider
) : FlowUseCase<LoadOnCallCalendarUseCase.Input, List<Shift>>(
    dispatcherProvider.io()
) {

    data class Input(
        val googleSignInAccount: GoogleSignInAccount
    )


    @ExperimentalCoroutinesApi
    override fun execute(parameters: Input): Flow<Result<List<Shift>>> {
        return callbackFlow {
            sheetsReader.readSheet(
                googleAccount = parameters.googleSignInAccount,
                id = "1s4igidc3c3z5u6fNSWc7Dwsyz1lObUC3KptXaDYH38A",
                range = "A1:AF16",
                sheetName = null,
                callback = object : ReadSheetCallback {
                    override fun onSuccess(data: ArrayList<*>) {
                        try {
                            (data as? ArrayList<ArrayList<String>>)?.let {
                                val onCallMonth = calendarParser.parse(it)
                                printCalendar(onCallMonth)
                                sendBlocking(Result.Success(onCallMonth))
                            } ?: throw CalendarParseException("")
                        } catch (e: Exception) {
                            sendBlocking(Result.Error(e))
                        }
                    }

                    override fun onFailure(exception: Exception) {
                        sendBlocking(Result.Error(exception))
                    }

                    override fun onCancel() {
                        sendBlocking(Result.Error(Exception()))
                    }
                }
            )

            awaitClose { sheetsReader.unregisterReadSheetCallback() }
        }.catch { e ->
            emit(Result.Error(e))
        }
    }

    private fun printCalendar(shifts: List<Shift>) {
        val pattern = "dd-MM"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.ITALY)
        shifts.forEach {
            Timber.d("${simpleDateFormat.format(it.date)} -> ${it.employee.name}")
        }
    }
}