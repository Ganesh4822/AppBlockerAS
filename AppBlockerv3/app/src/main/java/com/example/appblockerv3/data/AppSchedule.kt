package com.example.appblockerv3.data

data class AppSchedule(
    val groupId: Long = 0, // Unique Id to store data in DB
    val days: String, // 1,2,3 for Monday, Tuesday, Wednesday
    val startTime: String?, // Format as "hh:mm a" "09:00 AM")
    val endTime: String?,
    val isAllDay: Boolean = false
)
