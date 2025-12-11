package com.rexosphere.money_splitter

import com.rexosphere.money_splitter.expense_calculator.ExpenseSplitScenarios

/**
 * Test runner for expense splitting scenarios
 * Add this to your MainActivity or create a test button to run it
 */
object ExpenseCalculatorDemo {
    
    fun runDemo() {
        val scenarios = ExpenseSplitScenarios()
        scenarios.runAllScenarios()
    }
}
