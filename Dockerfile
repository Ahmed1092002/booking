# Build Stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Install Redis
RUN apt-get update && apt-get install -y redis-server && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar
COPY start.sh .

# Make script executable
RUN chmod +x start.sh

EXPOSE 8080
ENTRYPOINT ["./start.sh"]
