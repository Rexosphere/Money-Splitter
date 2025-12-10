package com.rexosphere.money_splitter

import androidx.compose.ui.window.ComposeUIViewController
import com.rexosphere.money_splitter.data.database.DatabaseDriverFactory
import com.rexosphere.money_splitter.data.database.DatabaseProvider

fun MainViewController(): androidx.compose.ui.window.ComposeUIViewController {
    // Initialize the SQLite database
    DatabaseProvider.initialize(DatabaseDriverFactory())
    
    return ComposeUIViewController { App() }
}