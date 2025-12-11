package com.rexosphere.money_splitter.presentation.add_expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rexosphere.money_splitter.ui.components.*

@Composable
fun AddExpenseScreen(
    modifier: Modifier = Modifier,
    viewModel: AddExpenseViewModel = viewModel { AddExpenseViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = androidx.compose.material3.SnackbarHostState()

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            snackbarHostState.showSnackbar("Expense saved successfully! 🎉")
            viewModel.resetForm()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Header
            item {
                Text(
                    text = "Add Expense",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Amount Input Card
            item {
                PremiumCard {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.amount,
                        onValueChange = { viewModel.updateAmount(it) },
                        placeholder = { Text("0.00") },
                        prefix = { Text("Rs. ", style = MaterialTheme.typography.headlineSmall) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // Description Input
            item {
                PremiumCard {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        placeholder = { Text("What's this for?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // Split With Section
            item {
                SectionHeader(title = "Split With")
            }

            // Friend Selection
            items(uiState.friends) { friend ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = uiState.selectedFriends.contains(friend.id),
                                onCheckedChange = { viewModel.toggleFriend(friend.id) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            UserAvatar(name = friend.name, size = 40)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = friend.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (uiState.selectedFriends.contains(friend.id)) {
                            OutlinedTextField(
                                value = uiState.friendShares[friend.id] ?: "",
                                onValueChange = { viewModel.updateFriendShare(friend.id, it) },
                                placeholder = { Text("0.00") },
                                prefix = { Text("Rs. ") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(120.dp),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Current User Share
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = uiState.includeSelf,
                                onCheckedChange = { viewModel.toggleIncludeSelf() }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            UserAvatar(name = "Me", size = 40)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "You",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        if (uiState.includeSelf) {
                            Text(
                                text = "Rs. ${uiState.friendShares["current_user"] ?: "0.00"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Save Button
            item {
                PrimaryGradientButton(
                    text = if (uiState.isSaving) "Saving..." else "Save Expense",
                    onClick = { viewModel.saveExpense() },
                    enabled = !uiState.isSaving && uiState.amount.toDoubleOrNull() != null
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
