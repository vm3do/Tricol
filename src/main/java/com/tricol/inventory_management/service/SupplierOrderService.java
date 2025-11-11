package com.tricol.inventory_management.service;

import com.tricol.inventory_management.dto.request.create.SupplierOrderRequestDTO;
import com.tricol.inventory_management.dto.request.update.SupplierOrderUpdateDTO;
import com.tricol.inventory_management.dto.response.SupplierOrderResponseDTO;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierOrderService {
    private final SupplierRepository supplierRepository;
    private final SupplierOrderRepository supplierOrderRepository;
    private final ProductRepository productRepository;
    private final SupplierOrderMapper supplierOrderMapper;

    public SupplierOrderResponseDTO createOrder(SupplierOrderRequestDTO requestDTO) {
        // fetch supplier
        Supplier supplier = supplierRepository.findById(requestDTO.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        // map DTO items to entities
        List<SupplierOrderItem> items = requestDTO.getItems().stream()
                .map(itemDTO -> {
                    Product product = productRepository.findById(itemDTO.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemDTO.getProductId()));

                    SupplierOrderItem item = SupplierOrderItem.builder()
                            .product(product)
                            .quantity(itemDTO.getQuantity())
                            .unitPrice(itemDTO.getUnitPrice())
                            .build();
                    return item;
                })
                .toList();

        // calculate total amount
        BigDecimal totalAmount = items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // create order
        SupplierOrder order = SupplierOrder.builder()
                .supplier(supplier)
                .items(items)
                .orderDate(requestDTO.getOrderDate())
                .status("PENDING") // or your default status
                .totalAmount(totalAmount)
                .build();

        // link items back to order
        items.forEach(i -> i.setSupplierOrder(order));

        // save order (cascade saves items)
        SupplierOrder savedOrder = supplierOrderRepository.save(order);

        return supplierOrderMapper.toDTO(savedOrder); // map to response DTO
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


    public SupplierOrderResponseDTO updateOrder(Long id, SupplierOrderUpdateDTO dto) {
        SupplierOrder existing = supplierOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id " + id + " does not exist"));

        supplierOrderMapper.updateEntity(dto, existing);

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

        if ("RECEIVED".equals(order.getStatus())) {
            throw new IllegalStateException("Order already received");
        }

        order.setStatus("RECEIVED");

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
    public List<SupplierOrderResponseDTO> getOrdersByStatus(String status) {
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
