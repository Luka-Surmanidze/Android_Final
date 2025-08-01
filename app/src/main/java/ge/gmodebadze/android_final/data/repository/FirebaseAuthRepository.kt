package ge.gmodebadze.android_final.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ge.gmodebadze.android_final.domain.model.User
import ge.gmodebadze.android_final.domain.repository.AuthRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
            profileImageUrl = "",
            createdAt = System.currentTimeMillis()
        )
    }

    suspend fun getUserProfile(): Result<User?> {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                return Result.success(null)
            }

            // Fetch complete user data from Firebase Database
            val userData = suspendCancellableCoroutine<User?> { continuation ->
                database.child("users").child(firebaseUser.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val user = snapshot.getValue(User::class.java)
                            continuation.resume(user)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            continuation.resumeWithException(error.toException())
                        }
                    })
            }

            Result.success(userData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(
        nickname: String,
        profession: String,
        profileImageUrl: String?
    ): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            // Generate email from nickname
            val newEmail = "$nickname@messenger.app"

            // Update email in Firebase Auth if it's different from current email
            if (currentUser.email != newEmail) {
                currentUser.updateEmail(newEmail).await()
            }

            // Update user data in Realtime Database
            val updates = mutableMapOf<String, Any>(
                "nickname" to nickname,
                "profession" to profession,
                "email" to newEmail  // Use the generated email
            )

            profileImageUrl?.let { url ->
                updates["profileImageUrl"] = url
            }

            database.child("users").child(currentUser.uid)
                .updateChildren(updates).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logoutUser(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}