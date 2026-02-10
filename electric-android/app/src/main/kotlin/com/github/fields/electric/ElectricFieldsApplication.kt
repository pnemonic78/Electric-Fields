package com.github.fields.electric

import android.app.Application
import timber.log.Timber

class ElectricFieldsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
