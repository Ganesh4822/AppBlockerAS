package com.example.appblockerv3.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.appblockerv3.data.db.dao.AppGroupDao
import com.example.appblockerv3.data.db.dao.AppScheduleDao
import com.example.appblockerv3.data.db.dao.GroupAppsJoinDao
import com.example.appblockerv3.data.db.dao.IndividualAppBlockDao
import com.example.appblockerv3.data.db.entities.GroupAppsJoinEntity
import com.example.appblockerv3.data.db.entities.GroupBlockEntity
import com.example.appblockerv3.data.db.entities.IndividualBlockEntity
import com.example.appblockerv3.data.db.entities.ScheduleEntity


@Database(
    entities = [
        GroupBlockEntity::class,
        ScheduleEntity::class,
        IndividualBlockEntity::class,
        GroupAppsJoinEntity::class
    ],
    version = 1,
    exportSchema = false // Set to true to export schema into a folder for version control
)

abstract  class AppBlockerDatabase  : RoomDatabase() {
    abstract fun appGroupDao(): AppGroupDao
    abstract fun appScheduleDao(): AppScheduleDao
    abstract fun individualBlockDao(): IndividualAppBlockDao
    abstract fun groupAppsJoinDao(): GroupAppsJoinDao

    companion object {
        @Volatile
        private var INSTANCE: AppBlockerDatabase? = null

        fun getDatabase(context: Context): AppBlockerDatabase {
            // If the INSTANCE is not null, then return it,
            // if it is, then create the database
            //Only one thread will access the database at once to avoid race conditions.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppBlockerDatabase::class.java,
                    "app_blocker_database"
                )

                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}