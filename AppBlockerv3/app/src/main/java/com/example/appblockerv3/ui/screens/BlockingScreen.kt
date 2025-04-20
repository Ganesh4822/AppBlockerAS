package com.example.appblockerv3.ui.screens

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.appblockerv3.R

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?
)

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun BlockingScreen() {
    val context = LocalContext.current
    val packageManager = context.packageManager
    var allApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    val searchQuery = remember { mutableStateOf("") }
    val selectedTabIndex = remember { mutableStateOf(0) }
    var blockedApps by remember { mutableStateOf<Set<String>>(emptySet()) } // Placeholder for blocked apps

    LaunchedEffect(key1 = true) {
        //val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val tempAppList = mutableListOf<AppInfo>()
        for (app in packages) {
            try {
                val appName = packageManager.getApplicationLabel(app).toString()
                val packageName = app.packageName
                val icon = packageManager.getApplicationIcon(app)
                if (packageManager.getLaunchIntentForPackage(packageName) != null){
                    tempAppList.add(
                        AppInfo(
                            packageName = packageName,
                            appName = appName,
                            icon = icon
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        allApps = tempAppList.sortedBy { it.appName }
        // TODO: Load the actual list of blocked apps here and update the 'blockedApps' state
        blockedApps = setOf("com.whatsapp", "com.instagram") // Example blocked apps
    }

    val filteredAllApps = remember(allApps, searchQuery.value) {
        allApps.filter { it.appName.contains(searchQuery.value, ignoreCase = true) }
    }

    val filteredBlockedApps = remember(allApps, blockedApps, searchQuery.value) {
        allApps.filter { it.packageName in blockedApps && it.appName.contains(searchQuery.value, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.blocking)) })
        },
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Block, contentDescription = stringResource(R.string.blocking)) },
                    label = { Text(stringResource(R.string.blocking)) },
                    selected = true,
                    onClick = { selectedTabIndex.value = 1 }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Analytics, contentDescription = stringResource(R.string.analytics)) },
                    label = { Text(stringResource(R.string.analytics)) },
                    selected = selectedTabIndex.value == 1,
                    onClick = { selectedTabIndex.value = 1 }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = { Text(stringResource(R.string.search_app)) },
                trailingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search)) }
            )

            TabRow(selectedTabIndex = selectedTabIndex.value) {
                Tab(
                    selected = selectedTabIndex.value == 0,
                    onClick = { selectedTabIndex.value = 0 },
                    text = { Text(stringResource(R.string.all_apps_count, filteredAllApps.size)) }
                )
                Tab(
                    selected = selectedTabIndex.value == 1,
                    onClick = { selectedTabIndex.value = 1 },
                    text = { Text(stringResource(R.string.blocked_apps_count, filteredBlockedApps.size)) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // App List Display
            val appsToDisplay = if (selectedTabIndex.value == 0) filteredAllApps else filteredBlockedApps
            if (appsToDisplay.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_apps_found)) // You might want different messages for empty all apps and empty blocked apps
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    items(appsToDisplay) { appInfo ->
                        AppListItem(appInfo = appInfo) {
                            // TODO: Handle click on the app item (e.g., navigate to settings)
                            println("Clicked on ${appInfo.appName}")
                        }
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun AppListItem(appInfo: AppInfo, onAppClick: (AppInfo) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onAppClick(appInfo) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconBitmap = remember(appInfo.icon) { appInfo.icon?.toBitmap()?.asImageBitmap() }
        if (iconBitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = iconBitmap,
                contentDescription = appInfo.appName,
                modifier = Modifier.size(48.dp)
            )
        } else {
            Icon(Icons.Filled.Block, contentDescription = appInfo.appName, modifier = Modifier.size(48.dp)) // Placeholder if no icon
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(appInfo.appName, style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.configure)) // Placeholder for navigation/configure icon
    }
}


// Add this to your strings.xml
/*
<string name="no_apps_found">No apps found.</string>
<string name="configure">Configure</string>
*/