package com.rexosphere.money_splitter.presentation.friends

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    modifier: Modifier = Modifier,
    viewModel: FriendsViewModel = viewModel { FriendsViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Friend")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(uiState.friends) { friend ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(text = friend.name, modifier = Modifier.padding(16.dp))
                }
            }
        }

        if (uiState.showAddDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideAddDialog() },
                title = { Text("Add Friend") },
                text = {
                    OutlinedTextField(
                        value = uiState.newFriendName,
                        onValueChange = { viewModel.updateFriendName(it) },
                        label = { Text("Friend Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = { viewModel.addFriend() }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideAddDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
