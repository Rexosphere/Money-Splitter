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
    val friendDebts: List<Pair<User, Double>> = emptyList()
)

class HomeViewModel(private val repository: ExpenseRepository = ExpenseRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val balances = repository.getNetBalances()
            val currentUserId = repository.currentUser.id

            var totalOwed = 0.0
            var totalOwe = 0.0
            val friendDebts = mutableListOf<Pair<User, Double>>()

            balances.forEach { (user, balance) ->
                if (user.id == currentUserId) {
                    if (balance > 0) totalOwed = balance
                    else totalOwe = kotlin.math.abs(balance)
                } else {
                    // Positive balance means they owe us, negative means we owe them
                    val currentUserBalance = balances[repository.currentUser] ?: 0.0

                    // Calculate what this specific friend owes or is owed
                    if (balance < 0) {
                        // This friend owes someone (possibly us)
                        friendDebts.add(Pair(user, kotlin.math.abs(balance)))
                    } else if (balance > 0) {
                        // This friend is owed by someone (possibly us)
                        friendDebts.add(Pair(user, -balance))
                    }
                }
            }

            // Get simplified debts for more accurate friend-by-friend breakdown
            val simplifiedDebts = repository.getSimplifiedDebts()
            val friendDebtMap = mutableMapOf<User, Double>()

            simplifiedDebts.forEach { (debtor, creditorAmount) ->
                val (creditor, amount) = creditorAmount

                when {
                    debtor.id == currentUserId -> {
                        // We owe someone
                        friendDebtMap[creditor] = -(friendDebtMap[creditor] ?: 0.0) - amount
                    }
                    creditor.id == currentUserId -> {
                        // Someone owes us
                        friendDebtMap[debtor] = (friendDebtMap[debtor] ?: 0.0) + amount
                    }
                }
            }

            _uiState.value = HomeUiState(
                netBalanceOwed = totalOwed,
                netBalanceOwe = totalOwe,
                friendDebts = friendDebtMap.toList().sortedByDescending { kotlin.math.abs(it.second) }
            )
        }
    }
}
