package com.ccp.portal.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/** Plain unit test (no browser, no Spring context) for document validation. */
class DocumentValidationServiceTest {

    private final DocumentValidationService service = new DocumentValidationService();

    @Test
    void acceptsRealPdf() {
        assertTrue(service.validate(file("evidence.pdf", "%PDF-1.4 content".getBytes(StandardCharsets.US_ASCII))).valid());
    }

    @Test
    void acceptsRealPng() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
        assertTrue(service.validate(file("screenshot.PNG", png)).valid());
    }

    @Test
    void rejectsRenamedExecutable() {
        DocumentValidationService.ValidationResult result =
                service.validate(file("evidence.pdf", "MZ fake executable".getBytes(StandardCharsets.US_ASCII)));

        assertFalse(result.valid());
        assertTrue(result.message().contains("does not match"));
    }

    @Test
    void rejectsUnsupportedExtension() {
        assertFalse(service.validate(file("script.exe", "MZ".getBytes(StandardCharsets.US_ASCII))).valid());
    }

    @Test
    void rejectsEmptyAndMissingFiles() {
        assertFalse(service.validate(null).valid());
        assertFalse(service.validate(file("empty.pdf", new byte[0])).valid());
    }

    @Test
    void rejectsFilesOverFiveMegabytes() {
        byte[] big = new byte[5 * 1024 * 1024 + 1];
        big[0] = '%'; big[1] = 'P'; big[2] = 'D'; big[3] = 'F';
        assertFalse(service.validate(file("big.pdf", big)).valid());
    }

    private static MultipartFile file(String name, byte[] content) {
        return new MultipartFile() {
            public String getName() { return "document"; }
            public String getOriginalFilename() { return name; }
            public String getContentType() { return "application/octet-stream"; }
            public boolean isEmpty() { return content.length == 0; }
            public long getSize() { return content.length; }
            public byte[] getBytes() { return content; }
            public InputStream getInputStream() { return new ByteArrayInputStream(content); }
            public void transferTo(File dest) { throw new UnsupportedOperationException(); }
        };
    }
}
