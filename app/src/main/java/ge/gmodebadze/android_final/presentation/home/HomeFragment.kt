package ge.gmodebadze.android_final.presentation.home

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ge.gmodebadze.android_final.R
import ge.gmodebadze.android_final.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: ChatAdapter
    private var isSearchVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchFunctionality()
        observeChats()
        observeUiState()
        setupBottomNavigation()
        setupFloatingActionButton()
        setupScrollBehavior()
    }

    private fun setupSearchFunctionality() {
        binding.topBar.searchCardView.setOnClickListener {
            if (!isSearchVisible) {
                showSearchLayout()
            }
        }

        binding.topBar.backFromSearchButton.setOnClickListener {
            hideSearchLayout()
        }

        binding.topBar.clearSearchButton.setOnClickListener {
            binding.topBar.searchEditText.setText("")
            viewModel.clearSearch()
        }

        binding.topBar.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()

                binding.topBar.clearSearchButton.isVisible = query.isNotEmpty()

                // Perform search
                viewModel.searchChats(query)
            }
        })

        binding.topBar.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    private fun showSearchLayout() {
        isSearchVisible = true

        binding.topBar.defaultSearchLayout.visibility = View.GONE

        binding.topBar.searchLayout.visibility = View.VISIBLE

        binding.topBar.searchEditText.requestFocus()
        showKeyboard()
    }

    private fun hideSearchLayout() {
        isSearchVisible = false

        binding.topBar.searchEditText.setText("")
        viewModel.clearSearch()

        hideKeyboard()

        binding.topBar.defaultSearchLayout.visibility = View.VISIBLE

        binding.topBar.searchLayout.visibility = View.GONE
    }

    private fun showKeyboard() {
        binding.topBar.searchEditText.post {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.topBar.searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = requireActivity().currentFocus
        currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter { chatItem ->
            val bundle = Bundle().apply {
                putString("chatId", chatItem.chatId)
            }
            findNavController().navigate(R.id.action_homeFragment_to_chatFragment, bundle)
        }
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = adapter
    }

    private fun observeChats() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chatItems.collectLatest { chatItems ->
                adapter.submitList(chatItems)

                if (chatItems.isEmpty() && isSearchVisible) {
                    Log.d("HomeFragment", "No users found for current search")
                }
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                // Show/hide loading indicator if you have one
                // binding.progressBar.isVisible = isLoading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collectLatest { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun setupScrollBehavior() {
        var isBottomBarVisible = true

        binding.chatRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // Don't hide/show bottom bar during search
                if (isSearchVisible) return

                val currentHeight = binding.topBar.root.layoutParams.height
                val newHeight = (currentHeight - dy / 3).coerceIn(dpToPx(100), dpToPx(160))

                val layoutParams = binding.topBar.root.layoutParams
                layoutParams.height = newHeight
                binding.topBar.root.layoutParams = layoutParams

                if (dy > 10 && isBottomBarVisible) {
                    hideBottomBar()
                    isBottomBarVisible = false
                } else if (dy < -10 && !isBottomBarVisible) {
                    showBottomBar()
                    isBottomBarVisible = true
                }
            }
        })
    }

    private fun hideBottomBar() {
        binding.bottomBar.bottomNav.animate()
            .translationY(binding.bottomBar.bottomNav.height.toFloat())
            .setDuration(200)
            .start()

        binding.bottomBar.fabUsers.animate()
            .translationY(40f)
            .setDuration(200).start()
    }

    private fun showBottomBar() {
        binding.bottomBar.bottomNav.animate()
            .translationY(0f)
            .setDuration(200)
            .start()

        binding.bottomBar.fabUsers.animate()
            .translationY(0f)
            .setDuration(200).start()
    }

    private fun setupBottomNavigation() {
        binding.bottomBar.bottomNav.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.menu_home -> {
                    true
                }
                R.id.menu_profile -> {
                    findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
                    true
                }
                else -> false
            }
        }

        binding.bottomBar.bottomNav.selectedItemId = R.id.menu_home
    }

    private fun setupFloatingActionButton() {
        binding.bottomBar.fabUsers.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_userSearchFragment)
        }
    }

    fun onBackPressed(): Boolean {
        return if (isSearchVisible) {
            hideSearchLayout()
            true
        } else {
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}