package com.example.indytest.DIDs

import android.app.Application
import timber.log.Timber
import com.anonyome.mysudo.R

class TestApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        setTheme(R.style.AppTheme)
        Timber.plant(Timber.DebugTree())
    }
}