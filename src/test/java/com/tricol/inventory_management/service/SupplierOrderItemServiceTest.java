//package com.tricol.inventory_management.service;
//
//import com.tricol.inventory_management.dto.response.SupplierOrderItemResponseDTO;
//import com.tricol.inventory_management.mapper.SupplierOrderItemMapper;
//import com.tricol.inventory_management.model.SupplierOrderItem;
//import com.tricol.inventory_management.repository.ProductRepository;
//import com.tricol.inventory_management.repository.SupplierOrderItemRepository;
//import com.tricol.inventory_management.repository.SupplierOrderRepository;
//import org.junit.jupiter.api.Test;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class SupplierOrderItemServiceTest {
//
//    @Test
//    void getItemById_returnsDto_whenItemExists() {
//        // Arrange - create mocks
//        SupplierOrderItemRepository itemRepo = mock(SupplierOrderItemRepository.class);
//        SupplierOrderRepository orderRepo = mock(SupplierOrderRepository.class);
//        ProductRepository productRepo = mock(ProductRepository.class);
//        SupplierOrderItemMapper mapper = mock(SupplierOrderItemMapper.class);
//
//        // Create service with mocked dependencies
//        SupplierOrderItemService service = new SupplierOrderItemService(itemRepo, orderRepo, productRepo, mapper);
//
//        // Prepare a sample entity and expected DTO
//        SupplierOrderItem entity = new SupplierOrderItem();
//        entity.setId(1L);
//
//        SupplierOrderItemResponseDTO expectedDto = new SupplierOrderItemResponseDTO();
//
//        when(itemRepo.findById(1L)).thenReturn(Optional.of(entity));
//        when(mapper.toDTO(entity)).thenReturn(expectedDto);
//
//        // Act
//        SupplierOrderItemResponseDTO actual = service.getItemById(1L);
//
//        // Assert
//        assertSame(expectedDto, actual, "Service should return the same DTO instance provided by the mapper");
//        verify(itemRepo).findById(1L);
//        verify(mapper).toDTO(entity);
//    }
//}
//
