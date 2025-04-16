package com.example.workmanagertest

import android.content.BroadcastReceiver
import android.content.Context

class TestInstallReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: android.content.Intent) {
        System.out.println("wkwDebug TestInstallReceiver onReceive")
        TestWorker.scheduleWorker(context)
    }
}