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

| Test ID | User Journey | Test Data | Expected Result |
|---|---|---|---|
| TC01 | Open portal | localhost:8081 | Homepage loads successfully |
| TC02 | Open Submit Complaint | None | Complaint form is displayed |
| TC03 | Validate complaint form | Empty mandatory fields | Browser validation prevents invalid submission |
| TC04 | Track invalid complaint | CCP-INVALID-999999 | Invalid complaint does not display complaint details |
| TC05 | Track valid complaint | Valid complaint ID | Complaint details and status timeline are displayed |

### Assertions

The Selenium tests verify:

1. Portal title and homepage content.
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