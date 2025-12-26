# Docker Setup for Hotel Booking System

This guide helps you set up Redis and PostgreSQL using Docker for the Hotel Booking System.

## Prerequisites

- **Docker Desktop** installed on Windows
- Download from: https://www.docker.com/products/docker-desktop/

---

## Quick Start (Recommended)

### Using Docker Compose

1. **Start all services** (Redis + PostgreSQL):

```bash
docker-compose up -d
```

2. **Verify services are running**:

```bash
docker-compose ps
```

You should see:

```
NAME                IMAGE              STATUS
booking-redis       redis:7-alpine     Up (healthy)
booking-postgres    postgres:15-alpine Up (healthy)
```

3. **Start your Spring Boot application**:

```bash
mvn spring-boot:run
```

4. **Stop services** (when done):

```bash
docker-compose down
```

---

## Manual Setup (Alternative)

### Option 1: Redis Only

If you only need Redis (and have PostgreSQL installed locally):

```bash
# Pull Redis image
docker pull redis:7-alpine

# Run Redis container
docker run -d \
  --name booking-redis \
  -p 6379:6379 \
  redis:7-alpine

# Verify Redis is running
docker ps
```

### Option 2: PostgreSQL Only

If you only need PostgreSQL (and have Redis installed locally):

```bash
# Pull PostgreSQL image
docker pull postgres:15-alpine

# Run PostgreSQL container
docker run -d \
  --name booking-postgres \
  -e POSTGRES_DB=booking \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=12345 \
  -p 5432:5432 \
  postgres:15-alpine

# Verify PostgreSQL is running
docker ps
```

---

## Connection Configuration

Your `application.yml` is already configured to connect to:

### Redis

```yaml
spring:
  data:
    redis:
      host: localhost # Docker container exposes to localhost
      port: 6379 # Default Redis port
```

### PostgreSQL

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/booking
    username: postgres
    password: 12345
```

No configuration changes needed! ✅

---

## Useful Docker Commands

### Check Container Status

```bash
# List running containers
docker ps

# List all containers (including stopped)
docker ps -a

# Check logs
docker logs booking-redis
docker logs booking-postgres
```

### Redis Commands

```bash
# Access Redis CLI
docker exec -it booking-redis redis-cli

# Test Redis connection
docker exec -it booking-redis redis-cli ping
# Should return: PONG

# View all keys (in Redis CLI)
KEYS *

# Exit Redis CLI
exit
```

### PostgreSQL Commands

```bash
# Access PostgreSQL shell
docker exec -it booking-postgres psql -U postgres -d booking

# List tables (in PostgreSQL shell)
\dt

# Exit PostgreSQL shell
\q
```

### Container Management

```bash
# Start containers
docker start booking-redis booking-postgres

# Stop containers
docker stop booking-redis booking-postgres

# Remove containers (when you want to start fresh)
docker rm -f booking-redis booking-postgres

# View container resource usage
docker stats
```

---

## Troubleshooting

### Issue: "Cannot connect to Redis"

**Solution 1:** Check if Redis is running

```bash
docker ps | grep redis
```

**Solution 2:** Test Redis connection

```bash
docker exec -it booking-redis redis-cli ping
```

**Solution 3:** Check Redis logs

```bash
docker logs booking-redis
```

### Issue: "Port 6379 is already in use"

**Solution:** Stop any existing Redis process

```bash
# Windows (cmd as Administrator)
netstat -ano | findstr :6379
taskkill /PID <PID> /F

# Or use a different port in docker-compose.yml
ports:
  - "6380:6379"  # Use 6380 on host instead
```

Then update `application.yml`:

```yaml
spring:
  data:
    redis:
      port: 6380
```

### Issue: "Cannot connect to PostgreSQL"

**Solution 1:** Ensure PostgreSQL container is running

```bash
docker ps | grep postgres
```

**Solution 2:** Check PostgreSQL logs

```bash
docker logs booking-postgres
```

### Issue: "Docker daemon is not running"

**Solution:** Start Docker Desktop application on Windows

---

## Data Persistence

Both Redis and PostgreSQL use Docker volumes for data persistence:

- **Redis data**: Stored in `redis-data` volume
- **PostgreSQL data**: Stored in `postgres-data` volume

Data persists even if you stop/restart containers! To completely remove data:

```bash
# Stop and remove containers
docker-compose down

# Remove volumes (WARNING: This deletes all data!)
docker volume rm booking_redis-data booking_postgres-data
```

---

## Production Recommendations

For production deployment, consider:

1. **Use secrets management** for passwords (not hardcoded)
2. **Enable Redis authentication**:

   ```bash
   docker run -d \
     --name booking-redis \
     -p 6379:6379 \
     redis:7-alpine \
     redis-server --requirepass yourpassword
   ```

   Update `application.yml`:

   ```yaml
   spring:
     data:
       redis:
         password: yourpassword
   ```

3. **Use environment-specific configurations**
4. **Set up Redis persistence** (AOF or RDB)
5. **Configure PostgreSQL backups**
6. **Use Docker networks** for container isolation

---

## Summary

✅ **Recommended approach**: Use `docker-compose up -d` to start both Redis and PostgreSQL  
✅ **Configuration**: No changes needed to `application.yml`  
✅ **Access**: Services are available at `localhost:6379` (Redis) and `localhost:5432` (PostgreSQL)  
✅ **Data**: Automatically persisted in Docker volumes

**Next Steps:**

1. Run `docker-compose up -d`
2. Wait 10 seconds for services to start
3. Run `mvn spring-boot:run`
4. Your application should now connect successfully! 🎉
