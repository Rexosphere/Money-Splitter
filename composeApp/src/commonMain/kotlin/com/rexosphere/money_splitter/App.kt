package com.rexosphere.money_splitter

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.rexosphere.money_splitter.presentation.expenses.ExpensesScreen
import com.rexosphere.money_splitter.presentation.friends.FriendsScreen
import com.rexosphere.money_splitter.presentation.groups.GroupsScreen
import com.rexosphere.money_splitter.presentation.home.HomeScreen
import com.rexosphere.money_splitter.presentation.payments.PaymentsScreen
import com.rexosphere.money_splitter.presentation.profile.ProfileScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

// --- Screen Definitions ---
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Expenses : Screen("expenses", "Expenses", Icons.Filled.Payment) // Or use a different icon like Receipt or List
    object Friends : Screen("friends", "Friends", Icons.Filled.Person)
    object Groups : Screen("groups", "Groups", Icons.Filled.Group)
    object Payments : Screen("payments", "Payments", Icons.Filled.Payment)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Expenses,
    Screen.Friends,
    Screen.Groups,
    Screen.Payments
)

// --- App Composable ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneySplitterApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    Scaffold(
        topBar = {
            if (currentScreen != Screen.Profile) {
                TopAppBar(
                    title = { Text(currentScreen.title) },
                    actions = {
                        IconButton(onClick = { currentScreen = Screen.Profile }) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Profile"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentScreen != Screen.Profile) {
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
        }
    ) { innerPadding ->
        when (currentScreen) {
            Screen.Home -> HomeScreen(modifier = Modifier.padding(innerPadding))
            Screen.Expenses -> ExpensesScreen(modifier = Modifier.padding(innerPadding))
            Screen.Friends -> FriendsScreen(modifier = Modifier.padding(innerPadding))
            Screen.Groups -> GroupsScreen(modifier = Modifier.padding(innerPadding))
            Screen.Payments -> PaymentsScreen(modifier = Modifier.padding(innerPadding))
            Screen.Profile -> ProfileScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigateBack = { currentScreen = Screen.Home }
            )
        }
    }
}

// --- Preview ---
@Preview(showBackground = true)
@Composable
fun AppPreview() {
    com.rexosphere.money_splitter.ui.theme.MoneySplitterTheme {
        MoneySplitterApp()
    }
}
