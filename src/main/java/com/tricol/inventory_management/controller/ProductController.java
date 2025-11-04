package com.tricol.inventory_management.controller;

import com.tricol.inventory_management.dto.request.create.ProductCreateRequestDTO;
import com.tricol.inventory_management.dto.request.update.ProductUpdateRequestDTO;
import com.tricol.inventory_management.dto.response.ProductResponseDTO;
import com.tricol.inventory_management.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

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

}
