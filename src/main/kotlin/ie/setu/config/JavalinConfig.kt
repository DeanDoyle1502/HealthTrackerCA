package ie.setu.config

import ie.setu.controllers.HealthTrackerController
import ie.setu.utils.jsonObjectMapper
import io.javalin.Javalin
import io.javalin.json.JavalinJackson

class JavalinConfig {

    val app = Javalin.create(
        { config ->
            config.jsonMapper(JavalinJackson(jsonObjectMapper()))
        }
    ).apply {
        exception(Exception::class.java) { e, ctx -> e.printStackTrace() }
        error(404) { ctx -> ctx.json("404 - Not Found") }
    }


    fun startJavalinService(): Javalin {
        app.start(getRemoteAssignedPort())
        registerRoutes(app)
        return app
    }

    private fun registerRoutes(app: Javalin) {

        //Users
        app.get("/api/users", HealthTrackerController::getAllUsers)
        app.get("/api/users/{user-id}", HealthTrackerController::getUserByUserId)
        app.post("/api/users", HealthTrackerController::addUser)
        app.delete("/api/users/{user-id}", HealthTrackerController::deleteUserById)
        app.patch("/api/users/{user-id}", HealthTrackerController::updateUserById)
        app.get("/api/users/email/{email}", HealthTrackerController::getUserByEmail)

        //Activities
        app.get("/api/activities", HealthTrackerController::getAllActivities)
        app.post("/api/activities", HealthTrackerController::addActivity)
        app.get("/api/users/{user-id}/activities", HealthTrackerController::getActivitiesByUserId)
        app.get("/api/activities/{activity-id}", HealthTrackerController::getActivityByActivityId)
        app.patch("/api/activities/{activity-id}", HealthTrackerController::updateActivityByActivityId)
        app.delete("/api/activities/{activity-id}", HealthTrackerController::deleteActivityByActivityId)
        app.delete("/api/users/{user-id}/activities", HealthTrackerController::deleteActivitiesByUserId)

        //Goals
        app.get("/api/goals", HealthTrackerController::getAllGoals)
        app.post("/api/goals", HealthTrackerController::addGoal)
        app.get("/api/goals/{goal-id}", HealthTrackerController::getGoalByGoalId)
        app.patch("/api/goals/{goal-id}", HealthTrackerController::updateGoalByGoalId)
        app.delete("/api/goals/{goal-id}", HealthTrackerController::deleteGoalByGoalId)
        app.get("/api/users/{user-id}/goals", HealthTrackerController::getGoalsByUserId)
        app.delete("/api/users/{user-id}/goals", HealthTrackerController::deleteGoalsByUserId)

        //Measurements
        app.get("/api/measurements", HealthTrackerController::getAllMeasurements)
        app.post("/api/measurements", HealthTrackerController::addMeasurement)
        app.get("/api/measurements/{measurement-id}", HealthTrackerController::getMeasurementByMeasurementId)
        app.patch("/api/measurements/{measurement-id}", HealthTrackerController::updateMeasurementByMeasurementId)
        app.delete("/api/measurements/{measurement-id}", HealthTrackerController::deleteMeasurementByMeasurementId)
        app.get("/api/users/{user-id}/measurements", HealthTrackerController::getMeasurementsByUserId)
        app.delete("/api/users/{user-id}/measurements", HealthTrackerController::deleteMeasurementsByUserId)


    }

    private fun getRemoteAssignedPort(): Int {
        val remotePort = System.getenv("PORT")
        return if (remotePort != null) {
            Integer.parseInt(remotePort)
        } else 8080
    }

    fun getJavalinService(): Javalin {
        registerRoutes(app)
        return app
    }

}
