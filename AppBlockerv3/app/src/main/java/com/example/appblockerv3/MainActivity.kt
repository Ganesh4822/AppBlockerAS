package com.example.appblockerv3

import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appblockerv3.ui.screens.BlockingScreen
import com.example.appblockerv3.ui.screens.CreateGroupScreen
import com.example.appblockerv3.ui.screens.SelectAppsScreen
import com.google.gson.Gson

@Composable
fun AnalyticsScreen() {
    Text(text = "Analytics Screen") // Placeholder
}

@Composable
fun FocusTimerScreen() {
    Text(text = "Focus Timer Screen") // Placeholder
}
@Composable
fun BlockingScreenNav(navController: NavHostController) {
    BlockingScreen(
        onNavigateToAnalytics = { navController.navigate("analytics") },
        onNavigateToFocusTimer = { navController.navigate("focus_timer") },
        onCreateGroupClick = { navController.navigate("select_apps") } // New callback
    )
}
@Composable
fun SelectAppsScreenNav(navController: NavHostController) {
    SelectAppsScreen(
        onNavigateBack = { navController.popBackStack() }, // Go back to the previous screen
        onCreateGroup = { selectedAppPackageNames ->
            val gson = Gson()
            val selectedAppsJson = gson.toJson(selectedAppPackageNames)
            navController.navigate("create_group/$selectedAppsJson")
        }
    )
}


@Composable
fun CreateGroupScreenNav(navController: NavHostController, selectedAppsJson: String?) {
    val gson = Gson()
    val selectedAppPackageNames = remember(selectedAppsJson) {
        if (selectedAppsJson != null) {
            gson.fromJson<List<String>>(selectedAppsJson, List::class.java) as List<String>
        } else {
            emptyList()
        }
    }

    CreateGroupScreen(
        onNavigateBack = { navController.popBackStack() },
        selectedAppPackageNames = selectedAppPackageNames,
        onSaveGroup = { groupName, appList ->
            println("Group Name: $groupName, Apps to save: $appList")
            // TODO: Implement the logic to save the group name and app list
            navController.popBackStack() // Navigate back after saving
        }
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
                            BlockingScreenNav(navController = navController)
                        }
                        composable("analytics") {
                            AnalyticsScreen()
                        }
                        composable("focus_timer") {
                            FocusTimerScreen()
                        }
                        composable("select_apps") {
                            SelectAppsScreenNav(
                                navController = navController
                            )
                        }
                        //Handling the selected app list to be passed to the create group screen
                        composable(
                            "create_group/{selectedAppsJson}",
                            arguments = listOf(navArgument("selectedAppsJson") { nullable = true })
                        ) { backStackEntry ->
                            val selectedAppsJson = backStackEntry.arguments?.getString("selectedAppsJson")
                            CreateGroupScreenNav(navController = navController, selectedAppsJson = selectedAppsJson)
                        }
                    }
                }
            }
        }
    }
}
