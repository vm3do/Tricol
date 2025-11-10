package com.tricol.inventory_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierOrderResponseDTO {
    private Long id;

    private SupplierResponseDTO supplier;

    private LocalDate orderDate;

    private BigDecimal totalAmount;

    private String status;

    private List<SupplierOrderItemResponseDTO> items;
}
