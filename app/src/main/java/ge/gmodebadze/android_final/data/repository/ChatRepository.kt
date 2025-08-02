package ge.gmodebadze.android_final.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import ge.gmodebadze.android_final.domain.model.Chat
import ge.gmodebadze.android_final.domain.model.Message
import ge.gmodebadze.android_final.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ChatRepository {

    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    suspend fun createOrGetChat(participantId: String): Result<String> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            // Generate chat ID based on user IDs (smaller ID first for consistency)
            val chatId = if (currentUserId < participantId) {
                "${currentUserId}_$participantId"
            } else {
                "${participantId}_$currentUserId"
            }

            val chatSnapshot = database.child("chats").child(chatId).get().await()

            if (!chatSnapshot.exists()) {
                val currentUserSnapshot = database.child("users").child(currentUserId).get().await()
                val participantSnapshot = database.child("users").child(participantId).get().await()

                val currentUser = currentUserSnapshot.getValue(User::class.java)
                val participant = participantSnapshot.getValue(User::class.java)

                if (currentUser == null || participant == null) {
                    return Result.failure(Exception("Failed to get user information"))
                }

                val chat = Chat(
                    id = chatId,
                    participants = listOf(currentUserId, participantId),
                    participantNames = mapOf(
                        currentUserId to currentUser.nickname,
                        participantId to participant.nickname
                    ),
                    participantImages = mapOf(
                        currentUserId to currentUser.profileImageUrl,
                        participantId to participant.profileImageUrl
                    ),
                    lastMessage = "",
                    lastMessageTime = System.currentTimeMillis(),
                    lastMessageSenderId = "",
                    isGroupChat = false,
                    createdAt = System.currentTimeMillis()
                )

                database.child("chats").child(chatId).setValue(chat).await()
            }

            Result.success(chatId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(chatId: String, messageText: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            // Get current user data
            val userSnapshot = database.child("users").child(currentUser.uid).get().await()
            val userData = userSnapshot.getValue(User::class.java)
                ?: return Result.failure(Exception("User data not found"))

            val messageId = database.child("messages").child(chatId).push().key
                ?: return Result.failure(Exception("Failed to generate message ID"))

            val message = Message(
                id = messageId,
                text = messageText,
                senderId = currentUser.uid,
                senderNickname = userData.nickname,
                senderProfileImageUrl = userData.profileImageUrl,
                timestamp = System.currentTimeMillis(),
                chatId = chatId
            )

            database.child("messages").child(chatId).child(messageId).setValue(message).await()

            val chatUpdates = mapOf(
                "lastMessage" to messageText,
                "lastMessageTime" to message.timestamp,
                "lastMessageSenderId" to currentUser.uid
            )
            database.child("chats").child(chatId).updateChildren(chatUpdates).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    message?.let { messages.add(it) }
                }
                messages.sortBy { it.timestamp }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        database.child("messages").child(chatId).addValueEventListener(listener)

        awaitClose {
            database.child("messages").child(chatId).removeEventListener(listener)
        }
    }

    suspend fun getChatInfo(chatId: String): Result<Chat> {
        return try {
            val snapshot = database.child("chats").child(chatId).get().await()
            val chat = snapshot.getValue(Chat::class.java)
                ?: return Result.failure(Exception("Chat not found"))

            Result.success(chat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addUserToGroupChat(chatId: String, userId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            val chatSnapshot = database.child("chats").child(chatId).get().await()
            val chat = chatSnapshot.getValue(Chat::class.java)
                ?: return Result.failure(Exception("Chat not found"))

            val userSnapshot = database.child("users").child(userId).get().await()
            val newUser = userSnapshot.getValue(User::class.java)
                ?: return Result.failure(Exception("User not found"))

            if (chat.participants.contains(userId)) {
                return Result.failure(Exception("User is already in the chat"))
            }

            val updatedParticipants = chat.participants + userId
            val updatedParticipantNames = chat.participantNames + (userId to newUser.nickname)
            val updatedParticipantImages = chat.participantImages + (userId to newUser.profileImageUrl)

            val updates = mapOf(
                "participants" to updatedParticipants,
                "participantNames" to updatedParticipantNames,
                "participantImages" to updatedParticipantImages,
                "isGroupChat" to true
            )

            database.child("chats").child(chatId).updateChildren(updates).await()

            val systemMessageId = database.child("messages").child(chatId).push().key
                ?: return Result.failure(Exception("Failed to generate message ID"))

            val systemMessage = Message(
                id = systemMessageId,
                text = "${newUser.nickname} joined the group",
                senderId = "system",
                senderNickname = "System",
                senderProfileImageUrl = "",
                timestamp = System.currentTimeMillis(),
                chatId = chatId
            )

            database.child("messages").child(chatId).child(systemMessageId).setValue(systemMessage).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllChats(): Flow<List<Chat>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chats = mutableListOf<Chat>()
                for (chatSnapshot in snapshot.children) {
                    val chat = chatSnapshot.getValue(Chat::class.java)
                    if (chat != null && chat.participants.contains(currentUserId)) {
                        chats.add(chat)
                    }
                }
                chats.sortByDescending { it.lastMessageTime }
                trySend(chats)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        database.child("chats").addValueEventListener(listener)

        awaitClose {
            database.child("chats").removeEventListener(listener)
        }
    }
}