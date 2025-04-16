package com.example.workmanagertest

import android.content.Context
import android.util.Log
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

object WorkerHelper {
    const val TAG = "WorkerJobHelper"

    /**
     * From the Strings uniqueWorkerIds, check if more than one is currently running in the WorkManager sdk
     */
    fun getConcurrentJobsRunningCount(
        context: Context,
        vararg uniqueWorkerIds: String?
    ): Int {
        var runningJobsCount = 0
        for (workerId in uniqueWorkerIds) {
            try {
                val periodicWorkers = WorkManager.getInstance(context)
                    .getWorkInfosForUniqueWork(workerId!!).get()
                for (workInfo in periodicWorkers) {
                    if (workInfo != null && workInfo.state == WorkInfo.State.RUNNING) {
                        runningJobsCount++
                    }
                }
            } catch (e: ExecutionException) {
                Log.d(TAG, "Fetching of active workers failed")
            } catch (e: InterruptedException) {
                Log.d(TAG, "Fetching of active workers interrupted")
            }
        }
        return runningJobsCount
    }

    fun getConcurrentWorkerCountAsync(
        context: Context,
        uniqueWorkerId: String,
        callback: (Int) -> Unit
    ) {
        var workersCount = 0
        try {
            val future = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(uniqueWorkerId)
            future.addListener({
                val workers = future.get()
                for (workInfo in workers) {
                    if (workInfo.state == WorkInfo.State.RUNNING || workInfo.state == WorkInfo.State.ENQUEUED) {
                        workersCount++
                    }
                }
                callback(workersCount)
            }, Executors.newSingleThreadExecutor())
        } catch (e: Exception) {
            Log.d(TAG, "PMR Timeout Exception Happened ${e.message}")
        }
    }
}