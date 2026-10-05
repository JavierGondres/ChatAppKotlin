package com.pucmm.chatApp.ui.conversations

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.pucmm.chatApp.R
import com.pucmm.chatApp.di.AppModule
import com.pucmm.chatApp.ui.auth.login.LoginActivity

class ConversationsActivity : AppCompatActivity() {
    private val viewModel: ConversationsViewModel by lazy {
        ConversationsViewModel(AppModule.chatRepository, AppModule.authRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)

        findViewById<Button>(R.id.buttonLogout).setOnClickListener {
            viewModel.signOut()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }
}
