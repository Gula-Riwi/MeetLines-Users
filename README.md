# MeetLines Users Service Setup

## Environment Configuration

This project uses environment variables to manage sensitive configuration like database passwords. Follow these steps to set up your development environment:

### 1. Create Environment File

Copy the example environment file to create your own `.env` file:

```bash
cp .env.example .env
```

### 2. Configure Passwords

Edit the `.env` file and replace the placeholder values with secure passwords:

```env
# Database Configuration
POSTGRES_DB=postgres
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password_here  # Replace with a strong password

# Keycloak Configuration
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=your_secure_admin_password_here  # Replace with a strong password
```

**Important:** Never commit the `.env` file to version control. It is already included in `.gitignore`.

### 3. Start Services

Once your `.env` file is configured, start the services with Docker Compose:

```bash
docker compose up -d
```

## Security Notes

- The `.env` file contains sensitive credentials and is excluded from version control
- Always use strong, unique passwords for production environments
- The compose.yaml file uses environment variable substitution with required validation
- If required environment variables are missing, Docker Compose will display an error

## Service Access

After starting the services:
- **Keycloak Admin Console**: http://localhost:8080
- **PostgreSQL Database**: localhost:5432

Use the credentials defined in your `.env` file to access these services.
