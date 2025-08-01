package ge.gmodebadze.android_final.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import ge.gmodebadze.android_final.domain.model.User
import ge.gmodebadze.android_final.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository : AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    override suspend fun registerUser(user: User, password: String): Result<Unit> {
        return try{
            val email = "${user.nickname}@messenger.app"
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("User ID is null"))

            val userData = user.copy(uid = uid, email = email)
            database.child("users").child(uid).setValue(userData).await()
            Result.success(Unit)

        } catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null

        return User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            nickname = firebaseUser.email?.substringBefore("@") ?: "",
            profession = "",
            createdAt = System.currentTimeMillis()
        )
    }

    override suspend fun logoutUser(): Result<Unit> {
        return Result.failure(Exception("Not yet implemented"))
    }

}