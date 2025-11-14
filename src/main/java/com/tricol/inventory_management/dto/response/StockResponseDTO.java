package com.tricol.inventory_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockResponseDTO {
    private Long productId;
    private String productReference;
    private String productName;
    private Integer currentStock;
    private BigDecimal stockValue;
    private Integer reorderPoint;
    private Boolean isLowStock;
}