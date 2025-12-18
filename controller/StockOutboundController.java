package com.tricol.Tricol.controller;

import com.tricol.Tricol.dto.request.create.StockOutboundRequestDTO;
import com.tricol.Tricol.dto.request.update.StockOutboundUpdateDTO;
import com.tricol.Tricol.dto.response.StockOutboundResponseDTO;
import com.tricol.Tricol.service.StockOutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-outbound")
@RequiredArgsConstructor
public class StockOutboundController {

    private final StockOutboundService stockOutboundService;

    @GetMapping
    public ResponseEntity<List<StockOutboundResponseDTO>> getAllOutbounds() {
        return ResponseEntity.ok(stockOutboundService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockOutboundResponseDTO> getOutboundById(@PathVariable Long id) {
        return ResponseEntity.ok(stockOutboundService.getById(id));
    }

    @PostMapping
    public ResponseEntity<StockOutboundResponseDTO> createOutbound(@RequestBody StockOutboundRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockOutboundService.createOutbound(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockOutboundResponseDTO> updateOutbound(@PathVariable Long id, @RequestBody StockOutboundUpdateDTO request) {
        return ResponseEntity.ok(stockOutboundService.updateOutbound(id, request));
    }

    @PutMapping("/{id}/validate")
    public ResponseEntity<StockOutboundResponseDTO> validateOutbound(@PathVariable Long id) {
        return ResponseEntity.ok(stockOutboundService.validateOutbound(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<StockOutboundResponseDTO> cancelOutbound(@PathVariable Long id) {
        return ResponseEntity.ok(stockOutboundService.cancelOutbound(id));
    }

    @GetMapping("/workshop/{workshop}")
    public ResponseEntity<List<StockOutboundResponseDTO>> getOutboundsByWorkshop(@PathVariable String workshop) {
        return ResponseEntity.ok(stockOutboundService.getByWorkshop(workshop));
    }
}