package com.ccp.portal.service;

import com.ccp.portal.model.Complaint;
import com.ccp.portal.model.ComplaintStatusHistory;
import com.ccp.portal.model.User;
import com.ccp.portal.repository.ComplaintStatusHistoryRepository;

import org.springframework.stereotype.Service;

import java.util.List;

/** Records and reads the audit trail of complaint status changes. */
@Service
public class ComplaintHistoryService {

    private final ComplaintStatusHistoryRepository historyRepository;

    public ComplaintHistoryService(ComplaintStatusHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public void record(
            Complaint complaint,
            Complaint.Status fromStatus,
            Complaint.Status toStatus,
            User changedBy,
            String note) {

        historyRepository.save(ComplaintStatusHistory.builder()
                .complaint(complaint)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(changedBy)
                .note(note)
                .build());
    }

    public List<ComplaintStatusHistory> getHistory(Complaint complaint) {
        return historyRepository.findByComplaintOrderByChangedAtAscIdAsc(complaint);
    }
}
