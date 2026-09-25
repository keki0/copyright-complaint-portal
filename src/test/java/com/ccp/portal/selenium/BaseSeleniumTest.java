
package com.ccp.portal.selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class BaseSeleniumTest {

    protected WebDriver driver;

    protected static final String BASE_URL =
            System.getProperty(
                    "selenium.baseUrl",
                    "http://localhost:8081"
            );

    @BeforeEach
    public void setUp() {

        ChromeOptions options = new ChromeOptions();

        options.setBinary(
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe"
        );

        boolean headless = Boolean.parseBoolean(
                System.getProperty("selenium.headless", "false")
        );

        if (headless) {
            options.addArguments("--headless=new");
        }

        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);

        driver.manage().window().maximize();

        driver.get(BASE_URL);
    }

    @RegisterExtension
    TestWatcher screenshotWatcher = new TestWatcher() {

        @Override
        public void testSuccessful(
                ExtensionContext context) {

            closeBrowser();
        }

        @Override
        public void testFailed(
                ExtensionContext context,
                Throwable cause) {

            takeScreenshot(context);
            closeBrowser();
        }

        @Override
        public void testAborted(
                ExtensionContext context,
                Throwable cause) {

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

    private void takeScreenshot(
            ExtensionContext context) {

        if (driver == null) {
            return;
        }

        try {

            String testName =
                    context.getTestClass()
                            .map(Class::getSimpleName)
                            .orElse("UnknownTest")
                    + "_"
                    + context.getDisplayName()
                            .replaceAll("[^a-zA-Z0-9.-]", "_");

            Path screenshotDir =
                    Paths.get("test-screenshots");

            Files.createDirectories(screenshotDir);

            File screenshot =
                    ((TakesScreenshot) driver)
                            .getScreenshotAs(OutputType.FILE);

            Path destination =
                    screenshotDir.resolve(testName + ".png");

            Files.copy(
                    screenshot.toPath(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

            System.out.println(
                    "Failure screenshot saved to: "
                            + destination.toAbsolutePath()
            );

        } catch (IOException e) {

            System.err.println(
                    "Could not save failure screenshot: "
                            + e.getMessage()
            );
        }
    }
}