# Order Management API

A production-grade backend REST API for order management built with Java 17 and Spring Boot. Features JWT authentication, role-based access control, Redis caching, Kafka event publishing, and full Docker support.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.x |
| Security | Spring Security + JWT |
| Database | PostgreSQL + Spring Data JPA + Hibernate |
| Caching | Redis |
| Messaging | Apache Kafka |
| Documentation | Swagger / OpenAPI |
| Containerisation | Docker + Docker Compose |
| Build Tool | Maven |

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Client (Postman)                   │
└─────────────────────┬───────────────────────────────┘
                      │ HTTP
┌─────────────────────▼───────────────────────────────┐
│              Spring Boot Application                 │
│                                                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │ Auth        │  │ Product     │  │ Order       │ │
│  │ Controller  │  │ Controller  │  │ Controller  │ │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘ │
│         │                │                │         │
│  ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐ │
│  │ Auth        │  │ Product     │  │ Order       │ │
│  │ Service     │  │ Service     │  │ Service     │ │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘ │
│         │                │     Redis Cache  │        │
│  ┌──────▼────────────────▼─────────────────▼──────┐ │
│  │              Repository Layer (JPA)             │ │
│  └──────────────────────┬──────────────────────────┘ │
│                         │              Kafka Producer │
└─────────────────────────┼──────────────────┬─────────┘
                          │                  │
              ┌───────────▼──┐    ┌──────────▼────────┐
              │  PostgreSQL  │    │   Kafka + Redis   │
              └──────────────┘    └───────────────────┘
```

---

## Features

- **JWT Authentication** — register, login, token-based stateless auth
- **Role-based Access Control** — ADMIN can manage products, USER can place orders
- **Order Management** — place orders, view orders, cancel orders with automatic stock management
- **Redis Caching** — product endpoints cached with 10-minute TTL, cache invalidated on updates
- **Kafka Event Publishing** — `OrderPlaced` event published on every new order, consumed by notification consumer
- **Global Exception Handling** — clean JSON error responses on every failure
- **Input Validation** — request validation with meaningful error messages
- **API Documentation** — Swagger UI with bearer auth support

---

## Domain Model

```
User (1) ──────────── (Many) Order
                               │
                          (1) Order (1)
                               │
                         (Many) OrderItem (Many) ──── (1) Product
```

**Key design decision:** `OrderItem` is used instead of `@ManyToMany` between `Order` and `Product` because the relationship needs to store `quantity` and `price at time of purchase` — data that a plain join table cannot hold. This also ensures historical order prices remain correct even if product prices change later.

---

## API Endpoints

### Authentication
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login and get JWT token |

### Products
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/products` | Authenticated | Get all products |
| GET | `/api/products/{id}` | Authenticated | Get product by ID |
| POST | `/api/products` | ADMIN only | Create product |
| PUT | `/api/products/{id}` | ADMIN only | Update product |
| DELETE | `/api/products/{id}` | ADMIN only | Delete product |

### Orders
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/orders` | Authenticated | Place new order |
| GET | `/api/orders/my` | Authenticated | Get my orders |
| GET | `/api/orders/{id}` | Authenticated | Get order by ID |
| PUT | `/api/orders/{id}/cancel` | Authenticated | Cancel order |

---

## Running Locally with Docker

### Prerequisites
- Docker Desktop installed and running

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/your-username/order-management-api.git
cd order-management-api
```

**2. Run everything with one command**
```bash
docker compose up --build
```

This starts 5 containers automatically:
- `orderdb-postgres` — PostgreSQL database
- `orderdb-redis` — Redis cache
- `orderdb-zookeeper` — Zookeeper for Kafka
- `orderdb-kafka` — Kafka broker
- `order-management-api` — Spring Boot application

**3. Access the API**
- API Base URL: `http://localhost:8085`
- Swagger UI: `http://localhost:8085/swagger-ui/index.html`

**4. Stop everything**
```bash
docker compose down
```

---

## Running Locally without Docker

### Prerequisites
- Java 17
- Maven
- PostgreSQL running on port 5432
- Redis running on port 6379
- Kafka running on port 9092

### Steps

**1. Create the database**
```sql
CREATE DATABASE orderdb;
```

**2. Update `application.yaml`** with your local credentials

**3. Run the app**
```bash
mvn spring-boot:run
```

---

## Testing the API

### 1. Register a user
```bash
POST /api/auth/register
{
    "name": "Prince Kumar",
    "email": "prince@gmail.com",
    "password": "password123"
}
```

### 2. Set ADMIN role (via psql or pgAdmin)
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'prince@gmail.com';
```

### 3. Login and copy the token
```bash
POST /api/auth/login
{
    "email": "prince@gmail.com",
    "password": "password123"
}
```

### 4. Create a product (as ADMIN)
```bash
POST /api/products
Authorization: Bearer <token>
{
    "name": "Mechanical Keyboard",
    "description": "RGB mechanical keyboard",
    "price": 1500.00,
    "stock": 50
}
```

### 5. Place an order (as USER)
```bash
POST /api/orders
Authorization: Bearer <token>
{
    "items": [
        {
            "productId": "paste-product-uuid-here",
            "quantity": 2
        }
    ]
}
```

---

## Project Structure

```
src/main/java/com/prince/order_management_api/
├── controller/          # REST controllers
├── service/             # Service interfaces
│   └── impl/            # Service implementations
├── repository/          # JPA repositories
├── entity/              # JPA entities
├── dto/
│   ├── request/         # Request DTOs
│   └── response/        # Response DTOs
├── security/            # JWT filter, UserDetails, SecurityConfig
├── kafka/               # Kafka producer and consumer
├── event/               # Kafka event classes
├── exception/           # Global exception handler
├── config/              # Redis, Kafka, Swagger configs
└── enums/               # Role, OrderStatus
```

---

## Key Technical Decisions

**Why `@Transactional` on `placeOrder` and `cancelOrder`?**
Both methods update multiple tables (order + multiple products). `@Transactional` ensures atomicity — if any step fails, all changes are rolled back, preventing partial states like stock being reduced without an order being created.

**Why Redis caching on products only?**
Products are read frequently and change rarely — ideal for caching. Orders are user-specific and change frequently, making caching less effective. User security context is not cached to avoid serialization complexity and security concerns.

**Why Kafka for order events?**
Decouples order processing from downstream actions (notifications, warehouse updates). In a real microservices setup the consumer would be a separate notification service. This pattern allows multiple consumers to react to the same event independently.

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/orderdb` | PostgreSQL URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | `your_password` | DB password |
| `SPRING_DATA_REDIS_HOST` | `localhost` | Redis host |
| `SPRING_DATA_REDIS_PORT` | `6379` | Redis port |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker |

---

## Author

**Prince Kumar Soni**
- LinkedIn: [your-linkedin]
- GitHub: [your-github]
