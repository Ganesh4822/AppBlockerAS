package com.example.appblockerv3.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "dm_apps_group",
    primaryKeys = ["groupId", "packageName"],
    foreignKeys = [
        ForeignKey(
            entity = GroupBlockEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE // If a group is deleted, its entries in this join table are also deleted
        )
    ]
)
data class GroupAppsJoinEntity(
    val groupId: Long,
    val packageName: String
)

