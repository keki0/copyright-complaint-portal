
package com.ccp.portal.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class BaseSeleniumTest {

    protected WebDriver driver;

    protected static final String BASE_URL =
            "http://localhost:8081";

    @BeforeEach
    public void setUp() {

        driver = new ChromeDriver();

        driver.manage().window().maximize();

        driver.get(BASE_URL);
    }

    @AfterEach
    public void tearDown() {

        if (driver != null) {
            driver.quit();
        }
    }

    @RegisterExtension
    ScreenshotOnFailure screenshotExtension =
            new ScreenshotOnFailure();

    private class ScreenshotOnFailure
            implements TestExecutionExceptionHandler {

        @Override
        public void handleTestExecutionException(
                ExtensionContext context,
                Throwable cause) throws Throwable {

            takeScreenshot(context);

            // Preserve the original test failure
            throw cause;
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
                            .replaceAll(
                                    "[^a-zA-Z0-9.-]",
                                    "_"
                            );

            Path screenshotDir =
                    Paths.get("test-screenshots");

            Files.createDirectories(screenshotDir);

            File screenshot =
                    ((TakesScreenshot) driver)
                            .getScreenshotAs(OutputType.FILE);

            Path destination =
                    screenshotDir.resolve(
                            testName + ".png"
                    );

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