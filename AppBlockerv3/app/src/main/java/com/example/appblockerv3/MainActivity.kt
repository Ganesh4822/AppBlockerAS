package com.example.appblockerv3

import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appblockerv3.ui.screens.BlockingScreen
import com.example.appblockerv3.ui.theme.AppBlockerV3Theme
import com.example.appblockerv3.R
import com.example.appblockerv3.ui.screens.SelectAppsScreen
import org.jetbrains.annotations.Blocking

@Composable
fun AnalyticsScreen() {
    Text(text = "Analytics Screen") // Placeholder
}

@Composable
fun FocusTimerScreen() {
    Text(text = "Focus Timer Screen") // Placeholder
}
@Composable
fun BlockingScreen(navController: NavHostController) {
    BlockingScreen(
        onNavigateToAnalytics = { navController.navigate("analytics") },
        onNavigateToFocusTimer = { navController.navigate("focus_timer") },
        onCreateGroupClick = { navController.navigate("select_apps") } // New callback
    )
}
@Composable
fun SelectAppsScreen(navController: NavHostController, onCreateGroup: (List<String>) -> Unit) {
    SelectAppsScreen(
        onNavigateBack = { navController.popBackStack() }, // Go back to the previous screen
        onCreateGroup = onCreateGroup
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "blocking") {
                        composable("blocking") {
                            BlockingScreen(navController = navController)
                        }
                        composable("analytics") {
                            AnalyticsScreen()
                        }
                        composable("focus_timer") {
                            FocusTimerScreen()
                        }
                        composable("select_apps") {
                            // Define how to handle the list of selected apps
                            val onGroupCreated: (List<String>) -> Unit = { selectedAppPackageNames ->
                                println("Selected apps for group: $selectedAppPackageNames")
                                // TODO: Implement the logic to create and save the group
                                // After creating the group, you might want to navigate back
                                navController.popBackStack()
                            }
                            SelectAppsScreen(
                                navController = navController, onCreateGroup = onGroupCreated
                            )
                        }
                    }
                }
            }
        }
    }
}
