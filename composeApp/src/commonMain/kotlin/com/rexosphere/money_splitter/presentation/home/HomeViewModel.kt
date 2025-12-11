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
        val currentUserId = repository.currentUser.id
        
        // Get the optimized payments (created by minimum cash flow algorithm)
        val payments = repository.getCurrentPayments().filter { !it.isSettled }
        
        // Calculate total owed to me and total I owe
        var totalOwed = 0.0
        var totalOwe = 0.0
        
        payments.forEach { payment ->
            if (payment.to.id == currentUserId) {
                totalOwed += payment.amount
            } else if (payment.from.id == currentUserId) {
                totalOwe += payment.amount
            }
        }
        
        // Build allDebts list from payments (already optimized!)
        val allDebts = payments.map { payment ->
            Triple(payment.from, payment.to, payment.amount)
        }.sortedByDescending { it.third }
        
        // Build myDebts for the summary cards
        val myDebts = mutableMapOf<User, Double>()
        payments.forEach { payment ->
            when {
                payment.from.id == currentUserId -> {
                    // I owe someone
                    myDebts[payment.to] = -(myDebts[payment.to] ?: 0.0) - payment.amount
                }
                payment.to.id == currentUserId -> {
                    // Someone owes me
                    myDebts[payment.from] = (myDebts[payment.from] ?: 0.0) + payment.amount
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
