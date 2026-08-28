package com.ccp.portal.service;

import com.ccp.portal.model.Complaint;
import com.ccp.portal.model.Review;
import com.ccp.portal.model.User;
import com.ccp.portal.repository.ComplaintRepository;
import com.ccp.portal.repository.ReviewRepository;
import com.ccp.portal.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ComplaintRepository complaintRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ReviewService(
            ComplaintRepository complaintRepository,
            ReviewRepository reviewRepository,
            UserRepository userRepository) {

        this.complaintRepository = complaintRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    public List<Complaint> getPendingComplaints() {

        return complaintRepository.findByStatus(
                Complaint.Status.SUBMITTED
        );
    }

    public Complaint getComplaint(Long id) {

        Complaint complaint =
                complaintRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Complaint not found"
                            )   
                        );

        if (complaint.getStatus() ==
                Complaint.Status.SUBMITTED) {

            complaint.setStatus(
                    Complaint.Status.UNDER_REVIEW
            );

            complaint =
                    complaintRepository.save(complaint);
        }

        return complaint;
    }

    public Complaint reviewComplaint(
            Long complaintId,
            String decision,
            String remarks) {

        Complaint complaint =
                complaintRepository.findById(complaintId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Complaint not found"
                                )
                        );

        if (complaint.getStatus() ==
                Complaint.Status.APPROVED ||
            complaint.getStatus() ==
                Complaint.Status.REJECTED) {

            throw new IllegalStateException(
                    "This complaint has already been finalized."
            );
        }

        if (remarks == null ||
                remarks.trim().length() < 10) {

            throw new IllegalArgumentException(
                    "Review remarks must contain at least 10 characters."
            );
        }

        if (!"APPROVE".equalsIgnoreCase(decision) &&
            !"REJECT".equalsIgnoreCase(decision)) {

            throw new IllegalArgumentException(
                    "Invalid review decision."
            );
        }

        User reviewer =
                userRepository.findByEmail(
                        "reviewer@copyrightportal.com"
                ).orElseGet(() -> {

                    User newReviewer = User.builder()
                            .name("Portal Reviewer")
                            .email("reviewer@copyrightportal.com")
                            .password("TEMPORARY")
                            .role(User.Role.REVIEWER)
                            .build();

                    return userRepository.save(newReviewer);
                });

        Review review = Review.builder()
                .complaint(complaint)
                .reviewer(reviewer)
                .remarks(remarks.trim())
                .decision(
                        decision.toUpperCase()
                )
                .build();

        reviewRepository.save(review);

        if ("APPROVE".equalsIgnoreCase(decision)) {

            complaint.setStatus(
                    Complaint.Status.APPROVED
            );

        } else {

            complaint.setStatus(
                    Complaint.Status.REJECTED
            );
        }

        return complaintRepository.save(complaint);
    }
}