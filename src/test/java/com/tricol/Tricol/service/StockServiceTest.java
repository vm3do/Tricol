package com.tricol.Tricol.service;

import com.tricol.Tricol.enums.OrderStatus;
import com.tricol.Tricol.model.*;
import com.tricol.Tricol.repository.ProductRepository;
import com.tricol.Tricol.repository.StockLotRepository;
import com.tricol.Tricol.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockLotRepository stockLotRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private StockService stockService;

    private Product testProduct;
    private StockLot testStockLot;
    private SupplierOrder testOrder;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .reference("PROD-001")
                .name("Test Product")
                .unitPrice(BigDecimal.valueOf(10.00))
                .reorderPoint(20)
                .build();

        testStockLot = StockLot.builder()
                .id(1L)
                .lotNumber("LOT-PROD-001-20251011-1")
                .product(testProduct)
                .initialQuantity(100)
                .remainingQuantity(100)
                .unitPrice(BigDecimal.valueOf(10.00))
                .entryDate(LocalDate.now())
                .build();

        Supplier testSupplier = Supplier.builder()
                .id(1L)
                .address("address")
                .city("oujda")
                .email("supp@supp.supp")
                .companyName("C")
                .contactPerson("contact")
                .build();

         testOrder = SupplierOrder.builder()
                .id(1L)
                .orderDate(LocalDate.now())
                .status(OrderStatus.DELIVERED)
                .supplier(testSupplier)
                .build();

        SupplierOrderItem item1 = SupplierOrderItem.builder()
                .id(1L)
                .product(testProduct)
                .unitPrice(BigDecimal.valueOf(10))
                .supplierOrder(testOrder)
                .quantity(100)
                .totalAmount(BigDecimal.valueOf(1000))
                .build();

        SupplierOrderItem item2 = SupplierOrderItem.builder()
                .id(2L)
                .product(testProduct)
                .unitPrice(BigDecimal.valueOf(10))
                .supplierOrder(testOrder)
                .quantity(100)
                .totalAmount(BigDecimal.valueOf(1000))
                .build();

        testOrder.setItems(List.of(item1, item2));

    }

    @Test
    void consumeStock_consumePartialLot_updateStock() {

        //arrange .. mock repo method li ghadi nkhdem biha f service
        when(stockLotRepository.findAvailableLotsByProductOrderByEntryDate(testProduct.getId())).thenReturn(List.of(testStockLot));

        //act
        stockService.processStockOutbound(testProduct, 70, testProduct.getReference(), "note");

        //assert
        ArgumentCaptor<StockLot> captor = ArgumentCaptor.forClass(StockLot.class);
        verify(stockLotRepository).save(captor.capture());
        assertEquals(30, captor.getValue().getRemainingQuantity());

    }

    @Test
    void consumeStock_consumeMultipleLots_updateStock() {

        //assert .. repo nmockiha
        StockLot lot1 = StockLot.builder()
                .id(1L)
                .lotNumber("LOT-001")
                .product(testProduct)
                .initialQuantity(50)
                .remainingQuantity(50)
                .unitPrice(BigDecimal.valueOf(10.00))
                .entryDate(LocalDate.now().minusDays(2))
                .build();

        StockLot lot2 = StockLot.builder()
                .id(2L)
                .lotNumber("LOT-002")
                .product(testProduct)
                .initialQuantity(80)
                .remainingQuantity(80)
                .unitPrice(BigDecimal.valueOf(12.00))
                .entryDate(LocalDate.now().minusDays(1))
                .build();

        when(stockLotRepository.findAvailableLotsByProductOrderByEntryDate(testProduct.getId())).thenReturn(List.of(lot1, lot2));

        //act
        stockService.processStockOutbound(testProduct, 70, testProduct.getReference(), "note");

        //assert
        ArgumentCaptor<StockLot> captor = ArgumentCaptor.forClass(StockLot.class);
        verify(stockLotRepository, times(2)).save(captor.capture());

        List<StockLot> savedLots = captor.getAllValues();

        assertEquals(0, savedLots.get(0).getRemainingQuantity());
        assertEquals(60, savedLots.get(1).getRemainingQuantity());
    }

    @Test
    void consumeStock_insufficientStock_throwException() {
        when(stockLotRepository.findAvailableLotsByProductOrderByEntryDate(testProduct.getId()))
                .thenReturn(List.of(testStockLot));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            stockService.processStockOutbound(testProduct, 150, testProduct.getReference(), "note");
        });

        assertEquals("Insufficient stock for product: PROD-001", exception.getMessage());
    }

    @Test
    void consumeStock_exactStockConsumption_deleteCompletely() {
        when(stockLotRepository.findAvailableLotsByProductOrderByEntryDate(testProduct.getId()))
                .thenReturn(List.of(testStockLot));

        stockService.processStockOutbound(testProduct, 100, testProduct.getReference(), "note");

        ArgumentCaptor<StockLot> captor = ArgumentCaptor.forClass(StockLot.class);
        verify(stockLotRepository).save(captor.capture());
        assertEquals(0, captor.getValue().getRemainingQuantity());
    }

    @Test
    void receiveSupplierOrder_createStockLot_returnStockLot() {
        //arr
        when(stockLotRepository.existsByLotNumber(anyString())).thenReturn(false);

        //act
        stockService.processStockEntry(testOrder);

        //assert
        ArgumentCaptor<StockLot> captor = ArgumentCaptor.forClass(StockLot.class);
        verify(stockLotRepository, times(2)).save(captor.capture());

        List<StockLot> savedLots = captor.getAllValues();
        assertEquals(2, savedLots.size());
        assertEquals(100, savedLots.get(0).getInitialQuantity());
        assertEquals(100, savedLots.get(0).getRemainingQuantity());
        assertTrue(savedLots.get(0).getLotNumber().startsWith("LOT-PROD-001"));
    }

    @Test
    void getTotalStockValuation_multipleLots_returnsCorrectSum() {
        StockLot lot1 = StockLot.builder()
                .id(1L)
                .lotNumber("LOT-001")
                .product(testProduct)
                .initialQuantity(50)
                .remainingQuantity(50)
                .unitPrice(BigDecimal.valueOf(10.00))
                .entryDate(LocalDate.now().minusDays(2))
                .build();

        StockLot lot2 = StockLot.builder()
                .id(2L)
                .lotNumber("LOT-002")
                .product(testProduct)
                .initialQuantity(80)
                .remainingQuantity(80)
                .unitPrice(BigDecimal.valueOf(12.00))
                .entryDate(LocalDate.now().minusDays(1))
                .build();

        when(stockLotRepository.findAll()).thenReturn(List.of(lot1, lot2));

        //act
        BigDecimal totalStockValuation = stockService.getTotalStockValuation();

        //assert
        BigDecimal expectedValue = BigDecimal.valueOf(50 * 10.00 + 80 * 12.00);
        assertEquals( expectedValue , totalStockValuation);
    }

}