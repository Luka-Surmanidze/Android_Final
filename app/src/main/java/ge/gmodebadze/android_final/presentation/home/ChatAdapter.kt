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
                    .circleCrop()
                    .into(binding.imageProfile)
            } ?: run {
                binding.imageProfile.setImageResource(R.drawable.avatar_image_placeholder)
            }

            binding.textUsername.text = item.userName
            binding.textLastMessage.text = item.lastMessage
            binding.textTimestamp.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.timestamp))

            binding.root.setOnClickListener { onChatClick(item) }
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
