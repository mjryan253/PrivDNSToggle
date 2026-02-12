package com.privdnstoggle.app

import android.app.Application
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DnsSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application
    private val resolver = application.contentResolver

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private val _currentHost = MutableStateFlow("")
    val currentHost: StateFlow<String> = _currentHost.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val _savedHostname = MutableStateFlow("")
    val savedHostname: StateFlow<String> = _savedHostname.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())

    private val settingsObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            refresh()
        }
    }

    init {
        refresh()
        registerSettingsObserver()
    }

    private fun registerSettingsObserver() {
        try {
            val modeUri = Settings.Global.getUriFor("private_dns_mode")
            val specifierUri = Settings.Global.getUriFor("private_dns_specifier")
            if (modeUri != null) {
                resolver.registerContentObserver(modeUri, true, settingsObserver)
            }
            if (specifierUri != null && specifierUri != modeUri) {
                resolver.registerContentObserver(specifierUri, true, settingsObserver)
            }
        } catch (_: Throwable) {
            // Ignore; refresh on onResume will still sync state
        }
    }

    private fun unregisterSettingsObserver() {
        try {
            resolver.unregisterContentObserver(settingsObserver)
        } catch (_: Throwable) { }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                _isActive.value = DnsManager.isActive(resolver)
                _currentHost.value = DnsManager.getCurrentHostname(resolver)
                _hasPermission.value = DnsManager.hasPermission(app)
                _savedHostname.value = DnsManager.getSavedHostname(app)
            } catch (_: Throwable) {
                // Defensive: never crash; state stays as-is
            }
        }
    }

    fun enableDns(hostname: String): Boolean {
        val success = DnsManager.enableDns(resolver, hostname)
        if (success) refresh()
        return success
    }

    fun disableDns(): Boolean {
        val success = DnsManager.disableDns(resolver)
        if (success) refresh()
        return success
    }

    fun saveHostname(hostname: String) {
        DnsManager.saveHostname(app, hostname.trim())
        refresh()
    }

    override fun onCleared() {
        super.onCleared()
        unregisterSettingsObserver()
    }
}
