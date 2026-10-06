package com.pucmm.chatApp.ui.conversations

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pucmm.chatApp.R
import com.pucmm.chatApp.di.AppModule
import com.pucmm.chatApp.ui.auth.login.LoginActivity
import com.pucmm.chatApp.ui.chat.ChatActivity
import kotlinx.coroutines.launch

class ConversationsActivity : AppCompatActivity() {
    private val viewModel: ConversationsViewModel by viewModels {
        ConversationsViewModel.factory(AppModule.chatRepository, AppModule.authRepository)
    }

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private lateinit var conversationAdapter: ConversationAdapter
    private var shownConversations: List<ConversationUiModel> = emptyList()
    private var newChatDialog: BottomSheetDialog? = null
    private var userAdapter: UserAdapter? = null
    private var emptyUsersView: TextView? = null
    private var progressUsersView: ProgressBar? = null
    private var hasLoggedOut = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_conversations)
        requestNotificationPermissionIfNeeded()

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

        findViewById<View>(R.id.buttonLogout).setOnClickListener {
            viewModel.signOut()
        }

        findViewById<View>(R.id.buttonCreateChat).setOnClickListener {
            showNewChatDialog()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerConversations)
        recyclerView.layoutManager = LinearLayoutManager(this)
        conversationAdapter = ConversationAdapter(emptyList()) { selectedConversation ->
            openChat(selectedConversation)
        }
        recyclerView.adapter = conversationAdapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun render(state: ConversationsUiState) {
        if (state.signedOut) {
            openLogin()
            return
        }
        findViewById<TextView>(R.id.textCurrentUser).text = if (state.currentUserName.isBlank()) {
            "Sesión iniciada"
        } else {
            "Sesión iniciada como ${state.currentUserName}"
        }

        if (state.conversations != shownConversations) {
            conversationAdapter.updateConversations(state.conversations)
            shownConversations = state.conversations
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerConversations)
        val emptyLayout = findViewById<View>(R.id.layoutEmptyConversations)
        val progress = findViewById<View>(R.id.progressConversations)
        val showEmpty = !state.isLoading && state.conversations.isEmpty()
        recyclerView.visibility = if (showEmpty || state.isLoading) View.GONE else View.VISIBLE
        emptyLayout.visibility = if (showEmpty) View.VISIBLE else View.GONE
        progress.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        renderAvailableUsers(state)

        state.errorMessage?.let { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            viewModel.consumeError()
        }

        state.openedConversation?.let { conversation ->
            viewModel.consumeOpenedConversation()
            newChatDialog?.dismiss()
            openChat(conversation)
        }
    }

    private fun openLogin() {
        if (hasLoggedOut) return
        hasLoggedOut = true
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun showNewChatDialog() {
        if (newChatDialog?.isShowing == true) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_new_chat, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogView)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = UserAdapter(emptyList()) { user ->
            viewModel.createConversation(user)
        }
        recyclerView.adapter = adapter

        userAdapter = adapter
        emptyUsersView = dialogView.findViewById(R.id.textEmptyUsers)
        progressUsersView = dialogView.findViewById(R.id.progressUsers)
        renderAvailableUsers(viewModel.uiState.value)

        dialog.setOnShowListener {
            val sheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            sheet?.let { bottomSheet ->
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        dialog.setOnDismissListener {
            userAdapter = null
            emptyUsersView = null
            progressUsersView = null
            if (newChatDialog === dialog) newChatDialog = null
        }
        newChatDialog = dialog
        dialog.show()
    }

    private fun renderAvailableUsers(state: ConversationsUiState) {
        userAdapter?.updateUsers(state.availableUsers)
        val showEmpty = !state.isLoading && state.availableUsers.isEmpty()
        emptyUsersView?.visibility = if (showEmpty) View.VISIBLE else View.GONE
        progressUsersView?.visibility = if (state.isLoading) View.VISIBLE else View.GONE
    }

    private fun openChat(conversation: ConversationUiModel) {
        startActivity(Intent(this, ChatActivity::class.java).apply {
            putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversation.conversationId)
            putExtra(ChatActivity.EXTRA_OTHER_USER_ID, conversation.otherUserId)
            putExtra(ChatActivity.EXTRA_OTHER_USER_NAME, conversation.otherUserName)
        })
    }
}
