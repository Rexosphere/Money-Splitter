package com.rexosphere.money_splitter.presentation.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileUiState(
    val userName: String = "John Doe",
    val userEmail: String = "john.doe@example.com",
    val totalExpenses: Int = 0,
    val totalFriends: Int = 0,
    val totalGroups: Int = 0
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
}
