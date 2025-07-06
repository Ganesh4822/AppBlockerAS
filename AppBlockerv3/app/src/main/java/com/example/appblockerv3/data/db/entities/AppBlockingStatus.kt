package com.example.appblockerv3.data.db.entities

data class AppBlockingStatus(
    val packageName: String,
    val isIndividuallyBlocked: Boolean,
    val groupCount: Int // How many groups this app is part of
)