package ge.gmodebadze.android_final.presentation.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ge.gmodebadze.android_final.data.repository.FirebaseAuthRepository
import ge.gmodebadze.android_final.domain.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    companion object {
        private const val TAG = "LoginViewModel"
    }

    fun login(email: String, password: String) {
        Log.d(TAG, "Attempting login with email: $email")

        _loginState.value = LoginState.Loading
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = authRepository.loginUser(email, password)
                _isLoading.value = false

                if (result.isSuccess) {
                    Log.d(TAG, "Login successful")
                    _loginState.value = LoginState.Success
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "Login failed", error)
                    _loginState.value = LoginState.Error(getErrorMessage(error))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login exception", e)
                _isLoading.value = false
                _loginState.value = LoginState.Error(getErrorMessage(e))
            }
        }
    }

    private fun getErrorMessage(error: Throwable?): String {
        return when {
            error?.message?.contains("no user record") == true ->
                "User not found"
            error?.message?.contains("invalid password") == true ->
                "Incorrect password"
            error?.message?.contains("network error") == true ->
                "Network error. Please check your connection"
            error?.message?.contains("too many requests") == true ->
                "Too many login attempts. Try again later"
            else -> "Login failed: ${error?.message ?: "Unknown error"}"
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "LoginViewModel cleared")
    }
}
