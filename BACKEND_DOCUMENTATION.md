# Hotel Booking System - Backend Documentation

## Table of Contents

1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Architecture](#architecture)
4. [Setup & Installation](#setup--installation)
5. [Database Configuration](#database-configuration)
6. [Security Implementation](#security-implementation)
7. [API Endpoints](#api-endpoints)
8. [Background Jobs](#background-jobs)
9. [Caching Strategy](#caching-strategy)
10. [Error Handling](#error-handling)
11. [Testing](#testing)
12. [Deployment](#deployment)

---

## 1. Project Overview

Enterprise-grade hotel booking system with advanced features including search, reviews, image management, calendar, promotions, and admin panel.

**Key Features:**

- JWT-based authentication with role-based access control
- Advanced search with filters and sorting
- Review and rating system
- Image upload to Cloudinary
- Availability calendar with seasonal pricing
- Discount codes and loyalty program
- Admin panel with audit logging

---

## 2. Technology Stack

### Core Framework

- **Spring Boot**: 3.5.8
- **Java**: 17
- **Build Tool**: Maven

### Database

- **Production**: PostgreSQL 15+
- **Migration**: Flyway

### Security

- **Authentication**: JWT (JSON Web Tokens)
- **Authorization**: Spring Security with role-based access
- **Password Hashing**: BCrypt

### Caching

- **Redis**: For search results and availability

### Background Jobs

- **JobRunr**: For scheduled tasks and async processing

### Cloud Services

- **Cloudinary**: Image storage and optimization

### Documentation

- **Swagger/OpenAPI**: API documentation

### Additional Libraries

- **Lombok**: Reduce boilerplate code
- **Jakarta Validation**: Request validation
- **iText7**: PDF generation
- **Apache POI**: Excel export

---

## 3. Architecture

### Layered Architecture

```
┌─────────────────────────────────────────────┐
│           Controllers (REST API)            │
│  - Handle HTTP requests/responses           │
│  - Request validation                       │
│  - DTO conversion                           │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│         Services (Business Logic)           │
│  - Business rules enforcement               │
│  - Transaction management                   │
│  - Complex operations                       │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│      Repositories (Data Access Layer)       │
│  - CRUD operations                          │
│  - Custom queries                           │
│  - JPA/Hibernate                            │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│         Database (PostgreSQL)               │
└─────────────────────────────────────────────┘
```

### Package Structure

```
com.example.booking/
├── admin/              # Admin panel functionality
├── analytics/          # Dashboard and statistics
├── auth/              # Authentication
├── booking/           # Booking management
├── calendar/          # Availability calendar
├── config/            # Configuration classes
├── exception/         # Custom exceptions
├── hotel/             # Hotel and room management
├── image/             # Image upload/management
├── promotion/         # Discounts and loyalty
├── review/            # Reviews and ratings
├── security/          # Security configuration
└── user/              # User management
```

---

## 4. Setup & Installation

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 15+
- Redis (for caching)
- Cloudinary account

### Environment Variables

Create `.env` file or set environment variables:

```bash
# Database
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# JWT
JWT_SECRET=your-256-bit-secret-key-here
JWT_EXPIRATION=86400000

# Cloudinary
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

### Installation Steps

1. **Clone Repository**

```bash
git clone <repository-url>
cd booking
```

2. **Create PostgreSQL Database**

```bash
psql -U postgres
CREATE DATABASE booking;
\q
```

3. **Configure Application**
   Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/booking
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
```

4. **Install Dependencies**

```bash
mvn clean install -DskipTests
```

5. **Run Database Migrations**

```bash
mvn flyway:migrate
```

6. **Start Application**

```bash
mvn spring-boot:run
```

7. **Access Application**

- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **JobRunr Dashboard**: http://localhost:8000/dashboard

---

## 5. Database Configuration

### PostgreSQL Setup

**Connection String:**

```
jdbc:postgresql://localhost:5432/booking
```

**Default Credentials:**

- Username: `postgres`
- Password: `postgres` (change in production!)

### Database Schema

The application uses **Flyway** for database migrations. Schema is managed through migration scripts in `src/main/resources/db/migration/`.

**Migrations:**

- V1: Initial schema (users, hotels, rooms, bookings)
- V2: Performance indexes
- V3: Rating fields and search indexes
- V4: Reviews table
- V5: Image tables
- V6: Calendar tables (blocked dates, seasonal pricing)
- V7: Booking management fields
- V8: Promotions and admin tables (discount codes, loyalty points, audit logs)

### Important Tables

- **users** - User accounts with roles
- **hotels** - Hotel information with ratings
- **rooms** - Room details with pricing
- **bookings** - Booking records with status tracking
- **reviews** - Customer reviews and seller responses
- **hotel_images / room_images** - Image galleries
- **blocked_dates** - Unavailable date ranges
- **seasonal_pricing** - Dynamic pricing rules
- **discount_codes** - Promotional codes
- **loyalty_points** - Customer loyalty program
- **audit_logs** - System activity tracking

---

## 6. Security Implementation

### JWT Authentication Flow

1. User logs in with email/password
2. Server validates credentials
3. Server generates JWT token with claims:
   - userId
   - email
   - roles
   - expiration
4. Client stores token
5. Client sends token in Authorization header
6. Server validates token on each request

### Role-Based Access Control

```java
// Public endpoints
@GetMapping("/api/hotels")
public List<Hotel> getAllHotels() { }

// Authenticated users only
@PreAuthorize("isAuthenticated()")
@GetMapping("/api/bookings/my-bookings")
public List<Booking> getMyBookings() { }

// Seller only
@PreAuthorize("hasRole('SELLER')")
@PostMapping("/api/hotels")
public Hotel createHotel() { }

// Admin only
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/api/admin/statistics")
public Map<String, Object> getStats() { }
```

---

## 7. API Endpoints

See `API_DOCUMENTATION.md` for complete endpoint reference.

**Main Endpoint Groups:**

- `/api/auth` - Authentication
- `/api/hotels` - Hotel management
- `/api/bookings` - Booking operations
- `/api/reviews` - Review system
- `/api/images` - Image upload
- `/api/calendar` - Availability calendar
- `/api/promotions` - Discounts & loyalty
- `/api/admin` - Admin panel

---

## 8. Background Jobs

### JobRunr Configuration

**Dashboard**: http://localhost:8000/dashboard

**Scheduled Jobs:**

- Auto-cancel no-show bookings (Daily at 2 AM)
- Clean up expired discount codes
- Update loyalty points statistics

---

## 9. Caching Strategy

### Redis Configuration

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### Cached Operations

- Hotel searches: 10 minutes TTL
- Room availability: 1 minute TTL
- User data: 30 minutes TTL

---

## 10. Error Handling

Global exception handler provides consistent error responses:

```json
{
  "timestamp": "2025-12-25T15:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/bookings"
}
```

---

## 11. Testing

```bash
# Unit tests
mvn test

# Integration tests with PostgreSQL
mvn verify

# Skip tests
mvn clean install -DskipTests
```

---

## 12. Deployment

### Production Checklist

- [ ] Use PostgreSQL instead of H2
- [ ] Configure secure passwords
- [ ] Set production JWT secret
- [ ] Configure Redis cluster
- [ ] Enable HTTPS
- [ ] Configure CORS for frontend domain
- [ ] Set up monitoring (Prometheus/Grafana)
- [ ] Configure logging (ELK stack)
- [ ] Set up automated backups
- [ ] Load testing
- [ ] Security audit

### Docker Deployment

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/booking-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

```bash
docker build -t booking-api .
docker run -p 8080:8080 \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=secret \
  -e CLOUDINARY_API_KEY=your_key \
  booking-api
```

### Environment-Specific Configuration

**Development:**

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true
```

**Production:**

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    enabled: true
    baseline-on-migrate: false
```

---

## Support & Maintenance

### Common Issues

**Database Connection Failed**

- Ensure PostgreSQL is running: `sudo systemctl status postgresql`
- Verify credentials in `application.yml`
- Check firewall allows port 5432

**Redis Connection Failed**

- Ensure Redis server is running: `redis-cli ping`
- Check port 6379 is accessible

**JWT Token Invalid**

- Verify JWT_SECRET matches
- Check token expiration time

### Monitoring

**Recommended Tools:**

- **Prometheus**: Metrics collection
- **Grafana**: Visualization
- **ELK Stack**: Log aggregation
- **Sentry**: Error tracking

---

## API Rate Limits (Recommended)

- Authentication: 5 requests/minute
- Search: 60 requests/minute
- Booking: 10 requests/minute
- Image upload: 20 requests/hour

---

## Contact

For technical support or questions:

- Email: dev-team@example.com
- Documentation: http://localhost:8080/swagger-ui.html
