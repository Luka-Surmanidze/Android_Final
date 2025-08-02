package ge.gmodebadze.android_final.presentation.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ge.gmodebadze.android_final.R
import ge.gmodebadze.android_final.databinding.ItemMessageReceivedBinding
import ge.gmodebadze.android_final.databinding.ItemMessageSentBinding

sealed class ChatMessage {
    data class Sent(val text: String, val time: String) : ChatMessage()
    data class Received(val text: String, val time: String) : ChatMessage()
}

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SENT = 0
        private const val TYPE_RECEIVED = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (messages[position]) {
            is ChatMessage.Sent -> TYPE_SENT
            is ChatMessage.Received -> TYPE_RECEIVED
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
        when (val message = messages[position]) {
            is ChatMessage.Sent -> (holder as SentMessageViewHolder).bind(message)
            is ChatMessage.Received -> (holder as ReceivedMessageViewHolder).bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    inner class SentMessageViewHolder(private val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage.Sent) {
            binding.messageText.text = message.text
            binding.messageTime.text = message.time
        }
    }

    inner class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage.Received) {
            binding.messageText.text = message.text
            binding.messageTime.text = message.time

            Glide.with(binding.senderProfileImage.context)
                .load(R.drawable.avatar_image_placeholder)
                .circleCrop()
                .into(binding.senderProfileImage)
        }
    }
}
