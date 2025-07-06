package com.example.appblockerv3.data.repository

import android.util.Log

import com.example.appblockerv3.data.db.dao.AppGroupDao
import com.example.appblockerv3.data.db.dao.AppScheduleDao
import com.example.appblockerv3.data.db.dao.GroupAppsJoinDao
import com.example.appblockerv3.data.db.dao.IndividualAppBlockDao
import com.example.appblockerv3.data.db.entities.AppBlockingStatus
import com.example.appblockerv3.data.db.entities.GroupAppsJoinEntity
import com.example.appblockerv3.data.db.entities.GroupBlockEntity
import com.example.appblockerv3.data.db.entities.GroupWithAppsAndSchedules
import com.example.appblockerv3.data.db.entities.ScheduleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class BlockingRepository(
    private val appGroupDao: AppGroupDao,
    private val appScheduleDao: AppScheduleDao,
    private val groupAppsJoinDao: GroupAppsJoinDao,
    private val individualAppBlockDao: IndividualAppBlockDao
) {

    fun getAppBlockingStatus(packageName: String): Flow<AppBlockingStatus> {
        return combine(
            individualAppBlockDao.getIndividualAppBlock(packageName),
            groupAppsJoinDao.getGroupCountForApp(packageName)
        ) { individualBlock, groupCount ->
            AppBlockingStatus(
                packageName = packageName,
                isIndividuallyBlocked = individualBlock != null,
                groupCount = groupCount
            )
        }
    }

    fun getAllAppsBlockingStatus(): Flow<Map<String, AppBlockingStatus>> {
        return combine(
            individualAppBlockDao.getAllIndividualBlockedAppPackageNames(),
            groupAppsJoinDao.getAppsWithGroupCounts()
        ) { individuallyBlockedApps, appsInGroupsWithCounts ->
            val statusMap = mutableMapOf<String, AppBlockingStatus>()

            // Add individually blocked apps
            individuallyBlockedApps.forEach { packageName ->
                statusMap[packageName] = AppBlockingStatus(
                    packageName = packageName,
                    isIndividuallyBlocked = true,
                    groupCount = statusMap[packageName]?.groupCount ?: 0
                )
            }

            // Add apps in groups
            appsInGroupsWithCounts.forEach { appWithCount ->
                statusMap[appWithCount.packageName] = AppBlockingStatus(
                    packageName = appWithCount.packageName,
                    isIndividuallyBlocked = statusMap[appWithCount.packageName]?.isIndividuallyBlocked ?: false,
                    groupCount = appWithCount.groupCount
                )
            }
            statusMap
        }
    }

    suspend fun insertAppGroup(groupBlock: GroupBlockEntity): Long {
        return appGroupDao.insertAppGroup(groupBlock)
    }




    suspend fun insertSchedule(schedule: ScheduleEntity): Long {
        return appScheduleDao.insertAppSchedule(schedule)
    }



    // --- GroupAppsJoin Operations ---
    suspend fun insertGroupAppsJoin(join: GroupAppsJoinEntity) {
        groupAppsJoinDao.insertGroupAppsJoin(join)
    }

    fun getAllGroupBlocksWithDetails(): Flow<List<GroupWithAppsAndSchedules>> {
        return appGroupDao.getAllGroupBlocksWithAppsAndSchedules()
    }


    /**
     * Saves a new group, its schedules, and associated apps to the database.
     * Handles the specific linking logic to schedules data model (scheduleID1, scheduleID2).
     */
    suspend fun saveNewGroup(
        groupName: String,
        appList: List<String>,
        schedules: List<ScheduleEntity>,
        usageLimitHours: Int,
        usageLimitMinutes: Int
    ): Long = withContext(Dispatchers.IO) { // Ensure this runs on an IO dispatcher

        //First insert schedules to get their IDs
        var scheduleId1: Long? = null
        var scheduleId2: Long? = null

        if (schedules.isNotEmpty()) {
            scheduleId1 = appScheduleDao.insertAppSchedule(schedules[0])
            if (schedules.size > 1) {
                scheduleId2 = appScheduleDao.insertAppSchedule(schedules[1])
            }
        }

        //Create the GroupBlockEntity with schedule IDs above scheduleIDs
        val newGroupBlock = GroupBlockEntity.createWithGeneratedId(
            groupName = groupName,
            usageLimitHours = usageLimitHours,
            usageLimitMinutes = usageLimitMinutes,
            scheduleID1 = scheduleId1,
            scheduleID2 = scheduleId2,
            isActive = true, // Default to active for new Groups
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        //Insert the GroupBlockEntity
        val groupId = appGroupDao.insertAppGroup(newGroupBlock)

        //Insert entries into the GroupAppsJoinEntity for each selected app
        appList.forEach { packageName ->
            groupAppsJoinDao.insertGroupAppsJoin(
                GroupAppsJoinEntity(groupId = groupId, packageName = packageName)
            )
        }

        val allgroups = appGroupDao.getAllAppGroups()
        Log.d("BlockingRepository", "All Groups: $allgroups")
        val allSchedules = appScheduleDao.getAllAppSchedules()
        Log.d("BlockingRepository", "All allSchedules: $allSchedules")
        groupId
    }
}