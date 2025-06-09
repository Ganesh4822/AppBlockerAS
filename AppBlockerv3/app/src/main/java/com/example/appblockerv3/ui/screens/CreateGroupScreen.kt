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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.appblockerv3.R
import com.example.appblockerv3.data.AppSchedule
import com.example.appblockerv3.data.db.AppBlockerDatabase
import com.example.appblockerv3.data.db.coverters.DaysOfWeek
import com.example.appblockerv3.data.db.entities.ScheduleEntity
import com.example.appblockerv3.utils.bottomsheets.BlockOnScheduleBottomSheet
import com.example.appblockerv3.utils.bottomsheets.DailyUsageLimitBottomSheet
import com.example.appblockerv3.utils.composeItems.ScheduleItem
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
        schedules: List<ScheduleEntity>, // we will save the list of schdules
        usageLimitHours: Int,
        usageLimitMinutes: Int
    ) -> Unit // Updated onSaveGroup
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    var groupName by remember { mutableStateOf("") }

    val daoGroup = AppBlockerDatabase.getDatabase(context).appGroupDao();
    val daoSchedule = AppBlockerDatabase.getDatabase(context).appScheduleDao();
    val daoGroupAppjoin = AppBlockerDatabase.getDatabase(context).groupAppsJoinDao();

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
    var selectedDays : Set<DaysOfWeek> by remember { mutableStateOf(emptySet()) }
    var startTime by remember { mutableStateOf<LocalTime?>(LocalTime.of(9, 0)) }
    var endTime by remember { mutableStateOf<LocalTime?>(LocalTime.of(17, 0)) }
    var isAllDay by remember { mutableStateOf(false) }

    //schedule entity Iterm
    var startTimeHrs by remember { mutableStateOf(9) }
    var endTimeHrs by remember { mutableStateOf(9) }
    var startTimemins by remember { mutableStateOf(30) }
    var endTimemins by remember { mutableStateOf(30) }
    var selectedDaysBitmask by remember { mutableStateOf(0) }
    val savedSchedules2 =  remember { mutableStateListOf<ScheduleEntity>()}

    //To save the list of schedules
    val savedSchedules = remember { mutableStateListOf<AppSchedule>() }

    // State for Daily Usage Limit
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
                    if (savedSchedules2.size < 2) { // Limit to two schedules
                        savedSchedules2.add(schedule2)
                    } else {
                        // Optionally show a message to the user that they can only add two schedules
                        println("Maximum of two schedules allowed per group.")
                    }
                    scope.launch { scheduleBottomSheetState.hide() }
                },
                initialSelectedDays = selectedDays,
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
                                ,savedSchedules2
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

                        ) {
                            Text(stringResource(R.string.block_on_schedule), modifier = Modifier.weight(1f))
                            if(savedSchedules2.size < 2){
                                IconButton(onClick = { scope.launch { scheduleBottomSheetState.show() } }) {
                                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_schedule))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        //show selected days and time range Here
                        if (savedSchedules2.isNotEmpty()) {
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
                                savedSchedules2.forEachIndexed { index, schedule ->
                                    ScheduleItem(
                                        schedule = schedule,
                                        onDelete = { savedSchedules2.removeAt(index) }
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