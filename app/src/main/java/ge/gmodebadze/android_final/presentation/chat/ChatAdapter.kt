package ge.gmodebadze.android_final.presentation.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import ge.gmodebadze.android_final.R
import ge.gmodebadze.android_final.databinding.ItemMessageReceivedBinding
import ge.gmodebadze.android_final.databinding.ItemMessageSentBinding
import ge.gmodebadze.android_final.domain.model.Message
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val onMakeGroupClick: () -> Unit
) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val TYPE_SENT = 0
        private const val TYPE_RECEIVED = 1
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
            TYPE_SENT
        } else {
            TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SENT -> {
                val binding = ItemMessageSentBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SentMessageViewHolder(binding)
            }
            TYPE_RECEIVED -> {
                val binding = ItemMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ReceivedMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    inner class SentMessageViewHolder(private val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.messageText.text = message.text
            binding.messageTime.text = formatTime(message.timestamp)

            if (message.text.equals("Make Group", ignoreCase = true)) {
                binding.root.setOnClickListener {
                    onMakeGroupClick()
                }
                binding.messageText.setTextColor(
                    binding.root.context.getColor(R.color.blue)
                )
            } else {
                binding.root.setOnClickListener(null)
                binding.messageText.setTextColor(
                    binding.root.context.getColor(R.color.white)
                )
            }
        }
    }

    inner class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.messageText.text = message.text
            binding.messageTime.text = formatTime(message.timestamp)

            Glide.with(binding.senderProfileImage.context)
                .load(message.senderProfileImageUrl.ifEmpty { R.drawable.avatar_image_placeholder })
                .placeholder(R.drawable.avatar_image_placeholder)
                .error(R.drawable.avatar_image_placeholder)
                .circleCrop()
                .into(binding.senderProfileImage)
        }
    }

    private fun formatTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            else -> {
                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                formatter.format(Date(timestamp))
            }
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }
}