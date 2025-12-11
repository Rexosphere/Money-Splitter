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

object ExpenseRepository {
    private val databaseHelper: DatabaseHelper? get() = DatabaseProvider.databaseHelper
    
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: Flow<List<Expense>> = _expenses.asStateFlow()

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: Flow<List<Payment>> = _payments.asStateFlow()

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: Flow<List<Group>> = _groups.asStateFlow()

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: Flow<List<User>> = _friends.asStateFlow()

    // Current user (hardcoded for now)
    val currentUser = User(
        id = "current_user",
        name = "Me",
        isAppUser = true  // Current user is always an app user
    )

    // Flag to track if we're using database
    private val useDatabase: Boolean get() = databaseHelper != null

    init {
        if (useDatabase) {
            // Initialize with database data
            databaseHelper?.initialize(currentUser)
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
        // Start with empty data - users can add their own friends and expenses
        _friends.value = emptyList()
        _expenses.value = emptyList()
        _payments.value = emptyList()
        _groups.value = emptyList()
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

    // Settle all payments between two specific users
    fun settlePaymentsByUserPair(debtorId: String, creditorId: String) {
        if (useDatabase) {
            // Find all pending payments from debtor to creditor
            val paymentsToSettle = databaseHelper?.getAllPayments()?.filter { payment ->
                !payment.isSettled &&
                payment.from.id == debtorId &&
                payment.to.id == creditorId
            } ?: emptyList()
            
            paymentsToSettle.forEach { payment ->
                databaseHelper?.settlePayment(payment.id)
            }
            refreshFromDatabase()
        } else {
            _payments.value = _payments.value.map { payment ->
                if (!payment.isSettled && 
                    payment.from.id == debtorId && 
                    payment.to.id == creditorId) {
                    payment.copy(isSettled = true)
                } else {
                    payment
                }
            }
        }
    }


    @OptIn(ExperimentalUuidApi::class)
    fun addFriend(
        name: String,
        phoneNumber: String? = null,
        email: String? = null,
        isAppUser: Boolean = false
    ) {
        val newFriend = User(
            id = Uuid.random().toString(),
            name = name,
            isAppUser = isAppUser,
            phoneNumber = phoneNumber,
            email = email,
            addedBy = currentUser.id
        )
        if (useDatabase) {
            databaseHelper?.addFriend(newFriend)
            refreshFromDatabase()
        } else {
            _friends.value = _friends.value + newFriend
        }
    }
    
    // Mark an existing contact as app user
    fun markAsAppUser(userId: String, phoneNumber: String? = null, email: String? = null) {
        if (useDatabase) {
            val user = databaseHelper?.getUserById(userId)
            if (user != null) {
                val updatedUser = user.copy(
                    isAppUser = true,
                    phoneNumber = phoneNumber ?: user.phoneNumber,
                    email = email ?: user.email
                )
                databaseHelper?.insertUser(updatedUser)
                refreshFromDatabase()
            }
        } else {
            _friends.value = _friends.value.map { friend ->
                if (friend.id == userId) {
                    friend.copy(
                        isAppUser = true,
                        phoneNumber = phoneNumber ?: friend.phoneNumber,
                        email = email ?: friend.email
                    )
                } else {
                    friend
                }
            }
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
    
    // Delete a friend
    fun deleteFriend(friendId: String) {
        if (useDatabase) {
            databaseHelper?.deleteFriend(friendId)
            refreshFromDatabase()
        } else {
            _friends.value = _friends.value.filter { it.id != friendId }
        }
    }
    
    // Delete a group
    fun deleteGroup(groupId: String) {
        if (useDatabase) {
            databaseHelper?.deleteGroup(groupId)
            refreshFromDatabase()
        } else {
            _groups.value = _groups.value.filter { it.id != groupId }
        }
    }
    
    // Update a friend
    fun updateFriend(
        friendId: String,
        name: String,
        phoneNumber: String? = null,
        email: String? = null,
        isAppUser: Boolean = false
    ) {
        if (useDatabase) {
            val existingUser = databaseHelper?.getUserById(friendId)
            if (existingUser != null) {
                val updatedUser = existingUser.copy(
                    name = name,
                    phoneNumber = phoneNumber,
                    email = email,
                    isAppUser = isAppUser
                )
                databaseHelper?.insertUser(updatedUser)
                refreshFromDatabase()
            }
        } else {
            _friends.value = _friends.value.map { friend ->
                if (friend.id == friendId) {
                    friend.copy(
                        name = name,
                        phoneNumber = phoneNumber,
                        email = email,
                        isAppUser = isAppUser
                    )
                } else {
                    friend
                }
            }
        }
    }
    
    // Update a group
    fun updateGroup(groupId: String, name: String, members: List<User>) {
        if (useDatabase) {
            val updatedGroup = Group(id = groupId, name = name, members = members)
            databaseHelper?.insertGroup(updatedGroup)
            refreshFromDatabase()
        } else {
            _groups.value = _groups.value.map { group ->
                if (group.id == groupId) {
                    group.copy(name = name, members = members)
                } else {
                    group
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun updatePaymentsFromExpenses() {
        val newPayments = mutableListOf<Payment>()
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        // Convert each expense into individual payments
        _expenses.value.forEach { expense ->
            // For each participant, calculate what they owe
            expense.participants.forEach { (participant, share) ->
                // Calculate how much this participant paid
                val participantPaid = expense.paidBy[participant] ?: 0.0
                val participantOwes = share - participantPaid
                
                if (participantOwes > 0.01) { // They owe money
                    // They need to pay each payer proportionally
                    expense.paidBy.forEach { (payer, paidAmount) ->
                        if (payer.id != participant.id) {
                            val payerShare = paidAmount / expense.amount
                            val amountToPay = participantOwes * payerShare
                            
                            if (amountToPay > 0.01) {
                                newPayments.add(
                                    Payment(
                                        id = Uuid.random().toString(),
                                        from = participant,
                                        to = payer,
                                        amount = amountToPay,
                                        date = now,
                                        isSettled = false
                                    )
                                )
                            }
                        }
                    }
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

    // Get ALL debts between user pairs (not simplified)
    // Returns: List of (debtor, creditor, amount)
    fun getAllDebts(): List<Triple<User, User, Double>> {
        val debtMap = mutableMapOf<Pair<String, String>, Triple<User, User, Double>>()

        _payments.value.filter { !it.isSettled }.forEach { payment ->
            val key = Pair(payment.from.id, payment.to.id)
            val reverseKey = Pair(payment.to.id, payment.from.id)

            // Check if there's already a debt in opposite direction
            if (debtMap.containsKey(reverseKey)) {
                val existing = debtMap[reverseKey]!!
                val newAmount = existing.third - payment.amount
                if (newAmount > 0.01) {
                    debtMap[reverseKey] = Triple(existing.first, existing.second, newAmount)
                } else if (newAmount < -0.01) {
                    debtMap.remove(reverseKey)
                    debtMap[key] = Triple(payment.from, payment.to, kotlin.math.abs(newAmount))
                } else {
                    debtMap.remove(reverseKey)
                }
            } else {
                val existing = debtMap[key]
                if (existing != null) {
                    debtMap[key] = Triple(existing.first, existing.second, existing.third + payment.amount)
                } else {
                    debtMap[key] = Triple(payment.from, payment.to, payment.amount)
                }
            }
        }

        return debtMap.values.toList().sortedByDescending { it.third }
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
