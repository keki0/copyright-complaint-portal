package com.ccp.portal.service;

import com.ccp.portal.dto.ComplaintRequest;
import com.ccp.portal.exception.ResourceNotFoundException;
import com.ccp.portal.model.Complaint;
import com.ccp.portal.model.Document;
import com.ccp.portal.model.User;
import com.ccp.portal.repository.ComplaintRepository;
import com.ccp.portal.repository.DocumentRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final DocumentRepository documentRepository;
    private final DocumentValidationService documentValidationService;
    private final ComplaintHistoryService historyService;

    @Value("${app.upload.dir}")
    private String uploadDirectory;

    public ComplaintService(
            ComplaintRepository complaintRepository,
            DocumentRepository documentRepository,
            DocumentValidationService documentValidationService,
            ComplaintHistoryService historyService) {

        this.complaintRepository = complaintRepository;
        this.documentRepository = documentRepository;
        this.documentValidationService = documentValidationService;
        this.historyService = historyService;
    }

    /**
     * Submits a complaint for the logged-in complainant. The complainant is
     * the authenticated user - it is never taken from form input.
     */
    @Transactional
    public Complaint submitComplaint(
            ComplaintRequest request,
            MultipartFile documentFile,
            User complainant) throws IOException {

        // 1. Validate document (extension, size and real file signature)
        DocumentValidationService.ValidationResult validation =
                documentValidationService.validate(documentFile);

        if (!validation.valid()) {
            throw new IllegalArgumentException(validation.message());
        }

        // 2. Generate complaint number
        String complaintNumber =
                "CCP-" + Year.now().getValue() + "-"
                        + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 3. Create complaint
        Complaint complaint = Complaint.builder()
                .complaintNumber(complaintNumber)
                .complainant(complainant)
                .workTitle(request.getWorkTitle().trim())
                .copyrightType(request.getCopyrightType())
                .description(request.getDescription().trim())
                .infringementDetails(request.getInfringementDetails().trim())
                .status(Complaint.Status.SUBMITTED)
                .build();

        Complaint savedComplaint = complaintRepository.save(complaint);

        // 4. Store the physical file under a random name (prevents path traversal)
        Path uploadPath = uploadRoot();
        Files.createDirectories(uploadPath);

        String originalFilename = baseName(documentFile.getOriginalFilename());

        String storedFilename =
                UUID.randomUUID() + "." + DocumentValidationService.extensionOf(originalFilename);

        Path targetPath = uploadPath.resolve(storedFilename).normalize();

        if (!targetPath.startsWith(uploadPath)) {
            throw new IOException("Invalid file path.");
        }

        try (InputStream in = documentFile.getInputStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 5. Save document metadata
        documentRepository.save(Document.builder()
                .complaint(savedComplaint)
                .fileName(originalFilename)
                .storedFileName(storedFilename)
                .filePath(targetPath.toString())
                .fileType(documentFile.getContentType())
                .fileSize(documentFile.getSize())
                .validationStatus("VALID")
                .validationMessage(validation.message())
                .build());

        // 6. Audit trail
        historyService.record(
                savedComplaint,
                null,
                Complaint.Status.SUBMITTED,
                complainant,
                "Complaint submitted with supporting document."
        );

        return savedComplaint;
    }

    public List<Complaint> getComplaintsOf(User complainant) {
        return complaintRepository.findByComplainantOrderBySubmittedAtDesc(complainant);
    }

    /**
     * Object-level authorization for tracking: complainants may only see
     * their own complaints; reviewers and admins may see any complaint.
     * Someone else's complaint is reported as "not found" so complaint IDs
     * cannot be probed.
     */
    public Complaint getComplaintForViewer(String complaintNumber, User viewer) {

        String normalizedNumber = complaintNumber == null
                ? ""
                : complaintNumber.trim().toUpperCase();

        Complaint complaint = complaintRepository
                .findByComplaintNumber(normalizedNumber)
                .orElseThrow(() -> notFound(normalizedNumber));

        if (!viewer.isStaff() && !complaint.isOwnedBy(viewer)) {
            throw notFound(normalizedNumber);
        }

        return complaint;
    }

    public List<Document> getDocuments(Complaint complaint) {
        return documentRepository.findByComplaintId(complaint.getId());
    }

    public Path uploadRoot() {
        return Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    /** Strips any directory part a browser may send with the file name. */
    private static String baseName(String fileName) {
        int slash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        return fileName.substring(slash + 1);
    }

    private ResourceNotFoundException notFound(String complaintNumber) {
        return new ResourceNotFoundException("No complaint was found with ID: " + complaintNumber);
    }
}
