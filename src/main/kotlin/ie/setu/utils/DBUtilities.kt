package ie.setu.utils

import ie.setu.domain.Activity
import ie.setu.domain.User
import ie.setu.domain.db.Activities
import ie.setu.domain.db.Users
import org.jetbrains.exposed.sql.ResultRow
import ie.setu.domain.Goal
import ie.setu.domain.db.Goals
import ie.setu.domain.Measurement
import ie.setu.domain.db.Measurements



fun mapToUser(it: ResultRow) = User(
    id = it[Users.id],
    name = it[Users.name],
    email = it[Users.email]
)

fun mapToActivity(it: ResultRow) = Activity(
    id = it[Activities.id],
    description = it[Activities.description],
    duration = it[Activities.duration],
    started = it[Activities.started],
    calories = it[Activities.calories],
    userId = it[Activities.userId]
)

fun mapToGoal(it: ResultRow) = Goal(
    id = it[Goals.id],
    userId = it[Goals.userId],
    goalType = it[Goals.goalType],
    target = it[Goals.target],
    unit = it[Goals.unit],
    startDate = it[Goals.startDate],
    endDate = it[Goals.endDate]
)

fun mapToMeasurement(it: ResultRow) = Measurement(
    id = it[Measurements.id],
    measurementType = it[Measurements.measurementType],
    value = it[Measurements.value],
    unit = it[Measurements.unit],
    recordedAt = it[Measurements.recordedAt],
    userId = it[Measurements.userId]
)



