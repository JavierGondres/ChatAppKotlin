package com.pucmm.chatApp.ui.auth.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pucmm.chatApp.R
import com.pucmm.chatApp.di.AppModule
import com.pucmm.chatApp.ui.conversations.ConversationsActivity
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private val viewModel: RegisterViewModel by viewModels {
        RegisterViewModel.factory(AppModule.authRepository)
    }

    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerRoot)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        val nameInput = findViewById<EditText>(R.id.editTextName)
        val emailInput = findViewById<EditText>(R.id.editTextEmail)
        val passwordInput = findViewById<EditText>(R.id.editTextPassword)
        val registerButton = findViewById<Button>(R.id.buttonRegister)
        val errorText = findViewById<TextView>(R.id.textError)

        registerButton.setOnClickListener {
            viewModel.register(
                name = nameInput.text.toString(),
                email = emailInput.text.toString(),
                password = passwordInput.text.toString()
            )
        }

        findViewById<TextView>(R.id.textLogin).setOnClickListener {
            finish()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    nameInput.error = state.nameError
                    emailInput.error = state.emailError
                    passwordInput.error = state.passwordError
                    errorText.text = state.authError
                    errorText.visibility = if (state.authError.isNullOrBlank()) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                    registerButton.isEnabled = !state.isLoading
                    registerButton.text = if (state.isLoading) "Creando cuenta..." else "Registrarse"
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
