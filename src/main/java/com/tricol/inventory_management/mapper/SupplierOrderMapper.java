package com.tricol.inventory_management.mapper;

import com.tricol.inventory_management.dto.request.create.SupplierOrderRequestDTO;
import com.tricol.inventory_management.dto.request.update.SupplierOrderUpdateDTO;
import com.tricol.inventory_management.dto.response.SupplierOrderResponseDTO;
import com.tricol.inventory_management.model.SupplierOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierOrderMapper {
//    @Mapping(source = "items", target = "items")
    SupplierOrderResponseDTO toDTO(SupplierOrder order);
    SupplierOrder toEntity(SupplierOrderRequestDTO dto);

    void updateEntity(SupplierOrderUpdateDTO dto, @MappingTarget SupplierOrder entity);
}

