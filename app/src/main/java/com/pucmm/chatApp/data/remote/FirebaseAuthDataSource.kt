package com.pucmm.chatApp.data.remote

import com.pucmm.chatApp.data.model.User

class FirebaseAuthDataSource {
    suspend fun signIn(email: String, password: String): Result<User> {
        return runCatching {
            // TODO: reemplazar por FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            User(
                uid = "demo_uid",
                email = email,
                displayName = "Usuario demo"
            )
        }
    }

    suspend fun signUp(email: String, password: String, displayName: String): Result<User> {
        return runCatching {
            // TODO: reemplazar por FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            User(
                uid = "new_uid",
                email = email,
                displayName = displayName
            )
        }
    }

    fun signOut() {
        // TODO: FirebaseAuth.getInstance().signOut()
    }

    fun getCurrentUser(): User? {
        // TODO: obtener usuario actual desde FirebaseAuth
        return null
    }
}
