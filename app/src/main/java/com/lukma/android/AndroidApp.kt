package com.lukma.android

import android.app.Application
import com.lukma.android.data.dataModule
import com.lukma.android.domain.useCaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AndroidApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AndroidApp)
            modules(
                listOf(
                    dataModule,
                    useCaseModule
                )
            )
        }
    }
}
