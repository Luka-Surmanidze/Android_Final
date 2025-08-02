package ge.gmodebadze.android_final.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import ge.gmodebadze.android_final.domain.model.User
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserSearchRepository {

    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    suspend fun getUsersWithPagination(lastUserId: String? = null, limit: Int = 20): Result<List<User>> {
        return try {
            val currentUserId = auth.currentUser?.uid

            val query: Query = if (lastUserId == null) {
                database.child("users").orderByKey().limitToFirst(limit)
            } else {
                database.child("users").orderByKey().startAfter(lastUserId).limitToFirst(limit)
            }

            val users = suspendCancellableCoroutine<List<User>> { continuation ->
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userList = mutableListOf<User>()
                        for (userSnapshot in snapshot.children) {
                            val user = userSnapshot.getValue(User::class.java)
                            // Exclude current user from the list
                            if (user != null && user.uid != currentUserId) {
                                userList.add(user)
                            }
                        }
                        continuation.resume(userList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
            }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val currentUserId = auth.currentUser?.uid

            val users = suspendCancellableCoroutine<List<User>> { continuation ->
                database.child("users")
                    .orderByChild("nickname")
                    .startAt(query)
                    .endAt(query + "\uf8ff")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userList = mutableListOf<User>()
                            for (userSnapshot in snapshot.children) {
                                val user = userSnapshot.getValue(User::class.java)
                                // Exclude current user and check if nickname contains query
                                if (user != null && user.uid != currentUserId &&
                                    user.nickname.contains(query, ignoreCase = true)) {
                                    userList.add(user)
                                }
                            }
                            continuation.resume(userList)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            continuation.resumeWithException(error.toException())
                        }
                    })
            }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}