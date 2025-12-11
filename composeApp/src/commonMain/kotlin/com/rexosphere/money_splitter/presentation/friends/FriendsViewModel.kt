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
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val friendToEdit: User? = null,
    val friendToDelete: User? = null,
    val newFriendName: String = "",
    val newFriendPhone: String = "",
    val newFriendEmail: String = "",
    val newFriendIsAppUser: Boolean = false,
    // Edit fields
    val editFriendName: String = "",
    val editFriendPhone: String = "",
    val editFriendEmail: String = "",
    val editFriendIsAppUser: Boolean = false
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

    // Add Dialog
    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(
            showAddDialog = false,
            newFriendName = "",
            newFriendPhone = "",
            newFriendEmail = "",
            newFriendIsAppUser = false
        )
    }

    fun updateFriendName(name: String) {
        _uiState.value = _uiState.value.copy(newFriendName = name)
    }
    
    fun updateFriendPhone(phone: String) {
        _uiState.value = _uiState.value.copy(newFriendPhone = phone)
    }
    
    fun updateFriendEmail(email: String) {
        _uiState.value = _uiState.value.copy(newFriendEmail = email)
    }
    
    fun toggleIsAppUser(isAppUser: Boolean) {
        _uiState.value = _uiState.value.copy(newFriendIsAppUser = isAppUser)
    }

    fun addFriend() {
        val name = _uiState.value.newFriendName
        if (name.isBlank()) return

        viewModelScope.launch {
            repository.addFriend(
                name = name,
                phoneNumber = _uiState.value.newFriendPhone.takeIf { it.isNotBlank() },
                email = _uiState.value.newFriendEmail.takeIf { it.isNotBlank() },
                isAppUser = _uiState.value.newFriendIsAppUser
            )
            hideAddDialog()
        }
    }
    
    // Edit Dialog
    fun showEditDialog(friend: User) {
        _uiState.value = _uiState.value.copy(
            showEditDialog = true,
            friendToEdit = friend,
            editFriendName = friend.name,
            editFriendPhone = friend.phoneNumber ?: "",
            editFriendEmail = friend.email ?: "",
            editFriendIsAppUser = friend.isAppUser
        )
    }
    
    fun hideEditDialog() {
        _uiState.value = _uiState.value.copy(
            showEditDialog = false,
            friendToEdit = null,
            editFriendName = "",
            editFriendPhone = "",
            editFriendEmail = "",
            editFriendIsAppUser = false
        )
    }
    
    fun updateEditFriendName(name: String) {
        _uiState.value = _uiState.value.copy(editFriendName = name)
    }
    
    fun updateEditFriendPhone(phone: String) {
        _uiState.value = _uiState.value.copy(editFriendPhone = phone)
    }
    
    fun updateEditFriendEmail(email: String) {
        _uiState.value = _uiState.value.copy(editFriendEmail = email)
    }
    
    fun toggleEditIsAppUser(isAppUser: Boolean) {
        _uiState.value = _uiState.value.copy(editFriendIsAppUser = isAppUser)
    }
    
    fun confirmEditFriend() {
        val friend = _uiState.value.friendToEdit ?: return
        val name = _uiState.value.editFriendName
        if (name.isBlank()) return
        
        viewModelScope.launch {
            repository.updateFriend(
                friendId = friend.id,
                name = name,
                phoneNumber = _uiState.value.editFriendPhone.takeIf { it.isNotBlank() },
                email = _uiState.value.editFriendEmail.takeIf { it.isNotBlank() },
                isAppUser = _uiState.value.editFriendIsAppUser
            )
            hideEditDialog()
        }
    }
    
    // Delete Dialog
    fun showDeleteDialog(friend: User) {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = true,
            friendToDelete = friend
        )
    }
    
    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = false,
            friendToDelete = null
        )
    }
    
    fun confirmDeleteFriend() {
        val friend = _uiState.value.friendToDelete ?: return
        viewModelScope.launch {
            repository.deleteFriend(friend.id)
            hideDeleteDialog()
        }
    }
}
