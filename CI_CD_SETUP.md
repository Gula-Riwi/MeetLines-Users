# CI/CD Setup Guide for MeetLines Microservices Platform

This guide explains how to configure GitHub Actions CI/CD for the MeetLines multi-module microservices platform.

## Overview

The MeetLines platform consists of two microservices:
- **Auth Service** (port 8081) - User authentication, synchronization, and management
- **Booking Service** (port 8082) - Appointment booking and meeting room management

The CI/CD pipeline consists of two workflows:
1. **CI (Continuous Integration)** - Runs on every push and pull request to build, test, and package both services
2. **CD (Continuous Deployment)** - Builds Docker images and deploys both services to VPS on every push to `main` branch

## Prerequisites

- GitHub repository with the MeetLines multi-module project
- VPS server with Docker and Docker Compose installed (Docker Compose V2 recommended)
- GitHub Container Registry access (automatically available for GitHub repos)
- Remote PostgreSQL database server accessible from VPS
- Java 17+ and Maven 3.8+ for local development
- Spring Boot 3.5.8

## Required GitHub Secrets

Navigate to your repository **Settings → Secrets and variables → Actions** and add the following secrets:

### Database Configuration

| Secret Name | Description | Example |
|------------|-------------|---------|
| `DB_HOST` | PostgreSQL server hostname or IP | `46.224.13.157` |
| `DB_PORT` | PostgreSQL server port | `5432` |
| `DB_NAME` | Database name | `meetline` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `your-secure-password` |


### VPS Deployment Configuration

| Secret Name | Description | Example |
|------------|-------------|---------|
| `VPS_HOST` | VPS server hostname or IP | `46.224.13.157` or `vps.gula.crudzaso.com` |
| `VPS_USERNAME` | SSH username | `root` |
| `VPS_SSH_KEY` | SSH private key for authentication | (Your private key content) |
| `VPS_PORT` | SSH port (optional) | `22` (default) |

## How to Generate SSH Key for VPS Deployment

1. **On your local machine**, generate a new SSH key pair:
   ```bash
   ssh-keygen -t ed25519 -C "github-actions-deploy" -f github-actions-key
   ```

2. **Copy the public key to your VPS**:
   ```bash
   ssh-copy-id -i github-actions-key.pub root@your-vps-host
   ```

3. **Add the private key to GitHub Secrets**:
   - Copy the content of `github-actions-key` (private key)
   - Paste it into the `VPS_SSH_KEY` secret in GitHub

4. **Test the connection**:
   ```bash
   ssh -i github-actions-key root@your-vps-host
   ```

## Workflow Details

### CI Workflow (`.github/workflows/ci.yml`)

Triggers on:
- Push to `main`, `develop`, `docs/**`, `feature/**`, or `refactor/**` branches
- Pull requests to `main` or `develop`

**Matrix Strategy**: Runs parallel builds for both `auth` and `booking` modules

Steps for each module:
1. Checkout code
2. Set up JDK 17 (Temurin)
3. Cache Maven dependencies
4. Make mvnw executable
5. Build parent POM and module with dependencies (`-am` flag)
6. Run module tests
7. Generate test reports (JUnit format)
8. Package module as JAR
9. Upload JAR artifact (7-day retention)
10. Run code coverage analysis
11. Upload coverage to Codecov (optional)

**Security Scan Job**:
- Runs Trivy vulnerability scanner on filesystem
- Reports CRITICAL and HIGH severity issues
- Uploads results to GitHub Security tab

### CD Workflow (`.github/workflows/cd.yml`)

Triggers on:
- Push to `main` branch
- Manual workflow dispatch (with optional per-service deployment control)

**Matrix Strategy**: Builds and pushes Docker images for both `auth` and `booking` modules

Jobs:

**1. Build and Push Docker Images**:
   - Builds multi-stage Docker images for both services
   - Pushes to GitHub Container Registry (`ghcr.io`)
   - Tags: `latest`, `main-<commit-sha>`, `<branch>`
   - Uses Docker Buildx for efficient caching
   - Build args: BUILD_DATE, VCS_REF, VERSION
   - Tag with `latest` and commit SHA

**2. Deploy to VPS**:
   - SSH into VPS server
   - Create `.env` file with all required secrets
   - Download latest `docker-compose.prod.yml`
   - Login to GitHub Container Registry
   - Pull latest Docker images for both services
   - Stop old containers with `docker compose down`
   - Start new containers with `docker compose up -d`
   - Wait for services to initialize (20 seconds)
   - Display running containers
   - Clean up old images (older than 24h)
   
**3. Health Checks**:
   - Auth Service: HTTP check on port 8081 (10 retries)
   - Booking Service: HTTP check on port 8082 (10 retries)
   - Deployment summary with service URLs and commit info

## VPS Server Preparation

### 1. Install Docker and Docker Compose

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose plugin
sudo apt install docker-compose-plugin -y

# Verify installation
docker --version
docker compose version
```

### 2. Create Docker Network

```bash
docker network create meetlines-agent
```

### 3. Create Project Directory

```bash
mkdir -p /root/projects/meetlines-users
cd /root/projects/meetlines-users
```

### 4. Configure Nginx Reverse Proxy (Optional)

#### Auth Service
Create `/etc/nginx/sites-available/meetlines-auth`:

```nginx
server {
    listen 80;
    server_name auth.meetlines.gula.crudzaso.com;

    location / {
        proxy_pass http://localhost:8081;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }

    location /actuator/health {
        proxy_pass http://localhost:8081/actuator/health;
        access_log off;
    }
}
```

#### Booking Service
Create `/etc/nginx/sites-available/meetlines-booking`:

```nginx
server {
    listen 80;
    server_name booking.meetlines.gula.crudzaso.com;

    location / {
        proxy_pass http://localhost:8082;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }

    location /actuator/health {
        proxy_pass http://localhost:8082/actuator/health;
        access_log off;
    }
}
```

Enable sites and obtain SSL certificates:
```bash
# Enable Auth service
sudo ln -s /etc/nginx/sites-available/meetlines-auth /etc/nginx/sites-enabled/

# Enable Booking service
sudo ln -s /etc/nginx/sites-available/meetlines-booking /etc/nginx/sites-enabled/

# Test and reload
sudo nginx -t
sudo systemctl reload nginx

# Install SSL certificates with Certbot
sudo certbot --nginx -d auth.meetlines.gula.crudzaso.com
sudo certbot --nginx -d booking.meetlines.gula.crudzaso.com
```

## Deployment Process

### Automatic Deployment

1. **Push to main branch**:
   ```bash
   git push origin main
   ```

2. **Monitor deployment**:
   - Go to GitHub repository → Actions tab
   - Watch the CI and CD workflows execute
   - Check for any errors

3. **Verify deployment**:
   - Check health endpoint: `https://api.meetlines.gula.crudzaso.com/actuator/health`
   - Monitor application logs on VPS: `docker logs -f meetlines-users`

### Manual Deployment

Trigger deployment manually from GitHub Actions tab:
1. Go to **Actions** → **CD - Deploy to VPS**
2. Click **Run workflow**
3. Select branch and confirm

## Database Migrations

The application uses Flyway for database migrations:

- Migrations are located in `src/main/resources/db/migration/`
- First migration: `V1__initial_schema.sql` (from existing `schema.sql`)
- Flyway runs automatically on application startup
- Migrations are tracked in `flyway_schema_history` table

### Creating New Migrations

1. Create a new file: `V2__description_of_change.sql`
2. Add SQL statements
3. Commit and push to trigger deployment
4. Flyway will automatically apply new migration

Example:
```sql
-- V2__add_user_timezone.sql
ALTER TABLE app_users ADD COLUMN timezone VARCHAR(50) DEFAULT 'UTC';
CREATE INDEX idx_users_timezone ON app_users(timezone);
```

## Monitoring and Troubleshooting

### Check Application Status

```bash
# On VPS - Check all containers
docker ps

# Auth Service logs
docker logs meetlines-auth
docker logs -f meetlines-auth  # Follow logs

# Booking Service logs
docker logs meetlines-booking
docker logs -f meetlines-booking  # Follow logs

# View both services simultaneously
docker compose logs -f

# Check specific service health
curl http://localhost:8081/actuator/health  # Auth
curl http://localhost:8082/actuator/health  # Booking
```

### Check Database Migrations

```bash
# Connect to PostgreSQL
psql -h 46.224.13.157 -U postgres -d meetline

# Check migration history
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

### Rollback Deployment

```bash
# On VPS
cd /root/projects/meetlines-users

# Stop current version
docker compose down

# Pull specific version for both services
docker pull ghcr.io/gula-riwi/meetlines-users-auth:main-abc1234
docker pull ghcr.io/gula-riwi/meetlines-users-booking:main-abc1234

# Update docker-compose.yml image tags to specific version
# Edit the file and change :latest to :main-abc1234

# Restart with specific version
docker compose up -d

# Verify rollback
docker ps
docker logs meetlines-auth
docker logs meetlines-booking
```

### Common Issues

**Issue**: Container fails health check
- **Solution**: Check logs with `docker logs meetlines-auth` or `docker logs meetlines-booking`
- Verify database connectivity from container: `docker exec meetlines-auth curl localhost:8081/actuator/health`
- Check if services are on the correct network: `docker network inspect meetlines-network`
- Ensure PostgreSQL is running and accessible

**Issue**: Database migration fails (Auth service)
- **Solution**: Check Flyway logs in Auth service output: `docker logs meetlines-auth | grep Flyway`
- Verify database credentials in .env file
- Check if migration SQL is valid
- Inspect migration history: `SELECT * FROM flyway_schema_history;`

**Issue**: Authentication/Authorization errors
- **Solution**: Verify security configuration in both services
- Check authentication endpoints are properly configured
- Review application logs for security-related errors
- Test endpoints: `curl http://localhost:8081/api/auth/me`

**Issue**: Services can't communicate with each other
- **Solution**: Ensure both services are on the `meetlines-network`
- Check inter-service network connectivity
- Verify service names resolve correctly: `docker exec meetlines-auth ping meetlines-booking`

**Issue**: Out of memory errors
- **Solution**: Review JVM settings (JAVA_OPTS in docker-compose.prod.yml)
- Monitor container memory: `docker stats`
- Adjust heap size if needed (default: 768MB max)

## Security Best Practices

1. **Never commit secrets to Git** - Use GitHub Secrets for all sensitive data
2. **Rotate credentials regularly** - Update secrets in GitHub and VPS
3. **Use HTTPS only** - Enforce SSL with Certbot/Let's Encrypt
4. **Limit SSH access** - Use SSH keys, disable password authentication
5. **Review dependency vulnerabilities** - GitHub Dependabot alerts enabled

## GitHub Container Registry

Images are automatically published to:
```
# Auth Service
ghcr.io/gula-riwi/meetlines-users-auth:latest
ghcr.io/gula-riwi/meetlines-users-auth:main-<commit-sha>

# Booking Service
ghcr.io/gula-riwi/meetlines-users-booking:latest
ghcr.io/gula-riwi/meetlines-users-booking:main-<commit-sha>
```

To pull manually:
```bash
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin
docker pull ghcr.io/gula-riwi/meetlines-users-auth:latest
docker pull ghcr.io/gula-riwi/meetlines-users-booking:latest
```

## Environment-Specific Configuration

### Development (docker-compose.yaml)
- Uses local PostgreSQL container
- Hibernate DDL: `create-drop` for tests, `update` for dev profile
- Actuator endpoints exposed without authentication
- Both services connect to local PostgreSQL container
- Services run on ports 8081 (auth) and 8082 (booking)
- Local database on port 5432

### Production (VPS - docker-compose.prod.yml)
- Uses remote PostgreSQL database only
- Both services deployed as separate containers
- Flyway manages database migrations (auth service)
- Hibernate DDL: `validate` (schema changes via Flyway only)
- Actuator health endpoint public, other endpoints protected
- Enhanced JVM settings: G1GC, 768MB max heap, optimized GC pauses
- Health checks with 60-second startup grace period
- Services connected via `meetlines-network` bridge network

## Project Structure

```
MeetLines/
├── .github/
│   └── workflows/
│       ├── ci.yml              # Continuous Integration
│       └── cd.yml              # Continuous Deployment
├── auth/                        # Auth microservice module
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── booking/                     # Booking microservice module
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── docker-compose.yaml          # Local development
├── docker-compose.prod.yml      # Production deployment
├── pom.xml                      # Parent Maven POM
└── CI_CD_SETUP.md              # This file
```

## Next Steps

1. ✅ Set up all GitHub Secrets (see Required GitHub Secrets section)
2. ✅ Prepare VPS server (Docker, network, directories)
3. ✅ Configure Nginx reverse proxy and SSL for both services
4. 🔄 Create Docker network: `docker network create meetlines-network`
5. 🔄 Push to main branch to trigger first deployment
6. 🔄 Monitor deployment in GitHub Actions
7. 🔄 Verify both services health and functionality
8. 📊 Set up monitoring (Grafana/Prometheus integration)
9. 🔔 Configure deployment notifications (Slack/Discord webhook)
10. 📝 Document API endpoints and integration patterns

## Maven Multi-Module Project

The project uses Maven multi-module architecture:

```xml
<!-- Parent POM -->
<modules>
    <module>auth</module>
    <module>booking</module>
</modules>
```

### Building Individual Modules

```bash
# Build only auth module (with dependencies)
./mvnw clean install -pl auth -am

# Build only booking module (with dependencies)
./mvnw clean install -pl booking -am

# Build all modules
./mvnw clean install

# Run specific module
./mvnw spring-boot:run -pl auth
./mvnw spring-boot:run -pl booking
```

### Module Dependencies

- **Parent POM**: Manages shared dependencies (Spring Boot 3.5.8, Testcontainers, etc.)
- **auth**: Independent module with Flyway migrations
- **booking**: Independent module, shares database with auth

## Support

For issues or questions:
- Check GitHub Actions logs for CI/CD failures
- Review application logs on VPS: `docker compose logs -f`
- Verify all secrets are correctly configured in GitHub
- Ensure VPS has internet access and Docker is running
- Check service health endpoints: `/actuator/health`
- Inspect Docker network: `docker network inspect meetlines-network`

---

## Key Features

✅ **Multi-Module Maven Project** - Parallel builds for auth and booking services  
✅ **Matrix Strategy CI/CD** - Efficient parallel execution  
✅ **Security Scanning** - Trivy integration for vulnerability detection  
✅ **Docker Multi-Stage Builds** - Optimized image sizes  
✅ **Health Checks** - Automated service verification  
✅ **Rollback Support** - Tagged images for version control  
✅ **Separate Deployments** - Independent service scaling  
✅ **Production-Ready** - G1GC, optimized heap, network isolation  

---

**Last Updated**: December 13, 2025  
**Spring Boot Version**: 3.5.8  
**Java Version**: 17  
**Maintained by**: Gula Team
