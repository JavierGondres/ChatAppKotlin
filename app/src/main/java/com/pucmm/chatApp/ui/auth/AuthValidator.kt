package com.pucmm.chatApp.ui.auth

import android.util.Patterns

object AuthValidator {
    fun nameError(name: String): String? {
        if (name.trim().isEmpty()) return "Ingresa tu nombre"
        return null
    }

    fun emailError(email: String): String? {
        val value = email.trim()
        if (value.isEmpty()) return "Ingresa tu correo"
        if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) return "Ingresa un correo válido"
        return null
    }

    fun passwordError(password: String): String? {
        if (password.isEmpty()) return "Ingresa tu contraseña"
        if (password.length < 6) return "La contraseña debe tener al menos 6 caracteres"
        return null
    }
}
