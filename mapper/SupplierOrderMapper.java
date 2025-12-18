package com.tricol.Tricol.mapper;

import com.tricol.Tricol.dto.request.create.SupplierOrderRequestDTO;
import com.tricol.Tricol.dto.request.update.SupplierOrderUpdateDTO;
import com.tricol.Tricol.dto.response.SupplierOrderResponseDTO;
import com.tricol.Tricol.model.SupplierOrder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierOrderMapper {

    SupplierOrderResponseDTO toDTO(SupplierOrder order);
    SupplierOrder toEntity(SupplierOrderRequestDTO dto);

    void updateEntity(SupplierOrderUpdateDTO dto, @MappingTarget SupplierOrder entity);
}

