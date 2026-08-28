package com.ccp.portal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComplaintRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @NotBlank(message = "Work title is required")
    private String workTitle;

    @NotBlank(message = "Copyright type is required")
    private String copyrightType;

    @NotBlank(message = "Description is required")
    @Size(min = 20, message = "Description must contain at least 20 characters")
    private String description;

    @NotBlank(message = "Infringement details are required")
    @Size(min = 20, message = "Infringement details must contain at least 20 characters")
    private String infringementDetails;
}