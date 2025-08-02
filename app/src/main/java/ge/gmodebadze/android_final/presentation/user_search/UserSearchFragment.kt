package ge.gmodebadze.android_final.presentation.user_search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ge.gmodebadze.android_final.databinding.FragmentUserSearchBinding
import ge.gmodebadze.android_final.domain.model.User

class UserSearchFragment : Fragment() {

    private var _binding: FragmentUserSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: UserSearchViewModel
    private lateinit var userAdapter: UserSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[UserSearchViewModel::class.java]

        setupRecyclerView()
        setupSearchView()
        setupClickListeners()
        observeViewModel()

        // Load initial users
        viewModel.loadUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserSearchAdapter { user ->
            onUserSelected(user)
        }

        binding.rvUsers.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(requireContext())

            // Add scroll listener for pagination
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    // Load more when near the end
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount - 5) {
                        viewModel.loadMoreUsers()
                    }
                }
            })
        }
    }

    private fun setupSearchView() {
        binding.etSearch.addTextChangedListener { editable ->
            val query = editable?.toString()?.trim() ?: ""
            if (query.isEmpty()) {
                viewModel.clearSearch()
            } else {
                viewModel.searchUsers(query)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UserSearchViewModel.UserSearchState.Idle -> {
                    // Initial state
                }
                is UserSearchViewModel.UserSearchState.Loading -> {
                    binding.rvUsers.visibility = View.GONE
                    binding.tvNoResults.visibility = View.GONE
                }
                is UserSearchViewModel.UserSearchState.Success -> {
                    binding.rvUsers.visibility = View.VISIBLE
                    binding.tvNoResults.visibility = View.GONE
                    userAdapter.submitList(state.users)

                    // Show loading indicator for pagination
                    binding.progressBarBottom.visibility =
                        if (state.isLoadingMore) View.VISIBLE else View.GONE
                }
                is UserSearchViewModel.UserSearchState.NoResults -> {
                    binding.rvUsers.visibility = View.GONE
                    binding.tvNoResults.visibility = View.VISIBLE
                    binding.tvNoResults.text = "No users found"
                }
                is UserSearchViewModel.UserSearchState.Error -> {
                    binding.rvUsers.visibility = View.GONE
                    binding.tvNoResults.visibility = View.GONE
                    showToast(state.message)
                }
            }
        }
    }

    private fun onUserSelected(user: User) {
        // Navigate to chat page with selected user
        // TODO: Implement navigation to chat with user
        showToast("Opening chat with ${user.nickname}")

        // Example navigation (adjust according to your nav graph):
        // val action = UserSearchFragmentDirections.actionUserSearchFragmentToChatFragment(user.uid)
        // findNavController().navigate(action)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}