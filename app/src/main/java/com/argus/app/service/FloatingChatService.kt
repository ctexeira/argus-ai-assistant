package com.argus.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.argus.app.MainActivity
import com.argus.app.R
import com.argus.app.ui.theme.ArgusTheme

class FloatingChatService : Service() {
    
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var chatView: View? = null
    private var isExpanded by mutableStateOf(false)
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "floating_chat_channel"
        
        fun startService(context: Context) {
            if (canDrawOverlays(context)) {
                val intent = Intent(context, FloatingChatService::class.java)
                context.startForegroundService(intent)
            } else {
                Toast.makeText(context, "Overlay permission required", Toast.LENGTH_LONG).show()
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, FloatingChatService::class.java)
            context.stopService(intent)
        }
        
        fun canDrawOverlays(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        startForeground(NOTIFICATION_ID, createNotification())
        showFloatingHead()
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        removeFloatingView()
        removeChatView()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Floating Chat Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Argus AI assistant accessible from anywhere"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Argus AI Assistant")
            .setContentText("Tap to access your AI assistant")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun showFloatingHead() {
        if (floatingView != null) return
        
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }
        
        floatingView = ComposeView(this).apply {
            setContent {
                ArgusTheme {
                    FloatingChatHead {
                        if (isExpanded) {
                            hideChatInterface()
                        } else {
                            showChatInterface()
                        }
                    }
                }
            }
        }
        
        // Make the floating head draggable
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        
        floatingView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(floatingView, layoutParams)
                    true
                }
                else -> false
            }
        }
        
        windowManager?.addView(floatingView, layoutParams)
    }
    
    private fun showChatInterface() {
        if (chatView != null) return
        
        isExpanded = true
        
        val layoutParams = WindowManager.LayoutParams(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            (resources.displayMetrics.heightPixels * 0.7).toInt(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }
        
        chatView = ComposeView(this).apply {
            setContent {
                ArgusTheme {
                    // We'll implement the floating chat interface here
                    FloatingChatInterface {
                        hideChatInterface()
                    }
                }
            }
        }
        
        windowManager?.addView(chatView, layoutParams)
    }
    
    private fun hideChatInterface() {
        isExpanded = false
        removeChatView()
    }
    
    private fun removeFloatingView() {
        floatingView?.let {
            windowManager?.removeView(it)
            floatingView = null
        }
    }
    
    private fun removeChatView() {
        chatView?.let {
            windowManager?.removeView(it)
            chatView = null
        }
    }
}

@Composable
fun FloatingChatHead(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
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

@Composable
fun FloatingChatInterface(
    onClose: () -> Unit
) {
    // Placeholder for now - we'll implement the full chat interface
    Box(
        modifier = Modifier
            .size(300.dp, 400.dp)
            .background(Color.White)
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Text("Chat Interface Coming Soon!")
    }
}