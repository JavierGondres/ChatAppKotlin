package com.pucmm.chatApp.ui.auth.register

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pucmm.chatApp.R
import com.pucmm.chatApp.di.AppModule

class RegisterActivity : AppCompatActivity() {
    private val viewModel: RegisterViewModel by lazy {
        RegisterViewModel(AppModule.authRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
    }
}
