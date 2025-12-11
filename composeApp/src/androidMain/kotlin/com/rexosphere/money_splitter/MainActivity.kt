package com.rexosphere.money_splitter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.rexosphere.money_splitter.data.database.DatabaseDriverFactory
import com.rexosphere.money_splitter.data.database.DatabaseProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize the SQLite database
        DatabaseProvider.initialize(DatabaseDriverFactory(applicationContext))

        setContent {
            com.rexosphere.money_splitter.ui.theme.MoneySplitterTheme {
                MoneySplitterApp()
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    MoneySplitterApp()
}