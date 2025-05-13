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
                     isSingleSelection: Boolean = false) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    var allApps by remember { mutableStateOf<List<SelectableAppInfo>>(emptyList()) }
    val searchQuery = remember { mutableStateOf("") }
    val selectedApps = remember { mutableStateListOf<String>() }
    Log.d("Appcheck", "SelectAppsScreen")
    LaunchedEffect(key1 = true) {
        val installedApps = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES)
        val tempAppList = mutableListOf<SelectableAppInfo>()
        for (packageInfo in installedApps) {
            try {
                val applicationInfo = packageInfo.applicationInfo
                if (applicationInfo != null) {
                    if ((applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 ||
                        (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                        val appName = applicationInfo.loadLabel(packageManager).toString()
                        val appIcon = try { applicationInfo.loadIcon(packageManager) } catch (e: Exception) { null }
                        tempAppList.add(
                            SelectableAppInfo(
                                packageName = packageInfo.packageName,
                                appName = appName,
                                icon = appIcon,
                                isSelected = mutableStateOf(false)  // Initially no apps are selected
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        allApps = tempAppList.sortedBy { it.appName }
    }

    val filteredApps = remember(allApps, searchQuery.value) {
        allApps.filter { it.appName.contains(searchQuery.value, ignoreCase = true) }
    }

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
                    onClick = { onCreateGroup(selectedApps.toList()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = selectedApps.isNotEmpty()
                ) {
                    Text(stringResource(R.string.create_a_group))
                }
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
                items(filteredApps) { appInfo ->
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

@Composable
fun SelectableAppListItem(appInfo: SelectableAppInfo
                          ,onAppSelected: (String, Boolean) -> Unit
                          ,isSingleSelection: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                if (isSingleSelection) {
                    onAppSelected(appInfo.packageName, true)
                } else {
                    onAppSelected(appInfo.packageName, !appInfo.isSelected.value)
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
        Text(appInfo.appName, style = MaterialTheme.typography.body1, modifier = Modifier.weight(1f))
        if (!isSingleSelection) {
            Checkbox(
                checked = appInfo.isSelected.value,
                onCheckedChange = { isChecked -> onAppSelected(appInfo.packageName, isChecked) }
            )
        }
    }
}
