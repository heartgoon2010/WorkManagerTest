package com.example.workmanagertest

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object TimeUtils {
    /**
     * Get current time in 24-hour format (HH:mm:ss)
     */
    fun getCurrentTime24Hour(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }

    /**
     * Get current time in 12-hour format with AM/PM (hh:mm:ss a)
     */
    fun getCurrentTime12Hour(): String {
        return SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
    }

    /**
     * Get current date and time (dd/MM/yyyy HH:mm:ss)
     */
    fun getCurrentDateTime(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
    }

    /**
     * Get current time using modern Java 8 Time API (requires API 26+)
     */
    fun getCurrentTimeModern(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        return LocalDateTime.now().format(formatter)
    }
} 