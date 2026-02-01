package com.tricol.Tricol.dto.request.create;

import com.tricol.Tricol.enums.OutboundReason;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockOutboundRequestDTO {
    private OutboundReason reason;
    private String workshop;
    private String notes;
    private List<StockOutboundItemRequestDTO> items;
}