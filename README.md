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

## OpenShift Deployment

### Deploy
```
oc new-app registry.access.redhat.com/ubi9/openjdk-17~https://github.com/DeanDoyle1502/HealthTrackerCA.git \
  --name health-tracker-ca

oc rollout status deployment/health-tracker-ca
```

### Enviornment Variables
```
oc set env deployment/health-tracker-ca \
  POSTGRESQL_HOST=postgresql \
  POSTGRESQL_SERVICE_PORT=5432 \
  POSTGRESQL_DATABASE=<db_name> \
  POSTGRESQL_USER=<db_user> \
  POSTGRESQL_PASSWORD=<db_password> \
  PORT=8080 \
  INIT_DB=true

oc rollout restart deployment/health-tracker-ca
oc rollout status deployment/health-tracker-ca
```

### Expose and testing
```
oc expose svc/health-tracker-ca
ROUTE=$(oc get route health-tracker-ca -o jsonpath='{.spec.host}')

curl -i "http://$ROUTE/api/users"
curl -i "http://$ROUTE/api/activities"
```

### INIT_DB

INIT_DB=true: initializes required tables on startup and may seed demo data (testing).
INIT_DB=false: disables seeding (recommended once the DB is already set up).

## Added Features

This API includes two additional database-backed features:

- **Goals**
    - Store user goals (e.g. target weight, weekly activity targets, etc.).
    - Endpoints allow creating goals and retrieving goals (all goals, by goal id, and by user id).

- **Measurements**
    - Store user measurements over time (e.g. weight, body measurements, or other tracked values).
    - Endpoints allow creating measurements and retrieving measurements (all measurements, by measurement id, and by user id).

- **Coverage Report with Jacoco**

- **Extra Integration testing**

- **CI/CD Github actions for deployment**
