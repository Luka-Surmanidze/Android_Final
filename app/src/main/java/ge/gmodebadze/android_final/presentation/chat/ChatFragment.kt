package ge.gmodebadze.android_final.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ge.gmodebadze.android_final.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(getDummyMessages())
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }
    }

    private fun setupListeners() {
        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                chatAdapter.addMessage(ChatMessage.Sent(messageText, "12:34"))
                binding.messageEditText.setText("")
                binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
            } else {
                Toast.makeText(requireContext(), "Enter a message", Toast.LENGTH_SHORT).show()
            }
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun getDummyMessages(): MutableList<ChatMessage> {
        return mutableListOf(
            ChatMessage.Received("Hi there!", "12:30"),
            ChatMessage.Sent("Hello! How are you?", "12:31"),
            ChatMessage.Received("I'm good! Working on the project.", "12:32"),
            ChatMessage.Sent("Nice! Keep going.", "12:33"),
            ChatMessage.Received("Hi there!", "12:30"),
            ChatMessage.Sent("Hello! How are you?", "12:31"),
            ChatMessage.Received("I'm good! Working on the project.", "12:32"),
            ChatMessage.Sent("Nice! Keep going.", "12:33"),
            ChatMessage.Received("Hi there!", "12:30"),
            ChatMessage.Sent("Hello! How are you?", "12:31"),
            ChatMessage.Received("I'm good! Working on the project.", "12:32"),
            ChatMessage.Sent("Nice! Keep going.", "12:33"),
            ChatMessage.Received("Hi there!", "12:30"),
            ChatMessage.Sent("Hello! How are you?", "12:31"),
            ChatMessage.Received("I'm good! Working on the project.", "12:32"),
            ChatMessage.Sent("Nice! Keep going.", "12:33")
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
