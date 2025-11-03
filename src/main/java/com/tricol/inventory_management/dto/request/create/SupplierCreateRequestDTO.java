package com.tricol.inventory_management.dto.request.create;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierCreateRequestDTO {

    @NotBlank(message = "Company name is required")
    @Size(max = 255)
    private String companyName;

    @Size(max = 2000)
    private String address;

    @Size(max = 255)
    private String contactPerson;

    @Email(message = "Invalid email")
    @Size(max = 255)
    private String email;

    @Size(max = 50)
    private String phone;

    @Size(max = 100)
    private String city;

    @NotBlank(message = "ICE is required")
    @Size(max = 50)
    private String ice;
}

