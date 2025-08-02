package ge.gmodebadze.android_final.presentation.user_search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ge.gmodebadze.android_final.data.repository.UserSearchRepository
import ge.gmodebadze.android_final.domain.model.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserSearchViewModel(
    private val userSearchRepository: UserSearchRepository = UserSearchRepository()
) : ViewModel() {

    sealed class UserSearchState {
        object Idle : UserSearchState()
        object Loading : UserSearchState()
        data class Success(val users: List<User>, val isLoadingMore: Boolean = false) : UserSearchState()
        data class Error(val message: String) : UserSearchState()
        object NoResults : UserSearchState()
    }

    private val _searchState = MutableLiveData<UserSearchState>(UserSearchState.Idle)
    val searchState: LiveData<UserSearchState> = _searchState

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentUsers = mutableListOf<User>()
    private var lastUserId: String? = null
    private var isSearchMode = false
    private var searchJob: Job? = null

    companion object {
        private const val TAG = "UserSearchViewModel"
        private const val SEARCH_DELAY_MS = 500L
        private const val PAGE_SIZE = 20
    }

    fun loadUsers() {
        Log.d(TAG, "Loading users with pagination")

        if (_searchState.value is UserSearchState.Loading) return

        _searchState.value = UserSearchState.Loading
        _isLoading.value = true
        isSearchMode = false

        viewModelScope.launch {
            try {
                val result = userSearchRepository.getUsersWithPagination(lastUserId, PAGE_SIZE)
                _isLoading.value = false

                result.fold(
                    onSuccess = { users ->
                        if (users.isNotEmpty()) {
                            currentUsers.addAll(users)
                            lastUserId = users.lastOrNull()?.uid
                            Log.d(TAG, "Loaded ${users.size} users, total: ${currentUsers.size}")
                            _searchState.value = UserSearchState.Success(currentUsers.toList())
                        } else {
                            if (currentUsers.isEmpty()) {
                                _searchState.value = UserSearchState.NoResults
                            } else {
                                _searchState.value = UserSearchState.Success(currentUsers.toList())
                            }
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to load users", exception)
                        _searchState.value = UserSearchState.Error(
                            exception.message ?: "Failed to load users"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading users", e)
                _isLoading.value = false
                _searchState.value = UserSearchState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun loadMoreUsers() {
        if (isSearchMode || _isLoading.value == true) return

        Log.d(TAG, "Loading more users")

        val currentState = _searchState.value
        if (currentState is UserSearchState.Success) {
            _searchState.value = currentState.copy(isLoadingMore = true)
        }

        viewModelScope.launch {
            try {
                val result = userSearchRepository.getUsersWithPagination(lastUserId, PAGE_SIZE)

                result.fold(
                    onSuccess = { users ->
                        if (users.isNotEmpty()) {
                            currentUsers.addAll(users)
                            lastUserId = users.lastOrNull()?.uid
                            Log.d(TAG, "Loaded ${users.size} more users, total: ${currentUsers.size}")
                        }
                        _searchState.value = UserSearchState.Success(currentUsers.toList())
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to load more users", exception)
                        _searchState.value = UserSearchState.Success(currentUsers.toList())
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading more users", e)
                _searchState.value = UserSearchState.Success(currentUsers.toList())
            }
        }
    }

    fun searchUsers(query: String) {
        Log.d(TAG, "Search query: $query")

        // Cancel previous search job
        searchJob?.cancel()

        if (query.length < 3) {
            // If query is less than 3 characters, load normal user list
            if (isSearchMode) {
                resetToUserList()
            }
            return
        }

        isSearchMode = true

        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY_MS) // Debounce delay

            _searchState.value = UserSearchState.Loading
            _isLoading.value = true

            try {
                val result = userSearchRepository.searchUsers(query)
                _isLoading.value = false

                result.fold(
                    onSuccess = { users ->
                        Log.d(TAG, "Search found ${users.size} users")
                        if (users.isNotEmpty()) {
                            _searchState.value = UserSearchState.Success(users)
                        } else {
                            _searchState.value = UserSearchState.NoResults
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Search failed", exception)
                        _searchState.value = UserSearchState.Error(
                            exception.message ?: "Search failed"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Search exception", e)
                _isLoading.value = false
                _searchState.value = UserSearchState.Error(e.message ?: "Search failed")
            }
        }
    }

    private fun resetToUserList() {
        Log.d(TAG, "Resetting to user list")
        isSearchMode = false
        _searchState.value = UserSearchState.Success(currentUsers.toList())
    }

    fun clearSearch() {
        Log.d(TAG, "Clearing search")
        searchJob?.cancel()
        resetToUserList()
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        Log.d(TAG, "UserSearchViewModel cleared")
    }
}