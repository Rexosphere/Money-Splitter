package com.rexosphere.money_splitter.expense_calculator

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Demo scenarios for expense splitting
 */
@OptIn(ExperimentalUuidApi::class)
class ExpenseSplitScenarios {
    
    private val calculator = ExpenseSplitCalculator()
    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    
    // Group members
    private val ifaz = Person("1", "Ifaz Ikram")
    private val kalanaPankaja = Person("2", "Kalana Pankaja")
    private val suhas = Person("3", "Suhas Dissanayaka")
    private val sangeeth = Person("4", "Sangeeth Kariyapperuma")
    private val kalanaAbeysundara = Person("5", "Kalana Abeysundara")
    
    /**
     * SCENARIO 1: Bus Ticket - Equal Split
     * 
     * Total cost: Rs.1000 (Rs.200 per person)
     * Ifaz paid Rs.500
     * Kalana Abeysundara paid Rs.500
     * 
     * Expected: 
     * - Ifaz should receive Rs.300 (paid 500, owes 200)
     * - Kalana Abeysundara should receive Rs.300 (paid 500, owes 200)
     * - Others should pay Rs.200 each
     */
    fun runScenario1() {
        val allMembers = listOf(ifaz, kalanaPankaja, suhas, sangeeth, kalanaAbeysundara)
        
        val expense = SharedExpense(
            id = Uuid.random().toString(),
            description = "Bus Ticket",
            totalAmount = 1000.0,
            date = today,
            contributions = listOf(
                Contribution(ifaz, 500.0),
                Contribution(kalanaAbeysundara, 500.0)
            ),
            shares = calculator.createEqualShares(allMembers, 1000.0)
        )
        
        val balances = calculator.calculateNetBalances(expense)
        val debts = calculator.simplifyDebts(balances)
        
        calculator.printResults("SCENARIO 1: Bus Ticket (Equal Split)", expense, balances, debts)
    }
    
    /**
     * SCENARIO 2: Group Expense - Custom Consumption
     * 
     * Total paid: Rs.3200
     * Total consumed: Rs.3200
     * 
     * Payments:
     * - Ifaz: Rs.1000
     * - Kalana Pankaja: Rs.1000
     * - Suhas: Rs.500
     * - Sangeeth: Rs.700
     * 
     * Consumption:
     * - Ifaz: Rs.400
     * - Kalana Pankaja: Rs.700
     * - Kalana Abeysundara: Rs.600
     * - Sangeeth: Rs.1000
     * - Suhas: Rs.500
     * 
     * Expected balances:
     * - Ifaz: +Rs.600 (paid 1000, consumed 400)
     * - Kalana Pankaja: +Rs.300 (paid 1000, consumed 700)
     * - Suhas: Rs.0 (paid 500, consumed 500)
     * - Sangeeth: -Rs.300 (paid 700, consumed 1000)
     * - Kalana Abeysundara: -Rs.600 (paid 0, consumed 600)
     */
    fun runScenario2() {
        val expense = SharedExpense(
            id = Uuid.random().toString(),
            description = "Group Expense (Custom Shares)",
            totalAmount = 3200.0,
            date = today,
            contributions = listOf(
                Contribution(ifaz, 1000.0),
                Contribution(kalanaPankaja, 1000.0),
                Contribution(suhas, 500.0),
                Contribution(sangeeth, 700.0)
                // Kalana Abeysundara paid nothing
            ),
            shares = listOf(
                Share(ifaz, 400.0),
                Share(kalanaPankaja, 700.0),
                Share(kalanaAbeysundara, 600.0),
                Share(sangeeth, 1000.0),
                Share(suhas, 500.0)
            )
        )
        
        val balances = calculator.calculateNetBalances(expense)
        val debts = calculator.simplifyDebts(balances)
        
        calculator.printResults("SCENARIO 2: Group Expense (Custom Consumption)", expense, balances, debts)
    }
    
    /**
     * Run all scenarios
     */
    fun runAllScenarios() {
        println("\n")
        println("╔═══════════════════════════════════════════════════════════╗")
        println("║     EXPENSE SPLITTING CALCULATOR - DEMO SCENARIOS        ║")
        println("╚═══════════════════════════════════════════════════════════╝")
        
        runScenario1()
        runScenario2()
        
        println("\n✨ All scenarios completed!\n")
    }
}

/**
 * Main function to run the scenarios
 */
fun main() {
    val scenarios = ExpenseSplitScenarios()
    scenarios.runAllScenarios()
}
