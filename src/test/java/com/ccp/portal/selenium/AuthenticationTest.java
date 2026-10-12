package com.ccp.portal.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/** Authentication (login / logout / registration) and role-based authorization. */
public class AuthenticationTest extends BaseSeleniumTest {

    @Test
    void anonymousUserIsRedirectedToLogin() {

        driver.get(BASE_URL + "/reviewer/dashboard");

        wait.until(ExpectedConditions.urlContains("/login"));

        assertTrue(driver.findElement(By.id("login-form")).isDisplayed(),
                "Protected page should redirect to the login form");
    }

    @Test
    void invalidCredentialsShowError() {

        login("nobody@test.local", "WrongPass123");

        assertTrue(driver.findElement(By.id("login-error")).isDisplayed(),
                "Invalid credentials should show an error");
        assertTrue(driver.getCurrentUrl().contains("/login"),
                "User should stay on the login page");
    }

    @Test
    void complainantCanRegisterAndLogin() {

        String email = registerAndLoginComplainant();

        assertEquals(email, driver.findElement(By.id("nav-user")).getText().trim(),
                "Navbar should show the logged-in user's email");
        assertEquals("COMPLAINANT", driver.findElement(By.id("nav-role")).getText().trim(),
                "Self-registered users should be complainants");
    }

    @Test
    void complainantCannotOpenReviewerDashboard() {

        registerAndLoginComplainant();

        driver.get(BASE_URL + "/reviewer/dashboard");

        assertTrue(driver.findElement(By.id("access-denied-title")).isDisplayed(),
                "Complainant should receive Access Denied for reviewer pages");
    }

    @Test
    void reviewerLoginOpensDashboard() {

        loginAsReviewer();

        assertEquals("REVIEWER", driver.findElement(By.id("nav-role")).getText().trim());
        assertTrue(driver.getPageSource().contains("Reviewer Dashboard"));
    }

    @Test
    void logoutEndsTheSession() {

        loginAsReviewer();
        logout();

        assertTrue(driver.findElement(By.id("logout-message")).isDisplayed(),
                "Logout confirmation should be shown");

        driver.get(BASE_URL + "/reviewer/dashboard");
        wait.until(ExpectedConditions.urlContains("/login"));
    }
}
