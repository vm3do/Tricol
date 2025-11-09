package com.tricol.inventory_management.mapper;

import com.tricol.inventory_management.dto.request.create.SupplierOrderItemRequestDTO;
import com.tricol.inventory_management.dto.request.update.SupplierOrderItemUpdateDTO;
import com.tricol.inventory_management.dto.response.SupplierOrderItemResponseDTO;
import com.tricol.inventory_management.model.SupplierOrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierOrderItemMapper {
    SupplierOrderItemResponseDTO toDTO(SupplierOrderItem item);
    SupplierOrderItem toEntity(SupplierOrderItemRequestDTO dto);
    void updateEntity(SupplierOrderItemUpdateDTO dto, @MappingTarget SupplierOrderItem entity);
}
