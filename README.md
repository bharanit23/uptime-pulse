# UptimePulse

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Ready-4169E1?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)
[![CI Build](https://github.com/bharanit23/uptime-pulse/actions/workflows/ci.yml/badge.svg)](https://github.com/bharanit23/uptime-pulse/actions)

A lightweight, concurrent service availability and latency monitoring platform built with Java 21, Spring Boot 3, and relational persistence. It performs asynchronous HTTP health checks on configurable schedules, tracks response latency percentiles, manages incident lifecycles, and serves real-time status dashboards.

---

## Live Deployment

* **Production URL:** [https://uptime-pulse-kc57.onrender.com](https://uptime-pulse-kc57.onrender.com)
* **Public Status View:** [https://uptime-pulse-kc57.onrender.com/#public](https://uptime-pulse-kc57.onrender.com/#public)
* **API Endpoint:** [https://uptime-pulse-kc57.onrender.com/api/monitors](https://uptime-pulse-kc57.onrender.com/api/monitors)

---

## Overview & Architecture

UptimePulse is engineered as a standalone, low-overhead monitoring daemon designed to monitor critical HTTP endpoints, payment webhooks, and REST APIs. 

### Data Flow

```mermaid
flowchart TD
    Client([Client Browser]) -->|HTTP / JSON| WebUI["Tailwind + Vanilla JS Dashboard"]
    WebUI -->|REST Endpoints| Controller["Spring MVC REST Controllers"]
    
    subgraph CoreApplication["UptimePulse Application"]
        Controller --> MonitorService["Monitor & Metrics Service"]
        Scheduler["Async Scheduled Worker\n(ExecutorService Pool)"] -->|Periodic Probes| HealthChecker["HealthCheckService\n(Java HttpClient)"]
        HealthChecker --> MonitorService
        MonitorService --> DataAccess["Spring Data JPA Repositories"]
    end

    HealthChecker -->|Outbound HTTP/HTTPS| ExternalServices["Target Services & APIs"]
    DataAccess --> Storage[("Relational Database\nPostgreSQL / Embedded H2")]
```

---

## Core Capabilities

* **Non-Blocking Scheduled Probes:** Background polling jobs run on dedicated thread pools using Java's built-in `HttpClient`, avoiding worker thread starvation during slow downstream network calls.
* **Latency & SLA Aggregation:** Records millisecond response latencies and computes uptime SLA percentages over rolling windows.
* **Automated Incident Tracking:** Detects state transitions (`OPERATIONAL` ↔ `DEGRADED` ↔ `DOWN`). When an outage occurs, an incident record is logged with root cause details and automatically marked resolved when healthy responses resume.
* **Dual Persistence Strategy:** Native support for cloud PostgreSQL via standard JDBC configurations, with an automatic in-memory H2 fallback for zero-dependency local development and CI test runners.
* **Public Status Page:** Includes a read-only stakeholder view displaying service health and historical incident logs.

---

## Database Model

```mermaid
erDiagram
    MONITORS ||--o{ PING_LOGS : "records"
    MONITORS ||--o{ INCIDENTS : "triggers"

    MONITORS {
        bigint id PK
        varchar name
        varchar url
        varchar http_method
        int interval_seconds
        int expected_status_code
        varchar status
        bigint last_latency_ms
        timestamp last_checked_at
        double uptime_percentage
        boolean active
        timestamp created_at
    }

    PING_LOGS {
        bigint id PK
        bigint monitor_id FK
        int status_code
        bigint response_time_ms
        boolean is_successful
        varchar error_message
        timestamp timestamp
    }

    INCIDENTS {
        bigint id PK
        bigint monitor_id FK
        timestamp started_at
        timestamp resolved_at
        varchar status
        varchar root_cause
    }
```

The `ping_logs` table includes a composite index on `(monitor_id, timestamp DESC)` to optimize time-series lookups and status graph queries.

---

## REST API Specification

| Method | Route | Description |
| :--- | :--- | :--- |
| `GET` | `/api/monitors` | List all registered monitors with recent log history |
| `GET` | `/api/monitors/{id}` | Fetch single monitor details and metrics |
| `POST` | `/api/monitors` | Register a new endpoint monitor |
| `PUT` | `/api/monitors/{id}` | Update endpoint target or check interval |
| `DELETE` | `/api/monitors/{id}` | Delete a monitor and its historical logs |
| `POST` | `/api/monitors/{id}/ping` | Trigger an immediate manual health check probe |
| `POST` | `/api/monitors/{id}/toggle` | Pause or resume background monitoring |
| `GET` | `/api/dashboard/stats` | Aggregated system health metrics |
| `GET` | `/api/dashboard/incidents` | Recent incident and outage logs |
| `GET` | `/api/status/public` | Unauthenticated public status payload |

---

## Local Development

### Requirements
* Java 21 or higher
* Git

### Build and Run

```bash
git clone https://github.com/bharanit23/uptime-pulse.git
cd uptime-pulse

# Run via Gradle wrapper
./gradlew bootRun
```

On Windows:
```powershell
.\gradlew.bat bootRun
```

The application starts on `http://localhost:8080`.

---

## Environment Configuration

Configuration properties can be set via standard environment variables:

| Variable | Default | Description |
| :--- | :--- | :--- |
| `PORT` | `8080` | HTTP port for the web server |
| `SPRING_DATASOURCE_URL` | `jdbc:h2:mem:uptimedb;...` | PostgreSQL or H2 JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | `sa` | Database user account |
| `SPRING_DATASOURCE_PASSWORD` | `""` | Database password |
| `SPRING_DATASOURCE_DRIVER` | `org.h2.Driver` | Driver class (`org.postgresql.Driver` for PostgreSQL) |

---

## Author

Developed by [Bharani](https://github.com/bharanit23).
