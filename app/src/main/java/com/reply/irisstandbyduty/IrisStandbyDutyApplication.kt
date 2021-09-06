package com.reply.irisstandbyduty

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Created by Reply on 06/09/21.
 */

@HiltAndroidApp
class IrisStandbyDutyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }

}