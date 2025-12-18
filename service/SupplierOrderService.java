package com.tricol.Tricol.service;

import com.tricol.Tricol.dto.request.create.SupplierOrderItemRequestDTO;
import com.tricol.Tricol.dto.request.create.SupplierOrderRequestDTO;
import com.tricol.Tricol.dto.request.update.SupplierOrderUpdateDTO;
import com.tricol.Tricol.dto.response.SupplierOrderResponseDTO;
import com.tricol.Tricol.enums.OrderStatus;
import com.tricol.Tricol.exception.ResourceNotFoundException;
import com.tricol.Tricol.mapper.SupplierOrderMapper;
import com.tricol.Tricol.model.Product;
import com.tricol.Tricol.model.Supplier;
import com.tricol.Tricol.model.SupplierOrder;
import com.tricol.Tricol.model.SupplierOrderItem;
import com.tricol.Tricol.repository.ProductRepository;
import com.tricol.Tricol.repository.SupplierOrderRepository;
import com.tricol.Tricol.repository.SupplierRepository;
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
    private final StockService stockService;


    public SupplierOrderResponseDTO createOrder(SupplierOrderRequestDTO createDTO) {

        Supplier supplier = supplierRepository
                .findById(createDTO.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found with ID: " + createDTO.getSupplierId()));


        SupplierOrder order = SupplierOrder.builder()
                .supplier(supplier)
                .orderDate(createDTO.getOrderDate())
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

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

        order.calculateTotalAmount();

        SupplierOrder saved = supplierOrderRepository.save(order);

        return supplierOrderMapper.toDTO(saved);
    }



    public SupplierOrderResponseDTO getOrderById(Long id) {
        SupplierOrder order = supplierOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id " + id + " does not exist"));
        return supplierOrderMapper.toDTO(order);
    }


    public List<SupplierOrderResponseDTO> getAllOrders() {
        return supplierOrderRepository.findAll()
                .stream()
                .map(supplierOrderMapper::toDTO)
                .toList();
    }


    public SupplierOrderResponseDTO updateOrder(Long id, SupplierOrderUpdateDTO dto) {
        SupplierOrder existing = supplierOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id " + id + " does not exist"));

        if (dto.getOrderDate() != null) {
            existing.setOrderDate(dto.getOrderDate());
        }

        if (dto.getStatus() != null) {
            existing.setStatus(OrderStatus.valueOf(dto.getStatus().toUpperCase()));
        }

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {

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


    public SupplierOrderResponseDTO validateOrder(Long id) {
        SupplierOrder order = supplierOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id " + id + " does not exist"));

        if (!OrderStatus.PENDING.equals(order.getStatus())) {
            throw new IllegalStateException("Only pending orders can be validated");
        }

        order.setStatus(OrderStatus.VALIDATED);
        SupplierOrder saved = supplierOrderRepository.save(order);
        return supplierOrderMapper.toDTO(saved);
    }


    public SupplierOrderResponseDTO receiveOrder(Long id) {
        SupplierOrder order = supplierOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id " + id + " does not exist"));

        if (OrderStatus.DELIVERED.equals(order.getStatus())) {
            throw new IllegalStateException("Order already received");
        }

        if (!OrderStatus.VALIDATED.equals(order.getStatus())) {
            throw new IllegalStateException("Order is not validated");
        }

        order.setStatus(OrderStatus.DELIVERED);

        stockService.processStockEntry(order);

        SupplierOrder saved = supplierOrderRepository.save(order);
        return supplierOrderMapper.toDTO(saved);
    }


    public List<SupplierOrderResponseDTO> getOrdersBySupplier(Long supplierId) {
        return supplierOrderRepository.findBySupplierId(supplierId)
                .stream()
                .map(supplierOrderMapper::toDTO)
                .toList();
    }


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
