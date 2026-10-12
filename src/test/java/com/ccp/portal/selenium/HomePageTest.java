package com.ccp.portal.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.*;

public class HomePageTest extends BaseSeleniumTest {

    @Test
    void verifyHomePageLoads() {

        driver.get(BASE_URL);

        String page = driver.getPageSource();

        assertTrue(page.contains("Copyright Complaint Portal"), "Home page should contain portal title");
        assertTrue(page.contains("Protect Your Creative Work"), "Home page should contain main heading");
        assertTrue(page.contains("Submit a Complaint"), "Home page should contain Submit Complaint option");
    }

    @Test
    void verifyHomePageShowsLoginAndRegisterForVisitors() {

        driver.get(BASE_URL);

        assertTrue(driver.findElement(By.id("nav-login")).isDisplayed(),
                "Visitors should see a Login link");
        assertTrue(driver.findElement(By.id("nav-register")).isDisplayed(),
                "Visitors should see a Register link");
    }
}
