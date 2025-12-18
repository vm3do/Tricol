package com.tricol.Tricol.controller;

import com.tricol.Tricol.dto.response.*;
import com.tricol.Tricol.mapper.ProductMapper;
import com.tricol.Tricol.mapper.StockMapper;
import com.tricol.Tricol.model.Product;
import com.tricol.Tricol.model.StockMovement;
import com.tricol.Tricol.service.ProductService;
import com.tricol.Tricol.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.tricol.Tricol.enums.MovementType;


@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final ProductService productService;
    private final StockMapper stockMapper;
    private final ProductMapper productMapper;

    @GetMapping
    public ResponseEntity<StockSummaryResponseDTO> getStockSummary() {
        Map<String, Object> summary = stockService.getStockSummary();
        
        @SuppressWarnings("unchecked")
        List<Product> products = (List<Product>) summary.get("products");
        @SuppressWarnings("unchecked")
        Map<Long, Integer> stockLevels = (Map<Long, Integer>) summary.get("stockLevels");
        @SuppressWarnings("unchecked")
        Map<Long, BigDecimal> stockValues = (Map<Long, BigDecimal>) summary.get("stockValues");
        @SuppressWarnings("unchecked")
        List<Product> alerts = (List<Product>) summary.get("alerts");
        BigDecimal totalValue = (BigDecimal) summary.get("totalValue");

        List<StockResponseDTO> stocks = products.stream()
                .map(product -> {
                    Integer currentStock = stockLevels.get(product.getId());
                    BigDecimal stockValue = stockValues.get(product.getId());
                    Boolean isLowStock = product.getReorderPoint() != null && 
                                       currentStock <= product.getReorderPoint();
                    return stockMapper.toStockDTO(product, currentStock, stockValue, isLowStock);
                })
                .collect(Collectors.toList());

        List<ProductResponseDTO> alertDTOs = alerts.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());

        StockSummaryResponseDTO response = new StockSummaryResponseDTO(
                stocks,
                totalValue,
                alertDTOs,
                products.size(),
                alerts.size()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/movements/search")
    public List<StockMovement> searchMovements(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) MovementType type,
            @RequestParam(required = false) String lotNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return stockService.searchMovements(productId, reference, type, lotNumber, startDate, endDate);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<StockResponseDTO> getProductStock(@PathVariable Long productId) {
        Product product = productService.findEntityById(productId);
        Integer currentStock = stockService.getCurrentStock(productId);
        BigDecimal stockValue = stockService.getStockValuation(productId);
        Boolean isLowStock = product.getReorderPoint() != null && 
                           currentStock <= product.getReorderPoint();

        StockResponseDTO response = stockMapper.toStockDTO(product, currentStock, stockValue, isLowStock);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/movements")
    public ResponseEntity<List<StockMovementResponseDTO>> getAllMovements() {
        List<StockMovement> movements = stockService.getAllMovements();
        List<StockMovementResponseDTO> response = movements.stream()
                .map(stockMapper::toMovementDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/movements/product/{productId}")
    public ResponseEntity<List<StockMovementResponseDTO>> getProductMovements(@PathVariable Long productId) {
        List<StockMovement> movements = stockService.getMovementsByProduct(productId);
        List<StockMovementResponseDTO> response = movements.stream()
                .map(stockMapper::toMovementDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<ProductResponseDTO>> getStockAlerts() {
        List<Product> alerts = stockService.getStockAlerts();
        List<ProductResponseDTO> response = alerts.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/valuation")
    public ResponseEntity<Map<String, BigDecimal>> getTotalValuation() {
        BigDecimal totalValue = stockService.getTotalStockValuation();
        return ResponseEntity.ok(Map.of("totalValue", totalValue));
    }
}