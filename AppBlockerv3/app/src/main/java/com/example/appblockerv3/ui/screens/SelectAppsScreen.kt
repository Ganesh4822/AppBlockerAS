package com.example.appblockerv3.ui.screens

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appblockerv3.AppBlockerApplication
import com.example.appblockerv3.R
import com.example.appblockerv3.data.repository.BlockingRepository
import com.example.appblockerv3.ui.viewmodels.AppInfoWithStatus
import com.example.appblockerv3.ui.viewmodels.SelectAppsViewModel
import com.example.appblockerv3.ui.viewmodels.SelectAppsViewModelFactory


data class SelectableAppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    var isSelected: MutableState<Boolean> //This state is kept mutable as user wants to check/uncheck
    //apps from the App list
)

/*
Contains conditional UI based on the isSingleSelection param.
when isSingleSelection param is false UI loads with the checkboxes
when isSingleSelection param is true UI loads without the checkboxes.
*/
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SelectAppsScreen(onNavigateBack: () -> Unit
                     ,onCreateGroup: (List<String>) -> Unit = {}
                     ,onAppSelected: (String) -> Unit = {},
                     isSingleSelection: Boolean = false,
                     viewModel: SelectAppsViewModel = viewModel(
                             factory = SelectAppsViewModelFactory(
                                 LocalContext.current.applicationContext as AppBlockerApplication,
                                 (LocalContext.current.applicationContext as AppBlockerApplication).database.let { db ->
                                     BlockingRepository(
                                         appGroupDao = db.appGroupDao(),
                                         appScheduleDao = db.appScheduleDao(),
                                         groupAppsJoinDao = db.groupAppsJoinDao(),
                                         individualAppBlockDao = db.individualBlockDao()
                                     )
                                 }
                             )
                     )
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    var allApps by remember { mutableStateOf<List<SelectableAppInfo>>(emptyList()) }
    //val searchQuery = remember { mutableStateOf("") }
    val selectedApps = remember { mutableStateListOf<String>() }
    Log.d("Appcheck", "SelectAppsScreen")
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredApps1 by viewModel.filteredApps.collectAsState()

    val currentSelectedApps = remember(filteredApps1) {
        filteredApps1.filter { it.isSelected && !it.isDisabled }.map { it.packageName }
    }
//    LaunchedEffect(key1 = true) {
//        val installedApps = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES)
//        val tempAppList = mutableListOf<SelectableAppInfo>()
//        for (packageInfo in installedApps) {
//            try {
//                val applicationInfo = packageInfo.applicationInfo
//                if (applicationInfo != null) {
//                    if ((applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 ||
//                        (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
//                        val appName = applicationInfo.loadLabel(packageManager).toString()
//                        val appIcon = try { applicationInfo.loadIcon(packageManager) } catch (e: Exception) { null }
//                        tempAppList.add(
//                            SelectableAppInfo(
//                                packageName = packageInfo.packageName,
//                                appName = appName,
//                                icon = appIcon,
//                                isSelected = mutableStateOf(false)  // Initially no apps are selected
//                            )
//                        )
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//        allApps = tempAppList.sortedBy { it.appName }
//    }

//    val filteredApps = remember(allApps, searchQuery.value) {
//        allApps.filter { it.appName.contains(searchQuery.value, ignoreCase = true) }
//    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isSingleSelection) stringResource(R.string.select_app_to_block) else stringResource(
                            R.string.select_apps
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        bottomBar = {
            if (!isSingleSelection) {
                Button(
                    onClick = { onCreateGroup(currentSelectedApps) }, // Pass currentSelectedApps
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = currentSelectedApps.isNotEmpty() // Enable only if apps are selected
                ) {
                    Text(stringResource(R.string.add_to_group)) // Changed text based on image
                }
            } else {
                // For single selection, if an app is selected, the "Add to Group" button might change
                // or you might have a different CTA.
                // For now, let's keep it simple or remove if not needed.
                // If onAppSelected is meant to immediately act, no bottom bar needed for single select.
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(MaterialTheme.colors.background,
                        CircleShape),
                placeholder = { Text(stringResource(R.string.search_app)) },
                trailingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                items(filteredApps1) { appInfo ->
                    SelectableAppListItem(
                        appInfo = appInfo,
                        onAppSelected = { packageName, isChecked ->
                            val app = allApps.find { it.packageName == packageName }
                            app?.isSelected?.value = isChecked
                            if (isSingleSelection) {
                                if (isChecked) {
                                    onAppSelected(packageName) // Notify the single app selection
                                }
                            } else {
                                if (isChecked) {
                                    if (!selectedApps.contains(packageName)) {
                                        selectedApps.add(packageName)
                                        Log.d("Appcheck", selectedApps.toString())
                                    }
                                } else {
                                    selectedApps.remove(packageName)
                                }
                            }
                        },
                        isSingleSelection = isSingleSelection
                    )
                    Divider()
                }
            }
        }
    }
}

//@Composable
//fun SelectableAppListItem(appInfo: SelectableAppInfo
//                          ,onAppSelected: (String, Boolean) -> Unit
//                          ,isSingleSelection: Boolean) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp)
//            .clickable {
//                if (isSingleSelection) {
//                    onAppSelected(appInfo.packageName, true)
//                } else {
//                    onAppSelected(appInfo.packageName, !appInfo.isSelected.value)
//                }
//            },
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        val iconBitmap = remember(appInfo.icon) { appInfo.icon?.toBitmap()?.asImageBitmap() }
//        if (iconBitmap != null) {
//            androidx.compose.foundation.Image(
//                bitmap = iconBitmap,
//                contentDescription = appInfo.appName,
//                modifier = Modifier.size(48.dp)
//            )
//        } else {
//            Icon(Icons.Filled.Block, contentDescription = appInfo.appName, modifier = Modifier.size(48.dp)) // Placeholder
//        }
//        Spacer(modifier = Modifier.width(16.dp))
//        Text(appInfo.appName, style = MaterialTheme.typography.body1, modifier = Modifier.weight(1f))
//        if (!isSingleSelection) {
//            Checkbox(
//                checked = appInfo.isSelected.value,
//                onCheckedChange = { isChecked -> onAppSelected(appInfo.packageName, isChecked) }
//            )
//        }
//    }
//}


@Composable
fun SelectableAppListItem(
    appInfo: AppInfoWithStatus,
    onAppSelected: (String, Boolean) -> Unit,
    isSingleSelection: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(enabled = !appInfo.isDisabled) { // Disable click if app is blocked
                if (isSingleSelection) {
                    if (!appInfo.isBlocked) { // Only allow selection if not already blocked
                        onAppSelected(appInfo.packageName, true)
                    }
                } else {
                    // For multi-selection, toggle selection if not disabled
                    if (!appInfo.isDisabled) {
                        onAppSelected(appInfo.packageName, !appInfo.isSelected)
                    }
                }
            },
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
            Icon(Icons.Filled.Block, contentDescription = appInfo.appName, modifier = Modifier.size(48.dp)) // Placeholder
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            appInfo.appName,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.weight(1f),
            color = if (appInfo.isDisabled) MaterialTheme.colors.onSurface.copy(alpha = 0.6f) else MaterialTheme.colors.onSurface // Grey out text if disabled
        )

        // Show "Blocked" icon if app is blocked by any rule
        if (appInfo.isBlocked) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = stringResource(R.string.blocked),
                tint = MaterialTheme.colors.error, // Red tint for blocked
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(R.string.blocked), // "Blocked" text
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.error
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Checkbox for multi-selection
        if (!isSingleSelection) {
            Checkbox(
                checked = appInfo.isSelected,
                onCheckedChange = { isChecked ->
                    if (!appInfo.isDisabled) { // Only allow change if not disabled
                        onAppSelected(appInfo.packageName, isChecked)
                    }
                },
                enabled = !appInfo.isDisabled // Disable checkbox if app is blocked/in max groups
            )
        }
    }
}
