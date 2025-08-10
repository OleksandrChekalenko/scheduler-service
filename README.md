# Treatment Plan Scheduler Service

A simple Spring Boot application that implements a scheduler service to generate treatment tasks from treatment plans based on their recurrence patterns.

## Task Overview

This is a coding test implementation that demonstrates:
- **Treatment Plan Entity**: Contains treatment action, patient, start/end time, recurrence pattern
- **Treatment Task Entity**: Contains treatment action, patient, start time, status  
- **Scheduler Service**: Generates tasks from plans automatically based on recurrence patterns
- **H2 Database**: Embedded database for persistence

## Core Functionality

The scheduler service:
1. Finds active treatment plans in the database
2. Parses their recurrence patterns (DAILY, WEEKLY, MONTHLY, ONCE)
3. Generates corresponding treatment tasks for the next 24 hours
4. Runs automatically every 5 minutes
5. Prevents duplicate task creation

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.6+

### Run the Application
```bash
./mvnw spring-boot:run
```

### Test the Scheduler
```bash
# Manual trigger (generates tasks from test data)
curl -X POST http://localhost:8080/api/scheduler/run

# View H2 console to see generated tasks
open http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:treatment_scheduler
# Username: sa, Password: password
```

### Run Tests
```bash
./mvnw test
```

## Sample Data

The application creates 2 test treatment plans on startup:
- **PATIENT_001**: Daily treatment at 08:00 and 20:00
- **PATIENT_002**: Weekly treatment on Mondays at 10:00

## Recurrence Pattern Format

| Pattern | Example | Description |
|---------|---------|-------------|
| DAILY | `DAILY:08:00,20:00` | Daily at specified times |
| WEEKLY | `WEEKLY:MONDAY:10:00` | Weekly on specific day |
| MONTHLY | `MONTHLY:15:14:00` | Monthly on specific day |
| ONCE | `ONCE:09:30` | One-time execution |

## Architecture

- **Entities**: `TreatmentPlan`, `TreatmentTask`
- **Repositories**: JPA repositories for data access
- **Services**: `TreatmentSchedulerService`, `RecurrencePatternService`
- **Controller**: Simple endpoint for manual triggering
- **Database**: H2 in-memory database

This is a minimal implementation focused on demonstrating the core scheduling functionality as specified in the coding task requirements.
