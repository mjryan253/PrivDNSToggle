package com.privdnstoggle.app

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrivDnsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DnsSettingsScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivDnsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = dynamicColorScheme(),
        typography = Typography(),
        content = content
    )
}

@Composable
fun dynamicColorScheme(): ColorScheme {
    val context = LocalContext.current
    return if (android.os.Build.VERSION.SDK_INT >= 31) {
        dynamicLightColorScheme(context)
    } else {
        lightColorScheme()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DnsSettingsScreen() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var hostname by remember { mutableStateOf(DnsManager.getSavedHostname(context)) }
    var isActive by remember { mutableStateOf(DnsManager.isActive(context.contentResolver)) }
    var currentHost by remember { mutableStateOf(DnsManager.getCurrentHostname(context.contentResolver)) }
    var hasPermission by remember { mutableStateOf(DnsManager.hasPermission(context)) }
    var setupExpanded by remember { mutableStateOf(!hasPermission) }

    // Refresh state when returning to the screen
    LaunchedEffect(Unit) {
        isActive = DnsManager.isActive(context.contentResolver)
        currentHost = DnsManager.getCurrentHostname(context.contentResolver)
        hasPermission = DnsManager.hasPermission(context)
        setupExpanded = !hasPermission
    }

    fun refreshState() {
        isActive = DnsManager.isActive(context.contentResolver)
        currentHost = DnsManager.getCurrentHostname(context.contentResolver)
        hasPermission = DnsManager.hasPermission(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PrivDNS Toggle") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Status Card ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (isActive)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column {
                        Text(
                            text = if (isActive) "Active" else "Inactive",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isActive)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isActive && currentHost.isNotBlank()) {
                            Text(
                                text = currentHost,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isActive)
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = isActive,
                        onCheckedChange = {
                            val success = if (it) {
                                val host = hostname.trim()
                                if (host.isBlank()) {
                                    Toast.makeText(context, "Enter a DNS hostname first", Toast.LENGTH_SHORT).show()
                                    false
                                } else {
                                    DnsManager.saveHostname(context, host)
                                    DnsManager.enableDns(context.contentResolver, host)
                                }
                            } else {
                                DnsManager.disableDns(context.contentResolver)
                            }
                            if (!success) {
                                Toast.makeText(
                                    context,
                                    "Permission denied. See setup instructions below.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            refreshState()
                        }
                    )
                }
            }

            // --- Hostname Input ---
            OutlinedTextField(
                value = hostname,
                onValueChange = { hostname = it },
                label = { Text("DNS Hostname or IP") },
                placeholder = { Text("e.g. dns.adguard.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    focusManager.clearFocus()
                    val host = hostname.trim()
                    if (host.isBlank()) {
                        Toast.makeText(context, "Hostname cannot be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    DnsManager.saveHostname(context, host)
                    val success = DnsManager.enableDns(context.contentResolver, host)
                    if (success) {
                        Toast.makeText(context, "DNS set to $host", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Permission denied. See setup instructions below.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    refreshState()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save & Apply")
            }

            // --- Setup Instructions ---
            val adbCommand = "adb shell pm grant com.privdnstoggle.app android.permission.WRITE_SECURE_SETTINGS"

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column {
                    TextButton(
                        onClick = { setupExpanded = !setupExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (setupExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Setup Instructions",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (!hasPermission) {
                            Text(
                                text = "Required",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    AnimatedVisibility(visible = setupExpanded) {
                        Column(
                            modifier = Modifier.padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Connect your phone via USB with USB debugging enabled, then run:",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Surface(
                                color = MaterialTheme.colorScheme.inverseSurface,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = adbCommand,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = MaterialTheme.colorScheme.inverseOnSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(ClipboardManager::class.java)
                                            clipboard.setPrimaryClip(
                                                ClipData.newPlainText("ADB command", adbCommand)
                                            )
                                            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = MaterialTheme.colorScheme.inverseOnSurface,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "This grants the app permission to change Private DNS settings. " +
                                        "Only needed once — persists across app updates.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
