package ie.setu.domain.db

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Goals : Table("goals") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val goalType = varchar("goal_type", 50)
    val target = double("target")
    val unit = varchar("unit", 20)
    val startDate = varchar("start_date", 20).nullable()
    val endDate = varchar("end_date", 20).nullable()

    override val primaryKey = PrimaryKey(id, name = "PK_Goals_ID")
}
