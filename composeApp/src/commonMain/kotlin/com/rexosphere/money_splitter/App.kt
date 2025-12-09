package com.rexosphere.money_splitter

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.rexosphere.money_splitter.presentation.add_expense.AddExpenseScreen
import com.rexosphere.money_splitter.presentation.groups.GroupsScreen
import com.rexosphere.money_splitter.presentation.home.HomeScreen
import com.rexosphere.money_splitter.presentation.payments.PaymentsScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

// --- Screen Definitions ---
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Add : Screen("add", "Add", Icons.Filled.Add)
    object Groups : Screen("groups", "Groups", Icons.Filled.Group)
    object Payments : Screen("payments", "Payments", Icons.Filled.Payment)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Add,
    Screen.Groups,
    Screen.Payments
)

// --- App Composable ---
@Composable
fun MoneySplitterApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            Screen.Home -> HomeScreen(modifier = Modifier.padding(innerPadding))
            Screen.Add -> AddExpenseScreen(modifier = Modifier.padding(innerPadding))
            Screen.Groups -> GroupsScreen(modifier = Modifier.padding(innerPadding))
            Screen.Payments -> PaymentsScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}

// --- Preview ---
@Preview(showBackground = true)
@Composable
fun AppPreview() {
    MaterialTheme {
        MoneySplitterApp()
    }
}
