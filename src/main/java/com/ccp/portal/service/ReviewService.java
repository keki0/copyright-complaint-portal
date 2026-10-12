package com.ccp.portal.service;

import com.ccp.portal.exception.ResourceNotFoundException;
import com.ccp.portal.model.Complaint;
import com.ccp.portal.model.Review;
import com.ccp.portal.model.User;
import com.ccp.portal.repository.ComplaintRepository;
import com.ccp.portal.repository.ReviewRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class ReviewService {

    /** Dashboard filters. "PENDING" = not yet decided (SUBMITTED + UNDER_REVIEW). */
    public static final List<String> FILTERS =
            List.of("PENDING", "MINE", "SUBMITTED", "UNDER_REVIEW", "APPROVED", "REJECTED", "ALL");

    private final ComplaintRepository complaintRepository;
    private final ReviewRepository reviewRepository;
    private final ComplaintHistoryService historyService;

    public ReviewService(
            ComplaintRepository complaintRepository,
            ReviewRepository reviewRepository,
            ComplaintHistoryService historyService) {

        this.complaintRepository = complaintRepository;
        this.reviewRepository = reviewRepository;
        this.historyService = historyService;
    }

    public List<Complaint> getPendingComplaints() {
        return complaintRepository.findByStatusInOrderBySubmittedAtAsc(
                List.of(Complaint.Status.SUBMITTED, Complaint.Status.UNDER_REVIEW));
    }

    /**
     * Complaints for the reviewer dashboard, filtered by status tab and an
     * optional search on complaint ID / work title / complainant e-mail.
     */
    public List<Complaint> getDashboardComplaints(String filter, String query, User reviewer) {

        String normalizedFilter = normalizeFilter(filter);

        List<Complaint> complaints = switch (normalizedFilter) {
            case "PENDING" -> getPendingComplaints();
            case "MINE" -> getPendingComplaints().stream()
                    .filter(c -> c.isAssignedTo(reviewer))
                    .toList();
            case "ALL" -> complaintRepository.findAllByOrderBySubmittedAtDesc();
            default -> complaintRepository.findByStatus(Complaint.Status.valueOf(normalizedFilter));
        };

        if (query == null || query.isBlank()) {
            return complaints;
        }

        String q = query.trim().toLowerCase(Locale.ROOT);

        return complaints.stream()
                .filter(c -> c.getComplaintNumber().toLowerCase(Locale.ROOT).contains(q)
                        || c.getWorkTitle().toLowerCase(Locale.ROOT).contains(q)
                        || c.getComplainant().getEmail().toLowerCase(Locale.ROOT).contains(q))
                .toList();
    }

    public String normalizeFilter(String filter) {
        String value = filter == null ? "PENDING" : filter.trim().toUpperCase(Locale.ROOT);
        return FILTERS.contains(value) ? value : "PENDING";
    }

    public Map<Complaint.Status, Long> getStatusCounts() {

        Map<Complaint.Status, Long> counts = new EnumMap<>(Complaint.Status.class);

        for (Complaint.Status status : Complaint.Status.values()) {
            counts.put(status, complaintRepository.countByStatus(status));
        }

        return counts;
    }

    /** Read-only: viewing a complaint no longer changes its status. */
    public Complaint getComplaint(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found."));
    }

    public Optional<Review> getLatestReview(Complaint complaint) {
        return reviewRepository.findTopByComplaintOrderByReviewedAtDesc(complaint);
    }

    /**
     * "Start review": the reviewer claims a SUBMITTED complaint, which moves
     * it to UNDER_REVIEW and assigns it to them.
     */
    @Transactional
    public Complaint startReview(Long complaintId, User reviewer) {

        Complaint complaint = getComplaint(complaintId);

        if (complaint.getStatus() != Complaint.Status.SUBMITTED) {

            // Legacy complaints opened by the old MVP are UNDER_REVIEW with no reviewer.
            if (complaint.getStatus() == Complaint.Status.UNDER_REVIEW
                    && complaint.getAssignedReviewer() == null) {

                complaint.setAssignedReviewer(reviewer);
                historyService.record(complaint, Complaint.Status.UNDER_REVIEW,
                        Complaint.Status.UNDER_REVIEW, reviewer,
                        "Complaint assigned to " + reviewer.getName() + ".");
                return complaintRepository.save(complaint);
            }

            throw new IllegalStateException("Only newly submitted complaints can be claimed for review.");
        }

        complaint.setStatus(Complaint.Status.UNDER_REVIEW);
        complaint.setAssignedReviewer(reviewer);

        Complaint saved = complaintRepository.save(complaint);

        historyService.record(saved, Complaint.Status.SUBMITTED, Complaint.Status.UNDER_REVIEW,
                reviewer, "Review started by " + reviewer.getName() + ".");

        return saved;
    }

    /**
     * Records the final decision. Authorization rule: only the reviewer the
     * complaint is assigned to, or an ADMIN, may decide it.
     */
    @Transactional
    public Complaint reviewComplaint(Long complaintId, String decision, String remarks, User reviewer) {

        Complaint complaint = getComplaint(complaintId);

        if (complaint.isFinalized()) {
            throw new IllegalStateException("This complaint has already been finalized.");
        }

        if (complaint.getStatus() != Complaint.Status.UNDER_REVIEW) {
            throw new IllegalStateException("Start the review before recording a decision.");
        }

        if (!canDecide(complaint, reviewer)) {
            throw new AccessDeniedException("This complaint is assigned to another reviewer.");
        }

        if (remarks == null || remarks.trim().length() < 10) {
            throw new IllegalArgumentException("Review remarks must contain at least 10 characters.");
        }

        if (!"APPROVE".equalsIgnoreCase(decision) && !"REJECT".equalsIgnoreCase(decision)) {
            throw new IllegalArgumentException("Invalid review decision.");
        }

        reviewRepository.save(Review.builder()
                .complaint(complaint)
                .reviewer(reviewer)
                .remarks(remarks.trim())
                .decision(decision.toUpperCase(Locale.ROOT))
                .build());

        Complaint.Status newStatus = "APPROVE".equalsIgnoreCase(decision)
                ? Complaint.Status.APPROVED
                : Complaint.Status.REJECTED;

        complaint.setStatus(newStatus);

        if (complaint.getAssignedReviewer() == null) {
            complaint.setAssignedReviewer(reviewer);
        }

        Complaint saved = complaintRepository.save(complaint);

        historyService.record(saved, Complaint.Status.UNDER_REVIEW, newStatus, reviewer,
                "Decision recorded by " + reviewer.getName() + ".");

        return saved;
    }

    public boolean canDecide(Complaint complaint, User user) {
        return complaint.getStatus() == Complaint.Status.UNDER_REVIEW
                && (user.hasRole(User.Role.ADMIN)
                    || complaint.getAssignedReviewer() == null
                    || complaint.isAssignedTo(user));
    }
}
