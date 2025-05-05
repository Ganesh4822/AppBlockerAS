package com.example.appblockerv3.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.example.appblockerv3.R
import com.example.appblockerv3.data.AppSchedule
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class AppData(val packageName: String, val appName: String, val icon: Drawable?)

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

/*
This is a create group screen.
This screen contains the name of the group, the apps to be blocked, which are selected from the
app selection screen.
This screen also contains the block on schedule section and daily usage limit button.
*/

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CreateGroupScreen(
    onNavigateBack: () -> Unit,
    selectedAppPackageNames: List<String>,
    onSaveGroup: (
        groupName: String,
        appList: List<String>,
        schedules: List<AppSchedule>, // we will save the list of schdules
        usageLimitHours: Int,
        usageLimitMinutes: Int
    ) -> Unit // Updated onSaveGroup
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

    //To save the list of schedules
    val savedSchedules = remember { mutableStateListOf<AppSchedule>() }

    // State for Daily Usage Limit
    var showUsageLimitBottomSheet by remember { mutableStateOf(false) }
    val usageLimitBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    var usageLimitHours by rememberSaveable { mutableStateOf(0) }
    var usageLimitMinutes by rememberSaveable { mutableStateOf(0) }

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
                    val schedule = AppSchedule(
                        days = days.joinToString(","),
                        startTime = start?.format(DateTimeFormatter.ofPattern("hh:mm a")),
                        endTime = end?.format(DateTimeFormatter.ofPattern("hh:mm a")),
                        isAllDay = allDay
                    )
                    if (savedSchedules.size < 2) { // Limit to two schedules
                        savedSchedules.add(schedule)
                    } else {
                        // Optionally show a message to the user that they can only add two schedules
                        println("Maximum of two schedules allowed per group.")
                    }
                    scope.launch { scheduleBottomSheetState.hide() }
                },
                selectedDays = selectedDays,
                startTime = startTime,
                endTime = endTime,
                isAllDay = isAllDay
            )
        }
    ) {
        ModalBottomSheetLayout(
            sheetState = usageLimitBottomSheetState,
            sheetContent = {
                DailyUsageLimitBottomSheet(
                    onDismissRequest = { scope.launch { usageLimitBottomSheetState.hide() } },
                    onUsageLimitSaved = { hours, minutes ->
                        usageLimitHours = hours
                        usageLimitMinutes = minutes
                        scope.launch { usageLimitBottomSheetState.hide() }
                    },
                    initialHours = usageLimitHours,
                    initialMinutes = usageLimitMinutes
                )
            }
        ){
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
                            onSaveGroup(groupName
                                ,selectedAppPackageNames
                                ,savedSchedules
                                ,usageLimitHours
                                ,usageLimitMinutes
                                )
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
                    // Name of the group
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
                    Spacer(modifier = Modifier.height(16.dp))

                    // Apps Section
                    AppsSection(selectedAppsInfo)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Block on a Schedule
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .background(Color(0xFFE8EAFF))
                    ){
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

                        //show selected days and time range Here
                        if (savedSchedules.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .border(
                                        width = 1.dp,
                                        color = Color.LightGray,
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    "Schedules:",
                                    style = MaterialTheme.typography.subtitle1,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                savedSchedules.forEachIndexed { index, schedule ->
                                    ScheduleItem(
                                        schedule = schedule,
                                        onDelete = { savedSchedules.removeAt(index) }
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .border(
                                        width = 1.dp,
                                        color = Color.LightGray,
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    "No schedules added.",
                                    style = MaterialTheme.typography.body2,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    // Daily Usage Limit
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .background(Color(0xFFE8EAFF))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .clickable { scope.launch {
                                usageLimitBottomSheetState.show()
                            } }
                    ) {
                        Text(stringResource(R.string.daily_usage_limit), modifier = Modifier.weight(1f))
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_usage_limit))
                    }
                    //Show Time data if the usage limit data is selected
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .border(
                                width = 1.dp,
                                color = Color.LightGray, // Border color
                            )
                    ){
                        if (usageLimitHours != 0 || usageLimitMinutes != 0) {
                            Text(
                                text = "Max : ${usageLimitHours} Hrs ${usageLimitMinutes} Mins",
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun ScheduleItem(schedule: AppSchedule, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        val daysText = schedule.days.split(",").joinToString(" ") { day ->
            when (day.toInt()) {
                1 -> "M"
                2 -> "T"
                3 -> "W"
                4 -> "T"
                5 -> "F"
                6 -> "S"
                7 -> "S"
                else -> ""
            }
        }
        val timeText = if (schedule.isAllDay) "All Day" else "${schedule.startTime} - ${schedule.endTime}"

        Column {
            Text(text = daysText, style = MaterialTheme.typography.body1)
            Text(text = timeText, style = MaterialTheme.typography.body2)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Close, contentDescription = "Delete", tint = Color.Red)
        }
    }
}

/*
This is the bottom sheet that represents the block on schedule section.
When user clicks on the block on the schedule button, this bottom sheet will appear.
This consists the days, start time, end time, and all day checkbox.
when All days check box is checked, all the days in calender will be selected.
When the schedule is set, Apps will be blocked on the selected schedule.
 */
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
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    LaunchedEffect(localIsAllDay) {
        if (localIsAllDay) {
            localSelectedDays = (1..7).toList() // Select all days
        } else if (localSelectedDays.size == 7) {
            localSelectedDays = emptyList()
        }
    }

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
                                    localSelectedDays = if (localIsAllDay) {
                                        (1..7).toList()
                                    }
                                    else{
                                        if (isSelected) {
                                            localSelectedDays.filter { it != dayIndex }
                                        } else {
                                            localSelectedDays + dayIndex
                                        }.sorted()
                                    }
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

        //Time range
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

                    TextButton(onClick = { showStartTimePicker = true },
                        modifier = Modifier.background(Color.LightGray)) {
                        Text(
                            localStartTime?.format(DateTimeFormatter.ofPattern("hh:mm a"))
                                ?: "--:-- --", color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(50.dp))
                    Text("→", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(50.dp))

                    TextButton(onClick = { showEndTimePicker = true },
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
                     localStartTime,
                    localEndTime,
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
        if (showStartTimePicker) {
            localStartTime?.let {
                ShowTimePickerDialog(context = LocalContext.current,
                    onDismissRequest = { showStartTimePicker = false},
                    onTimeSelected = { hour, minute ->
                        localStartTime = LocalTime.of(hour, minute)
                        showStartTimePicker = false
                    },
                    initialTime = it
                )
            }
        }

        // End Time Picker Dialog
        if (showEndTimePicker) {
            localEndTime?.let {
                ShowTimePickerDialog(context = LocalContext.current,
                    onDismissRequest = { showEndTimePicker = false },
                    onTimeSelected = { hour, minute ->
                        localEndTime = LocalTime.of(hour, minute)
                        showEndTimePicker = false
                    },
                    initialTime = it
                )
            }
        }

    }

}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ShowTimePickerDialog(
    context: Context,
    onDismissRequest: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    initialTime: LocalTime
) {
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onTimeSelected(hourOfDay, minute)
        },
        initialTime.hour,
        initialTime.minute,
        true // Use false for 12-hour format
    )
    timePickerDialog.setOnDismissListener {
        onDismissRequest()
    }
    // Wrap in a Dialog to integrate with Compose
    Dialog(onDismissRequest = onDismissRequest) {
        // Empty Surface, the TimePickerDialog will draw itself
        Surface(color = Color.Transparent) {
            DisposableEffect(Unit) {
                timePickerDialog.show()
                onDispose {
                    timePickerDialog.dismiss()
                }
            }
        }
    }
}


/*
    This is Bottom sheet which is used to pick hours and minuts limit on overall app usage
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DailyUsageLimitBottomSheet(
    onDismissRequest: () -> Unit,
    onUsageLimitSaved: (hours: Int, minutes: Int) -> Unit,
    initialHours: Int,
    initialMinutes: Int
) {
    var localHours by remember { mutableStateOf(initialHours) }
    var localMinutes by remember { mutableStateOf(initialMinutes) }

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
            Text(stringResource(R.string.daily_usage_limit), style = MaterialTheme.typography.h6)
            IconButton(onClick = onDismissRequest) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close))
            }
        }



        Spacer(modifier = Modifier.height(16.dp))
        // Buttons
        TimeLimitPicker(
            selectedHour = localHours,
            selectedMinute = localMinutes,
            onHourSelected = { localHours = it },
            onMinuteSelected = { localMinutes = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.cancel), color = Color(0xFF3B82F6)) // Same blue color as Save button
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    onUsageLimitSaved(
                        localHours,
                        localMinutes
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

@Composable
fun TimeLimitPicker(
    selectedHour: Int,
    selectedMinute: Int,
    onHourSelected: (Int) -> Unit,
    onMinuteSelected: (Int) -> Unit
) {
    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Left: Hours
        NumberPicker(
            values = hours,
            selectedValue = selectedHour,
            onValueSelected = onHourSelected,
            label = "Hrs"
        )


        Box(
            modifier = Modifier
                .height(100.dp)
                .width(1.dp)
                .background(Color.Gray.copy(alpha = 0.5f))
        )

        NumberPicker(
            values = minutes,
            selectedValue = selectedMinute,
            onValueSelected = onMinuteSelected,
            label = "Mins"
        )
    }
}


@Composable
fun NumberPicker(
    values: List<Int>,
    selectedValue: Int,
    onValueSelected: (Int) -> Unit,
    label: String
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = values.indexOf(selectedValue).coerceAtLeast(0)
    )

    LaunchedEffect(listState.isScrollInProgress.not()) {
        val index = listState.firstVisibleItemIndex + 1
        if (index in values.indices) {
            onValueSelected(values[index])
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)

        LazyColumn(
            state = listState,
            modifier = Modifier.height(120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            itemsIndexed(values) { index, item ->
                val isSelected = item == selectedValue
                Text(
                    text = item.toString().padStart(2, '0'),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = Color.Black.copy(alpha = if (isSelected) 1f else 0.4f),
                    fontSize = 24.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

/*
Package_name :id
group_id
group_name :
schedule_id : [SHA,SHA]
usage_limit (minutes)
*/

/*
  dm_schedules

  schedule_id : SHA(days,start_time,end_time)
  days : 1,2,3,4,5
  start_time :
  end_time :
*/

// schedule_id 0 ,days=2,3,4,5,6, startTime=10:00 AM, endTime=07:00 AM