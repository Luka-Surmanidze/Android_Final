package ge.gmodebadze.android_final.domain.model

data class Message(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderNickname: String = "",
    val senderProfileImageUrl: String = "",
    val timestamp: Long = 0L,
    val chatId: String = ""
) {
    constructor() : this("", "", "", "", "", 0L, "")
}

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantImages: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val lastMessageSenderId: String = "",
    val isGroupChat: Boolean = false,
    val createdAt: Long = 0L
) {
    constructor() : this("", emptyList(), emptyMap(), emptyMap(), "", 0L, "", false, 0L)
}