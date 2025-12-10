package com.rexosphere.money_splitter.domain.model

import kotlinx.datetime.LocalDate

data class User(
    val id: String,
    val name: String,
    val isAppUser: Boolean = false,        // Is this person using the app?
    val phoneNumber: String? = null,       // For future linking
    val email: String? = null,             // For future linking
    val addedBy: String? = null            // Who added this contact
)

data class Expense(
    val id: String,
    val description: String,
    val amount: Double,
    val date: LocalDate,
    val paidBy: User,
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