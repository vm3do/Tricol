package com.tricol.inventory_management.controller;

import com.tricol.inventory_management.dto.request.create.ProductCreateRequestDTO;
import com.tricol.inventory_management.dto.request.update.ProductUpdateRequestDTO;
import com.tricol.inventory_management.dto.response.ProductResponseDTO;
import com.tricol.inventory_management.dto.response.StockResponseDTO;
import com.tricol.inventory_management.mapper.StockMapper;
import com.tricol.inventory_management.model.Product;
import com.tricol.inventory_management.service.ProductService;
import com.tricol.inventory_management.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final StockService stockService;
    private final StockMapper stockMapper;

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        return ResponseEntity.ok().body(productService.findAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id){
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> ProductCreate(@RequestBody ProductCreateRequestDTO request){
        ProductResponseDTO savedProduct = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequestDTO request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<StockResponseDTO> getProductStock(@PathVariable Long id) {
        Product product = productService.findEntityById(id);
        Integer currentStock = stockService.getCurrentStock(id);
        BigDecimal stockValue = stockService.getStockValuation(id);
        Boolean isLowStock = product.getReorderPoint() != null && 
                           currentStock <= product.getReorderPoint();

        StockResponseDTO response = stockMapper.toStockDTO(product, currentStock, stockValue, isLowStock);
        return ResponseEntity.ok(response);
    }
}
