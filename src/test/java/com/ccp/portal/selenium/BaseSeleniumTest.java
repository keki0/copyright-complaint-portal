package com.ccp.portal.selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.UUID;

public class BaseSeleniumTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    protected static final String BASE_URL =
            System.getProperty("selenium.baseUrl", "http://localhost:8081");

    /** Seeded reviewer account (APP_REVIEWER_EMAIL / APP_REVIEWER_PASSWORD on the server). */
    protected static final String REVIEWER_EMAIL =
            System.getProperty("selenium.reviewerEmail", "reviewer@copyrightportal.com");

    protected static final String REVIEWER_PASSWORD =
            System.getProperty("selenium.reviewerPassword", "Reviewer@12345");

    /** Password used for complainant accounts the tests register themselves. */
    protected static final String TEST_PASSWORD = "Selenium123";

    private static final String WINDOWS_CHROME =
            "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";

    @BeforeEach
    public void setUp() {

        ChromeOptions options = new ChromeOptions();

        // -Dselenium.chromeBinary=... overrides; otherwise use the usual Windows
        // install path if it exists, else let Selenium Manager locate Chrome.
        String chromeBinary = System.getProperty("selenium.chromeBinary", "");

        if (!chromeBinary.isBlank()) {
            options.setBinary(chromeBinary);
        } else if (new File(WINDOWS_CHROME).exists()) {
            options.setBinary(WINDOWS_CHROME);
        }

        boolean headless = Boolean.parseBoolean(
                System.getProperty("selenium.headless", "false"));

        if (headless) {
            options.addArguments("--headless=new");
        }

        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        driver.get(BASE_URL);
    }

    // ------------------------------------------------------------ helpers

    /** Logs in through the login form and waits until the browser leaves the login page. */
    protected void login(String email, String password) {

        driver.get(BASE_URL + "/login");

        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("login-btn")).click();

        wait.until(d -> !d.getCurrentUrl().contains("/login")
                || !d.findElements(By.id("login-error")).isEmpty());
    }

    protected void loginAsReviewer() {
        login(REVIEWER_EMAIL, REVIEWER_PASSWORD);
        wait.until(ExpectedConditions.urlContains("/reviewer/dashboard"));
    }

    /** Registers a brand-new complainant account and returns its email. */
    protected String registerComplainant() {

        String email = "selenium." + UUID.randomUUID().toString().substring(0, 8) + "@test.local";

        driver.get(BASE_URL + "/register");

        driver.findElement(By.id("name")).sendKeys("Selenium Complainant");
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("confirmPassword")).sendKeys(TEST_PASSWORD);

        clickWithScroll(driver.findElement(By.id("register-btn")));

        wait.until(ExpectedConditions.urlContains("registered"));

        return email;
    }

    /** Registers a new complainant, logs in and waits for "My Complaints". */
    protected String registerAndLoginComplainant() {

        String email = registerComplainant();
        login(email, TEST_PASSWORD);
        wait.until(ExpectedConditions.urlContains("/complaint/my"));

        return email;
    }

    protected void logout() {
        clickWithScroll(driver.findElement(By.id("logout-btn")));
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    protected void clickWithScroll(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});", element);
        element.click();
    }

    // -------------------------------------------------- screenshot support

    @RegisterExtension
    TestWatcher screenshotWatcher = new TestWatcher() {

        @Override
        public void testSuccessful(ExtensionContext context) {
            closeBrowser();
        }

        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            takeScreenshot(context);
            closeBrowser();
        }

        @Override
        public void testAborted(ExtensionContext context, Throwable cause) {
            closeBrowser();
        }
    };

    private void closeBrowser() {

        if (driver != null) {
            try {
                driver.quit();
            } finally {
                driver = null;
            }
        }
    }

    private void takeScreenshot(ExtensionContext context) {

        if (driver == null) {
            return;
        }

        try {

            String testName =
                    context.getTestClass().map(Class::getSimpleName).orElse("UnknownTest")
                            + "_"
                            + context.getDisplayName().replaceAll("[^a-zA-Z0-9.-]", "_");

            Path screenshotDir = Paths.get("test-screenshots");
            Files.createDirectories(screenshotDir);

            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destination = screenshotDir.resolve(testName + ".png");

            Files.copy(screenshot.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Failure screenshot saved to: " + destination.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("Could not save failure screenshot: " + e.getMessage());
        }
    }
}
