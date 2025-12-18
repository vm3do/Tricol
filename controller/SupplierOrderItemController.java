package com.tricol.Tricol.controller;

import com.tricol.Tricol.dto.request.create.SupplierOrderItemRequestDTO;
import com.tricol.Tricol.dto.request.update.SupplierOrderItemUpdateDTO;
import com.tricol.Tricol.dto.response.SupplierOrderItemResponseDTO;
import com.tricol.Tricol.service.SupplierOrderItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SupplierOrderItemController {
    private final SupplierOrderItemService service;

    public SupplierOrderItemController(SupplierOrderItemService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/supplier-order-items/{id}")
    public ResponseEntity<SupplierOrderItemResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getItemById(id));
    }

    @GetMapping("/api/v1/supplier-orders/{orderId}/items")
    public ResponseEntity<List<SupplierOrderItemResponseDTO>> getItemsByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(service.getItemsByOrder(orderId));
    }

    @PostMapping("/api/v1/supplier-orders/{orderId}/items")
    public ResponseEntity<SupplierOrderItemResponseDTO> create(@PathVariable Long orderId, @Valid @RequestBody SupplierOrderItemRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createItem(orderId, request));
    }

    @PutMapping("/api/v1/supplier-order-items/{id}")
    public ResponseEntity<SupplierOrderItemResponseDTO> update(@PathVariable Long id, @Valid @RequestBody SupplierOrderItemUpdateDTO request) {
        return ResponseEntity.ok(service.updateItem(id, request));
    }

    @DeleteMapping("/api/v1/supplier-order-items/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

}
