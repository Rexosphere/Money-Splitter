package com.rexosphere.money_splitter.domain.model

import kotlinx.datetime.LocalDate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Dining
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class User(
    val id: String,
    val name: String,
    val isAppUser: Boolean = false,        // Is this person using the app?
    val phoneNumber: String? = null,       // For future linking
    val email: String? = null,             // For future linking
    val addedBy: String? = null            // Who added this contact
)


enum class ExpenseCategory(
    val displayName: String,
    val icon: ImageVector,
    val color: Color
) {
    FOOD("Food", Icons.Default.Dining, Color(0xFFFF6D00)),
    TRANSPORT("Transport", Icons.Default.Commute, Color(0xFF2962FF)),
    HOME("Rent/Home", Icons.Default.Home, Color(0xFF00C853)),
    GROCERIES("Groceries", Icons.Default.LocalGroceryStore, Color(0xFFAA00FF)),
    ENTERTAINMENT("Entertainment", Icons.Default.Movie, Color(0xFFD500F9)),
    SHOPPING("Shopping", Icons.Default.ShoppingBag, Color(0xFFC51162)),
    OTHER("Other", Icons.Default.Category, Color(0xFF616161));
}

data class Expense(
    val id: String,
    val description: String,
    val amount: Double,
    val date: LocalDate,
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val paidBy: Map<User, Double>, // Multiple payers with their paid amounts
    val participants: Map<User, Double> // User and their share of the expense
)

data class Group(
    val id: String,
    val name: String,
    val members: List<User>
)

data class Payment(
    val id: String,
    val from: User,
    val to: User,
    val amount: Double,
    val date: LocalDate,
    val isSettled: Boolean
)