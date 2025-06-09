package com.example.appblockerv3.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(
    tableName = "schedules_fact")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val scheduleId: Long = 0,
    val scheduleDaysBitMask: Int,
    val startHour: Int,
    val startMin: Int,
    val endHour: Int,
    val endMin: Int
)
