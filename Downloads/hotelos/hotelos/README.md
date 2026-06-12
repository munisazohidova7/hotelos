# HotelOS — Real-Time Hotel Management System

**BTEC Module 4 — Programming | Assignment: HotelOS**

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Message Broker | RabbitMQ 3.x |
| WebSocket | STOMP over SockJS (Spring WebSocket) |
| Build Tool | Maven 3.8+ |
| IDE Recommended | IntelliJ IDEA or VS Code |

## Architecture Overview

```
┌─────────────────┐     ROOM_VACATED      ┌──────────────────────┐
│ Reception        │ ──────────────────→   │ Housekeeping          │
│ :8081           │                        │ :8082                 │
│ - Check-in      │     GUEST_CHECKED_IN   │ - Cleaning queue      │
│ - Check-out     │ ──────────────────→   │ - Mark room CLEAN     │
│ - Billing algo  │                        └──────────────────────┘
└─────────────────┘
        │                  RabbitMQ Exchange: hotelos.events
        │          ┌─────────────────────────────────┐
        └─────────→│  Topic Exchange (hotelos.events) │←──────────┐
                   └─────────────────────────────────┘            │
                            │             │                        │
                  ┌─────────┘             └──────────┐            │
                  ↓                                  ↓            │
        ┌──────────────────┐              ┌──────────────────┐    │
        │ Room Service     │              │ Maintenance      │    │
        │ :8083            │              │ :8084            │    │
        │ - Food orders    │              │ - Priority queue │    │
        │ - Order tracking │              │ - Tech assign    │    │
        └──────────────────┘              └──────────────────┘    │
                  │                                  │            │
                  └────────────────┬─────────────────┘            │
                                   ↓                              │
                         ┌──────────────────┐                     │
                         │ Dashboard        │─────────────────────┘
                         │ :8085            │  Subscribes to ALL events
                         │ - WebSocket push │  via wildcard binding (#)
                         │ - Spring Security│
                         └──────────────────┘
                                   │
                         WebSocket ↓ /topic/dashboard
                         ┌──────────────────┐
                         │  Browser         │
                         │  Real-time UI    │
                         └──────────────────┘
```

## Prerequisites

1. **Java 17+**
   ```bash
   java -version   # must show 17 or higher
   ```

2. **Maven 3.8+**
   ```bash
   mvn -version
   ```

3. **RabbitMQ** (must be running BEFORE starting services)
   - **Docker (recommended):**
     ```bash
     docker run -d --name rabbitmq \
       -p 5672:5672 -p 15672:15672 \
       rabbitmq:3-management
     ```
   - **Manual install:** https://www.rabbitmq.com/download.html
   - Default credentials: `guest / guest`
   - Management UI: http://localhost:15672

## Quick Start

### Step 1 — Build all modules
```bash
cd hotelos
mvn clean install -DskipTests
```

### Step 2 — Start RabbitMQ (if not already running)
```bash
docker start rabbitmq
# OR on Windows: rabbitmq-service start
```

### Step 3 — Start each service (open 5 terminals)

```bash
# Terminal 1 — Reception (port 8081)
cd reception-service
mvn spring-boot:run

# Terminal 2 — Housekeeping (port 8082)
cd housekeeping-service
mvn spring-boot:run

# Terminal 3 — Room Service (port 8083)
cd room-service
mvn spring-boot:run

# Terminal 4 — Maintenance (port 8084)
cd maintenance-service
mvn spring-boot:run

# Terminal 5 — Dashboard (port 8085)
cd dashboard
mvn spring-boot:run
```

### Step 4 — Open Dashboard
```
http://localhost:8085/login
Username: staff
Password: hotel123
```

---

## Test Scenario Walkthroughs (curl commands)

### TS-01: Check-in guest requesting floor 2 double room
```bash
curl -X POST http://localhost:8081/api/reception/checkin \
  -H "Content-Type: application/json" \
  -d '{
    "guestId": "G001",
    "fullName": "Ali Karimov",
    "email": "ali@example.com",
    "requestedRoomType": "DOUBLE",
    "checkInDate": "2025-01-15",
    "checkOutDate": "2025-01-18",
    "preferredFloor": 2,
    "wantsNearLift": false,
    "wantsNearStairs": false
  }'
```
**Expected:** Room 202 or 204 (floor 2, DOUBLE type, longest-clean)

### TS-02: Check-out from room 204
```bash
curl -X POST http://localhost:8081/api/reception/checkout/204
```
**Expected:** Bill calculated, room → DIRTY, ROOM_VACATED event published, Housekeeping queues room 204

### TS-03: Housekeeper marks room 204 clean
```bash
# First start cleaning
curl -X POST "http://localhost:8082/api/housekeeping/start/204?cleanerName=Malika"

# Then mark clean
curl -X POST http://localhost:8082/api/housekeeping/clean/204
```
**Expected:** Room 204 → CLEAN, Dashboard updates in real time via WebSocket

### TS-04: Room service order from room 301
```bash
curl -X POST "http://localhost:8083/api/roomservice/order?roomNumber=104" \
  -H "Content-Type: application/json" \
  -d '[
    {"name": "Coffee", "quantity": 2, "price": 5.0},
    {"name": "Sandwich", "quantity": 1, "price": 12.0}
  ]'
```
Then advance status:
```bash
curl -X POST http://localhost:8083/api/roomservice/advance/ORD-<timestamp>
```

### TS-05: Maintenance — broken shower in room 115, CRITICAL
```bash
curl -X POST "http://localhost:8084/api/maintenance/report?roomNumber=103&description=Broken+shower&urgency=CRITICAL"
```
**Expected:** Goes to front of queue, Technician-A assigned immediately

### TS-06: Concurrent check-in (run simultaneously)
```bash
# Run these two commands at the same time in separate terminals
curl -X POST http://localhost:8081/api/reception/checkin -H "Content-Type: application/json" \
  -d '{"guestId":"G010","fullName":"Bobur","email":"b@b.com","requestedRoomType":"SINGLE","checkInDate":"2025-01-15","checkOutDate":"2025-01-17"}'

curl -X POST http://localhost:8081/api/reception/checkin -H "Content-Type: application/json" \
  -d '{"guestId":"G011","fullName":"Dilnoza","email":"d@d.com","requestedRoomType":"SINGLE","checkInDate":"2025-01-15","checkOutDate":"2025-01-17"}'
```
**Expected:** Two different rooms assigned (synchronized assignment prevents double-booking)

### TS-07: No rooms available
```bash
# Request SUITE when no suites are clean
curl -X POST http://localhost:8081/api/reception/checkin -H "Content-Type: application/json" \
  -d '{"guestId":"G999","fullName":"Test Guest","email":"t@t.com","requestedRoomType":"SUITE","checkInDate":"2025-01-15","checkOutDate":"2025-01-16"}'
```
**Expected:** `{"success":false,"error":"No rooms available for type: SUITE","suggestion":"..."}`

### TS-08: Invalid room number input
```bash
curl -X POST "http://localhost:8081/api/reception/checkout/99999"
```
**Expected:** `{"success":false,"error":"Invalid room number: 99999"}`

---

## Service Port Summary

| Service | Port | Purpose |
|---------|------|---------|
| Reception | 8081 | Check-in, check-out, billing |
| Housekeeping | 8082 | Cleaning queue management |
| Room Service | 8083 | Food/beverage orders |
| Maintenance | 8084 | Issue reporting & priority queue |
| Dashboard | 8085 | Real-time WebSocket operations panel |
| RabbitMQ | 5672 | Message broker |
| RabbitMQ UI | 15672 | Broker management console |

---

## Git Log
```
(run: git log --oneline to see full history)
```

## Project Structure
```
hotelos/
├── shared/                    # Shared models & events
│   └── src/.../model/         # Room, Guest
│   └── src/.../event/         # HotelEvent, RabbitMQConstants
├── reception-service/         # Port 8081
│   └── RoomAssignmentService  # Core algorithm (LO1)
│   └── BillingService         # Secondary algorithm (LO1)
├── housekeeping-service/      # Port 8082
│   └── HousekeepingEventListener  # RabbitMQ subscriber (LO2 - Event-driven)
├── room-service/              # Port 8083
├── maintenance-service/       # Port 8084
│   └── MaintenanceService     # Priority queue algorithm (LO1)
└── dashboard/                 # Port 8085
    └── DashboardBroadcaster   # WebSocket push (LO2 - Event-driven + WebSocket)
    └── static/index.html      # Real-time UI
```
