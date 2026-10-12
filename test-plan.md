# Selenium Test Plan
## Copyright Complaint Portal

### Objective

To validate the critical user journeys of the Copyright Complaint Portal using Selenium WebDriver and JUnit 5.

### Test Environment

- Application: Copyright Complaint Portal
- Application URL: http://localhost:8081
- Browser: Google Chrome
- Automation Tool: Selenium WebDriver 4.35.0
- Testing Framework: JUnit 5
- Build Tool: Maven
- Database: MySQL 8
- Java: Java 21

### Test Cases

Since v1.1.0 the portal requires login, so tests register their own complainant
accounts and use the seeded reviewer account (`-Dselenium.reviewerEmail`,
`-Dselenium.reviewerPassword`).

| Test ID | Class | User Journey | Expected Result |
|---|---|---|---|
| TC01 | HomePageTest | Open portal | Homepage loads with title, heading and Submit option |
| TC02 | HomePageTest | Visitor navigation | Login and Register links are shown |
| TC03 | AuthenticationTest | Open reviewer page while logged out | Redirected to the login page |
| TC04 | AuthenticationTest | Login with wrong password | Error shown, user stays on login page |
| TC05 | AuthenticationTest | Register and log in | User lands on My Complaints with role COMPLAINANT |
| TC06 | AuthenticationTest | Complainant opens reviewer dashboard | Access Denied (403) page |
| TC07 | AuthenticationTest | Reviewer logs in | Lands on Reviewer Dashboard |
| TC08 | AuthenticationTest | Logout | Session ends; protected pages redirect to login |
| TC09 | SubmitComplaintTest | Open complaint form | Fields required; applicant email read-only from account |
| TC10 | SubmitComplaintTest | Empty complaint form | Browser validation prevents submission |
| TC11 | TrackComplaintTest | Track invalid complaint | Error message displayed |
| TC12 | ComplaintWorkflowTest | Submit -> review -> approve -> track | Other complainant cannot see it; owner sees APPROVED, remarks and history |
| UT01-06 | DocumentValidationServiceTest | Unit tests | Real PDF/PNG accepted; renamed, unsupported, empty and oversized files rejected |

### Assertions

The Selenium tests verify:

1. Portal title and homepage content.
1. Login, logout, registration and role-based access control.
1. End-to-end complaint workflow across complainant and reviewer roles.
2. Submit Complaint page availability.
3. Applicant and copyright sections.
4. Track Complaint page availability.
5. Complaint ID input functionality.
6. Complaint status information.
7. Invalid complaint handling.

### Failure Screenshot Mechanism

A JUnit TestWatcher is used to automatically capture a screenshot whenever a Selenium test fails.

Screenshots are stored in:

test-screenshots/

### Execution

The application is started locally on port 8081.

The Selenium test suite is executed using:

mvn clean test

### Test Reports

Maven Surefire generates local test reports in:

target/surefire-reports/

### Expected Outcome

All critical user journeys should execute successfully with zero test failures.