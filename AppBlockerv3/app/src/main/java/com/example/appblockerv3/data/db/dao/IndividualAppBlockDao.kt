package com.example.appblockerv3.data.db.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.appblockerv3.data.db.entities.IndividualBlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IndividualAppBlockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIndividualAppBlock(block: IndividualBlockEntity): Long

    @Update
    suspend fun updateIndividualAppBlock(block: IndividualBlockEntity)

    @Delete
    suspend fun deleteIndividualAppBlock(block: IndividualBlockEntity)

    @Query("SELECT * FROM individual_blocks_fact WHERE blockId = :blockId")
    fun getIndividualAppBlockById(blockId: Long): Flow<IndividualBlockEntity?>

    @Query("SELECT * FROM individual_blocks_fact WHERE packageName = :packageName")
    fun getIndividualAppBlockByPackageName(packageName: String): Flow<IndividualBlockEntity?> // Assuming one individual block per app

    @Query("SELECT * FROM individual_blocks_fact")
    fun getAllIndividualAppBlocks(): Flow<List<IndividualBlockEntity>>

    @Query("SELECT * FROM individual_blocks_fact WHERE isActive = 1")
    fun getActiveIndividualAppBlocks(): Flow<List<IndividualBlockEntity>>

    @Query("SELECT * FROM individual_blocks_fact WHERE packageName = :packageName")
    fun getIndividualAppBlock(packageName: String): Flow<IndividualBlockEntity?>

    @Query("SELECT packageName FROM individual_blocks_fact")
    fun getAllIndividualBlockedAppPackageNames(): Flow<List<String>>
}