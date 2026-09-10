package com.privdnstoggle.app

import kotlin.system.exitProcess

/**
 * Runs inside a process spawned by Shizuku with shell (or root) privilege, not in the app process.
 * Shizuku instantiates this class by name via reflection, so proguard-rules.pro keeps it intact.
 * There is no usable Context here: do not touch Settings, SharedPreferences, or DnsManager.
 */
class ShizukuUserService : IUserService.Stub() {

    private companion object {
        const val TAG = "ShizukuUserService"
    }

    override fun destroy() {
        exitProcess(0)
    }

    override fun grant(packageName: String, permission: String, userId: Int): String {
        return try {
            DebugLogger.d(TAG, "grant: pm grant --user $userId $packageName $permission")
            val process = ProcessBuilder("pm", "grant", "--user", userId.toString(), packageName, permission)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            if (exitCode == 0) "" else "pm grant exited with $exitCode: $output"
        } catch (e: Throwable) {
            DebugLogger.e(TAG, "grant: pm grant failed", e)
            "pm grant failed: ${e.message ?: "Unknown error"}"
        }
    }
}
