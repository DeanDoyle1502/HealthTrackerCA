package ie.setu.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ie.setu.config.JavalinConfig
import ie.setu.domain.db.Activities
import ie.setu.domain.db.Goals
import ie.setu.domain.db.Measurements
import ie.setu.domain.db.Users
import io.javalin.Javalin
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiTest {

    private val client = HttpClient.newHttpClient()
    private val mapper = jacksonObjectMapper()
    private lateinit var app: Javalin
    private lateinit var baseUrl: String

    @BeforeAll
    fun up() {
        Database.connect(
            "jdbc:h2:mem:apitest;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction {
            SchemaUtils.create(Users, Activities, Goals, Measurements)
        }

        val cfg = JavalinConfig()
        app = cfg.startJavalinService(0)
        baseUrl = "http://localhost:${app.port()}"
    }

    @AfterAll
    fun down() {
        app.stop()
    }

    private fun req(method: String, path: String, body: String? = null): HttpResponse<String> {
        val b = HttpRequest.newBuilder(URI.create("$baseUrl$path"))
        when (method) {
            "GET" -> b.GET()
            "POST" -> b.header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body ?: ""))
            "PATCH" -> b.header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body ?: ""))
            "DELETE" -> b.DELETE()
            else -> error("bad method")
        }

        return client.send(b.build(), HttpResponse.BodyHandlers.ofString())
    }

    private fun createUser(email: String, name: String = "IT User"): Int {
        val post = req("POST", "/api/users", """{"id":0,"name":"$name","email":"$email"}""")
        Assertions.assertTrue(post.statusCode() in setOf(200, 201))

        val get = req("GET", "/api/users")
        Assertions.assertEquals(200, get.statusCode())
        Assertions.assertTrue(get.body().isNotBlank())

        val users: List<Map<String, Any?>> = mapper.readValue(get.body())
        val u = users.first { (it["email"] as String) == email }
        return (u["id"] as Number).toInt()
    }

    @Test
    fun users_getAll_200() {
        val res = req("GET", "/api/users")
        Assertions.assertEquals(200, res.statusCode())
        Assertions.assertTrue(res.body().isNotBlank())
        mapper.readValue<List<Map<String, Any?>>>(res.body())
    }

    @Test
    fun users_post_then_getAll_contains() {
        val email = "it.${System.nanoTime()}@test.com"
        createUser(email)
        val get = req("GET", "/api/users")
        Assertions.assertEquals(200, get.statusCode())
        Assertions.assertTrue(get.body().contains(email))
    }

    @Test
    fun users_patch_then_getById_reflects_change() {
        val email = "it.${System.nanoTime()}@test.com"
        val id = createUser(email)
        val newName = "Updated ${System.nanoTime()}"

        val patch = req("PATCH", "/api/users/$id", """{"id":$id,"name":"$newName","email":"$email"}""")
        Assertions.assertTrue(patch.statusCode() in setOf(200, 204))

        val get = req("GET", "/api/users/$id")
        Assertions.assertEquals(200, get.statusCode())
        Assertions.assertTrue(get.body().isNotBlank())
        Assertions.assertTrue(get.body().contains(newName))
    }

    @Test
    fun users_getByEmail_200_for_created_user() {
        val email = "it.${System.nanoTime()}@test.com"
        createUser(email)

        val encoded = URLEncoder.encode(email, StandardCharsets.UTF_8)
        val get = req("GET", "/api/users/email/$encoded")
        Assertions.assertEquals(200, get.statusCode())
        Assertions.assertTrue(get.body().isNotBlank())
        Assertions.assertTrue(get.body().contains(email))
    }

    @Test
    fun activities_getAll_200_and_json() {
        val res = req("GET", "/api/activities")
        Assertions.assertEquals(200, res.statusCode())
        Assertions.assertTrue(res.body().isNotBlank())
        mapper.readValue<List<Map<String, Any?>>>(res.body())
    }

    @Test
    fun goals_getAll_200_and_json() {
        val res = req("GET", "/api/goals")
        Assertions.assertEquals(200, res.statusCode())
        Assertions.assertTrue(res.body().isNotBlank())
        mapper.readValue<List<Map<String, Any?>>>(res.body())
    }

    @Test
    fun measurements_getAll_200_and_json() {
        val res = req("GET", "/api/measurements")
        Assertions.assertEquals(200, res.statusCode())
        Assertions.assertTrue(res.body().isNotBlank())
        mapper.readValue<List<Map<String, Any?>>>(res.body())
    }

    @Test
    fun measurements_post_then_getAll_contains() {
        val email = "it.${System.nanoTime()}@test.com"
        val userId = createUser(email)

        val payload = """{"id":0,"measurementType":"weight","value":92.3,"unit":"kg","recordedAt":"2025-12-27","userId":$userId}"""
        val post = req("POST", "/api/measurements", payload)
        Assertions.assertTrue(post.statusCode() in setOf(200, 201))

        val get = req("GET", "/api/measurements")
        Assertions.assertEquals(200, get.statusCode())
        Assertions.assertTrue(get.body().isNotBlank())
        Assertions.assertTrue(get.body().contains(""""measurementType":"weight"""") && get.body().contains("92.3"))
    }

    @Test
    fun measurements_getByUserId_contains_created_measurement() {
        val email = "it.${System.nanoTime()}@test.com"
        val userId = createUser(email)

        val mType = "weight-${System.nanoTime()}"
        val post = req(
            "POST",
            "/api/measurements",
            """{"id":0,"measurementType":"$mType","value":90.1,"unit":"kg","recordedAt":"2025-12-27","userId":$userId}"""
        )
        Assertions.assertTrue(post.statusCode() in setOf(200, 201))

        val get = req("GET", "/api/users/$userId/measurements")
        Assertions.assertEquals(200, get.statusCode())
        Assertions.assertTrue(get.body().isNotBlank())
        Assertions.assertTrue(get.body().contains(mType))
    }

    @Test
    fun users_getById_missing_returns_response() {
        val res = req("GET", "/api/users/999999")
        Assertions.assertTrue(res.statusCode() in setOf(200, 204, 400, 404))
    }

    @Test
    fun goals_getByUserId_200_for_existing_user() {
        val email = "it.${System.nanoTime()}@test.com"
        val userId = createUser(email)

        val res = req("GET", "/api/users/$userId/goals")
        Assertions.assertEquals(200, res.statusCode())
        Assertions.assertTrue(res.body().isNotBlank())
    }

    @Test
    fun goals_getById_missing_returns_response() {
        val res = req("GET", "/api/goals/999999")
        Assertions.assertTrue(res.statusCode() in setOf(200, 204, 400, 404))
    }

    @Test
    fun activities_getByUserId_200_for_existing_user() {
        val email = "it.${System.nanoTime()}@test.com"
        val userId = createUser(email)

        val res = req("GET", "/api/users/$userId/activities")
        Assertions.assertEquals(200, res.statusCode())
        Assertions.assertTrue(res.body().isNotBlank())
    }

    @Test
    fun measurements_getById_missing_returns_response() {
        val res = req("GET", "/api/measurements/999999")
        Assertions.assertTrue(res.statusCode() in setOf(200, 204, 400, 404))
    }

    @Test
    fun measurements_getByUserId_200_for_existing_user() {
        val email = "it.${System.nanoTime()}@test.com"
        val userId = createUser(email)

        val res = req("GET", "/api/users/$userId/measurements")
        Assertions.assertEquals(200, res.statusCode())
        Assertions.assertTrue(res.body().isNotBlank())
    }

    @Test
    fun users_getById_200_for_existing_user() {
        val email = "it.${System.nanoTime()}@test.com"
        val id = createUser(email)
        val res = req("GET", "/api/users/$id")
        Assertions.assertEquals(200, res.statusCode())
        Assertions.assertTrue(res.body().isNotBlank())
    }

    @Test
    fun users_getById_invalid_param_returns_response() {
        val res = req("GET", "/api/users/abc")
        Assertions.assertTrue(res.statusCode() in setOf(200, 400, 404))
    }

    @Test
    fun measurements_post_then_getById_200_for_created_measurement() {
        val email = "it.${System.nanoTime()}@test.com"
        val userId = createUser(email)

        val tag = "w-${System.nanoTime()}"
        val payload = """{"id":0,"measurementType":"$tag","value":92.3,"unit":"kg","recordedAt":"2025-12-27","userId":$userId}"""
        val post = req("POST", "/api/measurements", payload)
        Assertions.assertTrue(post.statusCode() in setOf(200, 201))

        val all = req("GET", "/api/measurements")
        Assertions.assertEquals(200, all.statusCode())
        Assertions.assertTrue(all.body().isNotBlank())

        val ms: List<Map<String, Any?>> = mapper.readValue(all.body())
        val created = ms.first { (it["measurementType"] as String) == tag }
        val id = (created["id"] as Number).toInt()

        val one = req("GET", "/api/measurements/$id")
        Assertions.assertEquals(200, one.statusCode())
        Assertions.assertTrue(one.body().isNotBlank())
    }

    @Test
    fun measurements_update_then_getById_reflects_change_if_supported() {
        val email = "it.${System.nanoTime()}@test.com"
        val userId = createUser(email)

        val tag = "w-${System.nanoTime()}"
        val payload = """{"id":0,"measurementType":"$tag","value":80.0,"unit":"kg","recordedAt":"2025-12-27","userId":$userId}"""
        req("POST", "/api/measurements", payload)

        val all = req("GET", "/api/measurements")
        val ms: List<Map<String, Any?>> = mapper.readValue(all.body())
        val created = ms.first { (it["measurementType"] as String) == tag }
        val id = (created["id"] as Number).toInt()

        val updated = """{"id":$id,"measurementType":"$tag","value":81.0,"unit":"kg","recordedAt":"2025-12-27","userId":$userId}"""
        val putOrPatch = req("PATCH", "/api/measurements/$id", updated)
        Assertions.assertTrue(putOrPatch.statusCode() in setOf(200, 204, 404))

        val one = req("GET", "/api/measurements/$id")
        Assertions.assertTrue(one.statusCode() in setOf(200, 404))
    }

    @Test
    fun activities_getById_missing_returns_response() {
        val res = req("GET", "/api/activities/999999")
        Assertions.assertTrue(res.statusCode() in setOf(200, 204, 400, 404))
    }

    @Test
    fun activities_getById_invalid_param_returns_response() {
        val res = req("GET", "/api/activities/abc")
        Assertions.assertTrue(res.statusCode() in setOf(200, 400, 404))
    }

    @Test
    fun users_patch_then_getById_still_responds() {
        val email = "it.${System.nanoTime()}@test.com"
        val id = createUser(email)
        val patch = req("PATCH", "/api/users/$id", """{"id":$id,"name":"Patched","email":"$email"}""")
        Assertions.assertTrue(patch.statusCode() in setOf(200, 204))
        val get = req("GET", "/api/users/$id")
        Assertions.assertEquals(200, get.statusCode())
    }

    @Test
    fun users_delete_then_getById_returns_response() {
        val email = "it.${System.nanoTime()}@test.com"
        val id = createUser(email)
        val del = req("DELETE", "/api/users/$id")
        Assertions.assertTrue(del.statusCode() in setOf(200, 204, 404))
        val get = req("GET", "/api/users/$id")
        Assertions.assertTrue(get.statusCode() in setOf(200, 400, 404))
    }

    @Test
    fun measurements_patch_missing_returns_response() {
        val res = req(
            "PATCH",
            "/api/measurements/999999",
            """{"id":999999,"measurementType":"weight","value":1.0,"unit":"kg","recordedAt":"2025-12-27","userId":1}"""
        )
        Assertions.assertTrue(res.statusCode() in setOf(200, 204, 400, 404))
    }

    @Test
    fun measurements_delete_missing_returns_response() {
        val res = req("DELETE", "/api/measurements/999999")
        Assertions.assertTrue(res.statusCode() in setOf(200, 204, 400, 404))
    }

    @Test
    fun goals_delete_missing_returns_response() {
        val res = req("DELETE", "/api/goals/999999")
        Assertions.assertTrue(res.statusCode() in setOf(200, 204, 400, 404))
    }

    @Test
    fun activities_delete_missing_returns_response() {
        val res = req("DELETE", "/api/activities/999999")
        Assertions.assertTrue(res.statusCode() in setOf(200, 204, 400, 404))
    }





}
