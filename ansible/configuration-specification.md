# Copyright Complaint Portal
## Configuration Management Specification

### 1. Application Details
- Application: Copyright Complaint Portal
- Framework: Spring Boot
- Java version: 21
- Build tool: Maven
- Database: MySQL
- Container runtime: Docker
- Configuration management: Ansible

### 2. Target Environment
- Operating system: Ubuntu Linux 24.04 LTS
- Target node: Clean Ubuntu-based Docker container
- Deployment method: Docker container running the application image
- Application port: 8081

### 3. Server Prerequisites

| Category | Requirement | Purpose |
|---|---|---|
| Package | Python 3 | Ansible module support |
| Package | OpenSSH server | Remote Ansible connectivity |
| Package | Docker Engine / Docker CLI | Container deployment |
| Package | curl | Health-check verification |
| Package | ca-certificates | Secure HTTPS connections |
| Package | Java 21 | Application runtime, if run outside Docker |
| Package | Maven | Application build, if built on the target |
| User | ansible | Configuration management user |
| User | appuser | Application service user, if run directly |
| Folder | /opt/copyright-portal | Application deployment directory |
| Folder | /opt/copyright-portal/releases | Release and rollback artifacts |
| Folder | /var/log/copyright-portal | Application logs |
| Folder | /etc/copyright-portal | Application configuration |
| File | /etc/copyright-portal/application.env | Runtime environment variables |
| File | Dockerfile | Application image build instructions |
| File | docker-compose.yml | Optional container orchestration |
| Port | 8081/TCP | Spring Boot application |
| Port | 22/TCP | SSH for Ansible |
| Port | 3306/TCP | MySQL database connectivity |
| Service | Docker | Runs application containers |
| Service | SSH | Remote configuration management |
| Service | Copyright Portal container | Runs the application |

### 4. Application Configuration
- Application port: 8081
- Database: copyright_portal
- Database connection: MySQL
- Database credentials must be supplied through protected variables.
- Secrets must not be committed to version control.

### 5. Reliability Requirements
- Configuration must be repeatable and idempotent.
- The application health endpoint must return HTTP 200.
- The application must be recoverable using a previously stable image.
- Existing Task 11 and Task 12 containers must not be removed by configuration automation.

### 6. Validation
- Verify packages, users, directories, files, ports and services.
- Execute the Ansible playbook and save its execution log.
- Run the playbook a second time to demonstrate idempotency.
- Verify application health and demonstrate rollback/recovery.
