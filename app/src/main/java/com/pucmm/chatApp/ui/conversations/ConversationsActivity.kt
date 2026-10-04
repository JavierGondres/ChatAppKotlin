package com.pucmm.chatApp.ui.conversations

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pucmm.chatApp.R
import com.pucmm.chatApp.di.AppModule
import android.content.Intent
import android.view.View
import com.pucmm.chatApp.ui.chat.ChatActivity

class ConversationsActivity : AppCompatActivity() {
    private val viewModel: ConversationsViewModel by lazy {
        ConversationsViewModel(AppModule.chatRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_conversations)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.conversationsRoot)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        val emptyLayout: View = findViewById(R.id.layoutEmptyConversations)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerConversations)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val conversations = viewModel.getConversations()

        if (conversations.isEmpty()){
            recyclerView.visibility = View.GONE
            emptyLayout.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyLayout.visibility = View.GONE
        }

        val onClick: (ConversationUiModel) -> Unit = { selectedConversation -> // aqui creo el callback

            val intent = Intent(
                this,
                ChatActivity::class.java
            )

            intent.putExtra(
                "conversationId",
                selectedConversation.conversationId
            )

            intent.putExtra(
                "otherUserName",
                selectedConversation.otherUserName
            )

            startActivity(intent)
        }
        // se lo paso al adapter
        val adapter = ConversationAdapter(conversations, onClick)
        recyclerView.adapter = adapter

    }

}
