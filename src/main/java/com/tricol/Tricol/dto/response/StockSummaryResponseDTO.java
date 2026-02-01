package com.tricol.Tricol.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockSummaryResponseDTO {
    private List<StockResponseDTO> stocks;
    private BigDecimal totalValue;
    private List<ProductResponseDTO> alerts;
    private Integer totalProducts;
    private Integer lowStockCount;
}