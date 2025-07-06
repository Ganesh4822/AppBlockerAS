package com.example.appblockerv3.data.db.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appblockerv3.data.db.entities.GroupAppsJoinEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupAppsJoinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupAppsJoin(join: GroupAppsJoinEntity)

    @Query("DELETE FROM dm_apps_group WHERE groupId = :groupId AND packageName = :packageName")
    suspend fun deleteGroupAppJoin(groupId: Long, packageName: String)

    @Query("SELECT packageName FROM dm_apps_group WHERE groupId = :groupId")
    fun getAppsInGroup(groupId: Long): Flow<List<String>>

    @Query("SELECT groupId FROM dm_apps_group WHERE packageName = :packageName")
    fun getGroupsForApp(packageName: String): Flow<List<Long>>

    @Query("DELETE FROM dm_apps_group WHERE groupId = :groupId")
    suspend fun deleteAllAppsInGroup(groupId: Long)

    @Query("select * from dm_apps_group")
    fun getAllGroupAppsJoin(): Flow<List<GroupAppsJoinEntity>>

    @Query("SELECT COUNT(groupId) FROM dm_apps_group WHERE packageName = :packageName")
    fun getGroupCountForApp(packageName: String): Flow<Int>

    @Query("SELECT packageName, COUNT(groupId) AS groupCount FROM dm_apps_group GROUP BY packageName")
    fun getAppsWithGroupCounts(): Flow<List<AppPackageNameWithGroupCount>>


}

data class AppPackageNameWithGroupCount(
    val packageName: String,
    val groupCount: Int
)