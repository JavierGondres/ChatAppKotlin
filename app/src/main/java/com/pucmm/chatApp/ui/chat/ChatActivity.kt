package com.pucmm.chatApp.ui.chat

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.button.MaterialButton
import com.pucmm.chatApp.R
import com.pucmm.chatApp.data.model.Message
import com.pucmm.chatApp.di.AppModule
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private val otherUserName by lazy {
        intent.getStringExtra(EXTRA_OTHER_USER_NAME).orEmpty()
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.setPendingImage(uri)
    }

    private val viewModel: ChatViewModel by viewModels {
        ChatViewModel.factory(
            AppModule.chatRepository,
            AppModule.authRepository,
            AppModule.storageRepository,
            intent.getStringExtra(EXTRA_CONVERSATION_ID).orEmpty(),
            intent.getStringExtra(EXTRA_OTHER_USER_ID).orEmpty()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_chat)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chatRoot)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        findViewById<View>(R.id.buttonBack).setOnClickListener { finish() }

        findViewById<TextView>(R.id.textChatUser).text = otherUserName.ifBlank { "Usuario" }
        findViewById<TextView>(R.id.textChatAvatar).text = otherUserName.initials()

        val editTextMessage = findViewById<EditText>(R.id.editTextMessage)
        val buttonSend = findViewById<MaterialButton>(R.id.buttonSend)
        val buttonAttach = findViewById<View>(R.id.buttonAttach)
        val previewLayout = findViewById<View>(R.id.layoutImagePreview)
        val imagePreview = findViewById<ImageView>(R.id.imagePreview)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerMessages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = ChatAdapter(emptyList(), viewModel.currentUserId, otherUserName)
        recyclerView.adapter = adapter

        buttonAttach.setOnClickListener {
            pickImage.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        findViewById<View>(R.id.buttonRemoveImage).setOnClickListener {
            viewModel.clearPendingImage()
        }
        buttonSend.setOnClickListener {
            viewModel.sendMessage(editTextMessage.text.toString())
        }

        val progress = findViewById<View>(R.id.progressMessages)
        val emptyChat = findViewById<View>(R.id.textEmptyChat)
        var shownMessages: List<Message> = emptyList()
        var shownPreview: Uri? = null
        var wasSending = false

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    val showEmpty = !state.isLoading && state.messages.isEmpty()
                    emptyChat.visibility = if (showEmpty) View.VISIBLE else View.GONE
                    recyclerView.visibility = if (state.messages.isEmpty()) View.GONE else View.VISIBLE
                    if (state.messages != shownMessages) {
                        val shouldScroll = state.messages.size != shownMessages.size
                        adapter.updateMessages(state.messages)
                        if (shouldScroll && state.messages.isNotEmpty()) {
                            recyclerView.post {
                                recyclerView.scrollToPosition(state.messages.lastIndex)
                            }
                        }
                        shownMessages = state.messages
                    }

                    if (state.pendingImageUri != shownPreview) {
                        shownPreview = state.pendingImageUri
                        if (state.pendingImageUri == null) {
                            previewLayout.visibility = View.GONE
                            imagePreview.setImageDrawable(null)
                        } else {
                            previewLayout.visibility = View.VISIBLE
                            imagePreview.load(state.pendingImageUri)
                        }
                    }

                    val finishedSending = wasSending && !state.isSending &&
                        state.errorMessage == null &&
                        state.pendingImageUri == null
                    wasSending = state.isSending
                    if (finishedSending) {
                        editTextMessage.text.clear()
                    }
                    buttonSend.isEnabled = !state.isSending
                    buttonAttach.isEnabled = !state.isSending
                    buttonSend.text = if (state.isSending) "Enviando..." else "Enviar"

                    state.errorMessage?.let { message ->
                        Toast.makeText(this@ChatActivity, message, Toast.LENGTH_SHORT).show()
                        viewModel.consumeError()
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_CONVERSATION_ID = "conversationId"
        const val EXTRA_OTHER_USER_ID = "otherUserId"
        const val EXTRA_OTHER_USER_NAME = "otherUserName"
    }
}

private fun String.initials(): String {
    val parts = trim().split(" ").filter { part -> part.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}
