package ie.setu.repository

import ie.setu.domain.db.Measurements
import ie.setu.domain.db.Users
import ie.setu.domain.repository.MeasurementDAO
import ie.setu.domain.repository.UserDAO
import ie.setu.helpers.measurements
import ie.setu.helpers.nonExistingMeasurementId
import ie.setu.helpers.nonExistingMeasurementUserId
import ie.setu.helpers.updatedMeasurementRecordedAt
import ie.setu.helpers.updatedMeasurementType
import ie.setu.helpers.updatedMeasurementUnit
import ie.setu.helpers.updatedMeasurementValue
import ie.setu.helpers.users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MeasurementsDAOTest {

    private fun populateMeasurementTable(): MeasurementDAO {
        Database.connect(
            "jdbc:h2:mem:measurementsdao;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction {
            SchemaUtils.drop(Measurements, Users)
            SchemaUtils.create(Users, Measurements)
        }

        val userDAO = UserDAO()
        val measurementDAO = MeasurementDAO()

        users.forEach { userDAO.save(it) }
        measurements.forEach { measurementDAO.save(it) }

        return measurementDAO
    }

    @Nested
    inner class ReadMeasurements {

        @Test
        fun `getting all measurements from a populated table returns all rows`() {
            val measurementDAO = populateMeasurementTable()
            assertEquals(3, measurementDAO.getAll().size)
        }

        @Test
        fun `find by measurement id returns correct measurement`() {
            val measurementDAO = populateMeasurementTable()
            val m = measurementDAO.findByMeasurementId(1)
            assertNotNull(m)
            assertEquals(1, m?.id)
        }

        @Test
        fun `find by measurement id returns null when id does not exist`() {
            val measurementDAO = populateMeasurementTable()
            assertNull(measurementDAO.findByMeasurementId(nonExistingMeasurementId))
        }

        @Test
        fun `find by user id returns matching measurements`() {
            val measurementDAO = populateMeasurementTable()
            assertEquals(2, measurementDAO.findByUserId(1).size)
        }

        @Test
        fun `find by user id returns empty list when user id does not exist`() {
            val measurementDAO = populateMeasurementTable()
            assertEquals(0, measurementDAO.findByUserId(nonExistingMeasurementUserId).size)
        }
    }

    @Nested
    inner class UpdateMeasurements {

        @Test
        fun `updating an existing measurement returns 1`() {
            val measurementDAO = populateMeasurementTable()
            val m = measurementDAO.findByMeasurementId(1)
            assertNotNull(m)

            val updated = m!!.copy(
                measurementType = updatedMeasurementType,
                value = updatedMeasurementValue,
                recordedAt = updatedMeasurementRecordedAt,
                unit = updatedMeasurementUnit
            )

            assertEquals(1, measurementDAO.updateByMeasurementId(1, updated))

            val refreshed = measurementDAO.findByMeasurementId(1)
            assertEquals(updatedMeasurementValue, refreshed?.value)
        }

        @Test
        fun `updating a non-existing measurement returns 0`() {
            val measurementDAO = populateMeasurementTable()
            val m = measurementDAO.findByMeasurementId(1)!!
            val updated = m.copy(
                measurementType = updatedMeasurementType,
                value = updatedMeasurementValue,
                recordedAt = updatedMeasurementRecordedAt,
                unit = updatedMeasurementUnit
            )
            assertEquals(0, measurementDAO.updateByMeasurementId(nonExistingMeasurementId, updated))
        }
    }

    @Nested
    inner class DeleteMeasurements {

        @Test
        fun `deleting an existing measurement returns 1`() {
            val measurementDAO = populateMeasurementTable()
            assertEquals(1, measurementDAO.deleteByMeasurementId(1))
            assertNull(measurementDAO.findByMeasurementId(1))
        }

        @Test
        fun `deleting a non-existing measurement returns 0`() {
            val measurementDAO = populateMeasurementTable()
            assertEquals(0, measurementDAO.deleteByMeasurementId(nonExistingMeasurementId))
        }

        @Test
        fun `deleting measurements by user id returns number deleted`() {
            val measurementDAO = populateMeasurementTable()
            assertEquals(2, measurementDAO.deleteByUserId(1))
            assertEquals(0, measurementDAO.findByUserId(1).size)
        }

        @Test
        fun `deleting measurements by non-existing user id returns 0`() {
            val measurementDAO = populateMeasurementTable()
            assertEquals(0, measurementDAO.deleteByUserId(nonExistingMeasurementUserId))
        }
    }
}
