package com.example.appblockerv3.data.db.coverters


import androidx.room.TypeConverter


enum class DaysOfWeek(val bit: Int) {
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(4),
    THURSDAY(8),
    FRIDAY(16),
    SATURDAY(32),
    SUNDAY(64);

    companion object {
        fun fromBitmask(bitmask: Int): Set<DaysOfWeek> {
            val days = mutableSetOf<DaysOfWeek>()
            values().forEach { day ->
                if ((bitmask and day.bit) != 0) {
                    days.add(day)
                }
            }
            return days
        }

        fun toBitmask(days: Set<DaysOfWeek>): Int {
            var bitmask = 0
            days.forEach { day ->
                bitmask = bitmask or day.bit
            }
            return bitmask
        }
    }
}
class ScheduleDaysConverter {
    @TypeConverter
    fun fromDaysSet(days: Set<DaysOfWeek>): Int {
        return DaysOfWeek.toBitmask(days)
    }

    @TypeConverter
    fun toDaysSet(bitmask: Int): Set<DaysOfWeek> {
        return DaysOfWeek.fromBitmask(bitmask)
    }
}