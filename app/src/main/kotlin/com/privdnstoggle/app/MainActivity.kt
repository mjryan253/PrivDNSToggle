package com.privdnstoggle.app

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

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
        dynamicDarkColorScheme(context)
    } else {
        darkColorScheme()
    }
}

// ── Large Toggle Switch ──────────────────────────────────────────────────────

private val TrackWidth = 280.dp
private val TrackHeight = 72.dp
private val ThumbSize = 60.dp
private val ThumbPadding = 6.dp

private val ColorOff = Color(0xFF3A3A3C)
private val ColorOn = Color(0xFF34C759)
private val ThumbColor = Color.White

@Composable
fun LargeToggleSwitch(
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) ColorOn else ColorOff,
        animationSpec = tween(durationMillis = 300),
        label = "trackColor"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) TrackWidth - ThumbSize - ThumbPadding * 2 else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "thumbOffset"
    )

    Box(
        modifier = modifier
            .width(TrackWidth)
            .height(TrackHeight)
            .clip(RoundedCornerShape(TrackHeight / 2))
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled
            ) { onToggle(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        // OFF / ON labels inside the track
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ThumbPadding + ThumbSize / 2),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "OFF",
                color = Color.White.copy(alpha = if (!checked) 0f else 0.7f),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "ON",
                color = Color.White.copy(alpha = if (checked) 0f else 0.5f),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }

        // Sliding thumb
        Box(
            modifier = Modifier
                .padding(ThumbPadding)
                .offset(x = thumbOffset)
                .size(ThumbSize)
                .clip(CircleShape)
                .background(ThumbColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (checked) "ON" else "OFF",
                color = if (checked) ColorOn else ColorOff,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }
    }
}

// ── Main Screen ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DnsSettingsScreen() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var hostname by remember { mutableStateOf(DnsManager.getSavedHostname(context)) }
    var isActive by remember { mutableStateOf(DnsManager.isActive(context.contentResolver)) }
    var currentHost by remember { mutableStateOf(DnsManager.getCurrentHostname(context.contentResolver)) }
    var hasPermission by remember { mutableStateOf(DnsManager.hasPermission(context)) }
    var setupExpanded by remember { mutableStateOf(!hasPermission) }

    // Validation / connection-test state
    var isValidating by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var saveSuccess by remember { mutableStateOf(false) }

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
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Large Toggle Switch ──────────────────────────────────────

            Text(
                text = if (isActive) "Private DNS Active" else "Private DNS Off",
                style = MaterialTheme.typography.headlineSmall,
                color = if (isActive)
                    ColorOn
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isActive && currentHost.isNotBlank()) {
                Text(
                    text = currentHost,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            LargeToggleSwitch(
                checked = isActive,
                onToggle = { wantOn ->
                    if (wantOn) {
                        val host = DnsManager.getSavedHostname(context)
                        if (host.isBlank()) {
                            Toast.makeText(
                                context,
                                "Enter and save a DNS hostname first",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@LargeToggleSwitch
                        }
                        val success = DnsManager.enableDns(context.contentResolver, host)
                        if (!success) {
                            Toast.makeText(
                                context,
                                "Permission denied. See setup instructions below.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        val success = DnsManager.disableDns(context.contentResolver)
                        if (!success) {
                            Toast.makeText(
                                context,
                                "Permission denied. See setup instructions below.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    refreshState()
                },
                enabled = !isValidating
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── DNS Hostname / IP Input ──────────────────────────────────

            Text(
                text = "DNS Provider",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = hostname,
                onValueChange = {
                    hostname = it
                    validationError = null
                    saveSuccess = false
                },
                label = { Text("DNS Hostname or IP") },
                placeholder = { Text("e.g. dns.nextdns.io") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = validationError != null,
                enabled = !isValidating,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                shape = RoundedCornerShape(12.dp),
                supportingText = {
                    when {
                        validationError != null -> {
                            Text(
                                text = validationError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        saveSuccess -> {
                            Text(
                                text = "Saved and connected successfully",
                                color = ColorOn
                            )
                        }
                    }
                }
            )

            // ── Save Button with loading state ──────────────────────────

            Button(
                onClick = {
                    focusManager.clearFocus()
                    validationError = null
                    saveSuccess = false

                    val host = hostname.trim()

                    // Step 1: syntax validation
                    val syntaxError = DnsManager.validateHostnameSyntax(host)
                    if (syntaxError != null) {
                        validationError = syntaxError
                        return@Button
                    }

                    // Step 2: connection test
                    isValidating = true
                    scope.launch {
                        val result = DnsManager.testConnection(host)
                        isValidating = false

                        if (result.isSuccess) {
                            // Save and apply
                            DnsManager.saveHostname(context, host)
                            val applied = DnsManager.enableDns(context.contentResolver, host)
                            if (applied) {
                                saveSuccess = true
                                Toast.makeText(
                                    context,
                                    "DNS saved and set to $host",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                validationError =
                                    "Permission denied. Grant WRITE_SECURE_SETTINGS via ADB."
                            }
                            refreshState()
                        } else {
                            validationError =
                                result.exceptionOrNull()?.message ?: "Connection failed"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isValidating,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isValidating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Testing connection...")
                } else {
                    Text("Save", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Setup Instructions (collapsible) ─────────────────────────

            val adbCommand =
                "adb shell pm grant com.privdnstoggle.app android.permission.WRITE_SECURE_SETTINGS"

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
                            imageVector = if (setupExpanded)
                                Icons.Default.ExpandLess
                            else
                                Icons.Default.ExpandMore,
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
                                            val clipboard =
                                                context.getSystemService(ClipboardManager::class.java)
                                            clipboard.setPrimaryClip(
                                                ClipData.newPlainText(
                                                    "ADB command",
                                                    adbCommand
                                                )
                                            )
                                            Toast.makeText(
                                                context,
                                                "Copied!",
                                                Toast.LENGTH_SHORT
                                            ).show()
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
                                        "Only needed once \u2014 persists across app updates.",
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
