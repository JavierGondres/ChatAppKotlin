package com.pucmm.chatApp.ui.auth.login

import com.pucmm.chatApp.data.model.User
import com.pucmm.chatApp.data.repository.AuthRepository

class LoginViewModel(
    private val authRepository: AuthRepository
) {
    suspend fun login(email: String, password: String): Result<User> {
        return authRepository.signIn(email, password)
    }
}
