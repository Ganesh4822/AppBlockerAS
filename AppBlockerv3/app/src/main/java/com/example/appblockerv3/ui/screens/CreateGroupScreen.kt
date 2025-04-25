package com.example.appblockerv3.ui.screens

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.appblockerv3.R



@Composable
fun RoundedLabelInput() {
    var groupName by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 3.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFE8EAFF) //Light purple
            )
    ) {
        Text(
            text = "Group Name",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.h6,
            color = Color.Black
        )
        OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                modifier = Modifier.fillMaxWidth()
            )
    }
}

@Composable
fun AppsSection(selectedAppsInfo :List<AppData> ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE8EAFF)) // Light purple
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.apps_count, selectedAppsInfo.size), modifier = Modifier.weight(1f))
            IconButton(onClick = { /* TODO: Navigate back to app selection */ }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_apps))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow {
            items(selectedAppsInfo) { appData ->
                val iconBitmap = remember(appData.icon) { appData.icon?.toBitmap()?.asImageBitmap() }
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = appData.appName,
                        modifier = Modifier.size(48.dp).padding(end = 8.dp)
                    )
                } else {
                    Icon(Icons.Filled.Block, contentDescription = appData.appName, modifier = Modifier.size(48.dp).padding(end = 8.dp))
                }
            }
        }
    }
}


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CreateGroupScreen(
    onNavigateBack: () -> Unit,
    selectedAppPackageNames: List<String>,
    onSaveGroup: (String, List<String>) -> Unit // Callback for saving the group
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    var groupName by remember { mutableStateOf("") }
    val selectedAppsInfo = remember(selectedAppPackageNames) {
        selectedAppPackageNames.mapNotNull { packageName ->
            try {
                val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                val appName = applicationInfo.loadLabel(packageManager).toString()
                val appIcon = applicationInfo.loadIcon(packageManager)
                AppData(packageName, appName, appIcon)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.group)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = { onSaveGroup(groupName, selectedAppPackageNames) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color(0xFFE8EAFF))
            ) {
                Text(stringResource(R.string.save))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Name Field
            RoundedLabelInput()
            Spacer(modifier = Modifier.height(16.dp))
            // Apps Section
            AppsSection(selectedAppsInfo)

            Spacer(modifier = Modifier.height(16.dp))
            // Block on a Schedule
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8EAFF))
                    .padding(horizontal = 16.dp, vertical = 12.dp).clickable { /* TODO: Navigate to schedule settings */ }
            ) {
                Text(stringResource(R.string.block_on_schedule), modifier = Modifier.weight(1f))
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_schedule))
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Daily Usage Limit
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8EAFF))
                    .padding(horizontal = 16.dp, vertical = 12.dp).clickable { /* TODO: Navigate to usage limit settings */ }
            ) {
                Text(stringResource(R.string.daily_usage_limit), modifier = Modifier.weight(1f))
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_usage_limit))
            }
        }
    }
}

data class AppData(val packageName: String, val appName: String, val icon: Drawable?)

// Add these to your strings.xml
/*
<string name="group">Group</string>
<string name="name">Name</string>
<string name="apps_count">Apps (%d)</string>
<string name="add_apps">Add Apps</string>
<string name="block_on_schedule">Block on a Schedule</string>
<string name="add_schedule">Add Schedule</string>
<string name="daily_usage_limit">Daily Usage Limit</string>
<string name="add_usage_limit">Add Usage Limit</string>
<string name="save">Save</string>
*/