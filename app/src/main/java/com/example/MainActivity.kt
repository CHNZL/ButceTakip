package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.TransactionRepository
import com.example.ui.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BudgetViewModel
import com.example.viewmodel.BudgetViewModelFactory

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.LoginScreen

import androidx.activity.viewModels
import com.example.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TransactionRepository(
            database.transactionDao(), 
            database.savingDao(),
            database.categoryDao(),
            database.personDao()
        )
        val prefManager = com.example.data.PreferenceManager(applicationContext)
        val viewModelFactory = BudgetViewModelFactory(application, repository, prefManager)
        val viewModel = ViewModelProvider(this, viewModelFactory)[BudgetViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onBypassLogin = {
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            viewModel = authViewModel
                        )
                    }
                    composable("dashboard") {
                        DashboardScreen(
                            viewModel = viewModel,
                            authViewModel = authViewModel,
                            onSignOut = {
                                viewModel.logout()
                                authViewModel.signOut()
                                navController.navigate("login") {
                                    popUpTo("dashboard") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
