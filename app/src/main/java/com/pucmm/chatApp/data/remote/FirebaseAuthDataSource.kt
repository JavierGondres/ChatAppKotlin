package com.pucmm.chatApp.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.pucmm.chatApp.data.model.User
import com.pucmm.chatApp.data.util.Constants
import kotlinx.coroutines.tasks.await

class FirebaseAuthDataSource(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun signIn(email: String, password: String): Result<User> {
        return runCatching {
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(email.trim(), password)
                .await()
            authResult.user?.toAppUser()
                ?: error("No se pudo iniciar sesión")
        }.withAuthMessage()
    }

    suspend fun signUp(email: String, password: String, displayName: String): Result<User> {
        val authResult = try {
            firebaseAuth
                .createUserWithEmailAndPassword(email.trim(), password)
                .await()
        } catch (error: Exception) {
            return Result.failure(Exception(error.authMessage(), error))
        }

        val firebaseUser = authResult.user
            ?: return Result.failure(Exception("No se pudo crear la cuenta"))

        val name = displayName.trim()
        val profile = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        return try {
            firebaseUser.updateProfile(profile).await()
            val user = firebaseUser.toAppUser(name)
            firestore.collection(Constants.USERS_COLLECTION)
                .document(user.uid)
                .set(user.toFirestoreMap())
                .await()
            Result.success(user)
        } catch (error: Exception) {
            Result.failure(
                Exception(
                    "La cuenta se creó, pero no se pudo guardar el perfil. Intenta iniciar sesión.",
                    error
                )
            )
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.toAppUser()
    }

    private fun FirebaseUser.toAppUser(name: String = displayName.orEmpty()): User {
        return User(
            uid = uid,
            email = email.orEmpty(),
            displayName = name,
            photoUrl = photoUrl?.toString()
        )
    }

    private fun User.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "photoUrl" to photoUrl,
            "createdAt" to createdAt
        )
    }

    private fun <T> Result<T>.withAuthMessage(): Result<T> {
        return fold(
            onSuccess = { Result.success(it) },
            onFailure = { error -> Result.failure(Exception(error.authMessage(), error)) }
        )
    }

    private fun Throwable.authMessage(): String {
        val code = (this as? FirebaseAuthException)?.errorCode
            ?: (cause as? FirebaseAuthException)?.errorCode

        return when (code) {
            "ERROR_INVALID_EMAIL" -> "Ingresa un correo válido"
            "ERROR_WRONG_PASSWORD",
            "ERROR_INVALID_CREDENTIAL",
            "ERROR_INVALID_LOGIN_CREDENTIALS" -> "Correo o contraseña incorrectos"
            "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con ese correo"
            "ERROR_USER_DISABLED" -> "Esta cuenta está deshabilitada"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Ese correo ya está registrado"
            "ERROR_WEAK_PASSWORD" -> "La contraseña es demasiado débil"
            "ERROR_NETWORK_REQUEST_FAILED" -> "Revisa tu conexión a internet"
            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Espera un momento e inténtalo de nuevo"
            "ERROR_OPERATION_NOT_ALLOWED" -> "El registro con correo y contraseña no está habilitado"
            else -> "No se pudo completar la operación. Inténtalo de nuevo"
        }
    }
}
