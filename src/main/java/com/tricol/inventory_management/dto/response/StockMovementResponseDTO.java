package com.tricol.inventory_management.dto.response;

import com.tricol.inventory_management.enums.MovementType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponseDTO {
    private Long id;
    private String productReference;
    private String productName;
    private MovementType movementType;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String reference;
    private String notes;
    private String lotNumber;
    private LocalDateTime movementDate;
}