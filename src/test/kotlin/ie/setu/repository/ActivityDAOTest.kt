package ie.setu.repository

import ie.setu.domain.Activity
import ie.setu.domain.db.Activities
import ie.setu.domain.db.Users
import ie.setu.domain.repository.ActivityDAO
import ie.setu.domain.repository.UserDAO
import ie.setu.helpers.activities
import ie.setu.helpers.users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


val activityUser1 = users[0]
val activityUser2 = users[1]
val activityUser3 = users[2]

class ActivityDAOTest {

    companion object {
        @BeforeAll
        @JvmStatic
        internal fun setupInMemoryDatabaseConnection() {
            Database.connect(
                url = "jdbc:h2:mem:test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
                driver = "org.h2.Driver",
                user = "sa",
                password = ""
            )
        }
    }

    @Test
    fun `multiple activities added to table can be retrieved successfully`() {
        transaction {
            val activityDAO = populateActivityTable()

            val all = activityDAO.getAll()
            assertEquals(activities.size, all.size)

            val expectedUser1Count = activities.count { it.userId == activityUser1.id }
            val expectedUser2Count = activities.count { it.userId == activityUser2.id }

            assertEquals(expectedUser1Count, activityDAO.findByUserId(activityUser1.id).size)
            assertEquals(expectedUser2Count, activityDAO.findByUserId(activityUser2.id).size)
        }
    }

    @Nested
    inner class ReadActivities {

        @Test
        fun `getting all activities from a populated table returns all rows`() {
            transaction {
                val activityDAO = populateActivityTable()
                assertEquals(activities.size, activityDAO.getAll().size)
            }
        }

        @Test
        fun `get activity by id that doesn't exist results in no activity returned`() {
            transaction {
                val activityDAO = populateActivityTable()
                assertNull(activityDAO.findByActivityId(9999))
            }
        }

        @Test
        fun `get activity by id that exists results in a correct activity returned`() {
            transaction {
                val activityDAO = populateActivityTable()

                val first = activityDAO.getAll().first()
                val found = activityDAO.findByActivityId(first.id)

                assertEquals(first, found)
            }
        }

        @Test
        fun `get activities by user id returns correct rows`() {
            transaction {
                val activityDAO = populateActivityTable()

                val expectedUser1Count = activities.count { it.userId == activityUser1.id }
                assertEquals(expectedUser1Count, activityDAO.findByUserId(activityUser1.id).size)
            }
        }
    }

    @Nested
    inner class UpdateActivities {

        @Test
        fun `updating existing activity in table results in successful update`() {
            transaction {
                val activityDAO = populateActivityTable()

                val target = activityDAO.getAll().first()

                val updated = Activity(
                    id = target.id,
                    description = "Updated",
                    duration = 99.0,
                    calories = 999,
                    started = DateTime.parse("2025-02-01T10:00:00.000Z"),
                    userId = activityUser2.id
                )

                val rows = activityDAO.updateByActivityId(target.id, updated)
                assertEquals(1, rows)

                val reloaded = activityDAO.findByActivityId(target.id)
                assertEquals(updated.description, reloaded?.description)
                assertEquals(updated.duration, reloaded?.duration)
                assertEquals(updated.calories, reloaded?.calories)
                assertEquals(updated.started.millis, reloaded?.started?.millis)
                assertEquals(updated.userId, reloaded?.userId)
            }
        }

        @Test
        fun `updating non-existent activity in table results in no updates`() {
            transaction {
                val activityDAO = populateActivityTable()

                val updated = Activity(
                    id = 9999,
                    description = "Updated",
                    duration = 99.0,
                    calories = 999,
                    started = DateTime.parse("2025-02-01T10:00:00.000Z"),
                    userId = activityUser2.id
                )

                val rows = activityDAO.updateByActivityId(9999, updated)
                assertEquals(0, rows)
                assertNull(activityDAO.findByActivityId(9999))
                assertEquals(activities.size, activityDAO.getAll().size)
            }
        }
    }

    @Nested
    inner class DeleteActivities {

        @Test
        fun `deleting a non-existent activity in table results in no deletion`() {
            transaction {
                val activityDAO = populateActivityTable()

                assertEquals(activities.size, activityDAO.getAll().size)
                val rows = activityDAO.deleteByActivityId(9999)
                assertEquals(0, rows)
                assertEquals(activities.size, activityDAO.getAll().size)
            }
        }

        @Test
        fun `deleting an existing activity in table results in record being deleted`() {
            transaction {
                val activityDAO = populateActivityTable()

                val targetId = activityDAO.getAll().first().id

                assertEquals(activities.size, activityDAO.getAll().size)
                val rows = activityDAO.deleteByActivityId(targetId)
                assertEquals(1, rows)
                assertEquals(activities.size - 1, activityDAO.getAll().size)
            }
        }

        @Test
        fun `deleting activities by user id deletes correct number of rows`() {
            transaction {
                val activityDAO = populateActivityTable()

                val expectedUser1Count = activities.count { it.userId == activityUser1.id }
                val rows = activityDAO.deleteByUserId(activityUser1.id)

                assertEquals(expectedUser1Count, rows)
                assertEquals(activities.size - expectedUser1Count, activityDAO.getAll().size)
            }
        }
    }

    internal fun populateActivityTable(): ActivityDAO {
        // FK means Users must exist before Activities
        SchemaUtils.drop(Activities, Users)
        SchemaUtils.create(Users, Activities)

        val userDAO = UserDAO()
        userDAO.save(activityUser1)
        userDAO.save(activityUser2)
        userDAO.save(activityUser3)

        val activityDAO = ActivityDAO()
        activities.forEach { activityDAO.save(it) }

        return activityDAO
    }
}
