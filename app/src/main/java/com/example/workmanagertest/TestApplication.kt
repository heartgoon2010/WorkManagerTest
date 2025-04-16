package com.example.workmanagertest

import android.app.Application

class TestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        System.out.println("wkwDebug TestApplication")
        TestWorker.scheduleWorker(this)
    }
}