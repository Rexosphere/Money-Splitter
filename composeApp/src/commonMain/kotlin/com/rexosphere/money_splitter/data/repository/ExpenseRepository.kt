package com.rexosphere.money_splitter.data.repository

import com.rexosphere.money_splitter.data.database.DatabaseHelper
import com.rexosphere.money_splitter.data.database.DatabaseProvider
import com.rexosphere.money_splitter.domain.model.Expense
import com.rexosphere.money_splitter.domain.model.Group
import com.rexosphere.money_splitter.domain.model.Payment
import com.rexosphere.money_splitter.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ExpenseRepository(private val databaseHelper: DatabaseHelper? = DatabaseProvider.databaseHelper) {
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: Flow<List<Expense>> = _expenses.asStateFlow()

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: Flow<List<Payment>> = _payments.asStateFlow()

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: Flow<List<Group>> = _groups.asStateFlow()

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: Flow<List<User>> = _friends.asStateFlow()

    // Current user (hardcoded for now)
    val currentUser = User(id = "current_user", name = "Me")

    // Flag to track if we're using database
    private val useDatabase: Boolean = databaseHelper != null

    init {
        if (useDatabase) {
            // Initialize with database data
            databaseHelper?.initializeWithDummyData(currentUser)
            loadFromDatabase()
        } else {
            // Fallback to in-memory dummy data
            addDummyData()
        }
    }

    private fun loadFromDatabase() {
        databaseHelper?.let { db ->
            _friends.value = db.getAllFriends()
            _expenses.value = db.getAllExpenses()
            _payments.value = db.getAllPayments()
            _groups.value = db.getAllGroups()
        }
    }

    private fun refreshFromDatabase() {
        if (useDatabase) {
            loadFromDatabase()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun addDummyData() {
        val alice = User(id = "alice", name = "Alice")
        val bob = User(id = "bob", name = "Bob")
        val charlie = User(id = "charlie", name = "Charlie")

        _friends.value = listOf(alice, bob, charlie)

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        _expenses.value = listOf(
            Expense(
                id = Uuid.random().toString(),
                description = "Dinner",
                amount = 150.0,
                date = now,
                paidBy = currentUser,
                participants = mapOf(
                    currentUser to 50.0,
                    alice to 50.0,
                    bob to 50.0
                )
            ),
            Expense(
                id = Uuid.random().toString(),
                description = "Movie tickets",
                amount = 60.0,
                date = now,
                paidBy = alice,
                participants = mapOf(
                    currentUser to 20.0,
                    alice to 20.0,
                    charlie to 20.0
                )
            )
        )

        // Convert expenses to payments
        updatePaymentsFromExpenses()
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addExpense(expense: Expense) {
        if (useDatabase) {
            databaseHelper?.insertExpense(expense)
            refreshFromDatabase()
        } else {
            _expenses.value = _expenses.value + expense
        }
        updatePaymentsFromExpenses()
    }

    fun settlePayment(paymentId: String) {
        if (useDatabase) {
            databaseHelper?.settlePayment(paymentId)
            refreshFromDatabase()
        } else {
            _payments.value = _payments.value.map { payment ->
                if (payment.id == paymentId) {
                    payment.copy(isSettled = true)
                } else {
                    payment
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addFriend(name: String) {
        val newFriend = User(id = Uuid.random().toString(), name = name)
        if (useDatabase) {
            databaseHelper?.addFriend(newFriend)
            refreshFromDatabase()
        } else {
            _friends.value = _friends.value + newFriend
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addGroup(name: String, members: List<User>) {
        val newGroup = Group(
            id = Uuid.random().toString(),
            name = name,
            members = members
        )
        if (useDatabase) {
            databaseHelper?.insertGroup(newGroup)
            refreshFromDatabase()
        } else {
            _groups.value = _groups.value + newGroup
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun updatePaymentsFromExpenses() {
        val newPayments = mutableListOf<Payment>()
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        // Convert each expense into individual payments
        _expenses.value.forEach { expense ->
            expense.participants.forEach { (participant, share) ->
                if (participant.id != expense.paidBy.id && share > 0) {
                    newPayments.add(
                        Payment(
                            id = Uuid.random().toString(),
                            from = participant,
                            to = expense.paidBy,
                            amount = share,
                            date = now,
                            isSettled = false
                        )
                    )
                }
            }
        }

        // Preserve settled payments
        val settledPayments = _payments.value.filter { it.isSettled }
        _payments.value = newPayments + settledPayments

        // If using database, persist the new payments
        if (useDatabase) {
            databaseHelper?.deleteAllPayments()
            _payments.value.forEach { payment ->
                databaseHelper?.insertPayment(payment)
            }
        }
    }

    // Calculate net balances between users
    fun getNetBalances(): Map<User, Double> {
        val balances = mutableMapOf<User, Double>()

        _payments.value.filter { !it.isSettled }.forEach { payment ->
            // The person who owes money has negative balance
            balances[payment.from] = (balances[payment.from] ?: 0.0) - payment.amount
            // The person who is owed money has positive balance
            balances[payment.to] = (balances[payment.to] ?: 0.0) + payment.amount
        }

        return balances
    }

    // Get simplified debts (reduce transactions between users)
    fun getSimplifiedDebts(): List<Pair<User, Pair<User, Double>>> {
        val balances = getNetBalances().toMutableMap()
        val transactions = mutableListOf<Pair<User, Pair<User, Double>>>()

        while (balances.values.any { kotlin.math.abs(it) > 0.01 }) {
            // Find max creditor (person who is owed the most)
            val maxCreditor = balances.maxByOrNull { it.value }?.key ?: break
            val maxCredit = balances[maxCreditor] ?: 0.0

            if (maxCredit < 0.01) break

            // Find max debtor (person who owes the most)
            val maxDebtor = balances.minByOrNull { it.value }?.key ?: break
            val maxDebt = kotlin.math.abs(balances[maxDebtor] ?: 0.0)

            if (maxDebt < 0.01) break

            // Settle between them
            val amount = minOf(maxCredit, maxDebt)
            transactions.add(Pair(maxDebtor, Pair(maxCreditor, amount)))

            balances[maxCreditor] = maxCredit - amount
            balances[maxDebtor] = (balances[maxDebtor] ?: 0.0) + amount
        }

        return transactions
    }
}
