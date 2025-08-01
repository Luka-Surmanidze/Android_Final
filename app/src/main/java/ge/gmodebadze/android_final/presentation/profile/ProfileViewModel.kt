import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ge.gmodebadze.android_final.data.repository.FirebaseAuthRepository
import ge.gmodebadze.android_final.domain.model.User
import ge.gmodebadze.android_final.domain.repository.AuthRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    sealed class ProfileState {
        object Idle : ProfileState()
        object Loading : ProfileState()
        data class Success(val user: User) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }

    sealed class UpdateState {
        object Idle : UpdateState()
        object Loading : UpdateState()
        object Success : UpdateState()
        data class Error(val message: String) : UpdateState()
    }

    private val _profileState = MutableLiveData<ProfileState>(ProfileState.Idle)
    val profileState: LiveData<ProfileState> = _profileState

    private val _updateState = MutableLiveData<UpdateState>(UpdateState.Idle)
    val updateState: LiveData<UpdateState> = _updateState

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    fun loadUserProfile() {
        Log.d(TAG, "Loading user profile")

        _profileState.value = ProfileState.Loading
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Cast to FirebaseAuthRepository to access getUserProfile method
                val result = (authRepository as FirebaseAuthRepository).getUserProfile()
                _isLoading.value = false

                result.fold(
                    onSuccess = { user ->
                        if (user != null) {
                            Log.d(TAG, "Profile loaded successfully")
                            _profileState.value = ProfileState.Success(user)
                        } else {
                            Log.e(TAG, "User not found")
                            _profileState.value = ProfileState.Error("User not found")
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to load profile", exception)
                        _profileState.value = ProfileState.Error(
                            getErrorMessage(exception)
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Profile loading exception", e)
                _isLoading.value = false
                _profileState.value = ProfileState.Error(getErrorMessage(e))
            }
        }
    }



    fun updateProfile(
        nickname: String,
        profession: String,
        profileImageUrl: String? = null
    ) {
        Log.d(TAG, "Updating profile for nickname: $nickname")

        _updateState.value = UpdateState.Loading

        viewModelScope.launch {
            try {
                val result = authRepository.updateUserProfile(nickname, profession, profileImageUrl)

                result.fold(
                    onSuccess = {
                        Log.d(TAG, "Profile updated successfully")
                        _updateState.value = UpdateState.Success
                        loadUserProfile() // Reload profile data
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to update profile", exception)
                        _updateState.value = UpdateState.Error(
                            getErrorMessage(exception)
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Profile update exception", e)
                _updateState.value = UpdateState.Error(getErrorMessage(e))
            }
        }
    }

    fun logout() {
        Log.d(TAG, "Logging out user")

        viewModelScope.launch {
            try {
                val result = authRepository.logoutUser()
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "Logout successful")
                        // Navigation will be handled in the Fragment
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Logout failed", exception)
                        _profileState.value = ProfileState.Error(
                            getErrorMessage(exception)
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Logout exception", e)
                _profileState.value = ProfileState.Error(getErrorMessage(e))
            }
        }
    }

    private fun getErrorMessage(error: Throwable?): String {
        return when {
            error?.message?.contains("network error") == true ->
                "Network error. Please check your connection"
            error?.message?.contains("permission denied") == true ->
                "Permission denied. Please try again"
            error?.message?.contains("user not found") == true ->
                "User not found"
            else -> error?.message ?: "Unknown error occurred"
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ProfileViewModel cleared")
    }
}