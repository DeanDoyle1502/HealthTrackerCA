# HealthTrackerCA

Health Tracker API for assessment work.

## Tech
- Javalin
- Exposed
- PostgreSQL (prod/dev) + H2 (tests)

## Run (dev)
- Configure DB env/config
- Start the server
- Test: `mvn test`

## Project structure
- controllers/ routes/ handlers for HTTP endpoints
- dao/ database access layer
- models/ domain models
