package com.example.appblockerv3.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Objects


@Entity(
    tableName = "schedules_fact")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = false) val scheduleId: Long,
    val scheduleDaysBitMask: Int,
    val startHour: Int,
    val startMin: Int,
    val endHour: Int,
    val endMin: Int
){
    companion object {
        fun createWithGeneratedId(
            scheduleDaysBitMask: Int,
            startHour: Int,
            startMin: Int,
            endHour: Int,
            endMin: Int
        ): ScheduleEntity {
            // Use Objects.hash() for a clean and standard way to combine hashes
            val hash = Objects.hash(
                scheduleDaysBitMask,
                startHour,
                startMin,
                endHour,
                endMin
            ).toLong() // Convert the Int hash to Long

            return ScheduleEntity(
                scheduleId = hash,
                scheduleDaysBitMask = scheduleDaysBitMask,
                startHour = startHour,
                startMin = startMin,
                endHour = endHour,
                endMin = endMin
            )
        }
    }
}
