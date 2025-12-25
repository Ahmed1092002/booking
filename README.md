# Hotel Booking System - Spring Boot

A comprehensive hotel booking system built with Spring Boot, featuring JWT authentication, role-based authorization, Redis caching, background jobs, and RESTful APIs.

## 🚀 Features

### Core Functionality

- **User Management**: Registration and authentication with JWT tokens
- **Role-Based Access Control**: Separate roles for Sellers and Customers
- **Hotel Management**: Sellers can create and manage hotels and rooms
- **Booking System**: Customers can search and book available rooms
- **Dashboard Analytics**: Revenue and booking statistics for sellers

### Advanced Features

- **Redis Caching**: Optimized search performance with TTL-based caching
- **Background Jobs**: Automated booking cleanup using JobRunr
- **AOP Logging**: Centralized logging and performance tracking
- **Exception Handling**: Comprehensive error handling with @ControllerAdvice
- **DTO Pattern**: Clean separation between API contracts and domain models
- **Swagger/OpenAPI**: Interactive API documentation

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Redis Server (for caching)
- H2 Database (embedded, no setup required)

## 🛠️ Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd booking
```

### 2. Start Redis Server

```bash
# Windows
redis-server

# Linux/Mac
sudo service redis-server start
```

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 🌐 Access Points

| Service           | URL                                   | Description                   |
| ----------------- | ------------------------------------- | ----------------------------- |
| Main API          | http://localhost:8080                 | REST API endpoints            |
| Swagger UI        | http://localhost:8080/swagger-ui.html | Interactive API documentation |
| H2 Console        | http://localhost:8080/h2-console      | Database management           |
| JobRunr Dashboard | http://localhost:8000/dashboard       | Background jobs monitoring    |

### H2 Database Connection

- **JDBC URL**: `jdbc:h2:mem:bookingdb`
- **Username**: `sa`
- **Password**: _(empty)_

## 🔐 Authentication

### Register a New User

```bash
POST /api/auth/register
Content-Type: application/json

{
  "email": "seller@example.com",
  "password": "password123",
  "fullName": "John Seller",
  "roles": ["ROLE_SELLER"]
}
```

### Login

```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "seller@example.com",
  "password": "password123"
}
```

**Response:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "email": "seller@example.com",
    "fullName": "John Seller",
    "roles": ["ROLE_SELLER"]
  }
}
```

### Using the Token

Include the token in the `Authorization` header for protected endpoints:

```bash
Authorization: Bearer <your-token-here>
```

## 📚 API Endpoints

### Hotels

#### Create Hotel (Seller Only)

```bash
POST /api/hotels
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Grand Hotel",
  "city": "New York",
  "address": "123 Main St",
  "googleMapUrl": "https://maps.google.com/...",
  "amenities": ["Pool", "WiFi", "Gym"]
}
```

#### Add Room to Hotel (Seller Only)

```bash
POST /api/hotels/{hotelId}/rooms
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Deluxe Suite",
  "pricePerNight": 150.00,
  "capacity": 2,
  "viewType": "Sea View",
  "hasKitchen": true
}
```

#### Search Hotels by City

```bash
GET /api/hotels/search?city=New York
```

#### Get All Hotels

```bash
GET /api/hotels
```

### Bookings

#### Create Booking (Authenticated Users)

```bash
POST /api/bookings
Authorization: Bearer <token>
Content-Type: application/json

{
  "roomId": 1,
  "checkInDate": "2025-01-15",
  "checkOutDate": "2025-01-20"
}
```

#### Get My Bookings

```bash
GET /api/bookings/my-bookings
Authorization: Bearer <token>
```

### Dashboard

#### Get Seller Dashboard (Seller Only)

```bash
GET /api/dashboard/seller
Authorization: Bearer <token>
```

**Response:**

```json
{
  "totalRevenue": 15000.0,
  "totalBookings": 45,
  "totalHotels": 3,
  "familyBookings": 20,
  "singleCoupleBookings": 25
}
```

## 🏗️ Architecture

### Project Structure

```
src/main/java/com/example/booking/
├── analytics/          # Dashboard and analytics
├── aspect/            # AOP logging and performance tracking
├── auth/              # Authentication controllers and services
├── booking/           # Booking domain (entities, DTOs, services)
├── config/            # Configuration classes (Cache, OpenAPI)
├── exception/         # Custom exceptions and global handler
├── hotel/             # Hotel domain (entities, DTOs, services)
├── job/               # Background jobs (cleanup tasks)
├── security/          # Security configuration and JWT handling
└── user/              # User domain (entities, DTOs, services)
```

### Design Patterns Used

#### 1. **DTO Pattern**

Separates API contracts from domain models:

- **Request DTOs**: `CreateHotelRequest`, `CreateBookingRequest`
- **Response DTOs**: `HotelResponseDto`, `BookingResponseDto`
- **Mappers**: Convert between entities and DTOs

#### 2. **Service Layer Pattern**

Business logic separated from controllers:

- `HotelService`: Hotel and room management
- `BookingService`: Booking creation and validation
- `AuthService`: User registration and authentication

#### 3. **Repository Pattern**

Data access abstraction using Spring Data JPA:

- `HotelRepository`
- `BookingRepository`
- `UserRepository`

#### 4. **Utility Service Pattern**

`CurrentUserService`: Centralized user extraction from JWT

## 🔒 Security Features

### Role-Based Authorization

```java
@PreAuthorize("hasRole('SELLER')")    // Only sellers
@PreAuthorize("hasRole('CUSTOMER')")  // Only customers
@PreAuthorize("isAuthenticated()")    // Any authenticated user
```

### JWT Token Security

- Tokens contain user identity and roles
- User information extracted from token (not request parameters)
- Prevents user impersonation attacks

### Exception Handling

All exceptions handled centrally in `GlobalExceptionHandler`:

- `UnauthorizedException` (401)
- `ForbiddenException` (403)
- `ResourceNotFoundException` (404)
- `BadRequestException` (400)
- Validation errors
- Spring Security exceptions

## ⚡ Performance Optimizations

### Redis Caching

Configured with specific TTLs:

- **Hotel searches by city**: 10 minutes
- **Room availability**: 1 minute
- **Default**: 60 minutes

```java
@Cacheable("hotelsByCity")
public List<Hotel> getHotelsByCity(String city) {
    // Cached for 10 minutes
}
```

### Database Indexing

Optimized queries with indexes on:

- `hotels.city`
- `bookings.check_in_date`, `bookings.check_out_date`
- `users.email`

### AOP Performance Tracking

```java
@TrackTime
public void expensiveOperation() {
    // Execution time automatically logged
}
```

## 🔄 Background Jobs

### Booking Cleanup Job

Automatically cancels expired pending bookings:

- **Schedule**: Daily at midnight (cron: `0 0 0 * * *`)
- **Logic**: Finds bookings with status `PENDING` and check-in date in the past
- **Action**: Sets status to `CANCELLED`

View job status at: http://localhost:8000/dashboard

## 🧪 Testing

### Run Tests

```bash
mvn test
```

### Test Coverage

```bash
mvn clean test jacoco:report
```

## 📊 Database Schema

### Users Table

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    roles VARCHAR(255)
);
```

### Hotels Table

```sql
CREATE TABLE hotels (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address VARCHAR(500),
    google_map_url VARCHAR(500),
    amenities TEXT,
    seller_id BIGINT,
    FOREIGN KEY (seller_id) REFERENCES users(id)
);
```

### Rooms Table

```sql
CREATE TABLE rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    capacity INT NOT NULL,
    view_type VARCHAR(100),
    has_kitchen BOOLEAN,
    is_available BOOLEAN,
    hotel_id BIGINT,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id)
);
```

### Bookings Table

```sql
CREATE TABLE bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    total_price DECIMAL(10,2),
    status VARCHAR(50),
    booker_id BIGINT,
    room_id BIGINT,
    FOREIGN KEY (booker_id) REFERENCES users(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);
```

## 🔧 Configuration

### Application Properties (application.yml)

```yaml
spring:
  application:
    name: booking-service

  datasource:
    url: jdbc:h2:mem:bookingdb
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true

  data:
    redis:
      host: localhost
      port: 6379

org:
  jobrunr:
    dashboard:
      enabled: true
      port: 8000
```

### JWT Configuration

- **Secret Key**: Configured in `application.yml`
- **Expiration**: 24 hours (configurable)
- **Algorithm**: HS256

## 🐛 Troubleshooting

### Redis Connection Error

```
Error: Could not connect to Redis at localhost:6379
```

**Solution**: Start Redis server

```bash
redis-server
```

### Port Already in Use

```
Error: Port 8080 is already in use
```

**Solution**: Kill the process or change port in `application.yml`

```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

### H2 Database Already Closed

**Solution**: This is a harmless warning during shutdown. To disable:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:bookingdb;DB_CLOSE_ON_EXIT=FALSE
```

## 📝 Best Practices Implemented

1. ✅ **DTO Pattern**: API contracts separated from domain models
2. ✅ **JWT Security**: User identity from token, not parameters
3. ✅ **Role-Based Authorization**: `@PreAuthorize` annotations
4. ✅ **Global Exception Handling**: All exceptions in `@ControllerAdvice`
5. ✅ **Service Layer**: Business logic separated from controllers
6. ✅ **Caching Strategy**: Redis with specific TTLs
7. ✅ **Database Migrations**: Flyway for version control
8. ✅ **AOP**: Cross-cutting concerns (logging, performance)
9. ✅ **Clean Code**: No exception handling in controllers
10. ✅ **API Documentation**: Swagger/OpenAPI integration

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👥 Authors

- Your Name - Initial work

## 🙏 Acknowledgments

- Spring Boot Team
- JobRunr for background job management
- SpringDoc for OpenAPI integration
