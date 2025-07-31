package ge.gmodebadze.android_final.presentation.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ge.gmodebadze.android_final.data.repository.FirebaseAuthRepository
import ge.gmodebadze.android_final.domain.model.User
import ge.gmodebadze.android_final.domain.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    sealed class RegistrationState {
        object Idle : RegistrationState()
        object Loading : RegistrationState()
        object Success : RegistrationState()
        data class Error(val message: String) : RegistrationState()
    }

    private val _registrationState = MutableLiveData<RegistrationState>(RegistrationState.Idle)
    val registrationState: LiveData<RegistrationState> = _registrationState

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    companion object {
        private const val TAG = "RegisterViewModel"
    }

    fun registerUser(nickname: String, password: String, profession: String) {
        Log.d(TAG, "Starting registration for nickname: $nickname")

        _registrationState.value = RegistrationState.Loading
        _isLoading.value = true

        val user = User(
            uid = "",
            nickname = nickname,
            profession = profession,
            email = ""
        )

        viewModelScope.launch {
            try {
                val result = authRepository.registerUser(user, password)

                _isLoading.value = false

                if (result.isSuccess) {
                    Log.d(TAG, "Registration successful")
                    _registrationState.value = RegistrationState.Success
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "Registration failed", error)
                    _registrationState.value = RegistrationState.Error(
                        getErrorMessage(error)
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Registration exception", e)
                _isLoading.value = false
                _registrationState.value = RegistrationState.Error(
                    getErrorMessage(e)
                )
            }
        }
    }

    private fun getErrorMessage(error: Throwable?): String {
        return when {
            error?.message?.contains("email address is already in use") == true ->
                "Nickname already exists"
            error?.message?.contains("weak password") == true ->
                "Password is too weak. Try a stronger password"
            error?.message?.contains("network error") == true ->
                "Network error. Please check your connection"
            error?.message?.contains("too many requests") == true ->
                "Too many attempts. Please try again later"
            else -> "Registration failed: ${error?.message ?: "Unknown error"}"
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared")
    }
}