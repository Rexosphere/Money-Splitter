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
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val groupToEdit: Group? = null,
    val groupToDelete: Group? = null,
    val newGroupName: String = "",
    val selectedMembers: Set<String> = emptySet(),
    val includeSelf: Boolean = true,
    // Edit fields
    val editGroupName: String = "",
    val editSelectedMembers: Set<String> = emptySet(),
    val editIncludeSelf: Boolean = true
)

class GroupsViewModel(private val repository: ExpenseRepository = ExpenseRepository) : ViewModel() {
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

    // Create Dialog
    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun hideCreateDialog() {
        _uiState.value = _uiState.value.copy(
            showCreateDialog = false,
            newGroupName = "",
            selectedMembers = emptySet(),
            includeSelf = true
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

    fun toggleIncludeSelf() {
        _uiState.value = _uiState.value.copy(includeSelf = !_uiState.value.includeSelf)
    }

    fun createGroup() {
        val state = _uiState.value
        if (state.newGroupName.isBlank() || state.selectedMembers.isEmpty()) return

        viewModelScope.launch {
            val members = state.friends.filter { state.selectedMembers.contains(it.id) }
            val finalMembers = if (state.includeSelf) members + repository.currentUser else members
            repository.addGroup(state.newGroupName, finalMembers)
            hideCreateDialog()
        }
    }
    
    // Edit Dialog
    fun showEditDialog(group: Group) {
        val isCurrentUserInGroup = group.members.any { it.id == repository.currentUser.id }
        _uiState.value = _uiState.value.copy(
            showEditDialog = true,
            groupToEdit = group,
            editGroupName = group.name,
            editSelectedMembers = group.members.map { it.id }.toSet() - repository.currentUser.id,
            editIncludeSelf = isCurrentUserInGroup
        )
    }
    
    fun hideEditDialog() {
        _uiState.value = _uiState.value.copy(
            showEditDialog = false,
            groupToEdit = null,
            editGroupName = "",
            editSelectedMembers = emptySet(),
            editIncludeSelf = true
        )
    }
    
    fun updateEditGroupName(name: String) {
        _uiState.value = _uiState.value.copy(editGroupName = name)
    }
    
    fun toggleEditMember(userId: String) {
        val selected = _uiState.value.editSelectedMembers
        val newSelected = if (selected.contains(userId)) {
            selected - userId
        } else {
            selected + userId
        }
        _uiState.value = _uiState.value.copy(editSelectedMembers = newSelected)
    }
    
    fun toggleEditIncludeSelf() {
        _uiState.value = _uiState.value.copy(editIncludeSelf = !_uiState.value.editIncludeSelf)
    }
    
    fun confirmEditGroup() {
        val group = _uiState.value.groupToEdit ?: return
        val state = _uiState.value
        if (state.editGroupName.isBlank() || state.editSelectedMembers.isEmpty()) return
        
        viewModelScope.launch {
            val members = state.friends.filter { state.editSelectedMembers.contains(it.id) }
            val finalMembers = if (state.editIncludeSelf) members + repository.currentUser else members
            repository.updateGroup(group.id, state.editGroupName, finalMembers)
            hideEditDialog()
        }
    }
    
    // Delete Dialog
    fun showDeleteDialog(group: Group) {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = true,
            groupToDelete = group
        )
    }
    
    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = false,
            groupToDelete = null
        )
    }
    
    fun confirmDeleteGroup() {
        val group = _uiState.value.groupToDelete ?: return
        viewModelScope.launch {
            repository.deleteGroup(group.id)
            hideDeleteDialog()
        }
    }
}
