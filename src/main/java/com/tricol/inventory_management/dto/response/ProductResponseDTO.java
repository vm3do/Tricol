package com.tricol.inventory_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private Long id;
    private String reference;
    private String name;
    private String description;
    private BigDecimal unitPrice;
    private String category;
    private Integer reorderPoint;
    private String unit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
