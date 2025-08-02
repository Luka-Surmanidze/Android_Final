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

    private val _allChats = MutableStateFlow<List<ChatItem>>(emptyList())
    private val _searchQuery = MutableStateFlow("")

    val chatItems: StateFlow<List<ChatItem>> = combine(
        _allChats,
        _searchQuery
    ) { chats, query ->
        if (query.isEmpty()) {
            chats
        } else {
            chats.filter { chatItem ->
                chatItem.userName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val currentUserId = chatRepository.currentUserId
                if (currentUserId == null) {
                    _error.value = "User not authenticated"
                    _isLoading.value = false
                    return@launch
                }

                chatRepository.getAllChats().collectLatest { chatList ->
                    val result = chatList.mapNotNull { chat ->
                        // Only handle private chats for now
                        if (!chat.isGroupChat) {
                            val otherUserId = chat.participants.firstOrNull { it != currentUserId }
                            val userName = otherUserId?.let { chat.participantNames[it] } ?: "Unknown User"
                            val userPhoto = otherUserId?.let { chat.participantImages[it] }

                            ChatItem(
                                chatId = chat.id,
                                userName = userName,
                                lastMessage = chat.lastMessage.ifEmpty { "No messages yet" },
                                timestamp = chat.lastMessageTime,
                                userPhotoUrl = userPhoto
                            )
                        } else {
                            null // Skip group chats for now
                        }
                    }
                    _allChats.value = result
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Failed to load chats: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun searchChats(query: String) {
        _searchQuery.value = query.trim()
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun clearError() {
        _error.value = null
    }

    fun refreshChats() {
        loadChats()
    }
}

