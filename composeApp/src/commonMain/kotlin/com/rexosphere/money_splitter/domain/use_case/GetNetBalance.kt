package com.rexosphere.money_splitter.domain.use_case

import com.rexosphere.money_splitter.domain.model.Payment

class GetNetBalance {
    fun execute(payments: List<Payment>, userId: String): Double {
        val youOwe = payments.filter { it.from.id == userId && !it.isSettled }.sumOf { it.amount }
        val youAreOwed = payments.filter { it.to.id == userId && !it.isSettled }.sumOf { it.amount }
        return youAreOwed - youOwe
    }
}