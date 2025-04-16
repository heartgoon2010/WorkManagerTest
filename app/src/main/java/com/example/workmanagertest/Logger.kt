class TubiLogger {
    companion object {
        private const val CLIENT_LOG_BUFFER_SIZE = 100
        private val _logFlow = MutableSharedFlow<SimpleClientLog>(
            replay = CLIENT_LOG_BUFFER_SIZE,
            extraBufferCapacity = CLIENT_LOG_BUFFER_SIZE
        )
        val logFlow: SharedFlow<SimpleClientLog> = _logFlow

        fun log(loggingType: LoggingType, subType: LogSubType, message: String) {
            val emitSuccess = _logFlow.tryEmit(SimpleClientLog(loggingType, subType, message))
            if (!emitSuccess) {
                // Log the failure using Android's system log since our logging system failed
                android.util.Log.e("TubiLogger", "Failed to emit log: Buffer full. Message: $message")
                
                // You might want to track this metric
                FirebaseCrashlytics.getInstance().recordException(
                    Exception("Log emission failed: Buffer full. Message: $message")
                )
            }
        }

        fun startCollecting() {
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    logFlow.collect { clientLog ->
                        try {
                            val logEvent = ClientLogEvent.newBuilder()
                                .setClientCommon(getClientCommon())
                                .setLogType(clientLog.loggingType.type.toProto())
                                .setLogSubtype(clientLog.subType.toProto())
                                .setLevel(clientLog.loggingType.level.toProto())
                                .setDeviceId(AppHelper.getUUID().toProto())
                                .setPlatform(getPlatform().name.toProto())
                                .setVersion(getVersionName().toProto())
                                .setMessage(clientLog.message.toProto())
                                .build()
                            
                            ClientEventSender.INSTANCE.createAndSendEventV3(
                                CLIENT_LOG_EVENT_NAME,
                                logEvent
                            )
                        } catch (e: Exception) {
                            // Log individual message processing failures
                            android.util.Log.e("TubiLogger", "Failed to process log message", e)
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }
                    }
                } catch (e: CancellationException) {
                    // Normal cancellation, don't report
                    android.util.Log.i("TubiLogger", "Log collection cancelled")
                } catch (e: Exception) {
                    // Log collection loop failed
                    android.util.Log.e("TubiLogger", "Log collection failed", e)
                    FirebaseCrashlytics.getInstance().recordException(e)
                    
                    // Optionally restart collection after a delay
                    delay(5000) // Wait 5 seconds
                    startCollecting() // Restart collection
                }
            }
        }
    }
} 