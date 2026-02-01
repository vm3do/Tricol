package com.tricol.Tricol.dto.request.update;

import com.tricol.Tricol.dto.request.create.SupplierOrderItemRequestDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierOrderUpdateDTO {
//    @NotNull(message = "Order date is required")
    private LocalDate orderDate;

//    @NotBlank(message = "Status is required")
    private String status;

//    @NotEmpty(message = "Order must contain at least one item")
    private List<@Valid SupplierOrderItemRequestDTO> items;
}
