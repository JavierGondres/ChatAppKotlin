package com.pucmm.chatApp.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pucmm.chatApp.data.repository.AuthRepository
import com.pucmm.chatApp.ui.auth.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val authError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(name: String, email: String, password: String) {
        if (_uiState.value.isLoading) return

        val nameError = AuthValidator.nameError(name)
        val emailError = AuthValidator.emailError(email)
        val passwordError = AuthValidator.passwordError(password)
        if (nameError != null || emailError != null || passwordError != null) {
            _uiState.value = RegisterUiState(
                nameError = nameError,
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            val result = authRepository.signUp(email.trim(), password, name.trim())
            _uiState.value = result.fold(
                onSuccess = { RegisterUiState(isSuccess = true) },
                onFailure = { error ->
                    RegisterUiState(
                        authError = error.message
                            ?: "No se pudo completar la operación. Inténtalo de nuevo"
                    )
                }
            )
        }
    }

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RegisterViewModel(authRepository) as T
                }
            }
        }
    }
}
