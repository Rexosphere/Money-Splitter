package com.rexosphere.money_splitter.presentation.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rexosphere.money_splitter.data.repository.ExpenseRepository
import com.rexosphere.money_splitter.domain.model.Group
import com.rexosphere.money_splitter.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GroupsUiState(
    val groups: List<Group> = emptyList(),
    val friends: List<User> = emptyList(),
    val showCreateDialog: Boolean = false,
    val newGroupName: String = "",
    val selectedMembers: Set<String> = emptySet()
)

class GroupsViewModel(private val repository: ExpenseRepository = ExpenseRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            launch {
                repository.groups.collect { groups ->
                    _uiState.value = _uiState.value.copy(groups = groups)
                }
            }
            launch {
                repository.friends.collect { friends ->
                    _uiState.value = _uiState.value.copy(friends = friends)
                }
            }
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun hideCreateDialog() {
        _uiState.value = _uiState.value.copy(
            showCreateDialog = false,
            newGroupName = "",
            selectedMembers = emptySet()
        )
    }

    fun updateGroupName(name: String) {
        _uiState.value = _uiState.value.copy(newGroupName = name)
    }

    fun toggleMember(userId: String) {
        val selected = _uiState.value.selectedMembers
        val newSelected = if (selected.contains(userId)) {
            selected - userId
        } else {
            selected + userId
        }
        _uiState.value = _uiState.value.copy(selectedMembers = newSelected)
    }

    fun createGroup() {
        val state = _uiState.value
        if (state.newGroupName.isBlank() || state.selectedMembers.isEmpty()) return

        viewModelScope.launch {
            val members = state.friends.filter { state.selectedMembers.contains(it.id) }
            repository.addGroup(state.newGroupName, members + repository.currentUser)
            hideCreateDialog()
        }
    }
}
