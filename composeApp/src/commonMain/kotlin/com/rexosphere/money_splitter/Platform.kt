package com.rexosphere.money_splitter

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform