package com.tricol.inventory_management.controller;

import com.tricol.inventory_management.dto.request.create.SupplierCreateRequestDTO;
import com.tricol.inventory_management.dto.request.update.SupplierUpdateRequestDTO;
import com.tricol.inventory_management.dto.response.SupplierResponseDTO;
import com.tricol.inventory_management.service.SupplierService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService){
        this.supplierService = supplierService;
    }


    @GetMapping
    public ResponseEntity<List<SupplierResponseDTO>> getAllSuppliers(){
        return ResponseEntity.ok(supplierService.findAllSuppliers());
    }

    @GetMapping("/ice/{ice}")
    public ResponseEntity<SupplierResponseDTO> getSupplierByIce(@PathVariable String ice){
        return ResponseEntity.ok(supplierService.findByIce(ice));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<SupplierResponseDTO> getSupplierById(@PathVariable Long id){
        return ResponseEntity.ok(supplierService.findById(id));
    }

    @PostMapping
    public ResponseEntity<SupplierResponseDTO> createSupplier(@RequestBody SupplierCreateRequestDTO request){
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponseDTO> updateSupplier(@PathVariable Long id , @RequestBody SupplierUpdateRequestDTO request){
        return ResponseEntity.ok(supplierService.updateSupplier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id){
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}
