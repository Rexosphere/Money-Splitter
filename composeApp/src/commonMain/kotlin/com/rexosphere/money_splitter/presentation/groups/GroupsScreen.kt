package com.rexosphere.money_splitter.presentation.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun GroupsScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupsViewModel = viewModel { GroupsViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Group")
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
                text = "Groups",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.groups) { group ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Action buttons in top-right corner
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                            ) {
                                // Edit button
                                IconButton(
                                    onClick = { viewModel.showEditDialog(group) },
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
                                    onClick = { viewModel.showDeleteDialog(group) },
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
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = group.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Members Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy((-8).dp)
                                ) {
                                    group.members.take(5).forEach { member ->
                                        UserAvatar(name = member.name, size = 36)
                                    }
                                    if (group.members.size > 5) {
                                        Box(
                                            modifier = Modifier.size(36.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "+${group.members.size - 5}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${group.members.size} members",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // Create Group Dialog
        if (uiState.showCreateDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideCreateDialog() },
                title = {
                    Text(
                        "Create Group",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.newGroupName,
                            onValueChange = { viewModel.updateGroupName(it) },
                            label = { Text("Group Name") },
                            placeholder = { Text("e.g., Roommates") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Text(
                            "Select Members:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Current user option
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = uiState.includeSelf,
                                    onCheckedChange = { viewModel.toggleIncludeSelf() }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                UserAvatar(name = "Me", size = 32)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "You",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            uiState.friends.forEach { friend ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = uiState.selectedMembers.contains(friend.id),
                                        onCheckedChange = { viewModel.toggleMember(friend.id) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    UserAvatar(name = friend.name, size = 32)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = friend.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.createGroup() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideCreateDialog() }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
        
        // Edit Group Dialog
        if (uiState.showEditDialog && uiState.groupToEdit != null) {
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
                        "Edit Group",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.editGroupName,
                            onValueChange = { viewModel.updateEditGroupName(it) },
                            label = { Text("Group Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Text(
                            "Select Members:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Current user option
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = uiState.editIncludeSelf,
                                    onCheckedChange = { viewModel.toggleEditIncludeSelf() }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                UserAvatar(name = "Me", size = 32)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "You",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            uiState.friends.forEach { friend ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = uiState.editSelectedMembers.contains(friend.id),
                                        onCheckedChange = { viewModel.toggleEditMember(friend.id) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    UserAvatar(name = friend.name, size = 32)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = friend.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmEditGroup() },
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
        if (uiState.showDeleteDialog && uiState.groupToDelete != null) {
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
                        "Delete Group?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "Are you sure you want to delete \"${uiState.groupToDelete?.name}\"? This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmDeleteGroup() },
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
