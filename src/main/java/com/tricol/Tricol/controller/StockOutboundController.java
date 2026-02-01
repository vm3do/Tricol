package com.tricol.Tricol.controller;

import com.tricol.Tricol.dto.request.create.StockOutboundRequestDTO;
import com.tricol.Tricol.dto.request.update.StockOutboundUpdateDTO;
import com.tricol.Tricol.dto.response.StockOutboundResponseDTO;
import com.tricol.Tricol.model.StockMovement;
import com.tricol.Tricol.repository.StockMovementRepository;
import com.tricol.Tricol.service.StockOutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-outbound")
@RequiredArgsConstructor
public class StockOutboundController {

    private final StockOutboundService stockOutboundService;
    private final StockMovementRepository stockMovementRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_BON_SORTIE')")
    public ResponseEntity<List<StockOutboundResponseDTO>> getAllOutbounds() {
        return ResponseEntity.ok(stockOutboundService.getAll());
    }

    @GetMapping("/test11")
    public ResponseEntity<List<Object[]>> getAllMovementsWith() {
        return ResponseEntity.ok(stockMovementRepository.findAllWithTypeAndNombreMovement());
    }



    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_BON_SORTIE')")
    public ResponseEntity<StockOutboundResponseDTO> getOutboundById(@PathVariable Long id) {
        return ResponseEntity.ok(stockOutboundService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_BON_SORTIE')")
    public ResponseEntity<StockOutboundResponseDTO> createOutbound(@RequestBody StockOutboundRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockOutboundService.createOutbound(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CREATE_BON_SORTIE')")
    public ResponseEntity<StockOutboundResponseDTO> updateOutbound(@PathVariable Long id, @RequestBody StockOutboundUpdateDTO request) {
        return ResponseEntity.ok(stockOutboundService.updateOutbound(id, request));
    }

    @PutMapping("/{id}/validate")
    @PreAuthorize("hasAuthority('VALIDATE_BON_SORTIE')")
    public ResponseEntity<StockOutboundResponseDTO> validateOutbound(@PathVariable Long id) {
        return ResponseEntity.ok(stockOutboundService.validateOutbound(id));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('CANCEL_BON_SORTIE')")
    public ResponseEntity<StockOutboundResponseDTO> cancelOutbound(@PathVariable Long id) {
        return ResponseEntity.ok(stockOutboundService.cancelOutbound(id));
    }

    @GetMapping("/workshop/{workshop}")
    @PreAuthorize("hasAuthority('VIEW_BON_SORTIE')")
    public ResponseEntity<List<StockOutboundResponseDTO>> getOutboundsByWorkshop(@PathVariable String workshop) {
        return ResponseEntity.ok(stockOutboundService.getByWorkshop(workshop));
    }
}