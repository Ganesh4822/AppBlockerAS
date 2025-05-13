package com.example.appblockerv3.utils.composeItems

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.appblockerv3.data.AppSchedule

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
