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
import com.example.appblockerv3.data.db.coverters.DaysOfWeek
import com.example.appblockerv3.data.db.entities.ScheduleEntity

@Composable
fun ScheduleItem(schedule: ScheduleEntity, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {

        val daysSet = DaysOfWeek.fromBitmask(schedule.scheduleDaysBitMask)
        val daysText2 = daysSet.joinToString(", "){  day->
            when(day){
                DaysOfWeek.MONDAY -> "M"
                DaysOfWeek.TUESDAY -> "TU"
                DaysOfWeek.WEDNESDAY -> "W"
                DaysOfWeek.THURSDAY -> "TH"
                DaysOfWeek.FRIDAY -> "F"
                DaysOfWeek.SATURDAY -> "SA"
                DaysOfWeek.SUNDAY -> "SU"
            }
        }
        val startTimeString = String.format("%02d:%02d", schedule.startHour, schedule.startMin)
        val endTimeString = String.format("%02d:%02d", schedule.endHour, schedule.endMin)
        val timeText = "$startTimeString - $endTimeString"

        Column {
            Text(text = daysText2, style = MaterialTheme.typography.body1)
            Text(text = timeText, style = MaterialTheme.typography.body2)
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Close, contentDescription = "Delete", tint = Color.Red)
        }
    }
}
