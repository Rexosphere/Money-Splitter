package com.rexosphere.money_splitter.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = MoneySplitterDatabase.Schema,
            context = context,
            name = "money_splitter_v2.db"
        )
    }
}
