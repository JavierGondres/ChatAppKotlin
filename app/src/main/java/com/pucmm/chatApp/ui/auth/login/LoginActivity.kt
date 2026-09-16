package com.pucmm.chatApp.ui.auth.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pucmm.chatApp.R
import com.pucmm.chatApp.di.AppModule

class LoginActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by lazy {
        LoginViewModel(AppModule.authRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }
}
