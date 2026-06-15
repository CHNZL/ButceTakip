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

import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.worker.PaymentReminderWorker

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.updater.AppUpdater
import com.example.updater.UpdateInfo

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setupWorkManager()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                setupWorkManager()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            setupWorkManager()
        }

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
        val appUpdater = AppUpdater(this)

        val startDest = if (prefManager.userId.isNotBlank()) "dashboard" else "login"

        setContent {
            val isDarkTheme by viewModel.darkThemeEnabled.collectAsState()
            var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

            LaunchedEffect(Unit) {
                updateInfo = appUpdater.checkForUpdate()
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                updateInfo?.let { info ->
                    AlertDialog(
                        onDismissRequest = { updateInfo = null },
                        title = { Text("Yeni Güncelleme Mevcut!") },
                        text = { Text("Sürüm ${info.versionName} hazır.\n\n${info.releaseNotes}") },
                        confirmButton = {
                            TextButton(onClick = {
                                appUpdater.downloadAndInstallUpdate(info)
                                updateInfo = null
                            }) {
                                Text("Şimdi Yükle")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { updateInfo = null }) {
                                Text("Daha Sonra")
                            }
                        }
                    )
                }

                NavHost(navController = navController, startDestination = startDest) {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onBypassLogin = {
                                prefManager.userId = "offline_user"
                                viewModel.login("offline_user")
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

    private fun setupWorkManager() {
        val prefManager = com.example.data.PreferenceManager(applicationContext)
        val interval = prefManager.updateIntervalHours.toLong()
        val workRequest = PeriodicWorkRequestBuilder<PaymentReminderWorker>(interval, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "PaymentReminderWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
