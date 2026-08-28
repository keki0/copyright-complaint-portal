package com.ccp.portal.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String complaintNumber;

    @ManyToOne
    @JoinColumn(name = "complainant_id", nullable = false)
    private User complainant;

    @Column(nullable = false)
    private String workTitle;

    @Column(nullable = false)
    private String copyrightType;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 3000)
    private String infringementDetails;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private LocalDateTime submittedAt;

    private LocalDateTime updatedAt;

    public enum Status {
        SUBMITTED,
        UNDER_REVIEW,
        APPROVED,
        REJECTED
    }

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = Status.SUBMITTED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}