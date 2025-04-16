package com.example.workmanagertest

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class TestWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        private const val TAG = "TestWorker"
        private const val UNIQUE_WORK_NAME_INITIAL = "ANDROID_TV_WORKER_INITIAL"


        fun scheduleWorker(context: Context) {
            Log.d(TAG, "scheduleWorker called")
            WorkerHelper.getConcurrentWorkerCountAsync(
                context,
                UNIQUE_WORK_NAME_INITIAL
            ) { workerCount ->
                Log.d(TAG, "scheduleWorker workerCount=$workerCount")
                if (workerCount > 0) {
                    return@getConcurrentWorkerCountAsync
                }
                enqueueWorker(context, 30000L) // 30 seconds
            }
        }

        private fun enqueueWorker(context: Context, durationMs: Long) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Do not fetch data if no connection
                .build()

            val workRequest = OneTimeWorkRequest.Builder(TestWorker::class.java)
                .setInitialDelay(durationMs, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_WORK_NAME_INITIAL,
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    workRequest
                )
        }
    }

    override fun doWork(): Result {
        if (WorkerHelper.getConcurrentJobsRunningCount(context, UNIQUE_WORK_NAME_INITIAL) > 1) {
            Log.d(TAG, "Concurrent job starting.  Ending job with success.")
            return Result.success()
        }

        Log.d(TAG, "TestWorker is running at: ${TimeUtils.getCurrentDateTime()}")

        enqueueWorker(context, 30000L) // 30 seconds
        return Result.success()
    }
}