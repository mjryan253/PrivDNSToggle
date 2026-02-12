This is a great pivot for the UI. The "Clean Filter" funnel image perfectly illustrates what the app does: taking chaotic, unsafe traffic and filtering it into a secure stream.

Here is a design proposal and the corresponding Jetpack Compose code to implement it.

### The Design Concept: "The Secure Core"

Instead of a generic settings list, we treat the app like a dashboard. The Funnel is the "Hero" image. When the app is **Active**, the funnel is bright and the "clean line" is visible. When **Inactive**, we dim the funnel to show that filtering is off.

**Visual Hierarchy:**

1. **Header:** "PrivDNS Toggle" (Your non-negotiable branding).
2. **Hero Section:** The Funnel Image centered.
* *Active:* Fully opaque, bright.
* *Inactive:* Lower opacity (0.5f) or grayscale to indicate the "filter" is off.


3. **Status Indicator:** Text below the image saying "Protection Active" (Green) or "Protection Disabled" (Grey).
4. **The Toggle:** Placed directly under the status, acting as the physical switch for the filter.
5. **Configuration Card:** A distinct section at the bottom for changing the DNS provider, separating "Daily Use" (Toggling) from "Setup" (Configuring).

### The Color Palette

Derived from your generated image:

* **Background:** `#1C1C1E` (Dark Grey/Black - matches the image background)
* **Accent/Active:** `#34C759` (The glowing green line)
* **Chaos/Error:** `#FF3B30` (The red tangled lines)
* **Surface:** `#2C2C2E` (For the input text field card)

---

### Implementation (Jetpack Compose)

You will need to import your generated image into `res/drawable` (name it `ic_funnel.png` or `ic_funnel.xml` if you vectorized it).

Here is the updated `DnsSettingsScreen` code for your `MainActivity.kt`.

```kotlin
// ... imports remain the same ...
// Add: import androidx.compose.ui.graphics.ColorFilter
// Add: import androidx.compose.ui.graphics.ColorMatrix

// Define the Palette based on the image
private val NeonGreen = Color(0xFF4CAF50) // Adjust to match image green
private val DarkBackground = Color(0xFF121212) // Matches image bg
private val SurfaceGrey = Color(0xFF252525)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DnsSettingsScreen() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // State holders
    var hostname by remember { mutableStateOf(DnsManager.getSavedHostname(context)) }
    var isActive by remember { mutableStateOf(DnsManager.isActive(context.contentResolver)) }
    var currentHost by remember { mutableStateOf(DnsManager.getCurrentHostname(context.contentResolver)) }
    var hasPermission by remember { mutableStateOf(DnsManager.hasPermission(context)) }
    var setupExpanded by remember { mutableStateOf(!hasPermission) }
    
    // Validation states
    var isValidating by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var saveSuccess by remember { mutableStateOf(false) }

    // Helper to refresh state
    fun refreshState() {
        isActive = DnsManager.isActive(context.contentResolver)
        currentHost = DnsManager.getCurrentHostname(context.contentResolver)
        hasPermission = DnsManager.hasPermission(context)
    }
    
    // Initial load
    LaunchedEffect(Unit) { refreshState() }

    Scaffold(
        containerColor = DarkBackground, // Seamless blend with image
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "PrivDNS Toggle",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // ─── HERO SECTION ──────────────────────────────────────────────
            Spacer(modifier = Modifier.height(20.dp))

            // The Funnel Image
            // Logic: If inactive, we slightly dim it to show "Power Off"
            val imageAlpha by animateFloatAsState(targetValue = if (isActive) 1f else 0.4f, label = "alpha")
            
            Image(
                painter = painterResource(id = R.drawable.ic_funnel), // REPLACE WITH YOUR IMAGE ID
                contentDescription = "Privacy Filter",
                modifier = Modifier
                    .size(220.dp) // Adjust size as needed
                    .alpha(imageAlpha)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Status Text
            Text(
                text = if (isActive) "Filtering Active" else "Protection Disabled",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isActive) NeonGreen else Color.Gray
            )
            
            if (isActive && currentHost.isNotBlank()) {
                Text(
                    text = "Connected to $currentHost",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─── TOGGLE SECTION ────────────────────────────────────────────
            
            // We use the existing LargeToggleSwitch but styled to fit the theme
            LargeToggleSwitch(
                checked = isActive,
                onToggle = { wantOn ->
                    // ... (Keep existing toggle logic) ...
                    if (wantOn) {
                        val host = DnsManager.getSavedHostname(context)
                        if (host.isBlank()) {
                            Toast.makeText(context, "Enter and save a DNS hostname first", Toast.LENGTH_SHORT).show()
                            return@LargeToggleSwitch
                        }
                        if (!DnsManager.enableDns(context.contentResolver, host)) {
                             Toast.makeText(context, "Permission denied.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        if (!DnsManager.disableDns(context.contentResolver)) {
                            Toast.makeText(context, "Permission denied.", Toast.LENGTH_LONG).show()
                        }
                    }
                    refreshState()
                },
                enabled = !isValidating,
                modifier = Modifier.width(200.dp).height(60.dp) // Slightly smaller than before to fit the image
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ─── CONFIGURATION CARD ────────────────────────────────────────
            
            // Visual separator using a Card for the "Settings" area
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceGrey),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Configuration",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Input Field
                    OutlinedTextField(
                        value = hostname,
                        onValueChange = { 
                            hostname = it
                            validationError = null
                            saveSuccess = false
                        },
                        label = { Text("DNS Hostname") },
                        placeholder = { Text("dns.adguard.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = validationError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            focusedLabelColor = NeonGreen,
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = NeonGreen,
                            errorBorderColor = MaterialTheme.colorScheme.error
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                    
                    // Error / Success Messages
                    if (validationError != null) {
                        Text(
                            text = validationError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                    if (saveSuccess) {
                        Text(
                            text = "✓ Configuration Saved",
                            color = NeonGreen,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save Button
                    Button(
                        onClick = {
                           // ... (Keep existing Save Logic) ...
                           focusManager.clearFocus()
                           validationError = null
                           saveSuccess = false
                           val host = hostname.trim()
                           val syntaxError = DnsManager.validateHostnameSyntax(host)
                           if (syntaxError != null) {
                               validationError = syntaxError
                               return@Button
                           }
                           isValidating = true
                           scope.launch {
                               try {
                                   val result = DnsManager.testConnection(host)
                                   withContext(Dispatchers.Main.immediate) {
                                       isValidating = false
                                       if (result.isSuccess) {
                                           DnsManager.saveHostname(context, host)
                                           // We only save, we don't auto-enable based on previous logic request
                                           saveSuccess = true
                                           refreshState()
                                       } else {
                                           validationError = result.exceptionOrNull()?.message ?: "Connection failed"
                                       }
                                   }
                               } catch (e: Exception) {
                                   withContext(Dispatchers.Main.immediate) {
                                       isValidating = false
                                       validationError = e.message
                                   }
                               }
                           }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !isValidating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = Color.Black // Black text on Green button is high contrast
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isValidating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Testing...")
                        } else {
                            Text("Save Configuration")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─── SETUP INSTRUCTIONS ────────────────────────────────────────
            // ... (Keep your existing Collapsible Setup Card here) ...
            // Just ensure the colors match the new theme (SurfaceGrey container)
             Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceGrey)
            ) {
                // ... content same as before ...
                // Just change text colors to Color.White or Color.Gray
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

```

### Why this design works better:

1. **Visual Feedback:** The image isn't just decoration; it dims/brightens based on the state (`isActive`), giving immediate feedback to the user.
2. **Grouping:** By putting the Input and Save button inside a separate grey card, we visually separate the "Dashboard" (Top half) from the "Settings" (Bottom half).
3. **Black OLED Theme:** Using `#121212` and `#1C1C1E` makes the glowing green elements of your new icon pop significantly more than the previous flat grey background.