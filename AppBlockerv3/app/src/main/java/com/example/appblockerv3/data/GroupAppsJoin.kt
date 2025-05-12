package com.example.appblockerv3.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "group_apps_join",
    primaryKeys = ["groupId", "packageName"],
    foreignKeys = [
        ForeignKey(
            entity = AppGroup::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GroupAppsJoin(
    val groupId: Long,
    val packageName: String
)