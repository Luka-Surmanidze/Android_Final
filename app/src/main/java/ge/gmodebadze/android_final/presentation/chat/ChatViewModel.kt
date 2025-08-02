package ge.gmodebadze.android_final.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ge.gmodebadze.android_final.data.repository.ChatRepository
import ge.gmodebadze.android_final.domain.model.Chat
import ge.gmodebadze.android_final.domain.model.Message
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val chatId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    init {
        loadChatInfo()
        loadMessages()
    }

    private fun loadChatInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            chatRepository.getChatInfo(chatId)
                .onSuccess { chat ->
                    _uiState.value = _uiState.value.copy(
                        chat = chat,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getMessages(chatId)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message
                    )
                }
                .collect { messagesList ->
                    _messages.value = messagesList
                }
        }
    }

    fun sendMessage(messageText: String) {
        if (messageText.trim().isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)

            chatRepository.sendMessage(chatId, messageText.trim())
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = error.message
                    )
                }
        }
    }

    fun addUserToGroup(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            chatRepository.addUserToGroupChat(chatId, userId)
                .onSuccess {
                    loadChatInfo() // Reload chat info to get updated participants
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun checkForMakeGroupMessage(message: Message): Boolean {
        return message.text.equals("Make Group", ignoreCase = true)
    }
}

data class ChatUiState(
    val chat: Chat? = null,
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)

class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val chatId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(chatRepository, chatId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}