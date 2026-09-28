# TrackViro – Corporate Expense Management System

TrackViro is a full-stack corporate expense management system built to simplify and automate employee expense submission, approval, validation, notification, and reimbursement processes.

The system provides separate workflows for **Employees, Managers, and Finance users** with secure authentication, role-based authorization, REST APIs, business-rule validation, automated notifications, testing, containerization, and cloud deployment.

## Project Overview

Managing corporate expenses manually can involve multiple steps such as submitting bills, checking expense limits, obtaining manager approval, finance verification, and processing reimbursements.

TrackViro provides a centralized digital workflow for these activities.

### Main Workflow

```text
Employee
   │
   │ Submit Expense
   ▼
Manager
   │
   │ Review & Approve / Reject
   ▼
Finance
   │
   │ Verify & Approve / Reject
   ▼
Reimbursement
```

The application also validates expenses against configured business rules and keeps users informed through notifications.

## Key Features

### Employee

- Secure login and authentication
- Submit business expenses
- Upload expense bills
- View submitted expenses
- Track expense status
- View notifications
- Request monthly expense limits
- Manage profile information

### Manager

- View department employee expenses
- Review submitted expenses
- Approve or reject expenses
- Monitor department-level expense activity
- Manage expense approval workflow

### Finance

- Manage users
- Manage departments
- Manage expense categories
- Configure monthly category limits
- Review manager-approved expenses
- Approve or reject expenses
- Process reimbursements
- Monitor overall expense activity

## Authentication & Authorization

TrackViro uses **Spring Security and JWT** for secure authentication and role-based authorization.

### Security Features

- JWT-based authentication
- BCrypt password hashing
- Role-based access control
- Protected REST APIs
- Stateless authentication
- Active/inactive user validation
- Role-specific API access
- Secure authentication flow

### User Roles

```text
EMPLOYEE
   │
   └── Submit and track expenses

MANAGER
   │
   └── Review department expenses

FINANCE
   │
   └── Manage and process organization-wide expenses
```

## Business Rules

TrackViro implements business rules to improve expense processing and reduce invalid submissions.

### Expense Validation

- Monthly expense limit validation
- Duplicate expense detection
- Expense status validation
- Role-based approval workflow
- Department-based manager access
- Active user validation
- Bill file size validation

### Expense Status Flow

```text
PENDING
   │
   ▼
MANAGER_APPROVED
   │
   ▼
FINANCE_APPROVED
   │
   ▼
REIMBURSED
```

An expense can also move to:

```text
REJECTED
```

depending on the approval decision.

## Notifications

The system provides automated notifications for important expense workflow events.

Examples include:

- Expense submission
- Approval
- Rejection
- Status changes

Email notifications are implemented using **Spring Mail / SMTP**.

## System Architecture

```text
                    ┌─────────────────────┐
                    │      React.js       │
                    │      Frontend       │
                    └──────────┬──────────┘
                               │
                             Axios
                               │
                               ▼
                    ┌─────────────────────┐
                    │       Nginx         │
                    │ Reverse Proxy / Web │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │    Spring Boot      │
                    │     REST APIs       │
                    └──────────┬──────────┘
                               │
                ┌──────────────┴──────────────┐
                │                             │
                ▼                             ▼
       ┌─────────────────┐           ┌─────────────────┐
       │ Spring Security │           │  Service Layer  │
       │      + JWT      │           │ Business Logic  │
       └─────────────────┘           └────────┬────────┘
                                              │
                                              ▼
                                    ┌─────────────────┐
                                    │ JPA / Hibernate │
                                    └────────┬────────┘
                                             │
                                             ▼
                                    ┌─────────────────┐
                                    │      MySQL      │
                                    └─────────────────┘
```

## Technology Stack

### Frontend

- React.js
- JavaScript
- HTML5
- CSS3
- Bootstrap
- Axios
- React Router
- Chart.js

### Backend

- Java 17
- Spring Boot
- Spring MVC
- Spring Security
- JWT
- Spring Data JPA
- Hibernate
- Spring Mail

### Database

- MySQL

### API & Documentation

- REST APIs
- Swagger
- OpenAPI

### Testing

- JUnit 5
- Mockito

### DevOps & Deployment

- Docker
- Docker Compose
- Nginx
- AWS EC2
- Ubuntu Linux

### Development Tools

- IntelliJ IDEA
- VS Code
- Spring Tool Suite
- Git
- GitHub

## Project Structure

```text
TrackViro-2.0/
│
├── backend/
│   └── trackviro-backend/
│       ├── src/
│       │   └── main/
│       │       ├── java/
│       │       │   └── com/
│       │       │       └── trackviro/
│       │       └── resources/
│       │
│       ├── pom.xml
│       └── Dockerfile
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   └── ...
│   │
│   ├── package.json
│   ├── Dockerfile
│   └── nginx.conf
│
├── docker-compose.yml
└── README.md
```

## REST API Modules

The backend provides REST APIs for the major application modules.

### Authentication

- Login
- Authentication
- JWT token generation
- Password-related operations

### User Management

- Create users
- Retrieve users
- Update user information
- Activate/deactivate users

### Department Management

- Create departments
- Manage departments
- Assign managers

### Category Management

- Create expense categories
- Configure category limits
- Manage categories

### Expense Management

- Submit expenses
- Retrieve expenses
- Approve expenses
- Reject expenses
- Process reimbursement

### Limit Request Management

- Submit limit requests
- Review limit requests
- Approve/reject requests

### Notification Management

- Create notifications
- Retrieve notifications
- Track user notifications

## API Documentation

Swagger/OpenAPI is integrated into the backend for API documentation and testing.

When running locally, Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

API documentation is generated from the REST controllers.

## Testing

The project includes unit testing using:

- JUnit 5
- Mockito

### Tested Areas

- Expense service
- User service
- Limit request service
- Business logic
- Validation scenarios
- Role-based access control

Service-layer tests are used to verify application business logic without requiring the complete application environment.

## Docker

TrackViro is containerized using Docker and Docker Compose.

The application consists of three primary containers:

```text
┌─────────────────────┐
│   Frontend + Nginx  │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│   Spring Boot API   │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│        MySQL        │
└─────────────────────┘
```

### Start the Application

```bash
docker compose up -d
```

### Stop the Application

```bash
docker compose down
```

### View Containers

```bash
docker compose ps
```

### View Backend Logs

```bash
docker compose logs -f backend
```

### View Frontend Logs

```bash
docker compose logs -f frontend
```

### View MySQL Logs

```bash
docker compose logs -f mysql
```

## AWS Deployment

TrackViro is designed to run in a cloud environment using **AWS EC2**.

### Deployment Stack

```text
AWS EC2
   │
   ├── Ubuntu Linux
   │
   └── Docker Compose
          │
          ├── React + Nginx
          ├── Spring Boot
          └── MySQL
```

The application uses a containerized deployment approach to keep the infrastructure simple and **cost-effective** while providing practical cloud deployment experience.

## Development Workflow

The project follows a layered backend architecture:

```text
React Frontend
      ↓
REST API
      ↓
Controller
      ↓
Service
      ↓
Repository
      ↓
JPA / Hibernate
      ↓
MySQL
```

Security is handled through:

```text
Request
   ↓
JWT Authentication
   ↓
Spring Security
   ↓
Role Authorization
   ↓
Controller
```

## Project Highlights

- Full-stack application using **React.js and Spring Boot**
- RESTful API architecture
- JWT-based authentication
- Three-role authorization system
- Spring Security implementation
- MySQL database integration
- JPA/Hibernate persistence
- Expense approval and reimbursement workflow
- Monthly expense limit validation
- Duplicate expense validation
- Automated email notifications
- Swagger/OpenAPI documentation
- JUnit 5 and Mockito testing
- Docker containerization
- Docker Compose orchestration
- Nginx reverse proxy
- AWS EC2 deployment
- Git/GitHub version control

## Development Practices

The project focuses on:

- Clean layered architecture
- Separation of frontend and backend
- Reusable React components
- REST API design
- Secure authentication
- Role-based authorization
- Business-rule validation
- Unit testing
- Containerized deployment
- Cost-effective infrastructure
- Maintainable and scalable code structure

AI-assisted development tools were used during development for:

- Coding assistance
- Debugging
- Refactoring
- Documentation

## Learning Outcomes

Through this project, the following practical areas were covered:

- Full-stack Java development
- React.js frontend development
- Spring Boot REST API development
- Spring Security and JWT
- Database design and integration
- JPA/Hibernate
- Unit testing
- API documentation
- Docker and Docker Compose
- Nginx configuration
- Linux server management
- AWS EC2 deployment
- Git and GitHub workflow

## Developer

**Vaishnavi Aher**

MCA Graduate | Java / Full-Stack Java Developer

## Project Purpose

TrackViro was developed as a **full-stack portfolio and learning project** to demonstrate practical skills in Java, Spring Boot, React.js, REST APIs, security, database management, testing, Docker, and cloud deployment.

## License

This project is developed for educational purposes.
