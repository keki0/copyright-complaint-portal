package com.ccp.portal.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.*;

public class TrackComplaintTest extends BaseSeleniumTest {

    @Test
    void verifyTrackComplaintPage() {

        driver.get(BASE_URL + "/complaint/track");

        WebElement heading =
                driver.findElement(By.tagName("h2"));

        assertEquals(
                "Track Your Complaint",
                heading.getText().trim(),
                "Track complaint page should be displayed"
        );

        WebElement complaintNumber =
                driver.findElement(
                        By.name("complaintNumber")
                );

        assertTrue(
                complaintNumber.isDisplayed(),
                "Complaint ID input should be displayed"
        );

        complaintNumber.sendKeys(
                "CCP-2026-INVALID"
        );

        WebElement submitButton =
                driver.findElement(
                        By.cssSelector("button[type='submit']")
                );

        assertTrue(
                submitButton.isDisplayed(),
                "Track Complaint button should be displayed"
        );

        submitButton.click();

        WebElement errorMessage =
                driver.findElement(
                        By.cssSelector(".alert.alert-danger")
                );

        assertTrue(
                errorMessage.isDisplayed(),
                "Error message should be displayed for invalid complaint ID"
        );

        assertFalse(
                errorMessage.getText().trim().isEmpty(),
                "Error message should contain text"
        );

        System.out.println(
                "Complaint tracking validation verified successfully."
        );
    }
}