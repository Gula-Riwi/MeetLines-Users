# CI/CD Setup Guide for MeetLines Users Microservice

This guide explains how to configure GitHub Actions CI/CD for the MeetLines Users microservice.

## Overview

The CI/CD pipeline consists of two workflows:
1. **CI (Continuous Integration)** - Runs on every push and pull request to build, test, and package the application
2. **CD (Continuous Deployment)** - Deploys to VPS on every push to `main` branch

## Prerequisites

- GitHub repository with the MeetLines Users code
- VPS server with Docker and Docker Compose installed
- GitHub Container Registry access (automatically available for GitHub repos)
- Database server (PostgreSQL) accessible from VPS
- Keycloak authentication server

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

### Keycloak OAuth2 Configuration

| Secret Name | Description | Example |
|------------|-------------|---------|
| `KEYCLOAK_ISSUER_URI` | Keycloak issuer URI | `https://auth.gula.crudzaso.com/realms/meetlines` |
| `KEYCLOAK_JWK_SET_URI` | Keycloak JWK set URI | `https://auth.gula.crudzaso.com/realms/meetlines/protocol/openid-connect/certs` |
| `KEYCLOAK_CLIENT_ID` | OAuth2 client ID | `meetlines-backend` |
| `KEYCLOAK_CLIENT_SECRET` | OAuth2 client secret | `your-client-secret` |

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
- Push to `main`, `develop`, or `feature/**` branches
- Pull requests to `main` or `develop`

Steps:
1. Checkout code
2. Set up JDK 17 (Temurin)
3. Cache Maven dependencies
4. Build with Maven
5. Run tests (including Testcontainers integration tests)
6. Generate test reports
7. Package application as JAR
8. Upload JAR artifact
9. Run code coverage

### CD Workflow (`.github/workflows/cd.yml`)

Triggers on:
- Push to `main` branch
- Manual workflow dispatch

Steps:
1. **Build and Push Docker Image**:
   - Build multi-stage Docker image
   - Push to GitHub Container Registry (`ghcr.io`)
   - Tag with `latest` and commit SHA

2. **Deploy to VPS**:
   - SSH into VPS server
   - Create `.env` file with secrets
   - Pull latest Docker image
   - Stop old containers
   - Start new containers with docker-compose
   - Clean up old images
   - Perform health check

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

Create `/etc/nginx/sites-available/meetlines-users`:

```nginx
server {
    listen 80;
    server_name api.meetlines.gula.crudzaso.com;

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

Enable site and obtain SSL certificate:
```bash
sudo ln -s /etc/nginx/sites-available/meetlines-users /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx

# Install SSL certificate with Certbot
sudo certbot --nginx -d api.meetlines.gula.crudzaso.com
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
# On VPS
docker ps
docker logs meetlines-users
docker logs -f meetlines-users  # Follow logs
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
docker-compose down

# Pull specific version
docker pull ghcr.io/gula-riwi/meetlines-users:main-abc1234

# Update docker-compose.yml image tag
# Then restart
docker-compose up -d
```

### Common Issues

**Issue**: Container fails health check
- **Solution**: Check logs with `docker logs meetlines-users`
- Verify database connectivity
- Ensure Keycloak is accessible

**Issue**: Database migration fails
- **Solution**: Check Flyway logs in application output
- Verify database credentials
- Check if migration SQL is valid

**Issue**: 401 Unauthorized errors
- **Solution**: Verify Keycloak configuration
- Check JWT issuer URI and JWK set URI
- Ensure client secret is correct

## Security Best Practices

1. **Never commit secrets to Git** - Use GitHub Secrets for all sensitive data
2. **Rotate credentials regularly** - Update secrets in GitHub and VPS
3. **Use HTTPS only** - Enforce SSL with Certbot/Let's Encrypt
4. **Limit SSH access** - Use SSH keys, disable password authentication
5. **Review dependency vulnerabilities** - GitHub Dependabot alerts enabled

## GitHub Container Registry

Images are automatically published to:
```
ghcr.io/gula-riwi/meetlines-users:latest
ghcr.io/gula-riwi/meetlines-users:main-<commit-sha>
```

To pull manually:
```bash
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin
docker pull ghcr.io/gula-riwi/meetlines-users:latest
```

## Environment-Specific Configuration

### Development
- Uses local PostgreSQL and Keycloak (docker-compose.yaml)
- Hibernate DDL: `create-drop` for tests
- Actuator endpoints exposed without authentication

### Production (VPS)
- Uses remote PostgreSQL (46.224.13.157)
- Flyway manages migrations
- Hibernate DDL: `validate` (schema changes via Flyway only)
- Actuator health endpoint public, other endpoints protected

## Next Steps

1. ✅ Set up all GitHub Secrets
2. ✅ Prepare VPS server (Docker, network, directories)
3. ✅ Configure Nginx reverse proxy and SSL
4. 🔄 Push to main branch to trigger first deployment
5. 🔄 Monitor deployment in GitHub Actions
6. 🔄 Verify application health and functionality
7. 📊 Set up monitoring (Grafana/Prometheus integration)
8. 🔔 Configure deployment notifications (Slack/Discord webhook)

## Support

For issues or questions:
- Check GitHub Actions logs
- Review application logs on VPS
- Verify all secrets are correctly configured
- Ensure VPS has internet access and Docker is running

---

**Last Updated**: December 2025
**Maintained by**: Gula Team
