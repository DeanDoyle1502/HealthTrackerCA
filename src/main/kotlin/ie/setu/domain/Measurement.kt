package ie.setu.domain

data class Measurement(
    var id: Int,
    var measurementType: String,
    var value: Double,
    var unit: String,
    var recordedAt: String,
    var userId: Int
)
