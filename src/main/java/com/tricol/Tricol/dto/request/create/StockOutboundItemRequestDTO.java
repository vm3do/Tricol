package com.tricol.Tricol.dto.request.create;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockOutboundItemRequestDTO {
    private Long productId;
    private Integer quantity;
    private String notes;
}