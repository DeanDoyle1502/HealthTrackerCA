package ie.setu

import ie.setu.config.DbConfig
import ie.setu.config.JavalinConfig
import ie.setu.domain.db.Users
import ie.setu.domain.db.Activities
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import ie.setu.domain.db.Goals



fun main() {

    DbConfig().getDbConnection()
    if (System.getenv("INIT_DB")?.lowercase() == "true") {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(Users, Activities, Goals)
        }
    }
    JavalinConfig().startJavalinService()
}