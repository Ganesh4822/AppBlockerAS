package com.example.appblockerv3.utils.bottomsheets

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appblockerv3.R


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
