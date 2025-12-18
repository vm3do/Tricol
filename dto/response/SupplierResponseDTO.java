package com.tricol.Tricol.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponseDTO {
    private Long id;
    private String companyName;
    private String address;
    private String contactPerson;
    private String email;
    private String phone;
    private String city;
    private String ice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

