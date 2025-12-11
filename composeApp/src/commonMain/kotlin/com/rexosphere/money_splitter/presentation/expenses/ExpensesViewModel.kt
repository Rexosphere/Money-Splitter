package com.rexosphere.money_splitter.presentation.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rexosphere.money_splitter.data.repository.ExpenseRepository
import com.rexosphere.money_splitter.domain.model.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExpensesUiState(
    val expenses: List<Expense> = emptyList(),
    val isAddExpenseVisible: Boolean = false,
    val editingExpense: Expense? = null // Track expense being edited
)

class ExpensesViewModel(private val repository: ExpenseRepository = ExpenseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    init {
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            repository.expenses.collect { expenses ->
                _uiState.value = _uiState.value.copy(
                    expenses = expenses.sortedByDescending { it.date }
                )
            }
        }
    }

    fun showAddExpenseDialog() {
        _uiState.value = _uiState.value.copy(isAddExpenseVisible = true, editingExpense = null)
    }

    fun hideAddExpenseDialog() {
        _uiState.value = _uiState.value.copy(isAddExpenseVisible = false, editingExpense = null)
    }

    fun startEditing(expense: Expense) {
        _uiState.value = _uiState.value.copy(isAddExpenseVisible = true, editingExpense = expense)
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
        }
    }
}
