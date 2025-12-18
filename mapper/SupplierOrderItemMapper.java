package com.tricol.Tricol.mapper;

import com.tricol.Tricol.dto.request.create.SupplierOrderItemRequestDTO;
import com.tricol.Tricol.dto.request.update.SupplierOrderItemUpdateDTO;
import com.tricol.Tricol.dto.response.SupplierOrderItemResponseDTO;
import com.tricol.Tricol.model.SupplierOrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierOrderItemMapper {
    SupplierOrderItemResponseDTO toDTO(SupplierOrderItem item);
    SupplierOrderItem toEntity(SupplierOrderItemRequestDTO dto);
    void updateEntity(SupplierOrderItemUpdateDTO dto, @MappingTarget SupplierOrderItem entity);
}
