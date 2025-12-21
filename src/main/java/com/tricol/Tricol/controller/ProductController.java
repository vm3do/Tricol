package com.tricol.Tricol.controller;

import com.tricol.Tricol.dto.request.create.ProductCreateRequestDTO;
import com.tricol.Tricol.dto.request.update.ProductUpdateRequestDTO;
import com.tricol.Tricol.dto.response.ProductResponseDTO;
import com.tricol.Tricol.dto.response.StockResponseDTO;
import com.tricol.Tricol.mapper.StockMapper;
import com.tricol.Tricol.model.Product;
import com.tricol.Tricol.service.ProductService;
import com.tricol.Tricol.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('VIEW_PRODUCT')")
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        return ResponseEntity.ok().body(productService.findAllProducts());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_PRODUCT')")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id){
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_PRODUCT')")
    public ResponseEntity<ProductResponseDTO> ProductCreate(@RequestBody ProductCreateRequestDTO request){
        ProductResponseDTO savedProduct = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_PRODUCT')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequestDTO request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @GetMapping("/{id}/stock")
    @PreAuthorize("hasAuthority('VIEW_STOCK')")
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
