package com.ccp.portal.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
public class DocumentValidationService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(
                    "pdf",
                    "doc",
                    "docx",
                    "jpg",
                    "jpeg",
                    "png"
            );

    public ValidationResult validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return new ValidationResult(
                    false,
                    "Document is required."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return new ValidationResult(
                    false,
                    "File size must not exceed 5 MB."
            );
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || !originalName.contains(".")) {
            return new ValidationResult(
                    false,
                    "File must have a valid extension."
            );
        }

        String extension =
                originalName
                        .substring(
                                originalName.lastIndexOf('.') + 1
                        )
                        .toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return new ValidationResult(
                    false,
                    "Unsupported file type. Allowed: PDF, DOC, DOCX, JPG, JPEG, PNG."
            );
        }

        return new ValidationResult(
                true,
                "Document validated successfully."
        );
    }

    public record ValidationResult(
            boolean valid,
            String message
    ) {}
}