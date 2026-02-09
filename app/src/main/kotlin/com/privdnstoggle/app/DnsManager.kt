package com.privdnstoggle.app

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocketFactory

object DnsManager {

    private const val PREFS_NAME = "dns_prefs"
    private const val KEY_HOSTNAME = "saved_hostname"
    private const val DEFAULT_HOSTNAME = "dns.adguard.com"

    private const val DNS_MODE_KEY = "private_dns_mode"
    private const val DNS_SPECIFIER_KEY = "private_dns_specifier"

    private const val CONNECTION_TIMEOUT_MS = 10_000L
    private const val DOT_PORT = 853 // DNS-over-TLS standard port

    const val MODE_OFF = "off"
    const val MODE_HOSTNAME = "hostname"
    const val MODE_OPPORTUNISTIC = "opportunistic"

    // --- Hostname / IP syntax validation ---

    private val HOSTNAME_REGEX = Regex(
        "^(?!-)[A-Za-z0-9-]{1,63}(?<!-)(\\.(?!-)[A-Za-z0-9-]{1,63}(?<!-))+$"
    )

    private val IPV4_REGEX = Regex(
        "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$"
    )

    private val IPV6_REGEX = Regex(
        "^\\[?([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}]?$"
    )

    /**
     * Validates hostname or IP syntax.
     * Returns null if valid, or an error message string if invalid.
     */
    fun validateHostnameSyntax(input: String): String? {
        val trimmed = input.trim()

        if (trimmed.isBlank()) {
            return "Hostname cannot be empty"
        }

        // Reject scheme prefixes
        if (trimmed.contains("://")) {
            return "Do not include a scheme (e.g. https://). Enter the hostname only."
        }

        // Reject trailing slashes
        if (trimmed.contains("/")) {
            return "Do not include paths or trailing slashes. Enter the hostname only."
        }

        // Reject port numbers
        if (trimmed.matches(Regex(".*:\\d+$"))) {
            return "Do not include a port number. Enter the hostname only."
        }

        // Check against valid patterns
        if (HOSTNAME_REGEX.matches(trimmed)) return null
        if (IPV4_REGEX.matches(trimmed)) return null
        if (IPV6_REGEX.matches(trimmed.removeSurrounding("[", "]"))) return null

        return "Invalid hostname or IP address format"
    }

    /**
     * Attempts a TLS connection to the given hostname on port 853 (DNS-over-TLS).
     * Times out after 10 seconds. Returns Result.success or Result.failure with a
     * descriptive message.
     */
    suspend fun testConnection(hostname: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                withTimeout(CONNECTION_TIMEOUT_MS) {
                    val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
                    val socket = factory.createSocket()
                    try {
                        socket.connect(
                            InetSocketAddress(hostname, DOT_PORT),
                            CONNECTION_TIMEOUT_MS.toInt()
                        )
                        // If connect succeeded, the DNS-over-TLS provider is reachable
                        Result.success(Unit)
                    } finally {
                        try { socket.close() } catch (_: Exception) {}
                    }
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Result.failure(Exception("Connection timed out after 10 seconds"))
            } catch (e: java.net.UnknownHostException) {
                Result.failure(Exception("Could not resolve hostname: ${hostname}"))
            } catch (e: java.net.ConnectException) {
                Result.failure(Exception("Connection refused by ${hostname}"))
            } catch (e: javax.net.ssl.SSLException) {
                Result.failure(Exception("TLS handshake failed: ${e.message}"))
            } catch (e: Exception) {
                Result.failure(Exception("Connection failed: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    // --- Read current system state ---

    fun getCurrentMode(resolver: ContentResolver): String {
        return Settings.Global.getString(resolver, DNS_MODE_KEY) ?: MODE_OFF
    }

    fun getCurrentHostname(resolver: ContentResolver): String {
        return Settings.Global.getString(resolver, DNS_SPECIFIER_KEY) ?: ""
    }

    fun isActive(resolver: ContentResolver): Boolean {
        return getCurrentMode(resolver) == MODE_HOSTNAME
    }

    // --- Write system state ---

    fun enableDns(resolver: ContentResolver, hostname: String): Boolean {
        return try {
            Settings.Global.putString(resolver, DNS_MODE_KEY, MODE_HOSTNAME)
            Settings.Global.putString(resolver, DNS_SPECIFIER_KEY, hostname)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    fun disableDns(resolver: ContentResolver): Boolean {
        return try {
            Settings.Global.putString(resolver, DNS_MODE_KEY, MODE_OFF)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    fun toggle(context: Context): Boolean {
        val resolver = context.contentResolver
        return if (isActive(resolver)) {
            disableDns(resolver)
        } else {
            val hostname = getSavedHostname(context)
            if (hostname.isBlank()) return false
            enableDns(resolver, hostname)
        }
    }

    // --- SharedPreferences for saved hostname ---

    fun getSavedHostname(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_HOSTNAME, DEFAULT_HOSTNAME) ?: DEFAULT_HOSTNAME
    }

    fun saveHostname(context: Context, hostname: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_HOSTNAME, hostname.trim()).apply()
    }

    // --- Permission check ---

    fun hasPermission(context: Context): Boolean {
        return try {
            // Try reading; if we can write we certainly can read
            Settings.Global.getString(context.contentResolver, DNS_MODE_KEY)
            // Attempt a no-op write to truly verify write access
            val current = getCurrentMode(context.contentResolver)
            Settings.Global.putString(context.contentResolver, DNS_MODE_KEY, current)
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
