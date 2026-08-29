package com.ccp.portal.repository;

import com.ccp.portal.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findTopByComplaintOrderByReviewedAtDesc(
            com.ccp.portal.model.Complaint complaint
    );
}