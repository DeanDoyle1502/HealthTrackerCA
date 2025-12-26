package ie.setu.domain.repository

import ie.setu.domain.Goal
import ie.setu.domain.db.Goals
import ie.setu.utils.mapToGoal
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class GoalDAO {

    fun getAll(): ArrayList<Goal> {
        val goalList: ArrayList<Goal> = arrayListOf()
        transaction {
            Goals.selectAll().map {
                goalList.add(mapToGoal(it))
            }
        }
        return goalList
    }

    fun findByGoalId(id: Int): Goal? {
        return transaction {
            Goals
                .selectAll()
                .where { Goals.id eq id }
                .map(::mapToGoal)
                .firstOrNull()
        }
    }

    fun findByUserId(userId: Int): ArrayList<Goal> {
        val goalList: ArrayList<Goal> = arrayListOf()
        transaction {
            Goals
                .selectAll()
                .where { Goals.userId eq userId }
                .map {
                    goalList.add(mapToGoal(it))
                }
        }
        return goalList
    }

    fun save(goal: Goal) {
        transaction {
            Goals.insert {
                it[userId] = goal.userId
                it[goalType] = goal.goalType
                it[target] = goal.target
                it[unit] = goal.unit
                it[startDate] = goal.startDate
                it[endDate] = goal.endDate
            }
        }
    }

    fun deleteByGoalId(goalId: Int): Int {
        return transaction {
            Goals.deleteWhere { Goals.id eq goalId }
        }
    }

    fun deleteByUserId(userId: Int): Int {
        return transaction {
            Goals.deleteWhere { Goals.userId eq userId }
        }
    }

    fun updateByGoalId(goalId: Int, goal: Goal): Int {
        return transaction {
            Goals.update({ Goals.id eq goalId }) {
                it[userId] = goal.userId
                it[goalType] = goal.goalType
                it[target] = goal.target
                it[unit] = goal.unit
                it[startDate] = goal.startDate
                it[endDate] = goal.endDate
            }
        }
    }
}
