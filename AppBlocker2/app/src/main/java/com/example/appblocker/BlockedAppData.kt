package com.example.appblocker



import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
data class Schedule(
    var DaysArray : MutableSet<String>,
    val startTime_endTime: String
)

data class BlockedAppData(
    var isBlockingEnabled: Boolean = false,
    var isScheduleBasedBlockingEnabled: Boolean = false,
    var isTimeBasedBlockingEnabled: Boolean = false,
    var schedules: MutableList<String>,
    var timeLimit: Int = 0 // in minutes
) {
    companion object {
        private val gson = Gson()

        fun fromJson(json: String?): BlockedAppData? {
            return json?.let {
                gson.fromJson(it, object : TypeToken<BlockedAppData>() {}.type)
            }
        }

        fun toJson(data: BlockedAppData): String {
            return gson.toJson(data)
        }
    }

}
