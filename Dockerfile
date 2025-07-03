# ---------- Stage 1: Build the application ----------
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project (except files excluded by .dockerignore)
COPY . .

# Build the application using Maven
RUN mvn clean package -DskipTests

# ---------- Stage 2: Run the application ----------
FROM eclipse-temurin:17-jre-alpine

# App will run on this port
EXPOSE 8081

# Create app directory
ENV APP_HOME=/usr/src/app
WORKDIR $APP_HOME

# Copy only the final .jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Command to run the app
CMD ["java", "-jar", "app.jar"]
