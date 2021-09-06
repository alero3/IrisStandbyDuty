package com.reply.irisstandbyduty.domain.service.sheets

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import timber.log.Timber
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * Created by Reply on 03/09/21.
 */
class SheetsServiceHelper @Inject constructor(
    private val sheetsService: Sheets
) {
    private val mExecutor: Executor = Executors.newSingleThreadExecutor()

    fun readSheet(spreadsheetId: String, range: String): Task<ArrayList<*>> {
        return Tasks.call(mExecutor, Callable {
            val response: ValueRange = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute()
            val values: List<List<Any>>? = response.getValues()
            if (values == null || values.isEmpty()) {
                Timber.d("No data found.")
                throw Exception()
            } else {
                Timber.d("Spreadsheet data found.")
                for (row in values) {
                    Timber.d("$row")
                }
                return@Callable values as? ArrayList<*> ?: throw Exception()
            }
        })
    }

}