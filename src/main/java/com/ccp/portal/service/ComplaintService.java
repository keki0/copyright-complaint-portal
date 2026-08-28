package com.ccp.portal.service;

import com.ccp.portal.dto.ComplaintRequest;
import com.ccp.portal.model.Complaint;
import com.ccp.portal.model.Document;
import com.ccp.portal.model.User;
import com.ccp.portal.repository.ComplaintRepository;
import com.ccp.portal.repository.DocumentRepository;
import com.ccp.portal.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Year;
import java.util.UUID;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentValidationService documentValidationService;

    @Value("${app.upload.dir}")
    private String uploadDirectory;

    public ComplaintService(
            ComplaintRepository complaintRepository,
            UserRepository userRepository,
            DocumentRepository documentRepository,
            DocumentValidationService documentValidationService) {

        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.documentValidationService = documentValidationService;
    }

    public Complaint submitComplaint(
            ComplaintRequest request,
            MultipartFile documentFile) throws IOException {

        // 1. Validate document
        DocumentValidationService.ValidationResult validation =
                documentValidationService.validate(documentFile);

        if (!validation.valid()) {
            throw new IllegalArgumentException(
                    validation.message()
            );
        }

        // 2. Find or create complainant
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseGet(() -> {

                    User newUser = User.builder()
                            .name(request.getName())
                            .email(request.getEmail())
                            .password("TEMPORARY")
                            .role(User.Role.COMPLAINANT)
                            .build();

                    return userRepository.save(newUser);
                });

        // 3. Generate complaint number
        String complaintNumber =
                "CCP-" +
                Year.now().getValue() +
                "-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        // 4. Create complaint
        Complaint complaint = Complaint.builder()
                .complaintNumber(complaintNumber)
                .complainant(user)
                .workTitle(request.getWorkTitle())
                .copyrightType(request.getCopyrightType())
                .description(request.getDescription())
                .infringementDetails(request.getInfringementDetails())
                .status(Complaint.Status.SUBMITTED)
                .build();

        Complaint savedComplaint =
                complaintRepository.save(complaint);

        // 5. Create upload directory
        Path uploadPath =
                Paths.get(uploadDirectory)
                        .toAbsolutePath()
                        .normalize();

        Files.createDirectories(uploadPath);

        // 6. Generate safe stored filename
        String originalFilename =
                documentFile.getOriginalFilename();

        String extension =
                originalFilename
                        .substring(
                                originalFilename.lastIndexOf('.')
                        )
                        .toLowerCase();

        String storedFilename =
                UUID.randomUUID() + extension;

        Path targetPath =
                uploadPath.resolve(storedFilename)
                        .normalize();

        // Security check: prevent path traversal
        if (!targetPath.startsWith(uploadPath)) {
            throw new IOException(
                    "Invalid file path."
            );
        }

        // 7. Store physical file
        Files.copy(
                documentFile.getInputStream(),
                targetPath,
                StandardCopyOption.REPLACE_EXISTING
        );

        // 8. Save document metadata
        Document document = Document.builder()
                .complaint(savedComplaint)
                .fileName(originalFilename)
                .storedFileName(storedFilename)
                .filePath(targetPath.toString())
                .fileType(documentFile.getContentType())
                .fileSize(documentFile.getSize())
                .validationStatus("VALID")
                .validationMessage(validation.message())
                .build();

        documentRepository.save(document);

        return savedComplaint;
    }
}