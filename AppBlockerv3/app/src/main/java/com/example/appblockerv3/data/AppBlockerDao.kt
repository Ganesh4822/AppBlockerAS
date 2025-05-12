package com.example.appblockerv3.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppBlockerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppGroup(appGroup: AppGroup): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSchedule(appSchedule: AppSchedule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupAppsJoin(join: GroupAppsJoin)

    @Query("SELECT * FROM app_group WHERE groupId = :groupId")
    suspend fun getAppGroupById(groupId: Long): AppGroup?

    @Query("SELECT * FROM app_schedule WHERE groupId = :groupId")
    suspend fun getAppSchedulesByGroupId(groupId: Long): List<AppSchedule>

    @Query("SELECT packageName FROM group_apps_join WHERE groupId = :groupId")
    suspend fun getAppsInGroup(groupId: Long): List<String>

    @Transaction
    @Query("SELECT * FROM app_group")
    fun getAllAppGroupsWithDetails(): Flow<List<AppGroupWithDetails>>

    @Transaction
    @Query("SELECT * FROM app_group WHERE groupId = :groupId")
    suspend fun getAppGroupWithDetails(groupId: Long): AppGroupWithDetails?
}

data class AppGroupWithDetails(
    @Embedded val appGroup: AppGroup,
    @Relation(
        parentColumn = "groupId",
        entityColumn = "groupId"
    )
    val schedules: List<AppSchedule>,
    @Relation(
        parentColumn = "groupId",
        entityColumn = "groupId",
        associateBy = Junction(GroupAppsJoin::class)
    )
    val appList: List<GroupAppsJoin> // We might just need package names here, adjust later
)