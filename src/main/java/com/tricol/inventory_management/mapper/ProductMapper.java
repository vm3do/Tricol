package com.tricol.inventory_management.mapper;

import com.tricol.inventory_management.dto.request.create.ProductCreateRequestDTO;
import com.tricol.inventory_management.dto.response.ProductResponseDTO;
import com.tricol.inventory_management.model.Product;
import com.tricol.inventory_management.dto.request.update.ProductUpdateRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    //reponse -> entity to dto
    ProductResponseDTO toDTO(Product product);

    //request -> dto to entity
    Product toEntity(ProductCreateRequestDTO dto);

    void updateEntity(ProductUpdateRequestDTO dto, @MappingTarget Product entity);
}
