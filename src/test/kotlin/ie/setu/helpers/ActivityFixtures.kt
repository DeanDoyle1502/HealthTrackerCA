package ie.setu.helpers

import ie.setu.domain.Activity
import org.joda.time.DateTime

val activities = arrayListOf(
    Activity(
        id = 0,
        description = "Run 5k",
        duration = 30.0,
        calories = 300,
        started = DateTime.parse("2025-01-01T10:00:00.000Z"),
        userId = 1
    ),
    Activity(
        id = 0,
        description = "Gym Session",
        duration = 45.0,
        calories = 450,
        started = DateTime.parse("2025-01-02T10:00:00.000Z"),
        userId = 1
    ),
    Activity(
        id = 0,
        description = "Walk",
        duration = 60.0,
        calories = 200,
        started = DateTime.parse("2025-01-03T10:00:00.000Z"),
        userId = 2
    )
)
