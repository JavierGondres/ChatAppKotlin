package com.pucmm.chatApp.ui.auth.register

import com.pucmm.chatApp.data.model.User
import com.pucmm.chatApp.data.repository.AuthRepository

class RegisterViewModel(
    private val authRepository: AuthRepository
) {
    suspend fun register(email: String, password: String, displayName: String): Result<User> {
        return authRepository.signUp(email, password, displayName)
    }
}
