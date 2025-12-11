package com.rexosphere.money_splitter.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rexosphere.money_splitter.data.repository.ExpenseRepository
import com.rexosphere.money_splitter.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val netBalanceOwed: Double = 0.0,
    val netBalanceOwe: Double = 0.0,
    val friendDebts: List<Pair<User, Double>> = emptyList(),
    val allDebts: List<Triple<User, User, Double>> = emptyList(), // (debtor, creditor, amount)
    val recentExpenses: List<com.rexosphere.money_splitter.domain.model.Expense> = emptyList()
)

class HomeViewModel(private val repository: ExpenseRepository = ExpenseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observePayments()
    }

    private fun observePayments() {
        viewModelScope.launch {
            // Observe payments to automatically update when expenses change
            repository.payments.collect { _ ->
                updateBalances()
            }
        }
        
        // Also observe expenses for the summary
        viewModelScope.launch {
            repository.expenses.collect { expenses ->
                _uiState.value = _uiState.value.copy(
                    recentExpenses = expenses.sortedByDescending { it.date }.take(5)
                )
            }
        }
    }

    private fun updateBalances() {
        val balances = repository.getNetBalances()
        val currentUserId = repository.currentUser.id

        var totalOwed = 0.0
        var totalOwe = 0.0

        balances.forEach { (user, balance) ->
            if (user.id == currentUserId) {
                if (balance > 0) totalOwed = balance
                else totalOwe = kotlin.math.abs(balance)
            }
        }

        // Get ALL debts (not simplified) for complete picture
        val allDebts = repository.getAllDebts()

        // Get simplified debts for calculating what YOU owe/are owed
        val simplifiedDebts = repository.getSimplifiedDebts()
        val myDebts = mutableMapOf<User, Double>()
        simplifiedDebts.forEach { (debtor, creditorAmount) ->
            val (creditor, amount) = creditorAmount

            when {
                debtor.id == currentUserId -> {
                    // We owe someone
                    myDebts[creditor] = -(myDebts[creditor] ?: 0.0) - amount
                }
                creditor.id == currentUserId -> {
                    // Someone owes us
                    myDebts[debtor] = (myDebts[debtor] ?: 0.0) + amount
                }
            }
        }

        _uiState.value = _uiState.value.copy(
            netBalanceOwed = totalOwed,
            netBalanceOwe = totalOwe,
            friendDebts = myDebts.toList().sortedByDescending { kotlin.math.abs(it.second) },
            allDebts = allDebts
        )
    }
}
