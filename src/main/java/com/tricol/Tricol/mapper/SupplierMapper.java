package com.tricol.Tricol.mapper;

import com.tricol.Tricol.dto.request.create.SupplierCreateRequestDTO;
import com.tricol.Tricol.dto.request.update.SupplierUpdateRequestDTO;
import com.tricol.Tricol.dto.response.SupplierResponseDTO;
import com.tricol.Tricol.model.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    SupplierResponseDTO toDTO(Supplier supplier);

    Supplier toEntity(SupplierCreateRequestDTO dto);

    void updateEntity(SupplierUpdateRequestDTO dto, @MappingTarget Supplier entity);
}

