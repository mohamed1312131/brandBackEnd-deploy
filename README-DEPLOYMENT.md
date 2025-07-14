# Backend Deployment Guide

## Environment Configuration

This application now uses environment variables for all sensitive configuration data. Follow these steps to set up your environment properly.

### 1. Environment Variables Setup

#### For Development:
1. Copy `.env.example` to `.env` in the root directory
2. Fill in your actual values in the `.env` file
3. The application will automatically use the `dev` profile

#### For Production:
1. Set the following environment variables in your deployment platform:

```bash
# Application
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# Database (MongoDB Atlas recommended)
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/hamza_backend_db?retryWrites=true&w=majority

# JWT Security
JWT_SECRET=your-super-secure-jwt-secret-here
JWT_EXPIRATION=86400000

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_PROTOCOL=smtp

# CORS Configuration
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://admin.yourdomain.com

# File Upload
MAX_FILE_SIZE=10MB
MAX_REQUEST_SIZE=50MB
```

### 2. Profile Configuration

The application supports multiple profiles:

- **dev** (default): Development settings with debug logging
- **prod**: Production settings with optimized logging and security

### 3. Security Considerations

#### JWT Secret Generation
Generate a secure JWT secret for production:
```bash
# Using OpenSSL
openssl rand -base64 32

# Using Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

#### Email Configuration
- Use Gmail App Passwords instead of regular passwords
- Enable 2FA on your Gmail account
- Generate an App Password specifically for this application

### 4. Database Setup

#### Development
- Use local MongoDB Community Server instance
- Default connection: `mongodb://localhost:27017/hamza_backend_db`
- Install MongoDB Community Server from: https://www.mongodb.com/try/download/community

#### Production Options

**Option A: MongoDB Atlas (Cloud)**
- Managed MongoDB service
- Connection: `mongodb+srv://username:password@cluster.mongodb.net/hamza_backend_db`
- Automatic backups and scaling

**Option B: Self-Hosted MongoDB Community Server (Your Choice)**
- Install MongoDB Community Server on your production server
- Connection: `mongodb://your-server-ip:27017/hamza_backend_db`
- More control over configuration and costs
- Requires manual setup and maintenance

##### Self-Hosted MongoDB Setup Steps:

1. **Install MongoDB Community Server on your production server:**
   ```bash
   # Ubuntu/Debian
   wget -qO - https://www.mongodb.org/static/pgp/server-7.0.asc | sudo apt-key add -
   echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list
   sudo apt-get update
   sudo apt-get install -y mongodb-org
   
   # CentOS/RHEL
   sudo yum install -y mongodb-org
   ```

2. **Configure MongoDB for production:**
   ```bash
   # Edit MongoDB configuration
   sudo nano /etc/mongod.conf
   
   # Key settings:
   net:
     port: 27017
     bindIp: 0.0.0.0  # Allow external connections
   
   security:
     authorization: enabled  # Enable authentication
   
   storage:
     dbPath: /var/lib/mongodb
   ```

3. **Create database user:**
   ```bash
   # Start MongoDB
   sudo systemctl start mongod
   sudo systemctl enable mongod
   
   # Connect to MongoDB
   mongosh
   
   # Create admin user
   use admin
   db.createUser({
     user: "admin",
     pwd: "your-secure-password",
     roles: ["userAdminAnyDatabase", "dbAdminAnyDatabase", "readWriteAnyDatabase"]
   })
   
   # Create application user
   use hamza_backend_db
   db.createUser({
     user: "hamza_app",
     pwd: "your-app-password",
     roles: ["readWrite"]
   })
   ```

4. **Update your production environment variables:**
   ```bash
   # For authenticated MongoDB
   MONGODB_URI=mongodb://hamza_app:your-app-password@your-server-ip:27017/hamza_backend_db
   
   # For local MongoDB without auth (development only)
   MONGODB_URI=mongodb://localhost:27017/hamza_backend_db
   ```

5. **Configure firewall:**
   ```bash
   # Allow MongoDB port (be careful with security)
   sudo ufw allow 27017
   
   # Or restrict to specific IPs
   sudo ufw allow from your-app-server-ip to any port 27017
   ```

6. **Set up backups:**
   ```bash
   # Create backup script
   #!/bin/bash
   mongodump --host localhost:27017 --db hamza_backend_db --out /backup/$(date +%Y%m%d)
   
   # Add to crontab for daily backups
   crontab -e
   0 2 * * * /path/to/backup-script.sh
   ```

##### Security Considerations for Self-Hosted MongoDB:
- Enable authentication in production
- Use strong passwords
- Restrict network access with firewall rules
- Keep MongoDB updated
- Monitor logs for suspicious activity
- Set up regular backups
- Use SSL/TLS for connections if possible

### 5. Deployment Platforms

#### Heroku
```bash
# Set environment variables
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set MONGODB_URI=your-mongodb-uri
heroku config:set JWT_SECRET=your-jwt-secret
# ... set other variables

# Deploy
git push heroku main
```

#### Railway
```bash
# Set environment variables in Railway dashboard
# Deploy via GitHub integration or CLI
```

#### Docker
```dockerfile
# Example Dockerfile
FROM openjdk:17-jdk-slim
COPY target/hamzaBackEnd-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

### 6. Build Commands

#### Development
```bash
mvn spring-boot:run
```

#### Production Build
```bash
mvn clean package -DskipTests
java -jar target/hamzaBackEnd-0.0.1-SNAPSHOT.jar
```

### 7. Health Checks

The application exposes health endpoints:
- Health: `GET /actuator/health`
- Info: `GET /actuator/info`

### 8. Troubleshooting

#### Common Issues:
1. **MongoDB Connection**: Ensure MONGODB_URI is correct and network access is configured
2. **JWT Errors**: Verify JWT_SECRET is set and properly encoded
3. **Email Issues**: Check Gmail App Password and SMTP settings
4. **CORS Errors**: Verify CORS_ALLOWED_ORIGINS includes your frontend domains

#### Logs:
- Development: Debug level logging enabled
- Production: Info level logging for performance

### 9. Security Checklist

- [ ] JWT_SECRET is secure and unique
- [ ] Database credentials are not hardcoded
- [ ] Email credentials use App Passwords
- [ ] CORS is configured for specific domains only
- [ ] Error messages don't expose sensitive information
- [ ] HTTPS is enabled in production
- [ ] Database access is restricted by IP/network

### 10. Monitoring

Consider adding:
- Application Performance Monitoring (APM)
- Log aggregation service
- Database monitoring
- Uptime monitoring
- Error tracking service

## Next Steps

After completing the backend environment configuration:
1. Set up production database (MongoDB Atlas)
2. Configure frontend environment variables
3. Set up CI/CD pipeline
4. Configure monitoring and logging
5. Set up backup strategies
