package com.pucmm.chatApp.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pucmm.chatApp.data.repository.AuthRepository
import com.pucmm.chatApp.ui.auth.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val emailError: String? = null,
    val passwordError: String? = null,
    val authError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun hasSession(): Boolean = authRepository.getCurrentUser() != null

    fun login(email: String, password: String) {
        if (_uiState.value.isLoading) return

        val emailError = AuthValidator.emailError(email)
        val passwordError = AuthValidator.passwordError(password)
        if (emailError != null || passwordError != null) {
            _uiState.value = LoginUiState(
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            val result = authRepository.signIn(email.trim(), password)
            _uiState.value = result.fold(
                onSuccess = { LoginUiState(isSuccess = true) },
                onFailure = { error ->
                    LoginUiState(
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
                    return LoginViewModel(authRepository) as T
                }
            }
        }
    }
}
