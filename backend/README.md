# StockPilot AI — Backend API

This is the Spring Boot backend API service for StockPilot AI, built with Java 21, Maven, Spring Security (JWT), and Spring Data JPA (PostgreSQL + Redis).

## Development Setup

### Prerequisites
- **Java 21**
- **Maven**
- **Docker & Docker Desktop** (for PostgreSQL and Redis containers)

### Database & Cache Setup
Before starting the backend, make sure the infrastructure services are running inside Docker:
```bash
# In the project root directory
docker compose up -d
```
*Note: PostgreSQL is mapped to host port `5433` to prevent conflicts with native services.*

### Run the Backend Locally
To run the Spring Boot server (port `8080`):
```bash
# In the backend directory
mvn spring-boot:run
```

### Run Tests
```bash
# In the backend directory
mvn test
```

---

## Authentication API Documentation

All endpoints are prefixed with `/api/auth`.

### 1. Register User
- **Endpoint**: `POST /api/auth/register`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "password123",
    "name": "Jane Doe"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "message": "User registered successfully!"
  }
  ```

### 2. Login User
- **Endpoint**: `POST /api/auth/login`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "refreshToken": "784d8520-252f-488f-a9cb-f187a41982b6",
    "id": 1,
    "email": "user@example.com",
    "name": "Jane Doe"
  }
  ```

### 3. Refresh Access Token
- **Endpoint**: `POST /api/auth/refresh`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "token": "784d8520-252f-488f-a9cb-f187a41982b6"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "784d8520-252f-488f-a9cb-f187a41982b6",
    "tokenType": "Bearer"
  }
  ```

### 4. Logout User (Guarded)
- **Endpoint**: `POST /api/auth/logout`
- **Headers**: `Authorization: Bearer <accessToken>`
- **Success Response (200 OK)**:
  ```json
  {
    "message": "Log out successful!"
  }
  ```

### 5. Get User Profile (Guarded)
- **Endpoint**: `GET /api/auth/me`
- **Headers**: `Authorization: Bearer <accessToken>`
- **Success Response (200 OK)**:
  ```json
  {
    "id": 1,
    "email": "user@example.com",
    "name": "Jane Doe",
    "createdAt": "2026-07-22T10:30:00",
    "updatedAt": "2026-07-22T10:30:00"
  }
  ```
