package ge.gmodebadze.android_final

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterViewModel : ViewModel() {

    private val _registrationResult = MutableLiveData<String>()
    val registrationResult: LiveData<String> = _registrationResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    companion object {
        private const val TAG = "RegisterViewModel"
    }

    fun registerUser(nickname: String, password: String, profession: String) {
        Log.d(TAG, "Starting registration for nickname: $nickname")
        _isLoading.value = true

        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance().reference

        val email = "${nickname}@messenger.app"

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                Log.d(TAG, "Firebase Auth successful")

                val user = authResult.user
                if (user != null) {
                    Log.d(TAG, "User UID: ${user.uid}")

                    val userData = mapOf(
                        "uid" to user.uid,
                        "nickname" to nickname,
                        "profession" to profession,
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )

                    Log.d(TAG, "Saving user data to: users/${user.uid}")

                    database.child("users").child(user.uid).setValue(userData)
                        .addOnSuccessListener {
                            Log.d(TAG, "User data saved successfully")
                            _isLoading.value = false
                            _registrationResult.value = "Registration was successful!"
                        }
                        .addOnFailureListener { error ->
                            Log.e(TAG, "Failed to save user data", error)
                            _isLoading.value = false
                            _registrationResult.value = "Registration Failed: ${error.message}"
                        }
                } else {
                    Log.e(TAG, "User is null after successful auth")
                    _isLoading.value = false
                    _registrationResult.value = "Registration Failed: User is null"
                }
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Firebase Auth failed", error)
                _isLoading.value = false

                val errorMessage = when {
                    error.message?.contains("email address is already in use") == true ->
                        "Nickname already in use"
                    error.message?.contains("weak password") == true ->
                        "try stronger password"
                    error.message?.contains("network error") == true ->
                        "network error"
                    else -> "Registration Failed: ${error.message}"
                }

                _registrationResult.value = " $errorMessage"
            }
    }
}