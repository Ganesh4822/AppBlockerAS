package com.example.appblockerv3.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_group")
data class AppGroup(
    @PrimaryKey(autoGenerate = true) val groupId: Long = 0,
    val groupName: String,
    val usageLimitHours: Int = 0,
    val usageLimitMinutes: Int = 0
)
