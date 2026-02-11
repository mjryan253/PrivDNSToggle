package com.privdnstoggle.app

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Debug logger that captures log entries for display in the debug menu.
 * Maintains a circular buffer of the last 100 log entries.
 */
object DebugLogger {
    private const val MAX_ENTRIES = 100
    private val logEntries = mutableListOf<String>()
    private val lock = ReentrantLock()
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    /**
     * Logs a debug message and captures it for UI display.
     */
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        val timestamp = dateFormat.format(Date())
        val entry = if (throwable != null) {
            "[$timestamp] $tag: $message\n${Log.getStackTraceString(throwable)}"
        } else {
            "[$timestamp] $tag: $message"
        }
        
        Log.d(tag, message, throwable)
        addEntry(entry)
    }

    /**
     * Logs an error message and captures it for UI display.
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val timestamp = dateFormat.format(Date())
        val entry = if (throwable != null) {
            "[$timestamp] $tag: $message\n${Log.getStackTraceString(throwable)}"
        } else {
            "[$timestamp] $tag: $message"
        }
        
        Log.e(tag, message, throwable)
        addEntry(entry)
    }

    /**
     * Adds an entry to the log buffer, maintaining max size.
     */
    private fun addEntry(entry: String) {
        lock.withLock {
            logEntries.add(entry)
            if (logEntries.size > MAX_ENTRIES) {
                logEntries.removeAt(0)
            }
        }
    }

    /**
     * Returns all log entries as a formatted string.
     */
    fun getAllLogs(): String {
        return lock.withLock {
            logEntries.joinToString("\n")
        }
    }

    /**
     * Returns all log entries as a list.
     */
    fun getLogEntries(): List<String> {
        return lock.withLock {
            logEntries.toList()
        }
    }

    /**
     * Clears all log entries.
     */
    fun clear() {
        lock.withLock {
            logEntries.clear()
        }
    }
}
