
# Task 9: Selenium Test Plan
## Copyright Complaint Portal

### 1. Objective

To automate the testing of critical user journeys of the Copyright Complaint Portal using Selenium WebDriver, JUnit 5 and Maven. The tests verify page navigation, complaint form availability, mandatory field validation and complaint tracking validation.

### 2. Testing Environment

| Component | Details |
|---|---|
| Application | Copyright Complaint Portal |
| Backend | Spring Boot |
| Database | MySQL |
| Testing tool | Selenium WebDriver |
| Browser | Google Chrome |
| Test framework | JUnit 5 |
| Build tool | Maven |
| Application URL | http://localhost:8081 |

### 3. Test Cases

| Test ID | User Journey | Test Scenario | Expected Result |
|---|---|---|---|
| TC-01 | Home Page | Open the application home page | Home page loads successfully and displays expected elements |
| TC-02 | Complaint Submission | Open the complaint form | Complaint form and required fields are displayed |
| TC-03 | Complaint Submission | Verify mandatory fields | Name, email, work title, copyright type, description, infringement details and supporting document are required |
| TC-04 | Complaint Submission | Submit an empty complaint form | Browser form validation prevents submission |
| TC-05 | Complaint Tracking | Open complaint tracking and verify validation | Tracking page loads and required tracking inputs are validated |

### 4. Test Data

The tests use the local application URL and the existing complaint form fields.

| Field | Test Data / Condition |
|---|---|
| Full Name | Empty input for required-field validation |
| Email | Empty input for required-field validation |
| Work Title | Empty input for required-field validation |
| Copyright Type | Default unselected option |
| Description | Empty input |
| Infringement Details | Empty input |
| Supporting Document | No file selected |

### 5. Test Execution Procedure

1. Start MySQL and ensure the application database is accessible.
2. Start the Copyright Complaint Portal on port 8081.
3. Verify application health using the Actuator health endpoint.
4. Open a terminal in the project root directory.
5. Execute `mvn clean test`.
6. Review the Maven Surefire test reports.
7. Verify that failure screenshots are saved when a test fails.

### 6. Assertions and Failure Handling

Selenium assertions verify the visibility of pages, forms and required fields.

JUnit 5 records test failures and errors. The BaseSeleniumTest class uses a TestWatcher to capture browser screenshots when a test fails.

Screenshots are saved in the test-screenshots directory as PNG files.

### 7. Test Reports

Maven Surefire generates text and XML reports in:

`target/surefire-reports`

Failure screenshots are stored in:

`test-screenshots`

### 8. Expected Outcome

All configured Selenium test cases should execute successfully against the running local application. The test report should record the number of tests executed, failures, errors and skipped tests.