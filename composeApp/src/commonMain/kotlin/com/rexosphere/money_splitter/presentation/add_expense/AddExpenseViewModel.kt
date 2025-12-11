package com.rexosphere.money_splitter.presentation.add_expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rexosphere.money_splitter.data.repository.ExpenseRepository
import com.rexosphere.money_splitter.domain.model.Expense
import com.rexosphere.money_splitter.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.math.roundToInt

// KMP-compatible number formatting
private fun formatAmount(amount: Double): String {
    val rounded = (amount * 100).roundToInt() / 100.0
    val intPart = rounded.toLong()
    val decPart = ((rounded - intPart) * 100).roundToInt()
    return "$intPart.${decPart.toString().padStart(2, '0')}"
}

data class AddExpenseUiState(
    val amount: String = "",
    val description: String = "",
    val friends: List<User> = emptyList(),
    val selectedFriends: Set<String> = emptySet(),
    val friendShares: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false
)

class AddExpenseViewModel(private val repository: ExpenseRepository = ExpenseRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

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

    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
        updateShares()
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun toggleFriend(friendId: String) {
        val selected = _uiState.value.selectedFriends
        val newSelected = if (selected.contains(friendId)) {
            selected - friendId
        } else {
            selected + friendId
        }
        _uiState.value = _uiState.value.copy(selectedFriends = newSelected)
        updateShares()
    }

    fun updateFriendShare(friendId: String, share: String) {
        val shares = _uiState.value.friendShares.toMutableMap()
        shares[friendId] = share
        _uiState.value = _uiState.value.copy(friendShares = shares)
    }

    private fun updateShares() {
        val amount = _uiState.value.amount.toDoubleOrNull() ?: return
        val selectedCount = _uiState.value.selectedFriends.size + 1 // +1 for current user
        if (selectedCount == 0) return

        val equalShare = amount / selectedCount
        val shares = mutableMapOf<String, String>()

        _uiState.value.selectedFriends.forEach { friendId ->
            shares[friendId] = formatAmount(equalShare)
        }
        shares[repository.currentUser.id] = formatAmount(equalShare)

        _uiState.value = _uiState.value.copy(friendShares = shares)
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveExpense() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull() ?: return
        val description = state.description.ifBlank { "Expense" }

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            val participants = mutableMapOf<User, Double>()

            // Add current user
            val currentUserShare = state.friendShares[repository.currentUser.id]?.toDoubleOrNull() ?: 0.0
            participants[repository.currentUser] = currentUserShare

            // Add selected friends
            state.friends.filter { state.selectedFriends.contains(it.id) }.forEach { friend ->
                val share = state.friendShares[friend.id]?.toDoubleOrNull() ?: 0.0
                participants[friend] = share
            }

            val expense = Expense(
                id = Uuid.random().toString(),
                description = description,
                amount = amount,
                date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
                paidBy = repository.currentUser,
                participants = participants
            )

            repository.addExpense(expense)

            _uiState.value = state.copy(
                isSaving = false,
                savedSuccessfully = true
            )
        }
    }

    fun resetForm() {
        _uiState.value = AddExpenseUiState(friends = _uiState.value.friends)
    }
}
