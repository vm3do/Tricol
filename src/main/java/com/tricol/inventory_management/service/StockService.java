package com.tricol.inventory_management.service;

import com.tricol.inventory_management.enums.MovementType;
import com.tricol.inventory_management.model.*;
import com.tricol.inventory_management.repository.StockLotRepository;
import com.tricol.inventory_management.repository.StockMovementRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StockService {

    private final StockLotRepository stockLotRepository;
    private final StockMovementRepository stockMovementRepository;


    public void processStockEntry(SupplierOrder supplierOrder) {
        log.info("Processing stock entry for order ID: {}", supplierOrder.getId());

        for (SupplierOrderItem item : supplierOrder.getItems()) {

            String lotNumber = generateLotNumber(item.getProduct(), supplierOrder);

            StockLot stockLot = StockLot.builder()
                    .lotNumber(lotNumber)
                    .product(item.getProduct())
                    .supplierOrder(supplierOrder)
                    .initialQuantity(item.getQuantity())
                    .remainingQuantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .entryDate(supplierOrder.getOrderDate())
                    .build();

            stockLotRepository.save(stockLot);
            log.info("Created stock lot: {} for product: {} with quantity: {}",
                    lotNumber, item.getProduct().getReference(), item.getQuantity());

            StockMovement movement = StockMovement.builder()
                    .product(item.getProduct())
                    .stockLot(stockLot)
                    .movementType(MovementType.ENTREE)
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .supplierOrder(supplierOrder)
                    .reference("Réception commande #" + supplierOrder.getId())
                    .notes("Entrée de stock - Lot: " + lotNumber)
                    .build();

            stockMovementRepository.save(movement);
        }

        log.info("Stock entry processed successfully for order ID: {}", supplierOrder.getId());
    }

    private String generateLotNumber(Product product, SupplierOrder supplierOrder) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseNumber = String.format("LOT-%s-%s-%d",
                product.getReference(),
                date,
                supplierOrder.getId());

        String lotNumber = baseNumber;
        int counter = 1;
        while (stockLotRepository.existsByLotNumber(lotNumber)) {
            lotNumber = baseNumber + "-" + counter;
            counter++;
        }

        return lotNumber;
    }

    public int getAvailableStock(Product product) {
        List<StockLot> lots = stockLotRepository.findByProduct(product);
        return lots.stream()
                .mapToInt(StockLot::getRemainingQuantity)
                .sum();
    }

    public List<StockLot> getAvailableLotsFIFO(Long productId) {
        return stockLotRepository.findAvailableLotsByProductOrderByEntryDate(productId);
    }

    public List<StockMovement> getProductMovements(Product product) {
        return stockMovementRepository.findByProductOrderByMovementDateDesc(product);
    }

    public List<StockMovement> getAllMovements() {
        return stockMovementRepository.findAllByOrderByMovementDateDesc();
    }
}


