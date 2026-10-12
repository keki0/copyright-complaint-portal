package com.ccp.portal.controller;

import com.ccp.portal.exception.ResourceNotFoundException;
import com.ccp.portal.model.Document;
import com.ccp.portal.model.User;
import com.ccp.portal.repository.DocumentRepository;
import com.ccp.portal.security.CurrentUserService;
import com.ccp.portal.service.ComplaintService;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Secure download of supporting documents. Files are never served from a
 * public folder: every request goes through this ownership check.
 */
@Controller
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final ComplaintService complaintService;
    private final CurrentUserService currentUserService;

    public DocumentController(
            DocumentRepository documentRepository,
            ComplaintService complaintService,
            CurrentUserService currentUserService) {

        this.documentRepository = documentRepository;
        this.complaintService = complaintService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/documents/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {

        User viewer = currentUserService.requireCurrentUser();

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found."));

        // Owner of the complaint, reviewers and admins only
        if (!viewer.isStaff() && !document.getComplaint().isOwnedBy(viewer)) {
            throw new ResourceNotFoundException("Document not found.");
        }

        Path root = complaintService.uploadRoot();
        Path file = root.resolve(document.getStoredFileName()).normalize();

        if (!file.startsWith(root) || !Files.isReadable(file)) {
            throw new ResourceNotFoundException("The stored file is no longer available.");
        }

        MediaType mediaType = mediaTypeOf(document);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(document.getFileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(new FileSystemResource(file));
    }

    private MediaType mediaTypeOf(Document document) {
        try {
            return document.getFileType() == null
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(document.getFileType());
        } catch (IllegalArgumentException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
