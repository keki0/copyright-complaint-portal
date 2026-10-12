package com.ccp.portal.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end journey across all roles:
 * complainant submits -> reviewer starts review and approves -> complainant tracks the result.
 * Also checks that another complainant cannot see the complaint.
 */
public class ComplaintWorkflowTest extends BaseSeleniumTest {

    private Path evidenceFile;

    @AfterEach
    void deleteEvidenceFile() throws IOException {
        if (evidenceFile != null) {
            Files.deleteIfExists(evidenceFile);
        }
    }

    @Test
    void complaintIsSubmittedReviewedAndTracked() throws IOException {

        // 1. Complainant submits a complaint with a real PDF signature
        String complainantEmail = registerAndLoginComplainant();

        evidenceFile = Files.createTempFile("selenium-evidence", ".pdf");
        Files.writeString(evidenceFile, "%PDF-1.4\n% Selenium test evidence\n", StandardCharsets.US_ASCII);

        driver.get(BASE_URL + "/complaint/new");

        driver.findElement(By.id("workTitle")).sendKeys("Selenium Workflow Song");
        new Select(driver.findElement(By.id("copyrightType"))).selectByVisibleText("Music");
        driver.findElement(By.id("description"))
                .sendKeys("An original song composed and recorded for the Selenium workflow test.");
        driver.findElement(By.id("infringementDetails"))
                .sendKeys("The song was re-uploaded on a video platform without any permission.");
        driver.findElement(By.id("document")).sendKeys(evidenceFile.toAbsolutePath().toString());
        clickWithScroll(driver.findElement(By.id("declaration")));
        clickWithScroll(driver.findElement(By.id("submit-complaint-btn")));

        WebElement numberElement = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("complaint-number")));

        String complaintNumber = numberElement.getText().trim();

        assertTrue(complaintNumber.startsWith("CCP-"), "A complaint ID should be generated");
        assertEquals("SUBMITTED", driver.findElement(By.id("complaint-status")).getText().trim());

        logout();

        // 2. A different complainant must not be able to track it
        registerAndLoginComplainant();
        driver.get(BASE_URL + "/complaint/track?id=" + complaintNumber);

        assertTrue(driver.findElements(By.id("track-result")).isEmpty(),
                "Another complainant must not see this complaint");
        assertTrue(driver.findElement(By.cssSelector(".alert.alert-danger")).isDisplayed());

        logout();

        // 3. Reviewer claims and approves it
        loginAsReviewer();

        driver.get(BASE_URL + "/reviewer/dashboard?filter=ALL&q=" + complaintNumber);
        clickWithScroll(driver.findElement(By.cssSelector("a.review-link")));

        clickWithScroll(wait.until(
                ExpectedConditions.elementToBeClickable(By.id("start-review-btn"))));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("decision-form")));

        String remarks = "Evidence verified: the uploaded work matches the original recording.";

        new Select(driver.findElement(By.id("decision"))).selectByValue("APPROVE");
        driver.findElement(By.id("remarks")).sendKeys(remarks);
        clickWithScroll(driver.findElement(By.id("submit-decision-btn")));

        wait.until(ExpectedConditions.urlContains("/reviewer/dashboard"));

        assertTrue(driver.findElement(By.cssSelector(".alert.alert-success")).getText()
                        .contains(complaintNumber),
                "Reviewer should see a confirmation for the decided complaint");

        logout();

        // 4. Original complainant tracks the final decision
        login(complainantEmail, TEST_PASSWORD);
        driver.get(BASE_URL + "/complaint/track?id=" + complaintNumber);

        assertEquals("APPROVED", driver.findElement(By.id("status-badge")).getText().trim());
        assertEquals(remarks, driver.findElement(By.id("review-remarks")).getText().trim());
        assertTrue(driver.findElements(By.cssSelector("#history-table tbody tr")).size() >= 3,
                "Status history should contain submitted, under review and approved entries");
    }
}
