package com.tricol.inventory_management.service;

import com.tricol.inventory_management.dto.request.create.SupplierCreateRequestDTO;
import com.tricol.inventory_management.dto.request.create.SupplierOrderItemRequestDTO;
import com.tricol.inventory_management.dto.request.update.SupplierOrderItemUpdateDTO;
import com.tricol.inventory_management.dto.response.SupplierOrderItemResponseDTO;
import com.tricol.inventory_management.exception.ResourceNotFoundException;
import com.tricol.inventory_management.mapper.SupplierOrderItemMapper;
import com.tricol.inventory_management.model.SupplierOrder;
import com.tricol.inventory_management.model.SupplierOrderItem;
import com.tricol.inventory_management.repository.ProductRepository;
import com.tricol.inventory_management.repository.SupplierOrderItemRepository;
import com.tricol.inventory_management.repository.SupplierOrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierOrderItemService {
    private final SupplierOrderItemRepository supplierOrderItemRepository;
    private final SupplierOrderRepository supplierOrderRepository;
    private final ProductRepository productRepository;
    private final SupplierOrderItemMapper supplierOrderItemMapper;

    public SupplierOrderItemResponseDTO createItem(Long orderId, SupplierOrderItemRequestDTO dto) {
        SupplierOrder order = supplierOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        SupplierOrderItem item = supplierOrderItemMapper.toEntity(dto);
        item.setSupplierOrder(order);

        SupplierOrderItem saved = supplierOrderItemRepository.save(item);
        return supplierOrderItemMapper.toDTO(saved);
    }


    public SupplierOrderItemResponseDTO updateItem(Long id, SupplierOrderItemUpdateDTO dto) {
        SupplierOrderItem existing = supplierOrderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item with id " + id + " not found"));

        supplierOrderItemMapper.updateEntity(dto, existing);
        SupplierOrderItem saved = supplierOrderItemRepository.save(existing);
        return supplierOrderItemMapper.toDTO(saved);
    }


    public void deleteItem(Long id) {
        SupplierOrderItem existing = supplierOrderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item with id " + id + " not found"));
        supplierOrderItemRepository.delete(existing);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public SupplierOrderItemResponseDTO getItemById(Long id) {
        SupplierOrderItem item = supplierOrderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item with id " + id + " not found"));
        return supplierOrderItemMapper.toDTO(item);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<SupplierOrderItemResponseDTO> getItemsByOrder(Long orderId) {
        if (!supplierOrderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Order with id " + orderId + " not found");
        }

        return supplierOrderItemRepository.findBySupplierOrderId(orderId)
                .stream()
                .map(supplierOrderItemMapper::toDTO)
                .toList();
    }


}
