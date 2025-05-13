package com.example.appblockerv3

import android.os.Build
import androidx.activity.ComponentActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
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
import com.example.appblockerv3.ui.screens.IndividualBlockOptions
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
        onCreateGroupClick = { navController.navigate("select_apps") },
        onSelectAppClick = { navController.navigate("select_single_app") }
    )
}
@Composable
fun SelectAppsScreenNav(navController: NavHostController) {
    SelectAppsScreen(
        onNavigateBack = { navController.popBackStack() },
        onCreateGroup = { selectedAppPackageNames ->
            val gson = Gson()
            val selectedAppsJson = gson.toJson(selectedAppPackageNames)
            navController.navigate("create_group/$selectedAppsJson")
        }
    )
}

@Composable
fun SelectAppsScreenNavForSingleSelectionNav(navController: NavHostController) {
    SelectAppsScreen(
        onNavigateBack = { navController.popBackStack() }, // Go back to the previous screen
        isSingleSelection = true,
        onAppSelected = { packageName -> navController.navigate("individual_block/$packageName")}
    )
}


@RequiresApi(Build.VERSION_CODES.O)
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
        onSaveGroup = { groupName, appList, schedules, usageLimitHours, usageLimitMinutes ->
            Log.d("MainActivity", "Group Name: $groupName")
            Log.d("MainActivity", "App List: $appList")
            Log.d("MainActivity", "Schedules: $schedules")
            Log.d("MainActivity", "Usage Limit: $usageLimitHours hours and $usageLimitMinutes minutes")
            // TODO: Implement logic to save this data in DB
            navController.popBackStack()
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SaveSingleAppSettingNav(navController: NavHostController, selectedApp: String) {
    val selectedAppPackageName = selectedApp

    IndividualBlockOptions (
        onNavigateBack = { navController.popBackStack() },
        selectedAppPackageName = selectedAppPackageName,
        onSaveSettings = {  appName, schedules, usageLimitHours, usageLimitMinutes ->
            Log.d("MainActivity", "App Name: $appName")
            Log.d("MainActivity", "Schedules: $schedules")
            Log.d("MainActivity", "Usage Limit: $usageLimitHours hours and $usageLimitMinutes minutes")
            // TODO: Implement logic to save this data in DB
            navController.popBackStack()
        }
    )
}
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
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
                        /*
                        All the routes are defined here.
                        Routes starts from the blocking screen This is the point where the flow starts.
                         */
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

                        /*
                            Handling Individual app blocks here
                            when a user switches to individual blocking screen he should see the app
                            list without the checkboxes and each app should be clickable.
                            This is handled in SelectAppsScreen.
                         */

                        composable("select_single_app") {
                            SelectAppsScreenNavForSingleSelectionNav(
                                navController = navController
                            )
                        }

                        //Handling the selected app list to be passed to the create group screen.
                        composable(
                            "create_group/{selectedAppsJson}",
                            arguments = listOf(navArgument("selectedAppsJson") { nullable = true })
                        ) { backStackEntry ->
                            val selectedAppsJson = backStackEntry.arguments?.getString("selectedAppsJson")
                            CreateGroupScreenNav(navController = navController, selectedAppsJson = selectedAppsJson)
                        }

                        //Handling schedule and daily usage limit setting for single app selection.
                        composable(
                            "individual_block/{packageName}", // This Parameter should exactly
                            arguments = listOf(navArgument("packageName") { nullable = true })
                        ) { backStackEntry ->
                            val selectedApp = backStackEntry.arguments?.getString("packageName")
                            if (selectedApp != null) {
                                SaveSingleAppSettingNav(navController = navController,selectedApp)
                            }
                        }
                    }
                }
            }
        }
    }
}
