package ie.setu.domain.repository

import ie.setu.domain.Measurement
import ie.setu.domain.db.Measurements
import ie.setu.utils.mapToMeasurement
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.ArrayList

class MeasurementDAO {

    fun getAll(): ArrayList<Measurement> {
        val measurementList: ArrayList<Measurement> = arrayListOf()
        transaction {
            Measurements.selectAll().map {
                measurementList.add(mapToMeasurement(it))
            }
        }
        return measurementList
    }

    fun findByMeasurementId(id: Int): Measurement? {
        return transaction {
            Measurements
                .selectAll()
                .where { Measurements.id eq id }
                .map(::mapToMeasurement)
                .firstOrNull()
        }
    }

    fun findByUserId(userId: Int): ArrayList<Measurement> {
        val measurementList: ArrayList<Measurement> = arrayListOf()
        transaction {
            Measurements
                .selectAll()
                .where { Measurements.userId eq userId }
                .map {
                    measurementList.add(mapToMeasurement(it))
                }
        }
        return measurementList
    }

    fun save(measurement: Measurement) {
        transaction {
            Measurements.insert {
                it[measurementType] = measurement.measurementType
                it[value] = measurement.value
                it[unit] = measurement.unit
                it[recordedAt] = measurement.recordedAt
                it[userId] = measurement.userId
            }
        }
    }

    fun deleteByMeasurementId(measurementId: Int): Int {
        return transaction {
            Measurements.deleteWhere { Measurements.id eq measurementId }
        }
    }

    fun deleteByUserId(userId: Int): Int {
        return transaction {
            Measurements.deleteWhere { Measurements.userId eq userId }
        }
    }

    fun updateByMeasurementId(measurementId: Int, measurement: Measurement): Int {
        return transaction {
            Measurements.update({ Measurements.id eq measurementId }) {
                it[measurementType] = measurement.measurementType
                it[value] = measurement.value
                it[unit] = measurement.unit
                it[recordedAt] = measurement.recordedAt
                it[userId] = measurement.userId
            }
        }
    }
}
