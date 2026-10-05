package com.pucmm.chatApp.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pucmm.chatApp.R
import com.pucmm.chatApp.di.AppModule
import com.pucmm.chatApp.ui.auth.register.RegisterActivity
import com.pucmm.chatApp.ui.conversations.ConversationsActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.factory(AppModule.authRepository)
    }

    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (viewModel.hasSession()) {
            openConversations()
            return
        }

        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.editTextEmail)
        val passwordInput = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val errorText = findViewById<TextView>(R.id.textError)

        loginButton.setOnClickListener {
            viewModel.login(
                email = emailInput.text.toString(),
                password = passwordInput.text.toString()
            )
        }

        findViewById<TextView>(R.id.textRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    emailInput.error = state.emailError
                    passwordInput.error = state.passwordError
                    errorText.text = state.authError
                    errorText.visibility = if (state.authError.isNullOrBlank()) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                    loginButton.isEnabled = !state.isLoading
                    loginButton.text = if (state.isLoading) "Entrando..." else "Entrar"
                    if (state.isSuccess) openConversations()
                }
            }
        }
    }

    private fun openConversations() {
        if (hasNavigated) return
        hasNavigated = true
        startActivity(Intent(this, ConversationsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
