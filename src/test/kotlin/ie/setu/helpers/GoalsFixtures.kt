package ie.setu.helpers

import ie.setu.domain.Goal

val goals = arrayListOf(
    Goal(
        id = 1,
        goalType = "weight",
        target = 85.0,
        unit = "kg",
        startDate = "2025-01-01",
        endDate = "2025-06-01",
        userId = 1
    ),
    Goal(
        id = 2,
        goalType = "steps",
        target = 10000.0,
        unit = "steps",
        startDate = "2025-01-01",
        endDate = "2025-02-01",
        userId = 1
    ),
    Goal(
        id = 3,
        goalType = "calories",
        target = 2000.0,
        unit = "kcal",
        startDate = "2025-01-05",
        endDate = "2025-03-01",
        userId = 2
    )
)
