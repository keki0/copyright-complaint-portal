package com.ccp.portal.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.*;

public class SubmitComplaintTest extends BaseSeleniumTest {

    @Test
    void verifyComplaintFormValidation() {

        String email = registerAndLoginComplainant();

        driver.get(BASE_URL + "/complaint/new");

        WebElement form = driver.findElement(By.id("complaint-form"));
        assertTrue(form.isDisplayed(), "Complaint form should be displayed");

        // Applicant details now come from the logged-in account
        WebElement applicantEmail = driver.findElement(By.id("applicant-email"));
        assertEquals(email, applicantEmail.getDomProperty("value"),
                "Applicant email should be pre-filled from the account");
        assertNotNull(applicantEmail.getDomAttribute("readonly"),
                "Applicant email should be read-only");

        String[] requiredFields = {
                "workTitle", "copyrightType", "description", "infringementDetails", "document"
        };

        for (String field : requiredFields) {
            assertNotNull(
                    driver.findElement(By.name(field)).getDomAttribute("required"),
                    field + " should be required");
        }

        WebElement submitButton = driver.findElement(By.id("submit-complaint-btn"));

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});", submitButton);

        assertTrue(submitButton.isDisplayed(), "Submit Complaint button should be displayed");
    }

    @Test
    void verifyEmptyComplaintFormValidation() {

        registerAndLoginComplainant();

        driver.get(BASE_URL + "/complaint/new");

        WebElement form = driver.findElement(By.id("complaint-form"));

        Boolean isValid = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].checkValidity();", form);

        assertFalse(isValid, "Empty complaint form should fail validation");
    }
}
