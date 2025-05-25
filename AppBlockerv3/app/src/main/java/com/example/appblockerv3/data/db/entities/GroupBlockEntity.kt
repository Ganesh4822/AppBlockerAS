package com.example.appblockerv3.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "groups_fact")
data class GroupBlockEntity(
    @PrimaryKey(autoGenerate = true) val groupId: Long = 0,
    val groupName: String,
    val usageLimitHours: Int = 0,
    val usageLimitMinutes: Int = 0,
    // Foreign keys to AppSchedule

    val scheduleID1: Long? = null, // Can be null if no first schedule
    val scheduleID2: Long? = null, // Can be null if no second schedule

    val isActive: Boolean = true, // Default to active

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

