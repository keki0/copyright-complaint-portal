package com.ccp.portal.repository;

import com.ccp.portal.model.Complaint;
import com.ccp.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByComplaintNumber(String complaintNumber);

    List<Complaint> findByStatus(Complaint.Status status);

    List<Complaint> findByStatusInOrderBySubmittedAtAsc(Collection<Complaint.Status> statuses);

    List<Complaint> findAllByOrderBySubmittedAtDesc();

    List<Complaint> findByComplainantOrderBySubmittedAtDesc(User complainant);

    long countByStatus(Complaint.Status status);
}
