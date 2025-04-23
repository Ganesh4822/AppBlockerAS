package com.example.appblockerv3.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appblockerv3.R // Replace with your actual R file

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun BlockingScreen(onNavigateToAnalytics: () -> Unit, onNavigateToFocusTimer: () -> Unit,
                   onCreateGroupClick: () -> Unit) {
    val selectedTabIndex = remember { mutableStateOf(0) }
    // 0 for Grouped Blocks, 1 for Individual Blocks

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.blocking)) })
        },
        /*
            Added bottom navigation menue to navigate to analytics and focus timer
         */
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Analytics, contentDescription = stringResource(R.string.analytics)) },
                    label = { Text(stringResource(R.string.analytics)) },
                    selected = false,
                    onClick = { onNavigateToAnalytics() }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Block, contentDescription = stringResource(R.string.blocking)) },
                    label = { Text(stringResource(R.string.blocking)) },
                    selected = true,
                    onClick = { /* Already on Blocking screen */ }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Timer, contentDescription = stringResource(R.string.focus_timer)) },
                    label = { Text(stringResource(R.string.focus_timer)) },
                    selected = false,
                    onClick = { onNavigateToFocusTimer() }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tabs for Grouped Blocks and Individual Blocks
            TabRow(selectedTabIndex = selectedTabIndex.value) {
                Tab(
                    selected = selectedTabIndex.value == 0,
                    onClick = { selectedTabIndex.value = 0 },
                    text = { Text(stringResource(R.string.grouped_blocks_count, 0)) } // Replace 0 with actual count
                )
                Tab(
                    selected = selectedTabIndex.value == 1,
                    onClick = { selectedTabIndex.value = 1 },
                    text = { Text(stringResource(R.string.individual_blocks_count, 0)) } // Replace 0 with actual count
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Content when no groups are created (for Grouped Blocks tab)
            if (selectedTabIndex.value == 0) {
                Text(
                    text = stringResource(R.string.no_groups_yet),
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.group_description),
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onCreateGroupClick) {
                    Text(stringResource(R.string.create_a_group))
                }
            } else {
                // TODO: Content for Individual Blocks tab (likely a list of individually blocked apps)

                    Text(
                        text = stringResource(R.string.no_apps_found),
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.individual_blocks_placeholder),
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { /* TODO: Implement Create a Group action */ }) {
                        Text(stringResource(R.string.select_app))
                    }

            }
        }
    }
}
