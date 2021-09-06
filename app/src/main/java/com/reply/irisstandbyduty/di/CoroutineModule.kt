package com.reply.irisstandbyduty.di

import com.reply.irisstandbyduty.shared.DefaultDispatcherProvider
import com.reply.irisstandbyduty.shared.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

/**
 * Created by Reply on 06/09/21.
 */

@Module
@InstallIn(ViewModelComponent::class)
object CoroutineModule {

    @Provides
    fun provideDefaultDispatcher(): DispatcherProvider {
        return DefaultDispatcherProvider()
    }

}
