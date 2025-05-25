package com.example.appblockerv3.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(
    tableName = "schedules_fact")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val scheduleId: Long = 0,
    val scheduleDaysBitMask: Int,
    val startHour: Int,
    val startMin: Int,
    val endHour: Int,
    val endMin: Int
)


/*
Okay based on the discussions we have done the below data modeling for room setup





groups_fact

  groupId : Unique-id

  groupName : String  (nullable)

  isActive : Boolean

  usageLimit : Integer

  createdAt : epoch

  updatedAt : epoch



  schedules_fact



  ScheduleId : uniqueId

 blockId: Long, // FK to either groups_fact.groupId OR individual_blocks_fact.blockId

 blockType: String, // "GROUP" or "INDIVIDUAL" (non-nullable)

  scheduleDays : BitMaskInt

  startHour : Integer

  startMin : Integer

  endHour : Integer

  endMin : Integer





  indiviual_apps_fact

  blockId : unique-id

  isActive : Boolean

  usageLimit: Integer

  createdAt : epoch

  updatedAt : epoch



  apps_fact



  package_name : primary_key

 IndividualBlockID : fk (nullable)

 GroupBlockID : fk (nullable)



// either blocked by individual block or grouped block  one key will be null







Approach 2

How about this we have kept max limit of schedules per app or group is 2 So what if we do a model like this





groups_fact

groupId : Unique-id

groupName : String

scheduleID1 : fk to schedules_fact (fk to schedules fact)

scheduleID2 : fk to schedules_fact (fk to schedules fact) (nullable)

isActive : Boolean

usageLimit : Integer

createdAt : epoch

updatedAt : epoch



schedules_fact



ScheduleId : uniqueId

scheduleDays : String("1,2,3")

startHour : Integer

startMin : Integer

endHour : Integer

endMin : Integer 





indiviual_apps_fact



blockId : unique-id

isActive : Boolean

usageLimit: Integer

scheduleID1 : fk to schedules_fact

scheduleID2 : fk to schedules_fact

createdAt : epoch

updatedAt : epoch



apps_fact



package_name : primary_key

IndividualBlockID : fk (nullable)

GroupBlockID : fk (nullable)





What this will do is keep schedules fact lmited(if two groups have same schedules only one entry of that schedules will be in schedules fact) and while checking of schedule for an app is active we can directly fetch perticular schedules for that app.


What do you think of this model
 */