package com.example.appblockerv3.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.appblockerv3.R
import com.example.appblockerv3.data.db.coverters.DaysOfWeek
import com.example.appblockerv3.data.db.entities.ScheduleEntity
import com.example.appblockerv3.utils.bottomsheets.BlockOnScheduleBottomSheet
import com.example.appblockerv3.utils.bottomsheets.DailyUsageLimitBottomSheet
import com.example.appblockerv3.utils.composeItems.ScheduleItem
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun IndividualBlockOptions(
    onNavigateBack: () -> Unit,
    selectedAppPackageName: String,
    onSaveSettings: (
        packageName: String,
        schedules: List<ScheduleEntity>, // we will save 2 schedules per app.
        usageLimitHours: Int,
        usageLimitMinutes: Int
    ) -> Unit // onSaveSettings
) {
    Log.d("IndividualBlockOptions", "selectedAppPackageName: $selectedAppPackageName")
    val context = LocalContext.current
    val packageManager = context.packageManager
    val appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(selectedAppPackageName, 0)).toString()
    Log.d("IndividualBlockOptions", "selectedAppName: $appName")
    val scheduleBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    // State to hold the schedule data.  Using rememberSaveable for surviving config changes.
    var selectedDays : Set<DaysOfWeek> by remember { mutableStateOf(emptySet()) }
    var startTime by remember { mutableStateOf<LocalTime?>(LocalTime.of(9, 0)) }
    var endTime by remember { mutableStateOf<LocalTime?>(LocalTime.of(17, 0)) }
    var isAllDay by remember { mutableStateOf(false) }

    //To save the list of schedules
    val savedSchedules = remember { mutableStateListOf<ScheduleEntity>() }

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

                    val schedule2 = ScheduleEntity(
                        scheduleDaysBitMask = DaysOfWeek.toBitmask(days),
                        startHour = start!!.hour,
                        startMin = start.minute,
                        endHour = end!!.hour,
                        endMin = end.minute
                    )
                    if (savedSchedules.size < 2) { // Limit to two schedules
                        savedSchedules.add(schedule2)
                    } else {
                        // Optionally show a message to the user that they can only add two schedules
                        println("Maximum of two schedules allowed per group.")
                    }
                    scope.launch { scheduleBottomSheetState.hide() }
                },
                initialSelectedDays  = selectedDays,
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
                        title = {Text(text = appName)},
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
                            onSaveSettings(selectedAppPackageName
                                ,savedSchedules
                                ,usageLimitHours
                                ,usageLimitMinutes
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .background(Color(0xFFE8EAFF)),
                        //enabled = groupName.isNotBlank() // Disable if group name is empty
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
                            if(savedSchedules.size < 2){
                                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_schedule))
                            }
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
