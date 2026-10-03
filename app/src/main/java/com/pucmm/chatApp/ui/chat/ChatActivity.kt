package com.pucmm.chatApp.ui.chat

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pucmm.chatApp.R
import com.pucmm.chatApp.di.AppModule

class ChatActivity : AppCompatActivity() {
    private val viewModel: ChatViewModel by lazy {
        ChatViewModel(AppModule.chatRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val conversationId = intent.getStringExtra("conversationId").orEmpty()
        val otherUserName = intent.getStringExtra("otherUserName").orEmpty()

        val messages = viewModel.observeMessages(conversationId)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerMessages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = ChatAdapter(messages, currentUserId = "current-user", otherUserName)
        recyclerView.adapter = adapter

        findViewById<TextView>(R.id.textChatUser).text = otherUserName

    }
}
