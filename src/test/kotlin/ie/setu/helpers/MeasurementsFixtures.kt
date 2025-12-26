package ie.setu.helpers

import ie.setu.domain.Measurement

val nonExistingMeasurementId = 999
val nonExistingMeasurementUserId = 999

val measurements = arrayListOf(
    Measurement(id = 1, measurementType = "weight", value = 80.5, recordedAt = "2025-05-25T07:30:00.000Z", unit = "kg", userId = 1),
    Measurement(id = 2, measurementType = "waist", value = 32.0, recordedAt = "2025-05-26T07:30:00.000Z", unit = "inch", userId = 1),
    Measurement(id = 3, measurementType = "weight", value = 75.2, recordedAt = "2025-05-25T17:45:00.000Z", unit = "kg", userId = 2)
)

val updatedMeasurementType = "weight"
val updatedMeasurementValue = 82.0
val updatedMeasurementRecordedAt = "2025-06-01T08:00:00.000Z"
val updatedMeasurementUnit = "kg"
