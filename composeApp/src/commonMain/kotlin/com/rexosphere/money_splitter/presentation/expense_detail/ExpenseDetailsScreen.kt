package com.rexosphere.money_splitter.presentation.expense_detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
fun ExpenseDetailsScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Details") },
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
            Text("Expense: Dinner") // Dummy data
            Text("Amount: $100.00") // Dummy data
            Text("Date: 2024-07-27") // Dummy data

            Spacer(modifier = Modifier.height(16.dp))

            Text("Participants:")
            Text("- You: $50.00") // Dummy data
            Text("- Alice: $25.00") // Dummy data
            Text("- Bob: $25.00") // Dummy data

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { /* Handle edit */ }, modifier = Modifier.weight(1f)) {
                    Text("Edit")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* Handle delete */ }, modifier = Modifier.weight(1f)) {
                    Text("Delete")
                }
            }
        }
    }
}