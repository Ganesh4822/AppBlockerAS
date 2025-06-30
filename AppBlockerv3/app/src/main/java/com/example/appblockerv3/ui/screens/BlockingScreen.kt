package com.example.appblockerv3.ui.screens

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.appblockerv3.AppBlockerApplication
import com.example.appblockerv3.R
import com.example.appblockerv3.data.db.entities.GroupWithAppsAndSchedules
import com.example.appblockerv3.data.db.entities.ScheduleEntity
import com.example.appblockerv3.data.repository.BlockingRepository
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CreateGroupButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF4263EB), // Material 2 property for background color
            contentColor = Color.White // Text color
        )
    ) {
        Text(
            text = "Create a Group",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun BlockingScreen(onNavigateToAnalytics: () -> Unit, onNavigateToFocusTimer: () -> Unit,
                   onCreateGroupClick: () -> Unit,onSelectAppClick: () -> Unit) {
    val selectedTabIndex = remember { mutableStateOf(0) }
    val context = LocalContext.current
    val application = context.applicationContext as AppBlockerApplication

    val repository = application.database.let { db ->
        BlockingRepository(
            appGroupDao = db.appGroupDao(),
            appScheduleDao = db.appScheduleDao(),
            groupAppsJoinDao = db.groupAppsJoinDao(),
            individualAppBlockDao = db.individualBlockDao()
        )
    }
    //val createGroupViewModel: CreateGroupViewModel = viewModel(factory = AppViewModelFactory(repository))
    // 0 for Grouped Blocks, 1 for Individual Blocks
    val groupedBlocks by application.database.appGroupDao().getAllGroupBlocksWithAppsAndSchedules()
        .collectAsState(initial = emptyList())



    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.blocking)) })
        },
        //Bottom navigation menue
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
                    text = { Text(stringResource(R.string.grouped_blocks_count, groupedBlocks.size)) } // Replace 0 with actual count
                )
                Tab(
                    selected = selectedTabIndex.value == 1,
                    onClick = { selectedTabIndex.value = 1 },
                    text = { Text(stringResource(R.string.individual_blocks_count, 0)) } // Replace 0 with actual count
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Content when no groups are created (for Grouped Blocks tab)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Content for Grouped Blocks tab
                if (selectedTabIndex.value == 0) {
                    if (groupedBlocks.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(), // This fills the weighted Column
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
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
                            // When there are no groups, the button is here, centered
                            CreateGroupButton(
                                onClick = onCreateGroupClick,
                                modifier = Modifier.padding(horizontal = 32.dp) // Add horizontal padding
                            )
                        }
                    } else {
                        // Display the list of groups using LazyColumn
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(), // This fills the weighted Column
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(groupedBlocks) { groupWithDetails ->
                                GroupBlockCard(group = groupWithDetails)
                            }
                            // Add some bottom padding to LazyColumn so content isn't covered by button
                            item {
                                Spacer(Modifier.height(80.dp)) // Sufficient space for the button
                            }
                        }
                    }
                } else { // Content for Individual Blocks tab
                    Column(
                        modifier = Modifier.fillMaxSize(), // This fills the weighted Column
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
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
                        Button(onClick = onSelectAppClick) {
                            Text(stringResource(R.string.select_app))
                        }
                    }
                }
            }

            if (selectedTabIndex.value == 0 && groupedBlocks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp)) // Space above the button
                CreateGroupButton(
                    onClick = onCreateGroupClick,
                    modifier = Modifier.padding(horizontal = 16.dp) // Add horizontal padding
                )
                Spacer(modifier = Modifier.height(16.dp)) // Space below the button
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GroupBlockCard(group: GroupWithAppsAndSchedules) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        elevation = 4.dp,
        backgroundColor = Color(0xFFEAEDFF)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Access group name from the embedded GroupBlockEntity
                Text(
                    text = group.groupBlock.groupName,
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { /* Handle menu click (e.g., edit/delete group) */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Apps and Daily Limit Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Use the list of package names from GroupWithAppsAndSchedules
                    val displayedApps = group.appPackageNames.take(4)
                    displayedApps.forEach { packageName ->
                        val appIcon = remember(packageName) {
                            try {
                                context.packageManager.getApplicationIcon(packageName)
                            } catch (e: PackageManager.NameNotFoundException) {
                                null
                            }
                        }
                        appIcon?.let {
                            Image(
                                bitmap = it.toBitmap().asImageBitmap(), // Convert Drawable to Bitmap, then to ImageBitmap
                                contentDescription = "App Icon",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                    Log.d("info", "Apps: ${group.appPackageNames}")
                    if (group.appPackageNames.size > 4) {
                        Text(
                            text = "+${group.appPackageNames.size - 4}",
                            color = Color.Black.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Divider between apps and daily limit
                Divider(
                    color = Color.Gray,
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Display daily limit, using the calculated totalMinutes property
                if (group.totalUsageLimitMinutes > 0) {
                    Text(
                        text = "${group.totalUsageLimitMinutes} mins / day",
                        color = Color.Black.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "No daily limit",
                        color = Color.Black.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Schedules - iterate over the combined schedules list from the POJO
            if (group.schedules.isNotEmpty()) {
                group.schedules.forEachIndexed { index, schedule ->
                    ScheduleRow(schedule = schedule)
                    if (index < group.schedules.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp)) // Space between schedules
                    }
                }
            } else {
                Text(
                    text = "No schedules set",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleRow(schedule: ScheduleEntity) {
    val daysString = formatDaysBitMask(schedule.scheduleDaysBitMask)
    val startTime = formatTime(schedule.startHour, schedule.startMin)
    val endTime = formatTime(schedule.endHour, schedule.endMin)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Horizontal dotted line (mimicking the image)
        Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
            val dotWidth = 2.dp.toPx()
            val dotSpacing = 4.dp.toPx()
            var currentX = 0f
            while (currentX < size.width) {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.5f),
                    start = Offset(currentX, 0f),
                    end = Offset(currentX + dotWidth, 0f),
                    strokeWidth = 1.dp.toPx()
                )
                currentX += dotWidth + dotSpacing
            }
        }
        Spacer(modifier = Modifier.height(8.dp)) // Space after the dotted line


        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = daysString,
                color = Color.Black.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$startTime \u2192 $endTime", // Arrow symbol
                color = Color.Black.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}


// --- Helper Functions (remain the same) ---

fun formatDaysBitMask(bitMask: Int): String {
    // Assuming 0=Sunday, 1=Monday, ..., 6=Saturday
    // Adjust these labels if you need more specific or different representations (e.g., "Th" for Thursday)
    val days = listOf("S", "M", "T", "W", "T", "F", "S")
    val selectedDays = mutableListOf<String>()
    for (i in days.indices) {
        if ((bitMask shr i) and 1 == 1) {
            // Simple string for the day. For ambiguity like Tue/Thu, consider a custom mapping:
            // if (i == 2) selectedDays.add("Tu") else if (i == 4) selectedDays.add("Th") else selectedDays.add(days[i])
            selectedDays.add(days[i])
        }
    }
    return if (selectedDays.size == 7) "Everyday" else selectedDays.joinToString(" ")
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatTime(hour: Int, minute: Int): String {
    val localTime = LocalTime.of(hour, minute)
    val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
    return localTime.format(formatter).lowercase(Locale.getDefault()) // "am/pm" to lowercase
}

fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable) {
        return bitmap
    }
    // Create a new bitmap with ARGB_8888 config for transparency
    val bitmap = Bitmap.createBitmap(
        intrinsicWidth.coerceAtLeast(1),
        intrinsicHeight.coerceAtLeast(1),
        Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}