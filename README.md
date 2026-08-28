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

## Core MVP Workflow

1. Complainant submits a copyright complaint.
2. Application validates complaint data.
3. Supporting document is validated.
4. Valid documents are stored securely.
5. Complaint receives a unique complaint ID.
6. Reviewer views pending complaints.
7. Reviewer approves or rejects the complaint.
8. Complaint status is updated.
9. Complainant tracks complaint status.

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot |
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