package com.ccp.portal.repository;

import com.ccp.portal.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByComplaintId(Long complaintId);
}