package com.example.appblockerv3.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.appblockerv3.data.db.entities.GroupBlockEntity
import com.example.appblockerv3.data.db.entities.GroupWithAppsAndSchedules
import kotlinx.coroutines.flow.Flow

@Dao
interface AppGroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppGroup(groupBlockEntity: GroupBlockEntity): Long

    @Update
    suspend fun updateAppGroup(groupBlockEntity: GroupBlockEntity)

    @Delete
    suspend fun deleteAppGroup(groupBlockEntity: GroupBlockEntity)

    @Query("SELECT * FROM groups_fact WHERE groupId = :groupId")
    fun getAppGroupById(groupId: Long): Flow<GroupBlockEntity?>

    @Query("SELECT * FROM groups_fact")
    fun getAllAppGroups(): Flow<List<GroupBlockEntity>>

    @Query("SELECT * FROM groups_fact WHERE isActive = 1")
    fun getActiveAppGroups(): Flow<List<GroupBlockEntity>>

    @Transaction // Essential for @Relation queries
    @Query("SELECT * FROM groups_fact")
    fun getAllGroupBlocksWithAppsAndSchedules(): Flow<List<GroupWithAppsAndSchedules>>
}