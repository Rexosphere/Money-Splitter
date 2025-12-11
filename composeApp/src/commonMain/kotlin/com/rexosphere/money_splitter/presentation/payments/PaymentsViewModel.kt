package com.rexosphere.money_splitter.presentation.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rexosphere.money_splitter.data.repository.ExpenseRepository
import com.rexosphere.money_splitter.domain.model.Payment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PaymentsUiState(
    val pendingPayments: List<Payment> = emptyList(),
    val settledPayments: List<Payment> = emptyList()
)

class PaymentsViewModel(private val repository: ExpenseRepository = ExpenseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PaymentsUiState())
    val uiState: StateFlow<PaymentsUiState> = _uiState.asStateFlow()

    init {
        loadPayments()
    }

    private fun loadPayments() {
        viewModelScope.launch {
            repository.payments.collect { payments ->
                _uiState.value = PaymentsUiState(
                    pendingPayments = payments.filter { !it.isSettled },
                    settledPayments = payments.filter { it.isSettled }
                )
            }
        }
    }

    fun settlePayment(paymentId: String) {
        viewModelScope.launch {
            repository.settlePayment(paymentId)
        }
    }
}
