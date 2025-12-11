package com.rexosphere.money_splitter.presentation.friends

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rexosphere.money_splitter.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    modifier: Modifier = Modifier,
    viewModel: FriendsViewModel = viewModel { FriendsViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Friend")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Text(
                text = "Friends",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.friends) { friend ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.85f),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Action buttons in top-right corner
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                            ) {
                                // Edit button
                                IconButton(
                                    onClick = { viewModel.showEditDialog(friend) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                // Delete button
                                IconButton(
                                    onClick = { viewModel.showDeleteDialog(friend) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            
                            // Main content
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                UserAvatar(name = friend.name, size = 56)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = friend.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                AnimatedBadge(
                                    text = if (friend.isAppUser) "User" else "Contact",
                                    icon = if (friend.isAppUser) "👤" else "📝",
                                    isAppUser = friend.isAppUser
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Friend Dialog
        if (uiState.showAddDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideAddDialog() },
                title = {
                    Text(
                        "Add Friend",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.newFriendName,
                            onValueChange = { viewModel.updateFriendName(it) },
                            label = { Text("Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = uiState.newFriendPhone,
                            onValueChange = { viewModel.updateFriendPhone(it) },
                            label = { Text("Phone (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = uiState.newFriendEmail,
                            onValueChange = { viewModel.updateFriendEmail(it) },
                            label = { Text("Email (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = uiState.newFriendIsAppUser,
                                onCheckedChange = { viewModel.toggleIsAppUser(it) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "This person uses the app",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.addFriend() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideAddDialog() }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
        
        // Edit Friend Dialog
        if (uiState.showEditDialog && uiState.friendToEdit != null) {
            AlertDialog(
                onDismissRequest = { viewModel.hideEditDialog() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(
                        "Edit Friend",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.editFriendName,
                            onValueChange = { viewModel.updateEditFriendName(it) },
                            label = { Text("Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = uiState.editFriendPhone,
                            onValueChange = { viewModel.updateEditFriendPhone(it) },
                            label = { Text("Phone (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = uiState.editFriendEmail,
                            onValueChange = { viewModel.updateEditFriendEmail(it) },
                            label = { Text("Email (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = uiState.editFriendIsAppUser,
                                onCheckedChange = { viewModel.toggleEditIsAppUser(it) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "This person uses the app",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmEditFriend() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideEditDialog() }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
        
        // Delete Confirmation Dialog
        if (uiState.showDeleteDialog && uiState.friendToDelete != null) {
            AlertDialog(
                onDismissRequest = { viewModel.hideDeleteDialog() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = {
                    Text(
                        "Delete Friend?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "Are you sure you want to delete ${uiState.friendToDelete?.name}? This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmDeleteFriend() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}
