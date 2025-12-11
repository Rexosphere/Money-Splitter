package com.rexosphere.money_splitter

import androidx.compose.ui.window.ComposeUIViewController
import com.rexosphere.money_splitter.data.database.DatabaseDriverFactory
import com.rexosphere.money_splitter.data.database.DatabaseProvider
import platform.uikit.UIViewController

fun MainViewController(): UIViewController {
    // Initialize database for iOS
    DatabaseProvider.initialize(DatabaseDriverFactory())
    
    return ComposeUIViewController {
        com.rexosphere.money_splitter.ui.theme.MoneySplitterTheme {
            MoneySplitterApp()
        }
    }
}