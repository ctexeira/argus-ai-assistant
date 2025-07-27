package com.argus.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.argus.app.service.FloatingChatService
import com.argus.app.ui.theme.ArgusTheme
import com.argus.app.utils.PermissionManager

class SettingsActivity : ComponentActivity() {
    
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Check if overlay permission was granted
        if (PermissionManager.hasOverlayPermission(this)) {
            // Permission granted, update UI
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ArgusTheme {
                SettingsScreen(
                    onBackPressed = { finish() },
                    onRequestOverlayPermission = {
                        PermissionManager.requestOverlayPermission(this)
                    },
                    onRequestAudioPermission = {
                        PermissionManager.requestAudioPermission(this)
                    },
                    onStartFloatingService = {
                        FloatingChatService.startService(this)
                    },
                    onStopFloatingService = {
                        FloatingChatService.stopService(this)
                    },
                    onOpenAISettings = {
                        val intent = Intent(this, AISettingsActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        PermissionManager.handlePermissionResult(
            requestCode,
            permissions,
            grantResults,
            onPermissionGranted = { permission ->
                // Handle permission granted
            },
            onPermissionDenied = { permission ->
                // Handle permission denied
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onRequestAudioPermission: () -> Unit,
    onStartFloatingService: () -> Unit,
    onStopFloatingService: () -> Unit,
    onOpenAISettings: () -> Unit
) {
    val context = LocalContext.current
    var hasOverlayPermission by remember { mutableStateOf(PermissionManager.hasOverlayPermission(context)) }
    var hasAudioPermission by remember { mutableStateOf(PermissionManager.hasAudioPermission(context)) }
    var isServiceRunning by remember { mutableStateOf(false) }
    
    // Update permission states when screen is focused
    LaunchedEffect(Unit) {
        hasOverlayPermission = PermissionManager.hasOverlayPermission(context)
        hasAudioPermission = PermissionManager.hasAudioPermission(context)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF00CED1),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permissions Section
            SettingsSection(title = "Permissions") {
                PermissionItem(
                    title = "System Overlay",
                    description = "Allow Argus to appear over other apps",
                    isGranted = hasOverlayPermission,
                    onRequestPermission = {
                        onRequestOverlayPermission()
                        hasOverlayPermission = PermissionManager.hasOverlayPermission(context)
                    }
                )
                
                PermissionItem(
                    title = "Microphone",
                    description = "Enable voice input for chat",
                    isGranted = hasAudioPermission,
                    onRequestPermission = {
                        onRequestAudioPermission()
                        hasAudioPermission = PermissionManager.hasAudioPermission(context)
                    }
                )
            }
            
            // AI Settings Section
            SettingsSection(title = "AI Configuration") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI Settings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Configure AI services and API keys",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Button(
                            onClick = onOpenAISettings,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00CED1)
                            )
                        ) {
                            Text("Configure")
                        }
                    }
                }
            }
            
            // Floating Service Section
            SettingsSection(title = "Floating Assistant") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "System-wide Access",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Access Argus from anywhere on your device",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Switch(
                                checked = isServiceRunning,
                                onCheckedChange = { enabled ->
                                    if (enabled && hasOverlayPermission) {
                                        onStartFloatingService()
                                        isServiceRunning = true
                                    } else if (!enabled) {
                                        onStopFloatingService()
                                        isServiceRunning = false
                                    }
                                },
                                enabled = hasOverlayPermission
                            )
                        }
                        
                        if (!hasOverlayPermission) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Overlay permission required to enable this feature",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            // About Section
            SettingsSection(title = "About") {
                InfoCard(
                    title = "Argus AI Assistant",
                    description = "Version 1.0\nYour intelligent chat companion"
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Granted",
                    tint = Color(0xFF4CAF50)
                )
            } else {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00CED1)
                    )
                ) {
                    Text("Grant")
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}