# Copyright Complaint Portal

A DevOps-enabled web application for submitting, validating, reviewing,
approving/rejecting, and tracking copyright complaints.

## Project Overview

The Copyright Complaint Portal provides a structured workflow for managing
copyright complaints from initial submission through reviewer decision and
status tracking.

The project is developed as a 15-week DevOps implementation covering:

- Agile planning
- Git and GitHub collaboration
- Jenkins CI/CD
- Automated testing with Selenium
- Docker containerization
- Configuration management
- Automated provisioning
- Deployment and rollback

## Roles and Security (v1.1.0)

| Role | Main pages |
|---|---|
| Complainant | `/register`, `/complaint/my`, `/complaint/new`, `/complaint/track` |
| Reviewer | `/reviewer/dashboard`, `/reviewer/complaint/{id}` |
| Admin | `/admin/users` + everything a reviewer can do |

Spring Security form login, BCrypt password hashing, role-based URL and method
security, ownership checks on complaints and documents, CSRF protection on every
form. Development accounts: `admin@copyrightportal.com / Admin@12345` and
`reviewer@copyrightportal.com / Reviewer@12345` (override with `APP_ADMIN_*` /
`APP_REVIEWER_*` environment variables).

Full details: [docs/Authentication-and-Authorization.md](docs/Authentication-and-Authorization.md)

## Core Workflow

1. Complainant registers and logs in.
2. Complainant submits a copyright complaint.
3. Application validates complaint data.
4. Supporting document is validated (type, size and file signature).
5. Valid documents are stored securely.
6. Complaint receives a unique complaint ID.
7. Reviewer views pending complaints and starts a review (complaint is assigned).
8. Assigned reviewer approves or rejects the complaint with remarks.
9. Every status change is recorded in the audit trail.
10. Complainant tracks complaint status from "My Complaints".

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot |
| Security | Spring Security (form login, BCrypt, RBAC, CSRF) |
| Build Tool | Maven |
| Database | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| Frontend | Thymeleaf + Bootstrap |
| Application Server | Spring Boot Embedded Tomcat |
| CI/CD | Jenkins |
| Testing | Selenium WebDriver |
| Containerization | Docker |
| Configuration Management | Ansible/Puppet |

## Local Application

Application URL:

http://localhost:8081

Jenkins:

http://localhost:8080

MySQL:

localhost:3306

## Project Structure

```text
copyright-complaint-portal/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ccp/portal/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       ├── templates/
│   │       └── application.properties
│   │
│   └── test/
│
├── uploads/
├── pom.xml
├── README.md
└── .gitignore

## Git Branching Strategy

| Branch | Purpose |
|---|---|
| `main` | Stable, release-ready code |
| `develop` | Integration branch for ongoing development |
| `feature/<name>` | New functionality |
| `bugfix/<name>` | Non-critical defect correction |
| `hotfix/<name>` | Urgent production fix |
| `release/<version>` | Release preparation |

### Branch Naming Examples

```text
feature/complaint-submission
feature/document-validation
feature/reviewer-workflow
feature/status-tracking

bugfix/duplicate-tracking-mapping

hotfix/database-connection

release/v1.0.0

## Project Backlog

### Completed

- [x] Complaint request submission
- [x] Document and data validation
- [x] Document storage
- [x] Reviewer action
- [x] Complaint approval/rejection
- [x] Complaint status tracking
- [x] Git feature branching
- [x] Pull Request and code review
- [x] Merge conflict creation and resolution
- [x] Release-ready MVP baseline

- [x] Jenkins Continuous Integration and Pipeline as Code
- [x] Selenium continuous testing
- [x] Docker image and Jenkins-Docker deployment
- [x] Ansible configuration management and provisioning
- [x] Authentication and role-based authorization (v1.1.0)
- [x] Complainant dashboard, reviewer assignment, status audit trail, admin user management (v1.1.0)

### Next

- [ ] E-mail notifications on status change
- [ ] Docker Compose for application + MySQL
- [ ] Object storage for uploaded documents