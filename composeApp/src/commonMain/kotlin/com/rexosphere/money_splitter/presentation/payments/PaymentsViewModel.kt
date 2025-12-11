package com.rexosphere.money_splitter.presentation.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rexosphere.money_splitter.data.repository.ExpenseRepository
import com.rexosphere.money_splitter.domain.model.Payment
import com.rexosphere.money_splitter.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SimplifiedDebt(
    val debtor: User,
    val creditor: User,
    val amount: Double
)

data class PaymentsUiState(
    val simplifiedDebts: List<SimplifiedDebt> = emptyList(),
    val settledPayments: List<Payment> = emptyList(),
    val showRecordPaymentDialog: Boolean = false,
    val selectedDebt: SimplifiedDebt? = null
)

class PaymentsViewModel(val repository: ExpenseRepository = ExpenseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PaymentsUiState())
    val uiState: StateFlow<PaymentsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.payments.collect { payments ->
                // Get ALL debts for "Who to Pay" section (not simplified)
                val allDebts = repository.getAllDebts().map { (debtor, creditor, amount) ->
                    SimplifiedDebt(debtor, creditor, amount)
                }
                
                // Get settled payments for history
                val settledPayments = payments.filter { it.isSettled }
                
                _uiState.value = _uiState.value.copy(
                    simplifiedDebts = allDebts,
                    settledPayments = settledPayments
                )
            }
        }
    }

    fun showRecordPaymentDialog(debt: SimplifiedDebt) {
        _uiState.value = _uiState.value.copy(
            showRecordPaymentDialog = true,
            selectedDebt = debt
        )
    }

    fun hideRecordPaymentDialog() {
        _uiState.value = _uiState.value.copy(
            showRecordPaymentDialog = false,
            selectedDebt = null
        )
    }

    fun recordPayment(debt: SimplifiedDebt) {
        viewModelScope.launch {
            // Settle all payments from debtor to creditor using the new repository method
            repository.settlePaymentsByUserPair(debt.debtor.id, debt.creditor.id)
            hideRecordPaymentDialog()
        }
    }
}
