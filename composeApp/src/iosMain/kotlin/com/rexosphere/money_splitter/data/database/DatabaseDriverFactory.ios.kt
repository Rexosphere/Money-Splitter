package com.rexosphere.money_splitter.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = MoneySplitterDatabase.Schema,
            name = "money_splitter_v2.db"
        )
    }
}
