package com.tricol.Tricol.mapper;

import com.tricol.Tricol.dto.request.create.ProductCreateRequestDTO;
import com.tricol.Tricol.dto.response.ProductResponseDTO;
import com.tricol.Tricol.model.Product;
import com.tricol.Tricol.dto.request.update.ProductUpdateRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    //response -> entity to dto
    ProductResponseDTO toDTO(Product product);

    //request -> dto to entity
    Product toEntity(ProductCreateRequestDTO dto);

    void updateEntity(ProductUpdateRequestDTO dto, @MappingTarget Product entity);
}
