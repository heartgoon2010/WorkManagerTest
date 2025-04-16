package com.example.workmanagertest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        scheduleWorker()
    }

    private fun scheduleWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<TestWorker>(
            30, TimeUnit.SECONDS,  // Repeat interval
            10, TimeUnit.SECONDS   // Flex interval
        )
        .setConstraints(constraints)
        .build()

        // Use unique work to ensure we don't schedule multiple instances
        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                "TestWorker",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,  // Keep existing if any
                workRequest
            )
    }
}