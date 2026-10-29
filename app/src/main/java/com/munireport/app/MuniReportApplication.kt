package com.munireport.app

import android.app.Application
import com.munireport.app.di.AppContainer

class MuniReportApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
