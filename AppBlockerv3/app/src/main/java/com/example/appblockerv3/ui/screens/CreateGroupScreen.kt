package com.example.appblockerv3.ui.screens

import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.appblockerv3.R
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class AppData(val packageName: String, val appName: String, val icon: Drawable?)

@Composable
fun RoundedLabelInput() {
    var groupName by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 3.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))// To only round the top corners
            .background(Color(0xFFE8EAFF) //Light purple
            )
    ) {
        Text(
            text = "Name",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.body2,
            color = Color.Black
        )
        OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            modifier = Modifier.fillMaxWidth()
                 .background(Color.White)

        )
    }
}

@Composable
fun AppsSection(selectedAppsInfo :List<AppData> ) {

    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(Color(0xFFE8EAFF))
    ) {
        // Header (Light Purple Background)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8EAFD)) // Light Purple
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.apps_count, selectedAppsInfo.size), modifier = Modifier.weight(1f))
            IconButton(onClick = { /* TODO: Navigate back to app selection */ }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_apps))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(
                    width = 1.dp,
                    color = Color.LightGray, // Border color
                )
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CreateGroupScreen(
    onNavigateBack: () -> Unit,
    selectedAppPackageNames: List<String>,
    onSaveGroup: (String, List<String>) -> Unit // Updated onSaveGroup
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    var groupName by remember { mutableStateOf("") }
    val selectedAppsInfo by remember(selectedAppPackageNames) {
        derivedStateOf {
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
    }

    val scheduleBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    // State to hold the schedule data.  Using rememberSaveable for surviving config changes.
    var selectedDays by remember { mutableStateOf(emptyList<Int>()) }
    var startTime by remember { mutableStateOf<LocalTime?>(LocalTime.of(9, 0)) }
    var endTime by remember { mutableStateOf<LocalTime?>(LocalTime.of(17, 0)) }
    var isAllDay by remember { mutableStateOf(false) }

    ModalBottomSheetLayout(
        sheetState = scheduleBottomSheetState,
        sheetContent = {
            BlockOnScheduleBottomSheet(
                onDismissRequest = { scope.launch { scheduleBottomSheetState.hide() } },
                onScheduleSaved = { days, start, end, allDay ->
                    selectedDays = days
                    startTime = start
                    endTime = end
                    isAllDay = allDay
                    scope.launch { scheduleBottomSheetState.hide() }
                },
                selectedDays = selectedDays,
                startTime = startTime,
                endTime = endTime,
                isAllDay = isAllDay
            )
        }
    ) {
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
                    onClick = {
                        onSaveGroup(groupName, selectedAppPackageNames)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color(0xFFE8EAFF)),
                    enabled = groupName.isNotBlank() // Disable if group name is empty
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
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clickable { scope.launch { scheduleBottomSheetState.show() } }
                ) {
                    Text(stringResource(R.string.block_on_schedule), modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_schedule))
                }
                Spacer(modifier = Modifier.height(16.dp))
                //show selected days
//                if (selectedDays.isNotEmpty()) {
//                    Text(
//                        text = "Blocking on: ${selectedDays.joinToString { day ->
//                            when (day) {
//                                1 -> "Mon"
//                                2 -> "Tue"
//                                3 -> "Wed"
//                                4 -> "Thu"
//                                5 -> "Fri"
//                                6 -> "Sat"
//                                7 -> "Sun"
//                                else -> ""
//                            }
//                        }}",
//                        style = MaterialTheme.typography.body2,
//                        modifier = Modifier.padding(bottom = 8.dp)
//                    )
//                }
//                if (isAllDay) {
//                    Text(
//                        text = "All Day",
//                        style = MaterialTheme.typography.body2,
//                        modifier = Modifier.padding(bottom = 8.dp)
//                    )
//                } else if (startTime != null && endTime != null) {
//                    Text(
//                        text = "Time: ${startTime?.format(DateTimeFormatter.ofPattern("hh:mm a"))} - ${endTime?.format(DateTimeFormatter.ofPattern("hh:mm a"))}",
//                        style = MaterialTheme.typography.body2
//                    )
//                }

                // Daily Usage Limit
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8EAFF))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clickable { /* TODO: Navigate to usage limit settings */ }
                ) {
                    Text(stringResource(R.string.daily_usage_limit), modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_usage_limit))
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BlockOnScheduleBottomSheet(
    onDismissRequest: () -> Unit,
    onScheduleSaved: (List<Int>, LocalTime?, LocalTime?, Boolean) -> Unit,
    selectedDays: List<Int>,
    startTime: LocalTime?,
    endTime: LocalTime?,
    isAllDay: Boolean
) {
    var localSelectedDays by remember { mutableStateOf(selectedDays) } // 1 for Mon, 7 for Sun
    var localStartTime by remember { mutableStateOf(startTime) }
    var localEndTime by remember { mutableStateOf(endTime) }
    var localIsAllDay by remember { mutableStateOf(isAllDay) }

    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.block_on_schedule), style = MaterialTheme.typography.h6)
            IconButton(onClick = onDismissRequest) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Select Block Days
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.select_block_days),
                        style = MaterialTheme.typography.subtitle1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.all_day))
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = localIsAllDay,
                            onCheckedChange = { localIsAllDay = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    dayLabels.forEachIndexed { index, label ->
                        val dayIndex = index + 1
                        val isSelected = localSelectedDays.contains(dayIndex)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    localSelectedDays = if (isSelected) {
                                        localSelectedDays.filter { it != dayIndex }
                                    } else {
                                        localSelectedDays + dayIndex
                                    }.sorted()
                                }
                                .background(
                                    if (isSelected) Color(0xFF3B82F6) else Color.LightGray,
                                    CircleShape
                                )
                                .wrapContentSize(Alignment.Center)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time Range (only if not all day)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.time_range),
                    style = MaterialTheme.typography.subtitle1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically
                    ,horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text(stringResource(R.string.start_time))
                    Spacer(modifier = Modifier.width(160.dp))
                    Text(stringResource(R.string.end_time))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically
                ,horizontalArrangement = Arrangement.SpaceEvenly) {

                    TextButton(onClick = { /* TODO: Implement time picker for start time */ },
                        modifier = Modifier.background(Color.LightGray)) {
                        Text(
                            localStartTime?.format(DateTimeFormatter.ofPattern("hh:mm a"))
                                ?: "--:-- --", color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(50.dp))
                    Text("→", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(50.dp))

                    TextButton(onClick = { /* TODO: Implement time picker for end time */ },
                        modifier = Modifier.background(Color.LightGray)) {
                        Text(
                            localEndTime?.format(DateTimeFormatter.ofPattern("hh:mm a"))
                                ?: "--:-- --", color = Color.Black
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Buttons
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center
//        ) {
//            TextButton(onClick = onDismissRequest, modifier = Modifier.background(Color.White)
//                .clip(RoundedCornerShape(12.dp)).border(1.dp, Color.)) {
//                Text(stringResource(R.string.cancel), color = Color.Black)
//            }
//            Spacer(modifier = Modifier.width(80.dp))
//            TextButton(onClick = {
//                onScheduleSaved(
//                    localSelectedDays,
//                    if (!localIsAllDay) localStartTime else null,
//                    if (!localIsAllDay) localEndTime else null,
//                    localIsAllDay
//                )
//            },modifier = Modifier.background(Color.LightGray).clip(RoundedCornerShape(12.dp))) {
//                Text(stringResource(R.string.save), color = Color.Black)
//            }
//        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.cancel), color = Color(0xFF3B82F6)) // Same blue color as Save button
            }

            Spacer(modifier = Modifier.width(16.dp)) // Space between buttons

            Button(
                onClick = {
                onScheduleSaved(
                    localSelectedDays,
                    if (!localIsAllDay) localStartTime else null,
                    if (!localIsAllDay) localEndTime else null,
                    localIsAllDay
                )
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF3B82F6), // Your Save button blue
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(R.string.save))
            }
        }

    }
}
