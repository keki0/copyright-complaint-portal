package com.ccp.portal.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class DocumentValidationService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final byte[] PDF = {'%', 'P', 'D', 'F'};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] ZIP = {'P', 'K', 0x03, 0x04};
    private static final byte[] OLE = {
            (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0,
            (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1
    };

    /**
     * Allowed extensions and the file signature ("magic bytes") each one
     * must start with. Checking the content as well as the extension stops
     * files that are only renamed (e.g. malware.exe -> evidence.pdf).
     */
    private static final Map<String, List<byte[]>> SIGNATURES = Map.of(
            "pdf", List.of(PDF),
            "png", List.of(PNG),
            "jpg", List.of(JPEG),
            "jpeg", List.of(JPEG),
            "docx", List.of(ZIP),
            "doc", List.of(OLE)
    );

    public ValidationResult validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return new ValidationResult(false, "Document is required.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return new ValidationResult(false, "File size must not exceed 5 MB.");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || !originalName.contains(".")) {
            return new ValidationResult(false, "File must have a valid extension.");
        }

        String extension = extensionOf(originalName);

        if (!SIGNATURES.containsKey(extension)) {
            return new ValidationResult(
                    false,
                    "Unsupported file type. Allowed: PDF, DOC, DOCX, JPG, JPEG, PNG."
            );
        }

        if (!hasExpectedSignature(file, SIGNATURES.get(extension))) {
            return new ValidationResult(
                    false,
                    "The file content does not match its ." + extension + " extension."
            );
        }

        return new ValidationResult(true, "Document validated successfully.");
    }

    public static String extensionOf(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private boolean hasExpectedSignature(MultipartFile file, List<byte[]> signatures) {

        try (InputStream in = file.getInputStream()) {

            byte[] header = in.readNBytes(8);

            for (byte[] signature : signatures) {
                if (header.length >= signature.length
                        && Arrays.equals(header, 0, signature.length, signature, 0, signature.length)) {
                    return true;
                }
            }

            return false;

        } catch (IOException e) {
            return false;
        }
    }

    public record ValidationResult(boolean valid, String message) {
    }
}
