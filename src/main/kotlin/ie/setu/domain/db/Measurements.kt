package ie.setu.domain.db

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Measurements : Table("measurements") {
    val id = integer("id").autoIncrement()
    val measurementType = varchar("measurementType", 100)
    val value = double("value")
    val unit = varchar("unit", 50)
    val recordedAt = varchar("recordedAt", 50)
    val userId = integer("userId").references(Users.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(id, name = "PK_Measurements_ID")
}
