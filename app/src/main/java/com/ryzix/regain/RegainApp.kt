package com.ryzix.regain

import android.app.Application
import com.ryzix.regain.data.AppContainer

class RegainApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
