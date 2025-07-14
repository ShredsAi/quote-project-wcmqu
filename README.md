# Quote Approval Workflow Shred

## Overview
The Quote Approval Workflow Shred manages the end-to-end lifecycle of quote approvals. It receives newly created quotes, applies configurable business rules, and facilitates review by moderators. Final decisions are recorded, an audit trail is maintained, and events are published to notify other shreds of status changes.

## Architecture
This service follows a Hexagonal (Ports & Adapters) Architecture:
- **Adapters (Outer Layer)**: REST controllers, RabbitMQ listeners, and publishers.
- **Ports**: Interfaces for application input/output and domain input/output.
- **Application/Core**: Use case services orchestrating business logic.
- **Domain**: Entities, value objects, and domain services implementing core rules.
- **Infrastructure**: JPA repositories, RabbitMQ clients, and configurations.

## Setup Instructions
1. Clone the repository:
   ```bash
   git clone https://github.com/your-org/quote-approval-workflow-shred.git
   cd quote-approval-workflow-shred
   ```
2. Configure environment variables or `application-prod.yml` for production.
3. Build the application:
   ```bash
   mvn clean package -DskipTests
   ```
4. Run with Docker Compose:
   ```bash
   docker-compose up --build
   ```
5. Access API at `http://localhost:8080/api/approval`.

## API Endpoints
| Method | URI                                      | Description                                |
|--------|------------------------------------------|--------------------------------------------|
| GET    | /api/approval/pending                    | List pending approval requests             |
| POST   | /api/approval/{requestId}/approve        | Approve a quote                            |
| POST   | /api/approval/{requestId}/reject         | Reject a quote                             |
| PUT    | /api/approval/{requestId}/assign         | Assign a moderator                         |
| GET    | /api/approval/audit/{requestId}          | Get audit trail                            |
| GET    | /api/approval/queues                     | List approval queues                       |

## Database Schema
See `src/main/resources/schema.sql` for table definitions:
- `approval_requests`
- `approval_decisions`
- `approval_audit_logs`
- `approval_queues`
- `moderation_rules`
- `approval_request_rule_executions`

## Messaging Queues
- **approval.assignments**: Consumed for automatic moderator assignments.
- **approval.notifications**: Published for stakeholder notifications.
- **approval.deadlines**: Consumed for deadline alerts.

## Business Rules
1. Approval within 48 hours (24h for high priority).
2. Only authenticated moderators with permissions.
3. Single-assignment before review.
4. Rejections require a predefined reason.
5. High/urgent to senior moderators.
6. Single appeal per rejection.
7. Decisions immutable (except appeals).
8. Enforce queue capacities.
9. No self-review by moderators.
10. Record all rule violations in audit trail.
