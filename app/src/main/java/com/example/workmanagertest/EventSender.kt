class EventSender {
    companion object {
        private const val TAG = "EventSender"
        private const val CHANNEL_CAPACITY = 30
        private const val PARALLEL_PROCESSING = 3
        
        private val eventQueue = Channel<V3RequestPackage>(
            capacity = CHANNEL_CAPACITY,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        
        private var eventDropCount = 0
        private var processingFailureCount = 0
        
        fun initialize(scope: CoroutineScope, ioDispatcher: CoroutineDispatcher) {
            scope.launch(ioDispatcher) {
                eventQueue.receiveAsFlow()
                    .buffer(CHANNEL_CAPACITY)
                    .flatMapMerge(concurrency = PARALLEL_PROCESSING) { event ->
                        flow {
                            try {
                                TubiLog.d(TAG, "Processing event: ${event.eventName}")
                                sendEventV3(event)
                                emit(Unit)
                            } catch (e: Exception) {
                                processingFailureCount++
                                TubiLog.e(TAG, "Failed to process event: ${event.eventName}", e)
                                FirebaseCrashlytics.getInstance().recordException(e)
                                FirebaseAnalytics.getInstance().logEvent("event_processing_failure", bundleOf(
                                    "event_name" to event.eventName,
                                    "failure_count" to processingFailureCount,
                                    "error_type" to e.javaClass.simpleName
                                ))
                                throw e
                            }
                        }
                    }
                    .catch { e ->
                        TubiLog.e(TAG, "Event processing stream failed", e)
                        FirebaseCrashlytics.getInstance().recordException(e)
                        // Restart the collection
                        delay(5000)
                        initialize(scope, ioDispatcher)
                    }
                    .collect()
            }
        }
        
        fun sendEvent(event: V3RequestPackage) {
            val result = eventQueue.trySend(event)
            if (!result.isSuccess) {
                eventDropCount++
                TubiLog.w(TAG, "Dropped event due to buffer overflow: ${event.eventName}")
                FirebaseCrashlytics.getInstance().recordException(
                    Exception("Event dropped: ${event.eventName}")
                )
                FirebaseAnalytics.getInstance().logEvent("event_drop", bundleOf(
                    "event_name" to event.eventName,
                    "drop_count" to eventDropCount,
                    "queue_size" to CHANNEL_CAPACITY
                ))
            }
        }
        
        private suspend fun sendEventV3(event: V3RequestPackage) {
            // Your existing sendEventV3 implementation
        }
    }
} 