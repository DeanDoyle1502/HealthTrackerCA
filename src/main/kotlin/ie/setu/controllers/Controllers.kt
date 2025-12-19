package ie.setu.controllers

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import ie.setu.domain.repository.UserDAO
import io.javalin.http.Context
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ie.setu.domain.Activity
import ie.setu.domain.User
import ie.setu.domain.repository.ActivityDAO


object HealthTrackerController {

    private val userDao = UserDAO()
    private val activityDAO = ActivityDAO()

    fun getAllUsers(ctx: Context) {
        ctx.json(userDao.getAll())
    }

    fun getUserByUserId(ctx: Context) {
        val user = userDao.findById(ctx.pathParam("user-id").toInt())
        if (user != null) {
            ctx.json(user)
        }
    }

    fun addUser(ctx: Context) {
        val mapper = jacksonObjectMapper()
        val user = mapper.readValue<User>(ctx.body())
        userDao.save(user)
        ctx.json(user)
    }

    fun getUserByEmail(ctx: Context) {
        val email = ctx.pathParam("email")
        val user = userDao.findByEmail(email)
        if (user != null) {
            ctx.json(user)
        }
    }

    fun deleteUserById(ctx: Context) {
        val id = ctx.pathParam("user-id").toInt()
        val rowsDeleted = userDao.delete(id)

        if (rowsDeleted > 0) {
            ctx.status(204)
        } else {
            ctx.status(404).result("User not found")
        }
    }

    fun updateUserById(ctx: Context) {
        val id = ctx.pathParam("user-id").toInt()
        val mapper = jacksonObjectMapper()
        val updatedUser = mapper.readValue<User>(ctx.body())

        val user = userDao.update(id, updatedUser)
        if (user != null) {
            ctx.json(user)
        } else {
            ctx.status(404).result("User not found")
        }
    }

    //--------------------------------------------------------------
    // ActivityDAO specifics
    //-------------------------------------------------------------

    fun getAllActivities(ctx: Context) {
        //mapper handles the deserialization of Joda date into a String.
        val mapper = jacksonObjectMapper()
            .registerModule(JodaModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        ctx.json(mapper.writeValueAsString(activityDAO.getAll()))
    }

    fun getActivitiesByUserId(ctx: Context) {
        val userId = ctx.pathParam("user-id").toInt()

        if (userDao.findById(userId) == null) {
            ctx.status(404).result("User not found")
            return
        }

        val activities = activityDAO.findByUserId(userId)

        val mapper = jacksonObjectMapper()
            .registerModule(JodaModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

        ctx.contentType("application/json")
        ctx.result(mapper.writeValueAsString(activities)) // returns [] when empty
    }


    fun addActivity(ctx: Context) {
        //mapper handles the serialisation of Joda date into a String.
        val mapper = jacksonObjectMapper()
            .registerModule(JodaModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        val activity = mapper.readValue<Activity>(ctx.body())
        activityDAO.save(activity)
        ctx.json(activity)
    }

    fun getActivityByActivityId(ctx: Context) {
        val activityId = ctx.pathParam("activity-id").toIntOrNull()
        if (activityId == null) {
            ctx.status(400).result("Invalid activity-id")
            return
        }

        val activity = activityDAO.findByActivityId(activityId)
        if (activity == null) {
            ctx.status(404).result("Activity not found")
        } else {
            val mapper = jodaMapper()
            ctx.contentType("application/json")
            ctx.result(mapper.writeValueAsString(activity))
        }
    }

    fun updateActivityByActivityId(ctx: Context) {
        val activityId = ctx.pathParam("activity-id").toIntOrNull()
        if (activityId == null) {
            ctx.status(400).result("Invalid activity-id")
            return
        }

        val mapper = jodaMapper()
        val updatedActivity = mapper.readValue<Activity>(ctx.body())

        val rowsUpdated = activityDAO.updateByActivityId(activityId, updatedActivity)
        if (rowsUpdated == 0) {
            ctx.status(404).result("Activity not found")
        } else {
            val refreshed = activityDAO.findByActivityId(activityId) ?: updatedActivity
            ctx.contentType("application/json")
            ctx.result(mapper.writeValueAsString(refreshed))
        }
    }

    fun deleteActivityByActivityId(ctx: Context) {
        val activityId = ctx.pathParam("activity-id").toIntOrNull()
        if (activityId == null) {
            ctx.status(400).result("Invalid activity-id")
            return
        }

        val rowsDeleted = activityDAO.deleteByActivityId(activityId)
        if (rowsDeleted == 0) {
            ctx.status(404).result("Activity not found")
        } else {
            ctx.status(204)
        }
    }

    fun deleteActivitiesByUserId(ctx: Context) {
        val userId = ctx.pathParam("user-id").toIntOrNull()
        if (userId == null) {
            ctx.status(400).result("Invalid user-id")
            return
        }

        if (userDao.findById(userId) == null) {
            ctx.status(404).result("User not found")
            return
        }

        activityDAO.deleteByUserId(userId)
        ctx.status(204)
    }


    private fun jodaMapper() = jacksonObjectMapper()
        .registerModule(JodaModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)


}
