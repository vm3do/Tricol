package com.tricol.inventory_management.service;

import com.tricol.inventory_management.dto.request.create.SupplierOrderItemRequestDTO;
import com.tricol.inventory_management.dto.request.create.SupplierOrderRequestDTO;
import com.tricol.inventory_management.dto.request.update.SupplierOrderItemUpdateDTO;
import com.tricol.inventory_management.dto.request.update.SupplierOrderUpdateDTO;
import com.tricol.inventory_management.dto.response.SupplierOrderResponseDTO;
import com.tricol.inventory_management.enums.OrderStatus;
import com.tricol.inventory_management.exception.ResourceNotFoundException;
import com.tricol.inventory_management.mapper.SupplierOrderMapper;
import com.tricol.inventory_management.model.Product;
import com.tricol.inventory_management.model.Supplier;
import com.tricol.inventory_management.model.SupplierOrder;
import com.tricol.inventory_management.model.SupplierOrderItem;
import com.tricol.inventory_management.repository.ProductRepository;
import com.tricol.inventory_management.repository.SupplierOrderRepository;
import com.tricol.inventory_management.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierOrderService {
    private final SupplierRepository supplierRepository;
    private final SupplierOrderRepository supplierOrderRepository;
    private final ProductRepository productRepository;
    private final SupplierOrderMapper supplierOrderMapper;

    public SupplierOrderResponseDTO createOrder(SupplierOrderRequestDTO createDTO) {

        Supplier supplier = supplierRepository
                .findById(createDTO.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found with ID: " + createDTO.getSupplierId()));

        SupplierOrder order = supplierOrderMapper.toEntity(createDTO);
        order.setSupplier(supplier);

        // Build order items
        for (SupplierOrderItemRequestDTO itemDTO : createDTO.getItems()) {
            Product product = productRepository
                    .findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with ID: " + itemDTO.getProductId()));

            SupplierOrderItem item = SupplierOrderItem.builder()
                    .product(product)
                    .supplierOrder(order)
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(itemDTO.getUnitPrice())
                    .build();

            item.calculateTotalAmount();
            order.addItem(item);
        }

        order.setStatus(OrderStatus.PENDING);

        order.calculateTotalAmount();

        SupplierOrder saved = supplierOrderRepository.save(order);

        return supplierOrderMapper.toDTO(saved);
    }



    @Transactional(Transactional.TxType.SUPPORTS)
    public SupplierOrderResponseDTO getOrderById(Long id) {
        SupplierOrder order = supplierOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id " + id + " does not exist"));
        return supplierOrderMapper.toDTO(order);
    }


    @Transactional(Transactional.TxType.SUPPORTS)
    public List<SupplierOrderResponseDTO> getAllOrders() {
        return supplierOrderRepository.findAll()
                .stream()
                .map(supplierOrderMapper::toDTO)
                .toList();
    }


//    public SupplierOrderResponseDTO updateOrder(Long id, SupplierOrderUpdateDTO dto) {
//        SupplierOrder existing = supplierOrderRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Order with id " + id + " does not exist"));
//
//        supplierOrderMapper.updateEntity(dto, existing);
//
//        SupplierOrder saved = supplierOrderRepository.save(existing);
//        return supplierOrderMapper.toDTO(saved);
//    }
public SupplierOrderResponseDTO updateOrder(Long id, SupplierOrderUpdateDTO dto) {
    SupplierOrder existing = supplierOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order with id " + id + " does not exist"));

    // Update basic fields only if provided (non-null)
    if (dto.getOrderDate() != null) {
        existing.setOrderDate(dto.getOrderDate());
    }

    if (dto.getStatus() != null) {
        existing.setStatus(OrderStatus.valueOf(dto.getStatus().toUpperCase()));
    }

    // Update items if provided
    if (dto.getItems() != null && !dto.getItems().isEmpty()) {
        // Clear existing items and add updated ones
        existing.getItems().clear();

        for (SupplierOrderItemRequestDTO itemDTO : dto.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemDTO.getProductId()));
            SupplierOrderItem item = SupplierOrderItem
                    .builder()
                    .supplierOrder(existing)
                    .unitPrice(itemDTO.getUnitPrice())
                    .quantity(itemDTO.getQuantity())
                    .product(product)
                    .build();
            item.calculateTotalAmount();
            existing.addItem(item);
        }

        // Recalculate total amount after items update
        existing.calculateTotalAmount();
    }

    SupplierOrder saved = supplierOrderRepository.save(existing);
    return supplierOrderMapper.toDTO(saved);
}



    public void deleteOrder(Long id) {
        SupplierOrder existing = supplierOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id " + id + " does not exist"));
        supplierOrderRepository.delete(existing);
    }


    public SupplierOrderResponseDTO receiveOrder(Long id) {
        SupplierOrder order = supplierOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id " + id + " does not exist"));

        if (OrderStatus.DELIVERED.equals(order.getStatus())) {
            throw new IllegalStateException("Order already received");
        }

        order.setStatus(OrderStatus.DELIVERED);

        // TODO: integrate stock creation / FIFO logic here

        SupplierOrder saved = supplierOrderRepository.save(order);
        return supplierOrderMapper.toDTO(saved);
    }


    @Transactional(Transactional.TxType.SUPPORTS)
    public List<SupplierOrderResponseDTO> getOrdersBySupplier(Long supplierId) {
        return supplierOrderRepository.findBySupplierId(supplierId)
                .stream()
                .map(supplierOrderMapper::toDTO)
                .toList();
    }


    @Transactional(Transactional.TxType.SUPPORTS)
    public List<SupplierOrderResponseDTO> getOrdersByStatus(OrderStatus status) {
        return supplierOrderRepository.findByStatus(status)
                .stream()
                .map(supplierOrderMapper::toDTO)
                .toList();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<SupplierOrderResponseDTO> getOrdersByDateRange(LocalDate start, LocalDate end) {
        return supplierOrderRepository.findByOrderDateBetween(start, end)
                .stream()
                .map(supplierOrderMapper::toDTO)
                .toList();
    }
}
