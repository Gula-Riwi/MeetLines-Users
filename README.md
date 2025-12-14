# MeetLines

A microservices-based meeting room booking platform built with Spring Boot.

## 🚀 Quick Start

### Prerequisites

- **Java 17+** (Java 21 recommended)
- **Maven 3.8+**
- **Docker & Docker Compose**
- **PostgreSQL 15** (via Docker)

### 1. Clone the Repository

```bash
git clone <repository-url>
cd MeetLines
```

### 2. Set Up Environment Variables

Create a `.env` file in the root directory:

```env
POSTGRES_PASSWORD=postgres
```

### 3. Start Infrastructure Services

Start PostgreSQL:

```bash
docker-compose up -d postgres
```

This will start:
- **PostgreSQL** on `localhost:5432`

### 4. Run the Application

#### Option A: Run from the auth module
```bash
cd auth
mvn spring-boot:run
```

#### Option B: Run with dev profile (from root)
```bash
mvn spring-boot:run -pl auth -Dspring-boot.run.profiles=dev
```

The application will start on **http://localhost:8081**

## 🏗️ Project Structure

```
MeetLines/
├── auth/                           # Authentication microservice
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/Gula/MeetLines/
│   │   │   │       ├── auth/       # User authentication & sync
│   │   │   │       ├── booking/    # Booking module (planned)
│   │   │   │       └── config/     # Security configuration
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── application-dev.properties
│   │   │       └── db/migration/   # Flyway migrations
│   │   └── test/
│   └── pom.xml
├── docs/                           # Additional documentation
├── docker-compose.yaml             # Development infrastructure
├── pom.xml                         # Parent POM
└── README.md
```

## 🔐 Authentication

The platform provides authentication and user management capabilities through the Auth service.

### Get a Token

```bash
curl -X POST http://localhost:8080/realms/meetlines/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=meetlines-backend" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "grant_type=password" \
  -d "username=YOUR_USERNAME" \
  -d "password=YOUR_PASSWORD"
```

## 📡 API Endpoints

### Authentication Service (Port 8081)

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| GET | `/api/auth/me` | ✅ | Get current user information |
| GET | `/api/auth/token-info` | ✅ | Get JWT token claims |
| GET | `/api/auth/health` | ❌ | Health check endpoint |

### Example Response

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "fullName": "John Doe",
  "phone": null,
  "authProvider": "local",
  "isEmailVerified": true,
  "createdAt": "2025-12-05T19:41:00"
}
```

## 🛠️ Development

### Building the Project

```bash
# Clean and install all modules
mvn clean install

# Build specific module
mvn clean install -pl auth
```

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl auth

# Run with coverage
mvn test jacoco:report
```

### Database Migrations

Flyway migrations are located in `auth/src/main/resources/db/migration/`

```bash
# Run migrations manually
mvn flyway:migrate -pl auth

# Check migration status
mvn flyway:info -pl auth
```

### Development Profiles

- **default**: Uses Flyway migrations, connects to production-like DB
- **dev**: Disables Flyway, uses Hibernate auto-update, connects to local PostgreSQL

Activate dev profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 🐳 Docker Deployment

### Development with Docker Compose

#### Full Stack Development Environment

Run the entire application stack (PostgreSQL and both microservices) with Docker:

```bash
# Build and start all services
docker compose up -d --build

# View logs
docker compose logs -f

# Stop all services
docker compose down

# Stop and remove volumes (clean slate)
docker compose down -v
```

**Services Running:**
- **PostgreSQL**: `localhost:5432`
- **Auth Service**: `http://localhost:8081`
- **Booking Service**: `http://localhost:8082`

#### Infrastructure Only (Database)

If you want to run the application locally but use Docker for database:

```bash
# Start only PostgreSQL
docker-compose up -d postgres

# Then run the services locally
cd auth
mvn spring-boot:run

# In another terminal for booking service
cd booking
mvn spring-boot:run
```

### Building Docker Images

#### Build Auth Service Image

```bash
# From project root
docker build -t meetlines-auth:latest -f auth/Dockerfile .

# Or from auth directory
cd auth
docker build -t meetlines-auth:latest .
```

#### Build with Specific Tag

```bash
docker build -t meetlines-auth:0.0.1-SNAPSHOT -f auth/Dockerfile .
```

### Running Individual Containers

#### Run Auth Service Container

```bash
# Run with environment variables
docker run -d \
  --name meetlines-auth \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/meetline \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  --network meetlines_network \
  meetlines-auth:latest

# View logs
docker logs -f meetlines-auth

# Stop container
docker stop meetlines-auth
docker rm meetlines-auth
```

### Docker Compose Configuration

The `docker-compose.yaml` includes:

- **postgres**: PostgreSQL 15 database with persistent volume
- **meetlines-auth**: Auth microservice
- **meetlines-booking**: Booking microservice

Environment variables are loaded from `.env` file.

### Environment Variables for Docker

Create a `.env` file with:

```env
# Database
POSTGRES_DB=meetline
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
```

### Docker Compose Commands

```bash
# Start services in background
docker-compose up -d

# Start specific services
docker-compose up -d postgres meetlines-auth meetlines-booking

# Rebuild and start
docker-compose up -d --build

# View service status
docker-compose ps

# View logs for all services
docker-compose logs -f

# View logs for specific service
docker-compose logs -f auth

# Stop services (keep data)
docker-compose stop

# Stop and remove containers (keep data)
docker-compose down

# Stop and remove everything including volumes
docker-compose down -v

# Restart a specific service
docker-compose restart auth

# Execute commands in running container
docker-compose exec auth bash
docker-compose exec postgres psql -U postgres
```

### Production Deployment

```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Docker Health Checks

The Auth service includes a health check endpoint:

```bash
# Check if container is healthy
docker ps

# Manual health check
curl http://localhost:8081/actuator/health
```

### Troubleshooting Docker

#### Container won't start

```bash
# Check container logs
docker-compose logs auth

# Check all containers status
docker-compose ps

# Restart specific service
docker-compose restart auth
```

#### Database connection issues

```bash
# Ensure PostgreSQL is running
docker-compose ps postgres

# Check PostgreSQL logs
docker-compose logs postgres

# Connect to PostgreSQL to verify
docker-compose exec postgres psql -U postgres -c "\l"
```

#### Port conflicts

```bash
# Check what's using the port (Windows)
netstat -ano | findstr :8081

# Stop conflicting service or change port in docker-compose.yaml
```

#### Clean restart

```bash
# Remove all containers, volumes, and images
docker-compose down -v --rmi all

# Rebuild from scratch
docker-compose up -d --build
```

## 📚 Documentation

- [Authentication Guide](AUTH_GUIDE.md) - Detailed authentication and user sync documentation
- [Implementation Summary](IMPLEMENTATION_SUMMARY.md) - What's been implemented
- [CI/CD Setup](CI_CD_SETUP.md) - Continuous integration and deployment
- [Architecture](docs/architecture.md) - System architecture overview

## 🧪 Testing

### Manual Testing with PowerShell

```powershell
# Test user sync
.\test-user-sync.ps1
```

### Integration Tests

The project includes Testcontainers for integration testing:

```bash
mvn verify -pl auth
```

## 🔧 Configuration

### Database Configuration

Edit `auth/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/meetline
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### OAuth2 Configuration

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/meetlines
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/meetlines/protocol/openid-connect/certs
```

## 🐛 Troubleshooting

### Application won't start from root directory

**Problem:** Running `mvn spring-boot:run` from root fails with "Unable to find a suitable main class"

**Solution:** Run from the auth module or specify the module:
```bash
cd auth && mvn spring-boot:run
# or
mvn spring-boot:run -pl auth
```

### Database connection failed

**Problem:** `FATAL: password authentication failed for user "postgres"`

**Solution:** 
1. Ensure PostgreSQL is running: `docker-compose up -d postgres`
2. Check credentials in `application.properties`
3. Try dev profile: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`

### Port already in use

**Problem:** Port 8080 or 8081 already in use

**Solution:**
```bash
# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Or change port in application.properties
server.port=8082
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Team

- **Project:** MeetLines
- **Organization:** Gula
- **Version:** 0.0.1-SNAPSHOT

## 🔗 Links

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

**Note:** This is an educational project developed as part of Riwi's Advanced Track program.
