package com.example.appblockerv3

import android.app.Application
import com.example.appblockerv3.data.db.AppBlockerDatabase import com.example.appblockerv3.data.repository.BlockingRepository


class AppBlockerApplication : Application() {

    // Lazily create the database instance. It will be created only when first accessed.
    val database: AppBlockerDatabase by lazy {
        AppBlockerDatabase.getDatabase(this)
    }

    // passing the DAOs is also a common pattern, hence commented here
    // val repository: BlockingRepository by lazy {
    //     BlockingRepository(
    //         appGroupDao = database.appGroupDao(),
    //         appScheduleDao = database.appScheduleDao(),
    //         groupAppsJoinDao = database.groupAppsJoinDao(),
    //         individualAppBlockDao = database.individualAppBlockDao()
    //     )
    // }

    override fun onCreate() {
        super.onCreate()
        // Any other application-wide initialization you might need later
    }
}