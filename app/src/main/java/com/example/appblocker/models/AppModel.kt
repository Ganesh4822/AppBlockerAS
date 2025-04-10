package com.example.appblocker.models

import android.graphics.drawable.Drawable

data class AppModel(
    val appName: String,
    val packageName: String,
    val icon: Drawable,
    val isBlocked: Boolean
)
