package com.pucmm.chatApp.data.repository

import com.pucmm.chatApp.data.model.User
import com.pucmm.chatApp.data.remote.FirebaseAuthDataSource

class AuthRepository(
    private val firebaseAuthDataSource: FirebaseAuthDataSource
) {
    suspend fun signIn(email: String, password: String): Result<User> {
        return firebaseAuthDataSource.signIn(email, password)
    }

    suspend fun signUp(email: String, password: String, displayName: String): Result<User> {
        return firebaseAuthDataSource.signUp(email, password, displayName)
    }

    fun signOut() {
        firebaseAuthDataSource.signOut()
    }

    fun getCurrentUser(): User? {
        return firebaseAuthDataSource.getCurrentUser()
    }
}
