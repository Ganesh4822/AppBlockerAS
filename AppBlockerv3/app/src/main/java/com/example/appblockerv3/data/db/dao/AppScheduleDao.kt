package com.example.appblockerv3.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import com.example.appblockerv3.data.db.entities.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSchedule(appSchedule: ScheduleEntity): Long

    @Update
    suspend fun updateAppSchedule(appSchedule: ScheduleEntity)

    @Delete
    suspend fun deleteAppSchedule(appSchedule: ScheduleEntity)

    @Query("SELECT * FROM schedules_fact WHERE scheduleId = :scheduleId")
    fun getAppScheduleById(scheduleId: Long): Flow<ScheduleEntity?>

    @Query("SELECT * FROM schedules_fact")
    fun getAllAppSchedules(): Flow<List<ScheduleEntity>>
}