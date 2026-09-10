package com.privdnstoggle.app

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Process
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import rikka.shizuku.Shizuku

/**
 * One-time bootstrap: uses Shizuku to run "pm grant" for WRITE_SECURE_SETTINGS so the user
 * does not need a computer. Not used once the permission is granted.
 * Call from the main thread; Shizuku delivers its callbacks on the main looper.
 */
object ShizukuHelper {

    private const val TAG = "ShizukuHelper"
    private const val REQUEST_CODE = 1001
    private const val BIND_TIMEOUT_MS = 15_000L
    private const val PERMISSION = "android.permission.WRITE_SECURE_SETTINGS"
    private const val UIDS_PER_USER = 100_000 // UserHandle.PER_USER_RANGE

    private val userServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(BuildConfig.APPLICATION_ID, ShizukuUserService::class.java.name)
    )
        .daemon(false)
        .processNameSuffix("shizuku")
        .debuggable(BuildConfig.DEBUG)
        .version(BuildConfig.VERSION_CODE)

    /**
     * Returns Result.success, or Result.failure with a user-facing message. Never throws.
     */
    suspend fun grantWriteSecureSettings(): Result<Unit> {
        return try {
            if (!Shizuku.pingBinder()) {
                DebugLogger.d(TAG, "grant: Shizuku binder not alive")
                return Result.failure(Exception("Shizuku is not running. Install and start Shizuku, then try again."))
            }
            if (Shizuku.isPreV11()) {
                DebugLogger.d(TAG, "grant: Shizuku pre-v11 is unsupported")
                return Result.failure(Exception("Shizuku is too old. Update the Shizuku app."))
            }
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                if (Shizuku.shouldShowRequestPermissionRationale()) {
                    DebugLogger.d(TAG, "grant: Shizuku permission permanently denied")
                    return Result.failure(Exception("Shizuku permission was denied. Allow PrivDNS Toggle in the Shizuku app."))
                }
                if (!requestPermission()) {
                    DebugLogger.d(TAG, "grant: Shizuku permission denied by user")
                    return Result.failure(Exception("Shizuku permission denied."))
                }
            }
            val error = bindAndGrant()
            if (error.isEmpty()) {
                DebugLogger.d(TAG, "grant: pm grant succeeded")
                Result.success(Unit)
            } else {
                DebugLogger.e(TAG, "grant: $error")
                Result.failure(Exception("Grant failed: $error"))
            }
        } catch (e: TimeoutCancellationException) {
            DebugLogger.e(TAG, "grant: timed out waiting for Shizuku user service", e)
            Result.failure(Exception("Shizuku did not respond. Try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            DebugLogger.e(TAG, "grant: unexpected error", e)
            Result.failure(Exception("Shizuku grant failed: ${e.message ?: "Unknown error"}"))
        }
    }

    private suspend fun requestPermission(): Boolean {
        val result = CompletableDeferred<Int>()
        val listener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == REQUEST_CODE) result.complete(grantResult)
        }
        Shizuku.addRequestPermissionResultListener(listener)
        try {
            Shizuku.requestPermission(REQUEST_CODE)
            return result.await() == PackageManager.PERMISSION_GRANTED
        } finally {
            Shizuku.removeRequestPermissionResultListener(listener)
        }
    }

    /** Returns an empty string on success, otherwise the error text from the user service. */
    private suspend fun bindAndGrant(): String {
        val binderDeferred = CompletableDeferred<IBinder>()
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                if (binder != null) {
                    binderDeferred.complete(binder)
                } else {
                    binderDeferred.completeExceptionally(IllegalStateException("Shizuku returned a null binder"))
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                binderDeferred.completeExceptionally(IllegalStateException("Shizuku user service disconnected"))
            }
        }
        try {
            Shizuku.bindUserService(userServiceArgs, connection)
            val binder = withTimeout(BIND_TIMEOUT_MS) { binderDeferred.await() }
            val service = IUserService.Stub.asInterface(binder)
            return withContext(Dispatchers.IO) {
                service.grant(BuildConfig.APPLICATION_ID, PERMISSION, Process.myUid() / UIDS_PER_USER)
            }
        } finally {
            try {
                Shizuku.unbindUserService(userServiceArgs, connection, true)
            } catch (_: Throwable) { }
        }
    }
}
