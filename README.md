# Secure File Sharing System

A secure file sharing web application built with Spring Boot that implements various cybersecurity features.

## Features

- 🔐 Secure user authentication with JWT
- 👥 Role-based access control (Admin/User)
- 🔒 File encryption using AES
- 📝 Security audit logging
- 🔄 Account lockout mechanism
- 📤 Secure file upload/download
- 📊 Activity monitoring

## Tech Stack

- Backend: Spring Boot 3.2.3
- Database: MySQL
- Security: Spring Security, JWT
- Encryption: AES (Java Crypto API)
- Build Tool: Maven

## Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

## Setup

1. Clone the repository:
```bash
git clone https://github.com/yourusername/secure-file-sharing.git
cd secure-file-sharing
```

2. Configure the database:
- Create a MySQL database named `secure_file_db`
- Update `application.properties` with your database credentials

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Authentication
- POST `/api/auth/register` - Register a new user
- POST `/api/auth/login` - Login and get JWT token

### Files
- POST `/api/files/upload` - Upload a file
- GET `/api/files/download/{fileId}` - Download a file
- GET `/api/files` - List user's files
- DELETE `/api/files/{fileId}` - Delete a file

### Audit Logs
- GET `/api/audit/user` - Get user's audit logs
- GET `/api/audit/admin` - Get all audit logs (Admin only)

## Security Features

1. **Password Security**
   - Passwords are hashed using BCrypt
   - Account lockout after 3 failed attempts
   - 15-minute lockout duration

2. **File Security**
   - Files are encrypted using AES before storage
   - Secure file transfer using HTTPS
   - Access control based on user roles

3. **Audit Trail**
   - All security events are logged
   - IP address tracking
   - Timestamp recording
   - User action monitoring

4. **Access Control**
   - Role-based access control (RBAC)
   - JWT-based authentication
   - Secure session management

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Security Considerations

- Change the default JWT secret key in production
- Use HTTPS in production
- Regularly update dependencies
- Monitor audit logs for suspicious activity
- Implement rate limiting for API endpoints
- Regular security audits 