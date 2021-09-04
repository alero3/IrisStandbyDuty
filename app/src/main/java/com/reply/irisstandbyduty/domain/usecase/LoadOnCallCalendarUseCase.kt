package com.reply.irisstandbyduty.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.reply.irisstandbyduty.domain.CalendarParseException
import com.reply.irisstandbyduty.domain.StandbyDutyCalendarParser
import com.reply.irisstandbyduty.domain.service.sheets.ReadSheetCallback
import com.reply.irisstandbyduty.domain.service.sheets.SheetsReader
import com.reply.irisstandbyduty.model.StandbyDutyMonth
import com.reply.irisstandbyduty.result.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import java.lang.Exception

/**
 * Created by Reply on 04/09/21.
 */
class LoadOnCallCalendarUseCase constructor(
    private val calendarParser: StandbyDutyCalendarParser,
    private val sheetsReader: SheetsReader,
    coroutineDispatcher: CoroutineDispatcher
) : FlowUseCase<LoadOnCallCalendarUseCase.Input, StandbyDutyMonth>(
    coroutineDispatcher
) {

    data class Input(
        val googleSignInAccount: GoogleSignInAccount
    )


    @ExperimentalCoroutinesApi
    override fun execute(parameters: Input): Flow<Result<StandbyDutyMonth>> {
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
}