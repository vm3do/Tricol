package com.tricol.Tricol.mapper;

import com.tricol.Tricol.dto.request.update.StockOutboundUpdateDTO;
import com.tricol.Tricol.dto.response.StockOutboundItemResponseDTO;
import com.tricol.Tricol.dto.response.StockOutboundResponseDTO;
import com.tricol.Tricol.model.StockOutbound;
import com.tricol.Tricol.model.StockOutboundItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StockOutboundMapper {

    StockOutboundResponseDTO toDTO(StockOutbound stockOutbound);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.reference", target = "productReference")
    @Mapping(source = "product.name", target = "productName")
    StockOutboundItemResponseDTO toItemDTO(StockOutboundItem item);

    void updateEntity(StockOutboundUpdateDTO dto, @MappingTarget StockOutbound entity);
}