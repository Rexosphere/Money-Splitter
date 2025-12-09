package com.rexosphere.money_splitter.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back press */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(it).padding(16.dp).fillMaxSize()
        ) {
            Text("Currency: USD") // Dummy data
            Spacer(modifier = Modifier.height(16.dp))
            Text("Notifications: On") // Dummy data
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* Handle backup */ }) {
                Text("Backup Data")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { /* Handle save */ }) {
                Text("Save")
            }
        }
    }
}