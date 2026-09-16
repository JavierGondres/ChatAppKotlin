package com.pucmm.chatApp.ui.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pucmm.chatApp.R
import com.pucmm.chatApp.di.AppModule

class ChatActivity : AppCompatActivity() {
    private val viewModel: ChatViewModel by lazy {
        ChatViewModel(AppModule.chatRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
    }
}
