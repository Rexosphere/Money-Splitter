package com.rexosphere.money_splitter.expense_calculator

import kotlinx.datetime.LocalDate
import kotlin.math.abs

/**
 * Represents a person in the expense splitting system
 */
data class Person(
    val id: String,
    val name: String
)

/**
 * Represents a contribution made by a person towards an expense
 */
data class Contribution(
    val person: Person,
    val amountPaid: Double
)

/**
 * Represents how much a person consumed/owes from an expense
 */
data class Share(
    val person: Person,
    val amountOwed: Double
)

/**
 * Represents a complete expense with who paid and who owes
 */
data class SharedExpense(
    val id: String,
    val description: String,
    val totalAmount: Double,
    val date: LocalDate,
    val contributions: List<Contribution>,  // Who paid
    val shares: List<Share>                 // Who owes
)

/**
 * Represents a simplified debt between two people
 */
data class Debt(
    val from: Person,  // Who owes
    val to: Person,    // Who is owed
    val amount: Double
)

/**
 * Main calculator for expense splitting
 */
class ExpenseSplitCalculator {
    
    /**
     * Calculate net balances for all people
     * Positive = money owed to them
     * Negative = money they owe
     */
    fun calculateNetBalances(expense: SharedExpense): Map<Person, Double> {
        val balances = mutableMapOf<Person, Double>()
        
        // Add contributions (what they paid)
        expense.contributions.forEach { contribution ->
            balances[contribution.person] = 
                (balances[contribution.person] ?: 0.0) + contribution.amountPaid
        }
        
        // Subtract shares (what they owe)
        expense.shares.forEach { share ->
            balances[share.person] = 
                (balances[share.person] ?: 0.0) - share.amountOwed
        }
        
        return balances
    }
    
    /**
     * Simplify debts to minimize number of transactions
     * Uses greedy algorithm: match largest creditor with largest debtor
     */
    fun simplifyDebts(balances: Map<Person, Double>): List<Debt> {
        val debts = mutableListOf<Debt>()
        val mutableBalances = balances.toMutableMap()
        
        while (mutableBalances.values.any { abs(it) > 0.01 }) {
            // Find person who is owed the most
            val maxCreditor = mutableBalances.maxByOrNull { it.value }?.key ?: break
            val maxCredit = mutableBalances[maxCreditor] ?: 0.0
            
            if (maxCredit < 0.01) break
            
            // Find person who owes the most
            val maxDebtor = mutableBalances.minByOrNull { it.value }?.key ?: break
            val maxDebt = abs(mutableBalances[maxDebtor] ?: 0.0)
            
            if (maxDebt < 0.01) break
            
            // Settle between them
            val settleAmount = minOf(maxCredit, maxDebt)
            debts.add(Debt(from = maxDebtor, to = maxCreditor, amount = settleAmount))
            
            // Update balances
            mutableBalances[maxCreditor] = maxCredit - settleAmount
            mutableBalances[maxDebtor] = (mutableBalances[maxDebtor] ?: 0.0) + settleAmount
        }
        
        return debts
    }
    
    /**
     * Create equal shares for all participants
     */
    fun createEqualShares(participants: List<Person>, totalAmount: Double): List<Share> {
        val sharePerPerson = totalAmount / participants.size
        return participants.map { Share(it, sharePerPerson) }
    }
    
    /**
     * Format and print results
     */
    fun printResults(
        scenarioName: String,
        expense: SharedExpense,
        balances: Map<Person, Double>,
        debts: List<Debt>
    ) {
        println("\n" + "=".repeat(60))
        println("  $scenarioName")
        println("=".repeat(60))
        
        println("\n📝 EXPENSE DETAILS:")
        println("   Description: ${expense.description}")
        println("   Total Amount: Rs.${String.format("%.2f", expense.totalAmount)}")
        
        println("\n💰 CONTRIBUTIONS (Who Paid):")
        expense.contributions.forEach { contrib ->
            println("   ${contrib.person.name}: Rs.${String.format("%.2f", contrib.amountPaid)}")
        }
        
        println("\n🍽️  SHARES (What Each Person Owes):")
        expense.shares.forEach { share ->
            println("   ${share.person.name}: Rs.${String.format("%.2f", share.amountOwed)}")
        }
        
        println("\n📊 NET BALANCES:")
        balances.toList()
            .sortedByDescending { it.second }
            .forEach { (person, balance) ->
                when {
                    balance > 0.01 -> println("   ${person.name}: +Rs.${String.format("%.2f", balance)} (should receive)")
                    balance < -0.01 -> println("   ${person.name}: -Rs.${String.format("%.2f", abs(balance))} (should pay)")
                    else -> println("   ${person.name}: Rs.0.00 (settled)")
                }
            }
        
        println("\n✅ SIMPLIFIED SETTLEMENTS:")
        if (debts.isEmpty()) {
            println("   All balanced! No settlements needed.")
        } else {
            debts.forEach { debt ->
                println("   ${debt.from.name} should pay ${debt.to.name}: Rs.${String.format("%.2f", debt.amount)}")
            }
        }
        
        println("\n" + "=".repeat(60) + "\n")
    }
}
