package com.tricol.Tricol.controller;

import com.tricol.Tricol.dto.request.create.SupplierOrderRequestDTO;
import com.tricol.Tricol.dto.request.update.SupplierOrderUpdateDTO;
import com.tricol.Tricol.dto.response.SupplierOrderResponseDTO;
import com.tricol.Tricol.service.SupplierOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class SupplierOrderController {

    private final SupplierOrderService supplierOrderService;

    public SupplierOrderController(SupplierOrderService supplierOrderService) {
        this.supplierOrderService = supplierOrderService;
    }

    @GetMapping
    public ResponseEntity<List<SupplierOrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(supplierOrderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierOrderResponseDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierOrderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<SupplierOrderResponseDTO> createOrder(@RequestBody SupplierOrderRequestDTO request){
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierOrderService.createOrder(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierOrderResponseDTO> updateOrder(@PathVariable Long id,@Valid @RequestBody SupplierOrderUpdateDTO request){
        return ResponseEntity.ok(supplierOrderService.updateOrder(id, request));
    }

    @PutMapping("/{id}/validate")
    public ResponseEntity<SupplierOrderResponseDTO> validateOrder(@PathVariable Long id) {
        return ResponseEntity.ok(supplierOrderService.validateOrder(id));
    }

    @PutMapping("/{id}/receive")
    public ResponseEntity<SupplierOrderResponseDTO> receiveOrder(@PathVariable Long id) {
        return ResponseEntity.ok(supplierOrderService.receiveOrder(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id){
        supplierOrderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<SupplierOrderResponseDTO>> getOrdersBySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(supplierOrderService.getOrdersBySupplier(supplierId));
    }
}
