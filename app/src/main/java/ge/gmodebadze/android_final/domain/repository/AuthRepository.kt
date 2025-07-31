package ge.gmodebadze.android_final.domain.repository

import ge.gmodebadze.android_final.domain.model.User

interface AuthRepository{
    suspend fun registerUser(user: User, password: String): Result<Unit>
    suspend fun loginUser(email: String, password: String): Result<Unit>
    fun getCurrentUser(): User?
    suspend fun logoutUser(): Result<Unit>
}