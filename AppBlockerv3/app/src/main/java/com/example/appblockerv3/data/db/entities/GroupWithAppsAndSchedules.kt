// In a new file, e.g., com.example.appblockerv3.data.db.entities.GroupWithAppsAndSchedules.kt
package com.example.appblockerv3.data.db.entities

import androidx.room.Embedded
import androidx.room.Relation

// This POJO will be used to query a GroupBlockEntity along with its associated Apps and Schedules
data class GroupWithAppsAndSchedules(
    @Embedded val groupBlock: GroupBlockEntity,

    @Relation(
        parentColumn = "groupId",
        entityColumn = "groupId",
        associateBy = androidx.room.Junction(GroupAppsJoinEntity::class)
    )
    val groupAppJoins: List<GroupAppsJoinEntity>,

    // Relation for scheduleID1
    @Relation(
        parentColumn = "scheduleID1",
        entityColumn = "scheduleId"
    )
    val schedule1: ScheduleEntity?, // Nullable because scheduleID1 can be null

    // Relation for scheduleID2
    @Relation(
        parentColumn = "scheduleID2",
        entityColumn = "scheduleId"
    )
    val schedule2: ScheduleEntity? // Nullable because scheduleID2 can be null
) {
    // Helper property to combine the two potential schedules into a list for easier UI iteration
    val appPackageNames: List<String>
        get() = groupAppJoins.map { it.packageName }

    val schedules: List<ScheduleEntity>
        get() = listOfNotNull(schedule1, schedule2)

    // Helper property to calculate total usage limit in minutes
    val totalUsageLimitMinutes: Int
        get() = (groupBlock.usageLimitHours * 60) + groupBlock.usageLimitMinutes
}