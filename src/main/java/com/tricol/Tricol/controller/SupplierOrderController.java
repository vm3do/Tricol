package com.tricol.Tricol.controller;

import com.tricol.Tricol.dto.request.create.SupplierOrderRequestDTO;
import com.tricol.Tricol.dto.request.update.SupplierOrderUpdateDTO;
import com.tricol.Tricol.dto.response.SupplierOrderResponseDTO;
import com.tricol.Tricol.service.SupplierOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('VIEW_ORDER')")
    public ResponseEntity<List<SupplierOrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(supplierOrderService.getAllOrders());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_ORDER')")
    public ResponseEntity<SupplierOrderResponseDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierOrderService.getOrderById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_ORDER')")
    public ResponseEntity<SupplierOrderResponseDTO> createOrder(@RequestBody SupplierOrderRequestDTO request){
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierOrderService.createOrder(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_ORDER')")
    public ResponseEntity<SupplierOrderResponseDTO> updateOrder(@PathVariable Long id,@Valid @RequestBody SupplierOrderUpdateDTO request){
        return ResponseEntity.ok(supplierOrderService.updateOrder(id, request));
    }

    @PutMapping("/{id}/validate")
    @PreAuthorize("hasAuthority('VALIDATE_ORDER')")
    public ResponseEntity<SupplierOrderResponseDTO> validateOrder(@PathVariable Long id) {
        return ResponseEntity.ok(supplierOrderService.validateOrder(id));
    }

    @PutMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('RECEIVE_ORDER')")
    public ResponseEntity<SupplierOrderResponseDTO> receiveOrder(@PathVariable Long id) {
        return ResponseEntity.ok(supplierOrderService.receiveOrder(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CANCEL_ORDER')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id){
        supplierOrderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/supplier/{supplierId}")
    @PreAuthorize("hasAuthority('VIEW_ORDER')")
    public ResponseEntity<List<SupplierOrderResponseDTO>> getOrdersBySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(supplierOrderService.getOrdersBySupplier(supplierId));
    }
}
