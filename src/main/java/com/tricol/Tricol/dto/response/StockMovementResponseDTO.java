package com.tricol.Tricol.dto.response;

import com.tricol.Tricol.enums.MovementType;
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