package com.rexosphere.money_splitter.data.database

import com.rexosphere.money_splitter.domain.model.Expense
import com.rexosphere.money_splitter.domain.model.Group
import com.rexosphere.money_splitter.domain.model.Payment
import com.rexosphere.money_splitter.domain.model.User
import kotlinx.datetime.LocalDate

class DatabaseHelper(driverFactory: DatabaseDriverFactory) {
    private val database = MoneySplitterDatabase(driverFactory.createDriver())
    private val queries = database.moneySplitterQueries

    // ============= USER OPERATIONS =============
    
    fun insertUser(user: User) {
        queries.insertUser(user.id, user.name)
    }

    fun getAllUsers(): List<User> {
        return queries.selectAllUsers().executeAsList().map { row ->
            User(id = row.id, name = row.name)
        }
    }

    fun getUserById(id: String): User? {
        return queries.selectUserById(id).executeAsOneOrNull()?.let { row ->
            User(id = row.id, name = row.name)
        }
    }

    // ============= FRIEND OPERATIONS =============

    fun addFriend(user: User) {
        queries.insertUser(user.id, user.name)
        queries.insertFriend(user.id)
    }

    fun getAllFriends(): List<User> {
        return queries.selectAllFriends().executeAsList().map { row ->
            User(id = row.id, name = row.name)
        }
    }

    fun deleteFriend(userId: String) {
        queries.deleteFriend(userId)
    }

    // ============= EXPENSE OPERATIONS =============

    fun insertExpense(expense: Expense) {
        // First ensure all users exist
        insertUser(expense.paidBy)
        expense.participants.keys.forEach { user ->
            insertUser(user)
        }

        // Insert the expense
        queries.insertExpense(
            id = expense.id,
            description = expense.description,
            amount = expense.amount,
            date = expense.date.toString(),
            paid_by_id = expense.paidBy.id
        )

        // Delete existing participants and re-add
        queries.deleteParticipantsByExpenseId(expense.id)
        expense.participants.forEach { (user, share) ->
            queries.insertExpenseParticipant(expense.id, user.id, share)
        }
    }

    fun getAllExpenses(): List<Expense> {
        val expenseRows = queries.selectAllExpenses().executeAsList()
        return expenseRows.mapNotNull { row ->
            val paidBy = getUserById(row.paid_by_id) ?: return@mapNotNull null
            val participants = getExpenseParticipants(row.id)
            
            Expense(
                id = row.id,
                description = row.description,
                amount = row.amount,
                date = LocalDate.parse(row.date),
                paidBy = paidBy,
                participants = participants
            )
        }
    }

    private fun getExpenseParticipants(expenseId: String): Map<User, Double> {
        return queries.selectParticipantsByExpenseId(expenseId).executeAsList()
            .mapNotNull { row ->
                val user = getUserById(row.user_id) ?: return@mapNotNull null
                user to row.share
            }.toMap()
    }

    fun deleteExpense(expenseId: String) {
        queries.deleteParticipantsByExpenseId(expenseId)
        queries.deleteExpense(expenseId)
    }

    // ============= GROUP OPERATIONS =============

    fun insertGroup(group: Group) {
        // Ensure all members exist as users
        group.members.forEach { member ->
            insertUser(member)
        }

        queries.insertGroup(group.id, group.name)
        
        // Delete existing members and re-add
        queries.deleteMembersByGroupId(group.id)
        group.members.forEach { member ->
            queries.insertGroupMember(group.id, member.id)
        }
    }

    fun getAllGroups(): List<Group> {
        val groupRows = queries.selectAllGroups().executeAsList()
        return groupRows.map { row ->
            val members = getGroupMembers(row.id)
            Group(
                id = row.id,
                name = row.name,
                members = members
            )
        }
    }

    private fun getGroupMembers(groupId: String): List<User> {
        return queries.selectMembersByGroupId(groupId).executeAsList()
            .mapNotNull { userId -> getUserById(userId) }
    }

    fun deleteGroup(groupId: String) {
        queries.deleteMembersByGroupId(groupId)
        queries.deleteGroup(groupId)
    }

    // ============= PAYMENT OPERATIONS =============

    fun insertPayment(payment: Payment) {
        // Ensure users exist
        insertUser(payment.from)
        insertUser(payment.to)

        queries.insertPayment(
            id = payment.id,
            from_user_id = payment.from.id,
            to_user_id = payment.to.id,
            amount = payment.amount,
            date = payment.date.toString(),
            is_settled = if (payment.isSettled) 1L else 0L
        )
    }

    fun getAllPayments(): List<Payment> {
        return queries.selectAllPayments().executeAsList().mapNotNull { row ->
            val fromUser = getUserById(row.from_user_id) ?: return@mapNotNull null
            val toUser = getUserById(row.to_user_id) ?: return@mapNotNull null
            
            Payment(
                id = row.id,
                from = fromUser,
                to = toUser,
                amount = row.amount,
                date = LocalDate.parse(row.date),
                isSettled = row.is_settled == 1L
            )
        }
    }

    fun getPendingPayments(): List<Payment> {
        return queries.selectPendingPayments().executeAsList().mapNotNull { row ->
            val fromUser = getUserById(row.from_user_id) ?: return@mapNotNull null
            val toUser = getUserById(row.to_user_id) ?: return@mapNotNull null
            
            Payment(
                id = row.id,
                from = fromUser,
                to = toUser,
                amount = row.amount,
                date = LocalDate.parse(row.date),
                isSettled = false
            )
        }
    }

    fun getSettledPayments(): List<Payment> {
        return queries.selectSettledPayments().executeAsList().mapNotNull { row ->
            val fromUser = getUserById(row.from_user_id) ?: return@mapNotNull null
            val toUser = getUserById(row.to_user_id) ?: return@mapNotNull null
            
            Payment(
                id = row.id,
                from = fromUser,
                to = toUser,
                amount = row.amount,
                date = LocalDate.parse(row.date),
                isSettled = true
            )
        }
    }

    fun settlePayment(paymentId: String) {
        queries.updatePaymentSettled(is_settled = 1L, id = paymentId)
    }

    fun deleteAllPayments() {
        queries.deleteAllPayments()
    }

    // ============= INITIALIZATION =============

    fun initializeWithDummyData(currentUser: User) {
        // Insert current user
        insertUser(currentUser)

        // Check if we already have data
        if (getAllFriends().isNotEmpty()) {
            return // Already initialized
        }

        // Add dummy friends
        val alice = User(id = "alice", name = "Alice")
        val bob = User(id = "bob", name = "Bob")
        val charlie = User(id = "charlie", name = "Charlie")

        addFriend(alice)
        addFriend(bob)
        addFriend(charlie)
    }
}
