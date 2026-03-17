[![LinkedIn][linkedin-shield]][linkedin-url]
[![CI][ci-shield]][ci-url]
<br />
<div align="center">
<h3 align="center">Order Service</h3>

  <p align="center">
    Event-driven microservice responsible for order creation and lifecycle management within the Order Fulfillment Platform.
    <br />
    <br />
    <a href="https://github.com/order-fulfillment-platform">View Organization</a>
  </p>
</div>

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#about">About</a></li>
    <li><a href="#built-with">Built With</a></li>
    <li><a href="#architecture">Architecture</a></li>
    <li><a href="#api">API</a></li>
    <li><a href="#events">Events</a></li>
    <li><a href="#getting-started">Getting Started</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>

## About

The Order Service is responsible for creating and managing orders within the Order Fulfillment Platform. It exposes a REST API for order creation, persists orders in its own PostgreSQL database, and publishes domain events to Apache Kafka using the **Outbox Pattern** to guarantee at-least-once delivery.

Key features:
- Order creation with automatic total amount calculation
- Outbox Pattern for reliable event publishing
- Flyway database migrations
- Integration tests with Testcontainers

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Built With

[![Spring Boot][springboot-shield]][springboot-url]
[![Apache Kafka][kafka-shield]][kafka-url]
[![PostgreSQL][postgres-shield]][postgres-url]
[![Docker][docker-shield]][docker-url]
[![Java][java-shield]][java-url]

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Architecture

### Outbox Pattern
```
POST /api/v1/orders
        │
        ▼
[Transaction]
  INSERT orders
  INSERT outbox_events
        │
        ▼
@Scheduled every 5s
  SELECT outbox_events WHERE processed = false
  → publish to Kafka
  → UPDATE processed = true
```

### Database Schema

| Table | Description |
|---|---|
| orders | Order records with status and total amount |
| order_items | Individual items belonging to an order |
| outbox_events | Transactional outbox for reliable event publishing |

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## API

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/v1/orders | Create a new order |
| GET | /api/v1/orders/{id} | Get order by ID |

### Create Order — Request
```json
{
  "customerId": "a3f8c2d1-1234-5678-abcd-ef0123456789",
  "items": [
    {
      "productId": "b7e9f3a2-1234-5678-abcd-ef0123456789",
      "quantity": 2,
      "unitPrice": 29.99
    }
  ]
}
```

### Create Order — Response
```json
{
  "id": "6c3cf148-363c-47b6-a411-01db3324486a",
  "customerId": "a3f8c2d1-1234-5678-abcd-ef0123456789",
  "status": "PENDING",
  "totalAmount": 59.98,
  "createdAt": "2026-03-17T10:00:00",
  "items": [
    {
      "id": "dad31eab-86fc-4a85-b988-40777a2f2cae",
      "productId": "b7e9f3a2-1234-5678-abcd-ef0123456789",
      "quantity": 2,
      "unitPrice": 29.99
    }
  ]
}
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Events

### Published

| Topic | Event | Description |
|---|---|---|
| order.created | ORDER_CREATED | Published when a new order is created |

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Getting Started

### Prerequisites

- Java 21
- Maven 3.9+
- Docker 24+

### Run locally
```bash
# Start infrastructure
docker-compose up -d order-postgres kafka zookeeper

# Run the service
mvn spring-boot:run
```

### Run tests
```bash
mvn test
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Contact

Eros Burelli — [LinkedIn](https://www.linkedin.com/in/eros-burelli-a458b1145/)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- MARKDOWN LINKS & IMAGES -->
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://www.linkedin.com/in/eros-burelli-a458b1145/
[springboot-shield]: https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white
[springboot-url]: https://spring.io/projects/spring-boot
[kafka-shield]: https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white
[kafka-url]: https://kafka.apache.org/
[postgres-shield]: https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white
[postgres-url]: https://www.postgresql.org/
[docker-shield]: https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white
[docker-url]: https://www.docker.com/
[java-shield]: https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white
[java-url]: https://www.java.com/
[ci-shield]: https://github.com/order-fulfillment-platform/order-service/actions/workflows/ci.yml/badge.svg
[ci-url]: https://github.com/order-fulfillment-platform/order-service/actions/workflows/ci.yml