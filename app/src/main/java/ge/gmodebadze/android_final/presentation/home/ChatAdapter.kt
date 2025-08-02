package ge.gmodebadze.android_final.presentation.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ge.gmodebadze.android_final.R
import ge.gmodebadze.android_final.databinding.ItemConversationBinding
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val onChatClick: (ChatItem) -> Unit
) : ListAdapter<ChatItem, ChatAdapter.ChatViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<ChatItem>() {
        override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
            return oldItem.chatId == newItem.chatId
        }

        override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
            return oldItem == newItem
        }
    }

    inner class ChatViewHolder(private val binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: ChatItem) {
            item.userPhotoUrl?.let { url ->
                Glide.with(binding.imageProfile.context)
                    .load(url)
                    .placeholder(R.drawable.avatar_image_placeholder)
                    .error(R.drawable.avatar_image_placeholder)
                    .circleCrop()
                    .into(binding.imageProfile)
            } ?: run {
                binding.imageProfile.setImageResource(R.drawable.avatar_image_placeholder)
            }

            binding.textUsername.text = item.userName

            binding.textLastMessage.text = if (item.lastMessage.isEmpty()) {
                "No messages yet"
            } else {
                item.lastMessage
            }

            binding.textTimestamp.text = formatTimestamp(item.timestamp)

            binding.root.setOnClickListener { onChatClick(item) }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60_000 -> "Now" // Less than 1 minute
                diff < 3_600_000 -> "${diff / 60_000}m"
                diff < 86_400_000 -> "${diff / 3_600_000}h"
                diff < 604_800_000 -> {
                    SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
                }
                else -> {
                    SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

data class ChatItem(
    val chatId: String,
    val userName: String,
    val lastMessage: String,
    val timestamp: Long,
    val userPhotoUrl: String? = null
)