package ge.gmodebadze.android_final.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ge.gmodebadze.android_final.databinding.ItemConversationBinding
import java.text.SimpleDateFormat
import java.util.*
class ChatAdapter(
    private val chatList: List<ChatItem>,
    private val onClick: (ChatItem) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatItem: ChatItem) {
            binding.textUsername.text = chatItem.userName
            binding.textLastMessage.text = chatItem.lastMessage
            binding.textTimestamp.text = formatTimestamp(chatItem.timestamp)

//            binding.imageProfile.setImageResource()

            binding.root.setOnClickListener { onClick(chatItem) }
        }

        private fun formatTimestamp(time: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - time

            return when {
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min ago"
                diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hrs ago"
                else -> {
                    val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
                    sdf.format(Date(time))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemConversationBinding.inflate(inflater, parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    override fun getItemCount(): Int = chatList.size
}


data class ChatItem(
    val userName: String,
    val lastMessage: String,
    val timestamp: Long,
    val userPhotoUrl: String? = null
)