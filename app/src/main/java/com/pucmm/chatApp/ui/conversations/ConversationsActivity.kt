package com.pucmm.chatApp.ui.conversations

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pucmm.chatApp.R
import com.pucmm.chatApp.di.AppModule

class ConversationsActivity : AppCompatActivity() {
    private val viewModel: ConversationsViewModel by lazy {
        ConversationsViewModel(AppModule.chatRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)
    }
}
