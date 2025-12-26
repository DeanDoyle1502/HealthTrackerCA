package ie.setu.domain

data class Goal(
    var id: Int = 0,
    var userId: Int,
    var goalType: String,
    var target: Double,
    var unit: String,
    var startDate: String? = null,
    var endDate: String? = null
)
