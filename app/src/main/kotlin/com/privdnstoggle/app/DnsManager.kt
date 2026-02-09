package com.privdnstoggle.app

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings

object DnsManager {

    private const val PREFS_NAME = "dns_prefs"
    private const val KEY_HOSTNAME = "saved_hostname"
    private const val DEFAULT_HOSTNAME = "dns.adguard.com"

    private const val DNS_MODE_KEY = "private_dns_mode"
    private const val DNS_SPECIFIER_KEY = "private_dns_specifier"

    const val MODE_OFF = "off"
    const val MODE_HOSTNAME = "hostname"
    const val MODE_OPPORTUNISTIC = "opportunistic"

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
