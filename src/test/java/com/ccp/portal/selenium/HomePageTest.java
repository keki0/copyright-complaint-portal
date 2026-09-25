package com.ccp.portal.selenium;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HomePageTest extends BaseSeleniumTest {

    @Test
    void verifyHomePageLoads() {

        driver.get(BASE_URL);

        String title = driver.getTitle();

        assertTrue(
                driver.getPageSource()
                        .contains("Copyright Complaint Portal"),
                "Home page should contain portal title"
        );

        assertTrue(
                driver.getPageSource()
                        .contains("Protect Your Creative Work"),
                "Home page should contain main heading"
        );

        assertTrue(
                driver.getPageSource()
                        .contains("Submit a Complaint"),
                "Home page should contain Submit Complaint option"
        );
    }
//     @Test
//     void temporaryScreenshotTest() {
//         driver.get(BASE_URL);

//         assertTrue(
//                 false,
//                 "Temporary test to verify failure screenshots"
//         );
//     }
}