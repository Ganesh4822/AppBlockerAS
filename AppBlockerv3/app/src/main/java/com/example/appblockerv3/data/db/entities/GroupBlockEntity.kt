package com.example.appblockerv3.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Objects

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
){
    companion object {
        fun createWithGeneratedId(
            groupName: String,
            usageLimitHours: Int,
            usageLimitMinutes: Int,
            scheduleID1: Long? = null,
            scheduleID2: Long? = null,
            isActive: Boolean = true,
            createdAt: Long = System.currentTimeMillis(),
            updatedAt: Long = System.currentTimeMillis()
        ): GroupBlockEntity {
            // Use Objects.hash() for a clean and standard way to combine hashes
            val hash = Objects.hash(
                groupName,
                usageLimitHours,
                usageLimitMinutes,
                scheduleID1,
                scheduleID2).toLong()

            return GroupBlockEntity(
                groupId = hash,
                groupName = groupName,
                usageLimitHours = usageLimitHours,
                usageLimitMinutes = usageLimitMinutes,
                scheduleID1=scheduleID1,
                scheduleID2=scheduleID2,
                isActive = isActive,
                createdAt = createdAt,
                updatedAt = updatedAt)
        }

    }
}

