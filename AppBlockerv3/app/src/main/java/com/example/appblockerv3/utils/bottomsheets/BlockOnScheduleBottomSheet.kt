package com.example.appblockerv3.utils.bottomsheets

import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.appblockerv3.R
import com.example.appblockerv3.data.db.coverters.DaysOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

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
    onScheduleSaved: (Set<DaysOfWeek>, LocalTime?, LocalTime?, Boolean) -> Unit,
    initialSelectedDays: Set<DaysOfWeek>,
    startTime: LocalTime?,
    endTime: LocalTime?,
    isAllDay: Boolean
) {
    val context = LocalContext.current

    var selectedDays by remember { mutableStateOf(initialSelectedDays) }

    var localDaysSet : Set<DaysOfWeek> by remember { mutableStateOf(emptySet()) }
    var localStartTime by remember { mutableStateOf(startTime ?: LocalTime.of(9, 0)) }
    var localEndTime by remember { mutableStateOf(endTime ?: LocalTime.of(17, 0)) }
    var localIsAllDay by remember { mutableStateOf(isAllDay) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val dayLabels = listOf("M", "TU", "W", "TH", "F", "SA","SU")
    val daysLabelsMap = mapOf("M" to DaysOfWeek.MONDAY,
                                "TU" to DaysOfWeek.TUESDAY,
                                "W" to DaysOfWeek.WEDNESDAY,
                                "TH" to DaysOfWeek.THURSDAY,
                                "F" to DaysOfWeek.FRIDAY,
                                "SA" to DaysOfWeek.SATURDAY,
                                "SU" to DaysOfWeek.SUNDAY,)

    LaunchedEffect(localIsAllDay) {
        if (localIsAllDay) {
            selectedDays = DaysOfWeek.entries.toSet()
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
                        val dayOfWeek = daysLabelsMap[label]
                        val isSelected = selectedDays.contains(dayOfWeek)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable(enabled = !isAllDay) {

                                    if (dayOfWeek != null) { // Null check for safety
                                        selectedDays = if (isSelected) {
                                            selectedDays - dayOfWeek
                                        } else {
                                            selectedDays + dayOfWeek
                                        }
                                    }
                                    Log.d("days", "selected days are2 : $localDaysSet ")
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
                    if (selectedDays.isEmpty()) {
                        Toast.makeText(context, "Please select at least one day.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (startTime != null) {
                        if (startTime.isAfter(endTime) || startTime == endTime) {
                            Toast.makeText(context, "End time must be after start time.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                    }
                    onScheduleSaved(
                        selectedDays,
                        localStartTime,
                        localEndTime,
                        localIsAllDay
                    )
                    Log.d("Days", "selected days are : $selectedDays")
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
