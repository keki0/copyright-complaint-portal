package com.ccp.portal.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.*;

public class SubmitComplaintTest extends BaseSeleniumTest {

    @Test
    void verifyComplaintFormValidation() {

        driver.get(BASE_URL + "/complaint/new");

        WebElement form =
                driver.findElement(By.cssSelector("form"));

        assertTrue(
                form.isDisplayed(),
                "Complaint form should be displayed"
        );

        WebElement name =
                driver.findElement(By.name("name"));

        WebElement email =
                driver.findElement(By.name("email"));

        WebElement workTitle =
                driver.findElement(By.name("workTitle"));

        WebElement copyrightType =
                driver.findElement(By.name("copyrightType"));

        WebElement description =
                driver.findElement(By.name("description"));

        WebElement infringementDetails =
                driver.findElement(By.name("infringementDetails"));

        WebElement document =
                driver.findElement(By.name("document"));

        assertTrue(
                name.getAttribute("required") != null,
                "Full Name should be required"
        );

        assertTrue(
                email.getAttribute("required") != null,
                "Email should be required"
        );

        assertTrue(
                workTitle.getAttribute("required") != null,
                "Work Title should be required"
        );

        assertTrue(
                copyrightType.getAttribute("required") != null,
                "Copyright Type should be required"
        );

        assertTrue(
                description.getAttribute("required") != null,
                "Description should be required"
        );

        assertTrue(
                infringementDetails.getAttribute("required") != null,
                "Infringement Details should be required"
        );

        assertTrue(
                document.getAttribute("required") != null,
                "Supporting document should be required"
        );

        WebElement submitButton =
                driver.findElement(
                        By.cssSelector("button[type='submit']")
                );

        assertTrue(
                submitButton.isDisplayed(),
                "Submit Complaint button should be displayed"
        );

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});",
                submitButton
        );

        System.out.println(
                "Complaint form validation elements verified successfully."
        );
    }
    @Test
        void verifyEmptyComplaintFormValidation() {

                driver.get(BASE_URL + "/complaint/new");

                WebElement form =
                        driver.findElement(By.cssSelector("form"));

                JavascriptExecutor js =
                        (JavascriptExecutor) driver;

                Boolean isValid = (Boolean) js.executeScript(
                        "return arguments[0].checkValidity();",
                        form
                );

                assertFalse(
                        isValid,
                        "Empty complaint form should fail validation"
                );
        }
}