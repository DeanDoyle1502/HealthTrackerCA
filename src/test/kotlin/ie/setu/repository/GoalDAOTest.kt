package ie.setu.repository

import ie.setu.domain.Goal
import ie.setu.domain.db.Goals
import ie.setu.domain.db.Users
import ie.setu.domain.repository.GoalDAO
import ie.setu.domain.repository.UserDAO
import ie.setu.helpers.goals
import ie.setu.helpers.users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GoalDAOTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setupDb() {
            Database.connect(
                "jdbc:h2:mem:goaldao;DB_CLOSE_DELAY=-1;",
                driver = "org.h2.Driver",
                user = "sa",
                password = ""
            )
        }
    }

    private fun populateGoalTable(): GoalDAO {
        setupDb()
        transaction {
            SchemaUtils.drop(Goals, Users)
            SchemaUtils.create(Users, Goals)
        }

        val userDAO = UserDAO()
        val goalDAO = GoalDAO()

        users.forEach { userDAO.save(it) }
        goals.forEach { goalDAO.save(it) }

        return goalDAO
    }

    @Nested
    inner class ReadGoals {

        @Test
        fun `getAll returns all goals`() {
            val goalDAO = populateGoalTable()
            assertEquals(goals.size, goalDAO.getAll().size)
        }

        @Test
        fun `findByGoalId returns a goal when id exists`() {
            val goalDAO = populateGoalTable()
            val goal = goalDAO.findByGoalId(1)
            assertNotNull(goal)
            assertEquals(1, goal?.id)
        }

        @Test
        fun `findByUserId returns only goals for that user`() {
            val goalDAO = populateGoalTable()
            val user1Goals = goalDAO.findByUserId(1)
            assertEquals(2, user1Goals.size)
        }
    }

    @Nested
    inner class CreateGoals {

        @Test
        fun `save adds a new goal`() {
            val goalDAO = populateGoalTable()

            val newGoal = Goal(
                id = 4,
                goalType = "distance",
                target = 5.0,
                unit = "km",
                startDate = "2025-02-01",
                endDate = "2025-04-01",
                userId = 1
            )

            goalDAO.save(newGoal)
            assertEquals(goals.size + 1, goalDAO.getAll().size)
        }
    }

    @Nested
    inner class UpdateGoals {

        @Test
        fun `updateByGoalId updates an existing goal`() {
            val goalDAO = populateGoalTable()

            val updatedGoal = Goal(
                id = 1,
                goalType = "weight",
                target = 82.5,
                unit = "kg",
                startDate = "2025-01-01",
                endDate = "2025-06-01",
                userId = 1
            )

            goalDAO.updateByGoalId(1, updatedGoal)

            val refreshed = goalDAO.findByGoalId(1)
            assertNotNull(refreshed)
            assertEquals(82.5, refreshed?.target)
        }
    }

    @Nested
    inner class DeleteGoals {

        @Test
        fun `deleteByGoalId removes a goal`() {
            val goalDAO = populateGoalTable()

            goalDAO.deleteByGoalId(1)

            val afterDelete = goalDAO.findByGoalId(1)
            assertEquals(null, afterDelete)
        }
    }
}
