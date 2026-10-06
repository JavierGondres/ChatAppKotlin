package com.pucmm.chatApp.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pucmm.chatApp.R
import com.pucmm.chatApp.data.util.ActiveChat
import com.pucmm.chatApp.di.AppModule
import com.pucmm.chatApp.ui.chat.ChatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FirebaseMessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch {
            AppModule.authRepository.saveFcmToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val conversationId = message.data["conversationId"].orEmpty()
        if (conversationId.isBlank() || conversationId == ActiveChat.conversationId) return

        val senderName = message.data["senderName"].orEmpty().ifBlank { "Nuevo mensaje" }
        val body = message.data["body"].orEmpty().ifBlank { "Nuevo mensaje" }
        val otherUserId = message.data["otherUserId"].orEmpty()
        showNotification(conversationId, otherUserId, senderName, body)
    }

    private fun showNotification(
        conversationId: String,
        otherUserId: String,
        senderName: String,
        body: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mensajes",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val chatIntent = Intent(this, ChatActivity::class.java).apply {
            putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversationId)
            putExtra(ChatActivity.EXTRA_OTHER_USER_ID, otherUserId)
            putExtra(ChatActivity.EXTRA_OTHER_USER_NAME, senderName)
        }
        val pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(chatIntent)
            getPendingIntent(
                conversationId.hashCode(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(senderName)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(this).notify(conversationId.hashCode(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "chat_messages"
    }
}
