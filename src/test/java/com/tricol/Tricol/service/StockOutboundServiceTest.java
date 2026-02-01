package com.tricol.Tricol.service;

import com.tricol.Tricol.enums.OutboundReason;
import com.tricol.Tricol.enums.OutboundStatus;
import com.tricol.Tricol.mapper.StockOutboundMapper;
import com.tricol.Tricol.model.Product;
import com.tricol.Tricol.model.StockOutbound;
import com.tricol.Tricol.model.StockOutboundItem;
import com.tricol.Tricol.repository.ProductRepository;
import com.tricol.Tricol.repository.StockOutboundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StockOutboundServiceTest {

    @Mock
    private StockOutboundRepository stockOutboundRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockOutboundMapper stockOutboundMapper;

    @Mock
    private StockService stockService;

    @InjectMocks
    private StockOutboundService stockOutboundService;

    private Product testProduct;
    private StockOutbound testOutbound;
    private StockOutboundItem testItem;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .reference("PROD-001")
                .name("Test Product")
                .unitPrice(BigDecimal.valueOf(10.00))
                .reorderPoint(20)
                .build();

        testItem = StockOutboundItem.builder()
                .id(1L)
                .product(testProduct)
                .quantity(50)
                .notes("Test item")
                .build();

        testOutbound = StockOutbound.builder()
                .id(1L)
                .reference("OUT-20251012-001")
                .reason(OutboundReason.PRODUCTION)
                .status(OutboundStatus.DRAFT)
                .workshop("Workshop A")
                .notes("Test outbound")
                .items(List.of(testItem))
                .build();
    }

    @Test
    void validateOutbound_draftToValidated_triggersStockConsumption() {
        // arrange
        when(stockOutboundRepository.findById(1L)).thenReturn(Optional.of(testOutbound));
        when(stockService.getCurrentStock(testProduct.getId())).thenReturn(100); // sufficient stock
        when(stockOutboundRepository.save(any(StockOutbound.class))).thenReturn(testOutbound);

        // act
        stockOutboundService.validateOutbound(1L);

        ArgumentCaptor<StockOutbound> captor = ArgumentCaptor.forClass(StockOutbound.class);
        verify(stockOutboundRepository).save(captor.capture());
        assertEquals(OutboundStatus.VALIDATED, captor.getValue().getStatus());

        verify(stockService).processStockOutbound(
                eq(testProduct),
                eq(50),
                eq("Stock outbound #1"),
                eq("PRODUCTION")
        );
    }

    @Test
    void validateOutbound_multipleItems_processesAllItems() {
        // arr
        Product product2 = Product.builder()
                .id(2L)
                .reference("PROD-002")
                .name("Test Product 2")
                .build();

        StockOutboundItem item2 = StockOutboundItem.builder()
                .id(2L)
                .product(product2)
                .quantity(30)
                .build();

        testOutbound.setItems(List.of(testItem, item2));

        when(stockOutboundRepository.findById(1L)).thenReturn(Optional.of(testOutbound));
        when(stockService.getCurrentStock(testProduct.getId())).thenReturn(100);
        when(stockService.getCurrentStock(product2.getId())).thenReturn(50);
        when(stockOutboundRepository.save(any(StockOutbound.class))).thenReturn(testOutbound);

        // act
        stockOutboundService.validateOutbound(1L);

        // assert
        verify(stockService).processStockOutbound(
                eq(testProduct), eq(50), anyString(), anyString());
        verify(stockService).processStockOutbound(
                eq(product2), eq(30), anyString(), anyString());
    }

    @Test
    void validateOutbound_alreadyValidated_throwsException() {
        testOutbound.setStatus(OutboundStatus.VALIDATED);
        when(stockOutboundRepository.findById(1L)).thenReturn(Optional.of(testOutbound));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            stockOutboundService.validateOutbound(1L);
        });

        assertEquals("Only draft outbounds can be validated", exception.getMessage());
        verify(stockService, never()).processStockOutbound(any(), anyInt(), anyString(), anyString());
    }

    @Test
    void validateOutbound_insufficientStock_throwsException() {
        when(stockOutboundRepository.findById(1L)).thenReturn(Optional.of(testOutbound));
        when(stockService.getCurrentStock(testProduct.getId())).thenReturn(20); // less than required 50

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            stockOutboundService.validateOutbound(1L);
        });

        assertTrue(exception.getMessage().contains("Insufficient stock for product: PROD-001"));
        verify(stockService, never()).processStockOutbound(any(), anyInt(), anyString(), anyString());
    }

}