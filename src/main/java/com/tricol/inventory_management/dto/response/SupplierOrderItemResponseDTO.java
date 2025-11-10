package com.tricol.inventory_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplierOrderItemResponseDTO {
    private Long id;

    private ProductResponseDTO product;

    private int quantity;

    private BigDecimal unitPrice;
}
