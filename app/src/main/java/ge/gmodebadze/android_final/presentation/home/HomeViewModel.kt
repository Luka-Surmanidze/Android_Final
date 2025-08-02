package ge.gmodebadze.android_final.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ge.gmodebadze.android_final.data.repository.ChatRepository
import ge.gmodebadze.android_final.domain.model.Chat
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _chatItems = MutableStateFlow<List<ChatItem>>(emptyList())
    val chatItems: StateFlow<List<ChatItem>> = _chatItems.asStateFlow()

    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            val currentUserId = chatRepository.currentUserId ?: return@launch

            chatRepository.getAllChats().collectLatest { chatList ->
                val result = chatList.mapNotNull { chat ->
                    val otherUserId = chat.participants.firstOrNull { it != currentUserId } ?: return@mapNotNull null
                    val userName = chat.participantNames[otherUserId] ?: return@mapNotNull null
                    val userPhoto = chat.participantImages[otherUserId]
                    ChatItem(
                        chatId = chat.id,
                        userName = userName,
                        lastMessage = chat.lastMessage,
                        timestamp = chat.lastMessageTime,
                        userPhotoUrl = userPhoto
                    )
                }
                _chatItems.value = result
            }
        }
    }

}
