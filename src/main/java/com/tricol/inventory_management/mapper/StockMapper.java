package com.tricol.inventory_management.mapper;

import com.tricol.inventory_management.dto.response.StockMovementResponseDTO;
import com.tricol.inventory_management.dto.response.StockResponseDTO;
import com.tricol.inventory_management.model.Product;
import com.tricol.inventory_management.model.StockMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockMapper {

    @Mapping(source = "product.reference", target = "productReference")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "stockLot.lotNumber", target = "lotNumber")
    StockMovementResponseDTO toMovementDTO(StockMovement movement);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.reference", target = "productReference")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "currentStock", target = "currentStock")
    @Mapping(source = "stockValue", target = "stockValue")
    @Mapping(source = "product.reorderPoint", target = "reorderPoint")
    @Mapping(source = "isLowStock", target = "isLowStock")
    StockResponseDTO toStockDTO(Product product, Integer currentStock, java.math.BigDecimal stockValue, Boolean isLowStock);
}