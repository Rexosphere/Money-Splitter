package com.rexosphere.money_splitter.data.database

/**
 * Simple dependency holder for the DatabaseHelper.
 * Initialize this from platform-specific code before using the repository.
 */
object DatabaseProvider {
    private var _databaseHelper: DatabaseHelper? = null
    
    val databaseHelper: DatabaseHelper?
        get() = _databaseHelper
    
    fun initialize(driverFactory: DatabaseDriverFactory) {
        if (_databaseHelper == null) {
            _databaseHelper = DatabaseHelper(driverFactory)
        }
    }
    
    fun isInitialized(): Boolean = _databaseHelper != null
}
