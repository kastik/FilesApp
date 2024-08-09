package com.kastik.files

import AppContext
import android.app.Application

class FilesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContext.setUp(applicationContext) //TODO This is probably a bad idea but it is needed for context in expect/actual declarations
    }
}