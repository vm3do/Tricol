package com.tricol.Tricol.service;

import com.tricol.Tricol.enums.MovementType;
import com.tricol.Tricol.exception.ResourceNotFoundException;
import com.tricol.Tricol.model.*;
import com.tricol.Tricol.repository.ProductRepository;
import com.tricol.Tricol.repository.StockLotRepository;
import com.tricol.Tricol.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import com.tricol.Tricol.specification.StockMovementSpecifications;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StockService {

    private final StockLotRepository stockLotRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;


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

    public List<StockMovement> getAllMovements() {
        return stockMovementRepository.findAllByOrderByMovementDateDesc();
    }

    public void processStockOutbound(Product product, Integer quantity, String reference, String notes) {
        log.info("Processing FIFO stock outbound for product: {} with quantity: {}", product.getReference(), quantity);
        
        List<StockLot> availableLots = stockLotRepository.findAvailableLotsByProductOrderByEntryDate(product.getId());
        
        int remainingQuantity = quantity;
        for (StockLot lot : availableLots) {
            if (remainingQuantity <= 0) break;
            
            int quantityToTake = Math.min(remainingQuantity, lot.getRemainingQuantity());
            
            lot.setRemainingQuantity(lot.getRemainingQuantity() - quantityToTake);
            stockLotRepository.save(lot);
            
            StockMovement movement = StockMovement.builder()
                    .product(product)
                    .stockLot(lot)
                    .movementType(MovementType.SORTIE)
                    .quantity(quantityToTake)
                    .unitPrice(lot.getUnitPrice())
                    .reference(reference)
                    .notes(notes + " - Lot: " + lot.getLotNumber())
                    .build();
            
            stockMovementRepository.save(movement);
            remainingQuantity -= quantityToTake;
        }
        
        if (remainingQuantity > 0) {
            throw new IllegalStateException("Insufficient stock for product: " + product.getReference());
        }
    }

    @Transactional(readOnly = true)
    public Integer getCurrentStock(Long productId) {
        List<StockLot> lots = stockLotRepository.findAvailableLotsByProductOrderByEntryDate(productId);
        return lots.stream().mapToInt(StockLot::getRemainingQuantity).sum();
    }

    @Transactional(readOnly = true)
    public BigDecimal getStockValuation(Long productId) {
        List<StockLot> lots = stockLotRepository.findAvailableLotsByProductOrderByEntryDate(productId);
        return lots.stream()
                .map(lot -> lot.getUnitPrice().multiply(BigDecimal.valueOf(lot.getRemainingQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalStockValuation() {
        List<StockLot> allLots = stockLotRepository.findAll();
        return allLots.stream()
                .filter(lot -> lot.getRemainingQuantity() > 0)
                .map(lot -> lot.getUnitPrice().multiply(BigDecimal.valueOf(lot.getRemainingQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public List<Product> getStockAlerts() {
        return productRepository.findAll().stream()
                .filter(product -> {
                    if (product.getReorderPoint() == null) return false;
                    Integer currentStock = getCurrentStock(product.getId());
                    return currentStock <= product.getReorderPoint();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StockMovement> getMovementsByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return stockMovementRepository.findByProductOrderByMovementDateDesc(product);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStockSummary() {
        List<Product> allProducts = productRepository.findAll();
        
        Map<Long, Integer> stockLevels = allProducts.stream()
                .collect(Collectors.toMap(
                        Product::getId,
                        product -> getCurrentStock(product.getId())
                ));
        
        Map<Long, BigDecimal> stockValues = allProducts.stream()
                .collect(Collectors.toMap(
                        Product::getId,
                        product -> getStockValuation(product.getId())
                ));
        
        return Map.of(
                "products", allProducts,
                "stockLevels", stockLevels,
                "stockValues", stockValues,
                "totalValue", getTotalStockValuation(),
                "alerts", getStockAlerts()
        );
    }

    @Transactional(readOnly = true)
    public List<StockMovement> searchMovements(Long productId,
                                               String reference,
                                               MovementType type,
                                               String lotNumber,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate) {

        Specification<StockMovement> spec = StockMovementSpecifications.hasProductId(productId)
                .and(StockMovementSpecifications.hasProductReference(reference))
                .and(StockMovementSpecifications.hasMovementType(type))
                .and(StockMovementSpecifications.hasLotNumber(lotNumber))
                .and(StockMovementSpecifications.movementDateBetween(startDate, endDate));

        return stockMovementRepository.findAll(spec);
    }

}


