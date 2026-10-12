package com.ccp.portal.repository;

import com.ccp.portal.model.Complaint;
import com.ccp.portal.model.ComplaintStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintStatusHistoryRepository
        extends JpaRepository<ComplaintStatusHistory, Long> {

    List<ComplaintStatusHistory> findByComplaintOrderByChangedAtAscIdAsc(Complaint complaint);
}
