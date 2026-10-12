package com.ccp.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Complaint form data. The complainant's name and e-mail are no longer
 * typed into the form: they are taken from the logged-in account, so a
 * user cannot file a complaint in someone else's name.
 */
@Getter
@Setter
public class ComplaintRequest {

    @NotBlank(message = "Work title is required")
    @Size(max = 255, message = "Work title must not exceed 255 characters")
    private String workTitle;

    @NotBlank(message = "Copyright type is required")
    private String copyrightType;

    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 2000, message = "Description must contain 20 to 2000 characters")
    private String description;

    @NotBlank(message = "Infringement details are required")
    @Size(min = 20, max = 3000, message = "Infringement details must contain 20 to 3000 characters")
    private String infringementDetails;
}
