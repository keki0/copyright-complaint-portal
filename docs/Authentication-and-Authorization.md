# Authentication, Authorization and Feature Enhancements

Release: `v1.1.0` (branch `feature/authentication-authorization`)

This document lists what changed compared with the `v1.0.0` MVP, how to run
the new version, and how the security model works.

---

## 1. Summary of changes

| Area | Before (v1.0.0) | After (v1.1.0) |
|---|---|---|
| Login | None - every page was public | Spring Security form login (`/login`), logout, 30-minute session timeout |
| Accounts | Complainants auto-created with password `TEMPORARY` | Self-registration (`/register`), BCrypt-hashed passwords, password policy, change-password page |
| Roles | Enum existed but was never enforced | COMPLAINANT, REVIEWER and ADMIN enforced on URLs, methods and individual records |
| Reviewer identity | Every decision saved as a hard-coded "Portal Reviewer" | The logged-in reviewer is recorded |
| Complaint ownership | Name/email typed into the form - anyone could file as anyone | Taken from the logged-in account |
| Tracking | Anyone with an ID could view any complaint | Complainants see only their own; staff see all |
| Documents | Stored but no one could view them | Secure download for the owner, reviewers and admins |
| File validation | Extension and size only | Extension, size **and file signature** (renamed `.exe` → `.pdf` is rejected) |
| Review flow | Opening a complaint silently changed its status | Explicit "Start Review" (claims and assigns), then decision |
| Reviewer dashboard | Only `SUBMITTED` complaints; opened ones disappeared (bug) | Filters (Pending, Assigned to me, each status, All), search, counters |
| Audit trail | None | `complaint_status_history` table, shown on tracking and review pages |
| Administration | None | `/admin/users`: create staff accounts, change roles, enable/disable users |
| Complainant dashboard | None | `/complaint/my` with counts per status |
| Errors | Whitelabel / 500 pages | Friendly 403, 404 and error pages; validation messages on forms |
| CSRF | Not applicable | Enabled on every form (token added by Thymeleaf `th:action`) |
| Tests | 4 Selenium tests, no login | 12 Selenium tests incl. login, role checks and full end-to-end journey + 6 unit tests |

---

## 2. Role model

| Capability | Visitor | Complainant | Reviewer | Admin |
|---|:-:|:-:|:-:|:-:|
| Home page, login, register | ✔ | ✔ | ✔ | ✔ |
| Submit complaint, "My Complaints" | | ✔ | | |
| Track a complaint | | own only | any | any |
| Download supporting document | | own only | any | any |
| Reviewer dashboard, start review | | | ✔ | ✔ |
| Record decision | | | if assigned to them | any (override) |
| Manage users and roles | | | | ✔ |
| Change own password | | ✔ | ✔ | ✔ |

Authorization is enforced in three layers (defence in depth):

1. **URL rules** - `SecurityConfig` (`/admin/**` → ADMIN, `/reviewer/**` → REVIEWER/ADMIN, ...).
2. **Method security** - `@PreAuthorize` on `ReviewerController` and `AdminController`.
3. **Object-level checks** in services - complaint ownership, reviewer assignment,
   "an admin cannot disable or demote themselves". Another user's complaint is
   reported as *not found* (not *forbidden*) so complaint IDs cannot be probed.

---

## 3. New and changed files

**New - security and configuration**
- `config/SecurityConfig.java` - filter chain, URL rules, form login, logout, BCrypt
- `config/DataInitializer.java` - seeds the first ADMIN and REVIEWER from environment variables
- `security/PortalUserDetailsService.java` - loads users from MySQL for Spring Security
- `security/CurrentUserService.java` - returns the logged-in `User` entity
- `security/RoleBasedAuthenticationSuccessHandler.java` - role-based landing page after login

**New - features**
- `model/ComplaintStatusHistory.java`, `repository/ComplaintStatusHistoryRepository.java`, `service/ComplaintHistoryService.java`
- `service/UserService.java`
- `controller/AuthController.java`, `AccountController.java`, `AdminController.java`, `DocumentController.java`, `ErrorPageController.java`, `GlobalControllerAdvice.java`
- `dto/RegistrationRequest.java`, `PasswordChangeRequest.java`, `CreateUserRequest.java`, `PasswordRules.java`
- `exception/ResourceNotFoundException.java`
- Templates: `fragments/layout.html`, `login.html`, `register.html`, `my-complaints.html`, `admin-users.html`, `account.html`, `access-denied.html`, `error.html`

**Changed**
- `pom.xml` - `spring-boot-starter-security`
- `model/User.java` (`disabled` flag), `model/Complaint.java` (`assignedReviewer`)
- `ComplaintService`, `ReviewService`, `DocumentValidationService`, all existing controllers and templates
- `application.properties` - seed accounts, session settings, upload limits
- `Jenkinsfile` - passes seed-account credentials to the test instance, Selenium and the container
- Selenium tests (all now log in) + `AuthenticationTest`, `ComplaintWorkflowTest`, `DocumentValidationServiceTest`

---

## 4. Database migration

No manual SQL is needed. With `spring.jpa.hibernate.ddl-auto=update`, Hibernate adds on first start:

- `users.disabled` (existing users get `0` = active)
- `complaints.assigned_reviewer_id`
- table `complaint_status_history`

Existing data keeps working:

- The old `reviewer@copyrightportal.com` account (password `TEMPORARY`) is given the
  configured reviewer password on start-up.
- Complainants created by the MVP can **claim** their account by registering with the
  same email; their earlier complaints then appear under *My Complaints*.
- Complaints left in `UNDER_REVIEW` with no reviewer can be claimed with *Start Review*.

---

## 5. Running locally

```bash
git checkout -b feature/authentication-authorization
mvn clean package -DskipTests        # downloads spring-boot-starter-security
java -jar target/copyright-complaint-portal-0.0.1-SNAPSHOT.jar
```

Open http://localhost:8081 and log in with the development accounts:

| Role | Email | Password (default) |
|---|---|---|
| Admin | admin@copyrightportal.com | `Admin@12345` |
| Reviewer | reviewer@copyrightportal.com | `Reviewer@12345` |
| Complainant | register at `/register` | your choice |

Override the defaults with environment variables before any shared deployment:
`APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD`, `APP_REVIEWER_EMAIL`, `APP_REVIEWER_PASSWORD`.
Accounts are created only if they do not exist yet; later changes to these variables
do not overwrite an existing password (use *My Account* to change it).

---

## 6. Jenkins

Create two **Username with password** credentials (Manage Jenkins → Credentials):

| ID | Username | Password |
|---|---|---|
| `portal-admin-credentials` | admin email | admin password |
| `portal-reviewer-credentials` | reviewer email | reviewer password |

The pipeline passes them to the test instance (so it seeds those accounts), to the Selenium
run (`-Dselenium.reviewerEmail/-Dselenium.reviewerPassword`) and to the Docker container.
Avoid `& ^ % ! < > |` in these passwords because they are used inside Windows batch steps.

Selenium tests register throw-away complainants named `selenium.<random>@test.local`
in the database used by the test instance.

---

## 7. Docker and Ansible

`.env.docker` (git-ignored, see `.env.docker.example`) now also contains the four
`APP_*` variables, so `ansible-playbook ansible/deploy.yml` starts the container with
the right accounts. After Jenkins pushes the new image, update `image_name`
in `ansible/deploy.yml` and `docker_image` in `ansible/group_vars/all.yml` to the new
`build-N` tag. Remove the old container first (`docker rm -f ccp-provisioned-container`)
because the playbook only creates the container when it is missing.

---

## 8. Running the tests

```bash
# against a running instance on 8081
mvn test -Dselenium.headless=true

# other options
-Dselenium.baseUrl=http://localhost:8082
-Dselenium.reviewerEmail=... -Dselenium.reviewerPassword=...
-Dselenium.chromeBinary=/path/to/chrome   # optional; Windows default is auto-detected
```

| Test class | What it verifies |
|---|---|
| `HomePageTest` | Public home page, Login/Register links for visitors |
| `AuthenticationTest` | Redirect to login, invalid credentials, registration, role badge, complainant blocked from reviewer pages (403), reviewer landing page, logout |
| `SubmitComplaintTest` | Form fields required, applicant details read-only from the account, empty form invalid |
| `TrackComplaintTest` | Track page, unknown ID shows an error |
| `ComplaintWorkflowTest` | Register → submit with PDF → other complainant cannot see it → reviewer starts review and approves → complainant sees APPROVED, remarks and 3 history entries |
| `DocumentValidationServiceTest` (unit) | Real PDF/PNG accepted; renamed executable, unsupported type, empty and >5 MB files rejected |
