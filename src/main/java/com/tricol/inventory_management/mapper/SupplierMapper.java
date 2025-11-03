package com.tricol.inventory_management.mapper;

import com.tricol.inventory_management.dto.request.create.SupplierCreateRequestDTO;
import com.tricol.inventory_management.dto.request.update.SupplierUpdateRequestDTO;
import com.tricol.inventory_management.dto.response.SupplierResponseDTO;
import com.tricol.inventory_management.model.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    SupplierResponseDTO toDTO(Supplier supplier);

    Supplier toEntity(SupplierCreateRequestDTO dto);

    void updateEntity(SupplierUpdateRequestDTO dto, @MappingTarget Supplier entity);
}

