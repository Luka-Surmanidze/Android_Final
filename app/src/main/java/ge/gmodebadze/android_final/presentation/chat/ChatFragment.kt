package ge.gmodebadze.android_final.presentation.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import ge.gmodebadze.android_final.R
import ge.gmodebadze.android_final.data.repository.ChatRepository
import ge.gmodebadze.android_final.databinding.FragmentChatBinding
import ge.gmodebadze.android_final.domain.model.Chat
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private var chatId: String = ""
    private var participantId: String? = null

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatRepository: ChatRepository

    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(chatRepository, chatId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            chatId = it.getString("chatId") ?: ""
            participantId = it.getString("participantId")
        }

        if (chatId.isEmpty() && participantId != null) {
            createChatWithParticipant(participantId!!)
        }
    }

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

        chatRepository = ChatRepository()
        setupRecyclerView()
        setupListeners()

        if (chatId.isNotEmpty()) {
            observeViewModel()
        }
    }



    private fun createChatWithParticipant(participantId: String) {
        lifecycleScope.launch {
            val repository = ChatRepository()
            repository.createOrGetChat(participantId)
                .onSuccess { createdChatId ->
                    chatId = createdChatId
                    observeViewModel()
                }
                .onFailure { error ->
                    Toast.makeText(requireContext(), "Failed to create chat: ${error.message}", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter { onMakeGroupClick() }

        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                var totalDy = 0

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    totalDy += dy
                    if (dy < 0 && totalDy <= -400) {
                        resizeAppBarByScroll(-dy)
                    } else if (dy > 0 && totalDy >= -400) {
                        resizeAppBarByScroll(-dy)
                    }
//                    Log.d("total_dy",totalDy.toString())
                }
            })
        }
    }

    private var currentAppBarHeight: Int = 0

    private fun resizeAppBarByScroll(dy: Int) {
        val layoutParams = binding.chatAppBar.layoutParams
        if (currentAppBarHeight == 0) {
            currentAppBarHeight = layoutParams.height
        }

        val minHeight = dpToPx(80)
        val maxHeight = dpToPx(120)

        val targetHeight = (currentAppBarHeight - dy / 3).coerceIn(minHeight, maxHeight)

        val smoothingFactor = 0.3f
        currentAppBarHeight = (currentAppBarHeight + ((targetHeight - currentAppBarHeight) * smoothingFactor)).toInt()

        if (layoutParams.height != currentAppBarHeight) {
            layoutParams.height = currentAppBarHeight
            binding.chatAppBar.layoutParams = layoutParams
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun setupListeners() {
        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                viewModel.sendMessage(messageText)
                binding.messageEditText.setText("")

                binding.messageEditText.clearFocus()
                scrollToBottom()
            } else {
                Toast.makeText(requireContext(), "Enter a message", Toast.LENGTH_SHORT).show()
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.messageEditText.setOnEditorActionListener { _, _, _ ->
            binding.sendButton.performClick()
            true
        }
    }

    private fun observeViewModel() {
        // Observe UI state
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }

        // Observe messages
        lifecycleScope.launch {
            viewModel.messages.collect { messages ->
                chatAdapter.submitList(messages) {
                    if (messages.isNotEmpty()) {
                        scrollToBottom()
                    }
                }
            }
        }
    }

    private fun updateUI(state: ChatUiState) {
        binding.sendButton.isEnabled = !state.isSending

        state.chat?.let { chat ->
            updateChatHeader(chat)
        }

        state.error?.let { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    private fun updateChatHeader(chat: Chat) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (chat.isGroupChat) {
            val participantNames = chat.participantNames.values.joinToString(", ")
            binding.chatUsernames.text = participantNames
        } else {
            val otherParticipantName = chat.participantNames.entries
                .firstOrNull { it.key != currentUserId }?.value ?: "Unknown"
            binding.chatUsernames.text = otherParticipantName
        }
    }

    private fun onMakeGroupClick() {
        val bundle = Bundle().apply {
            putBoolean("isForGroupChat", true)
            putString("chatId", chatId)
        }
//        findNavController().navigate(R.id.action_chatFragment_to_userSearchFragment, bundle)
    }

    private fun scrollToBottom() {
        if (chatAdapter.itemCount > 0) {
            binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
        }
    }

    fun addUserToGroup(userId: String) {
        viewModel.addUserToGroup(userId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}