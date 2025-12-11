package com.rexosphere.money_splitter.presentation.add_expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rexosphere.money_splitter.data.repository.ExpenseRepository
import com.rexosphere.money_splitter.domain.model.Expense
import com.rexosphere.money_splitter.domain.model.Group
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
    val groups: List<Group> = emptyList(),
    val selectedGroup: Group? = null,
    
    // Who paid section
    val selectedPayers: Set<String> = emptySet(),
    val payerAmounts: Map<String, String> = emptyMap(),
    
    // Split with section
    val selectedParticipants: Set<String> = emptySet(),
    val participantShares: Map<String, String> = emptyMap(),
    
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false
)

class AddExpenseViewModel(private val repository: ExpenseRepository = ExpenseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    init {
        loadFriends()
        loadGroups()
    }

    private fun loadFriends() {
        viewModelScope.launch {
            repository.friends.collect { friends ->
                _uiState.value = _uiState.value.copy(friends = friends)
            }
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            repository.groups.collect { groups ->
                _uiState.value = _uiState.value.copy(groups = groups)
            }
        }
    }

    fun selectGroup(group: Group?) {
        if (group == null) {
            _uiState.value = _uiState.value.copy(selectedGroup = null)
            return
        }
        
        // Auto-select all group members as participants
        val memberIds = group.members.map { it.id }.toSet()
        _uiState.value = _uiState.value.copy(
            selectedGroup = group,
            selectedParticipants = memberIds
        )
        updateParticipantShares()
    }

    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
        updatePayerAmounts()
        updateParticipantShares()
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    // Payer functions
    fun togglePayer(userId: String) {
        val selected = _uiState.value.selectedPayers
        val newSelected = if (selected.contains(userId)) {
            selected - userId
        } else {
            selected + userId
        }
        _uiState.value = _uiState.value.copy(selectedPayers = newSelected)
        updatePayerAmounts()
    }

    fun updatePayerAmount(userId: String, amount: String) {
        val amounts = _uiState.value.payerAmounts.toMutableMap()
        amounts[userId] = amount
        _uiState.value = _uiState.value.copy(payerAmounts = amounts)
    }

    private fun updatePayerAmounts() {
        val totalAmount = _uiState.value.amount.toDoubleOrNull() ?: return
        val payerCount = _uiState.value.selectedPayers.size
        if (payerCount == 0) return

        val equalAmount = totalAmount / payerCount
        val amounts = mutableMapOf<String, String>()

        _uiState.value.selectedPayers.forEach { userId ->
            amounts[userId] = formatAmount(equalAmount)
        }

        _uiState.value = _uiState.value.copy(payerAmounts = amounts)
    }

    // Participant functions
    fun toggleParticipant(userId: String) {
        val selected = _uiState.value.selectedParticipants
        val newSelected = if (selected.contains(userId)) {
            selected - userId
        } else {
            selected + userId
        }
        _uiState.value = _uiState.value.copy(selectedParticipants = newSelected)
        updateParticipantShares()
    }

    fun updateParticipantShare(userId: String, share: String) {
        val shares = _uiState.value.participantShares.toMutableMap()
        shares[userId] = share
        _uiState.value = _uiState.value.copy(participantShares = shares)
    }

    private fun updateParticipantShares() {
        val totalAmount = _uiState.value.amount.toDoubleOrNull() ?: return
        val participantCount = _uiState.value.selectedParticipants.size
        if (participantCount == 0) return

        val equalShare = totalAmount / participantCount
        val shares = mutableMapOf<String, String>()

        _uiState.value.selectedParticipants.forEach { userId ->
            shares[userId] = formatAmount(equalShare)
        }

        _uiState.value = _uiState.value.copy(participantShares = shares)
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveExpense() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull() ?: return
        val description = state.description.ifBlank { "Expense" }

        // Validate that we have payers and participants
        if (state.selectedPayers.isEmpty() || state.selectedParticipants.isEmpty()) return

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            // Build payers map
            val payers = mutableMapOf<User, Double>()
            state.selectedPayers.forEach { userId ->
                val user = if (userId == repository.currentUser.id) {
                    repository.currentUser
                } else {
                    state.friends.find { it.id == userId }
                }
                user?.let {
                    val paidAmount = state.payerAmounts[userId]?.toDoubleOrNull() ?: 0.0
                    payers[it] = paidAmount
                }
            }

            // Build participants map
            val participants = mutableMapOf<User, Double>()
            state.selectedParticipants.forEach { userId ->
                val user = if (userId == repository.currentUser.id) {
                    repository.currentUser
                } else {
                    state.friends.find { it.id == userId }
                }
                user?.let {
                    val share = state.participantShares[userId]?.toDoubleOrNull() ?: 0.0
                    participants[it] = share
                }
            }

            val expense = Expense(
                id = Uuid.random().toString(),
                description = description,
                amount = amount,
                date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
                paidBy = payers,
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
