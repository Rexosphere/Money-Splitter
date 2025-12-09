package com.rexosphere.money_splitter.presentation.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupsViewModel = viewModel { GroupsViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Groups") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showCreateDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Create Group")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(uiState.groups) { group ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = group.name)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Members: ${group.members.joinToString { it.name }}",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        if (uiState.showCreateDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideCreateDialog() },
                title = { Text("Create Group") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = uiState.newGroupName,
                            onValueChange = { viewModel.updateGroupName(it) },
                            label = { Text("Group Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Select Members:")
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.friends.forEach { friend ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = uiState.selectedMembers.contains(friend.id),
                                    onCheckedChange = { viewModel.toggleMember(friend.id) }
                                )
                                Text(friend.name)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.createGroup() }) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideCreateDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
