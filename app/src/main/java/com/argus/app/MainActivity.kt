package com.argus.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.argus.app.data.ChatMessage
import com.argus.app.ui.components.ChatInput
import com.argus.app.ui.components.MessageBubble
import com.argus.app.ui.components.TypingIndicator
import com.argus.app.service.FloatingChatService
import com.argus.app.ui.SettingsActivity
import com.argus.app.ui.theme.ArgusTheme
import com.argus.app.utils.PermissionManager
import com.argus.app.utils.ApiKeyManager
import com.argus.app.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure Gemini service is selected if available
        ApiKeyManager(this).ensureGeminiServiceIfAvailable()
        setContent {
            ArgusTheme {
                MainScreen(
                    onOpenSettings = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onStartFloatingService = {
                        if (PermissionManager.hasOverlayPermission(this)) {
                            FloatingChatService.startService(this)
                        } else {
                            startActivity(Intent(this, SettingsActivity::class.java))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    onOpenSettings: () -> Unit,
    onStartFloatingService: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        FloatingChatAssistant()
        
        // Top bar with settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Argus AI Assistant",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF00CED1)
            )
            
            Row {
                 IconButton(onClick = onStartFloatingService) {
                     Icon(
                         imageVector = Icons.Default.Launch,
                         contentDescription = "Enable Floating Mode",
                         tint = Color(0xFF00CED1)
                     )
                 }
                 
                 IconButton(onClick = onOpenSettings) {
                     Icon(
                         imageVector = Icons.Default.Settings,
                         contentDescription = "Settings",
                         tint = Color(0xFF00CED1)
                     )
                 }
             }
        }
    }
}

@Composable
fun FloatingChatAssistant() {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val chatViewModel: ChatViewModel = viewModel { ChatViewModel(context) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (expanded) {
            ChatInterface(
                viewModel = chatViewModel,
                onClose = { expanded = false }
            )
        } else {
            FloatingChatHead(
                onClick = { expanded = true }
            )
        }
    }
}

@Composable
fun FloatingChatHead(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Color(0xFF00CED1))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "AI",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInterface(
    viewModel: ChatViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages = viewModel.messages
    val currentMessage by viewModel.currentMessage
    val isAiTyping by viewModel.isAiTyping
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isAiTyping) {
        if (messages.isNotEmpty() || isAiTyping) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size)
            }
        }
    }
    
    Surface(
        modifier = modifier.size(340.dp, 500.dp),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Surface(
                color = Color(0xFF00CED1),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Argus AI Assistant",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    IconButton(
                        onClick = onClose
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close chat",
                            tint = Color.White
                        )
                    }
                }
            }
            
            // Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(message = message)
                }
                
                // Show streaming response if active
                if (viewModel.isStreaming.value && viewModel.streamingResponse.value.isNotEmpty()) {
                    item {
                        MessageBubble(
                            message = ChatMessage(
                                content = "",
                                isFromUser = false
                            ),
                            isStreaming = true,
                            streamingContent = viewModel.streamingResponse.value
                        )
                    }
                }
                
                if (isAiTyping && !viewModel.isStreaming.value) {
                    item {
                        TypingIndicator()
                    }
                }
            }
            
            // Input
            ChatInput(
                message = currentMessage,
                onMessageChange = viewModel::updateCurrentMessage,
                onSendMessage = viewModel::sendMessage,
                enabled = !isAiTyping,
                isStreaming = viewModel.isStreaming.value,
                onStopStreaming = viewModel::stopStreaming,
                quickSuggestions = if (viewModel.messages.isEmpty() || (viewModel.messages.size == 1 && !viewModel.messages.first().isFromUser)) {
                    viewModel.getQuickSuggestions()
                } else {
                    emptyList()
                },
                onQuickSuggestionClick = viewModel::sendQuickMessage
            )
        }
    }
}