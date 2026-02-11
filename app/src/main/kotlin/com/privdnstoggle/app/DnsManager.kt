package com.privdnstoggle.app

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.net.InetSocketAddress
import java.net.Socket
import javax.net.ssl.SSLSocketFactory

object DnsManager {

    private const val TAG = "DnsManager"
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
     * Returns null if valid, or an error message string if invalid. Never throws.
     */
    fun validateHostnameSyntax(input: String): String? {
        return try {
            DebugLogger.d(TAG, "validateHostnameSyntax: Validating '$input'")
            val trimmed = input.trim()

            if (trimmed.isBlank()) {
                DebugLogger.d(TAG, "validateHostnameSyntax: Hostname is blank")
                return "Hostname cannot be empty"
            }

            // Reject scheme prefixes
            if (trimmed.contains("://")) {
                DebugLogger.d(TAG, "validateHostnameSyntax: Contains scheme")
                return "Do not include a scheme (e.g. https://). Enter the hostname only."
            }

            // Reject trailing slashes
            if (trimmed.contains("/")) {
                DebugLogger.d(TAG, "validateHostnameSyntax: Contains slash")
                return "Do not include paths or trailing slashes. Enter the hostname only."
            }

            // Check IPv4 and IPv6 before port check (IPv6 contains colons, e.g. 2001:4860:4860::8888)
            if (IPV4_REGEX.matches(trimmed)) {
                DebugLogger.d(TAG, "validateHostnameSyntax: Valid IPv4")
                return null
            }
            val ipv6Part = trimmed.removeSurrounding("[", "]")
            if (IPV6_REGEX.matches(ipv6Part)) {
                DebugLogger.d(TAG, "validateHostnameSyntax: Valid IPv6")
                return null
            }

            // Reject port numbers (hostname:port only; don't confuse with IPv6)
            if (trimmed.matches(Regex(".*:\\d+$"))) {
                DebugLogger.d(TAG, "validateHostnameSyntax: Contains port")
                return "Do not include a port number. Enter the hostname only."
            }

            // Check hostname pattern
            if (HOSTNAME_REGEX.matches(trimmed)) {
                DebugLogger.d(TAG, "validateHostnameSyntax: Valid hostname")
                return null
            }

            DebugLogger.d(TAG, "validateHostnameSyntax: Invalid format")
            "Invalid hostname or IP address format"
        } catch (e: Throwable) {
            DebugLogger.e(TAG, "validateHostnameSyntax: Exception during validation", e)
            "Invalid hostname or IP address format"
        }
    }

    /**
     * Attempts a TLS connection to the given hostname on port 853 (DNS-over-TLS).
     * Times out after 10 seconds. Returns Result.success or Result.failure with a
     * descriptive message. Never throws.
     */
    suspend fun testConnection(hostname: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                DebugLogger.d(TAG, "testConnection: Starting connection test to '$hostname'")
                withTimeout(CONNECTION_TIMEOUT_MS) {
                    val trimmedHost = hostname.trim()
                    if (trimmedHost.isEmpty()) {
                        DebugLogger.e(TAG, "testConnection: Hostname is empty")
                        Result.failure(Exception("Hostname is empty"))
                    } else {
                        val factory = SSLSocketFactory.getDefault()
                        val socket: Socket = factory.createSocket(trimmedHost, DOT_PORT)
                        try {
                            socket.soTimeout = CONNECTION_TIMEOUT_MS.toInt()
                            DebugLogger.d(TAG, "testConnection: Connection successful to '$trimmedHost'")
                            Result.success(Unit)
                        } finally {
                            try { socket.close() } catch (_: Exception) {}
                        }
                    }
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                DebugLogger.e(TAG, "testConnection: Connection timed out", e)
                Result.failure(Exception("Connection timed out after 10 seconds"))
            } catch (e: java.net.UnknownHostException) {
                DebugLogger.e(TAG, "testConnection: Unknown host '$hostname'", e)
                Result.failure(Exception("Could not resolve hostname: $hostname"))
            } catch (e: java.net.ConnectException) {
                DebugLogger.e(TAG, "testConnection: Connection refused by '$hostname'", e)
                Result.failure(Exception("Connection refused by $hostname"))
            } catch (e: javax.net.ssl.SSLException) {
                DebugLogger.e(TAG, "testConnection: TLS handshake failed for '$hostname'", e)
                Result.failure(Exception("TLS handshake failed: ${e.message}"))
            } catch (e: Exception) {
                DebugLogger.e(TAG, "testConnection: Connection failed for '$hostname'", e)
                Result.failure(Exception("Connection failed: ${e.message ?: "Unknown error"}"))
            } catch (e: Throwable) {
                DebugLogger.e(TAG, "testConnection: Unexpected error for '$hostname'", e)
                Result.failure(Exception("Connection failed: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    // --- Read current system state (defensive: never throw so UI never crashes) ---

    fun getCurrentMode(resolver: ContentResolver): String {
        return try {
            Settings.Global.getString(resolver, DNS_MODE_KEY) ?: MODE_OFF
        } catch (_: Throwable) {
            MODE_OFF
        }
    }

    fun getCurrentHostname(resolver: ContentResolver): String {
        return try {
            Settings.Global.getString(resolver, DNS_SPECIFIER_KEY) ?: ""
        } catch (_: Throwable) {
            ""
        }
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
        val trimmed = hostname.trim()
        DebugLogger.d(TAG, "saveHostname: Saving hostname '$trimmed'")
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_HOSTNAME, trimmed).apply()
        DebugLogger.d(TAG, "saveHostname: Saved successfully")
    }

    // --- Permission check ---

    fun hasPermission(context: Context): Boolean {
        return try {
            val current = getCurrentMode(context.contentResolver)
            Settings.Global.putString(context.contentResolver, DNS_MODE_KEY, current)
            true
        } catch (_: SecurityException) {
            false
        } catch (_: Throwable) {
            false
        }
    }
}
