package com.rexosphere.money_splitter.presentation.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rexosphere.money_splitter.data.repository.ExpenseRepository
import com.rexosphere.money_splitter.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendsUiState(
    val friends: List<User> = emptyList(),
    val showAddDialog: Boolean = false,
    val newFriendName: String = ""
)

class FriendsViewModel(private val repository: ExpenseRepository = ExpenseRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        loadFriends()
    }

    private fun loadFriends() {
        viewModelScope.launch {
            repository.friends.collect { friends ->
                _uiState.value = _uiState.value.copy(friends = friends)
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(
            showAddDialog = false,
            newFriendName = ""
        )
    }

    fun updateFriendName(name: String) {
        _uiState.value = _uiState.value.copy(newFriendName = name)
    }

    fun addFriend() {
        val name = _uiState.value.newFriendName
        if (name.isBlank()) return

        viewModelScope.launch {
            repository.addFriend(name)
            hideAddDialog()
        }
    }
}
