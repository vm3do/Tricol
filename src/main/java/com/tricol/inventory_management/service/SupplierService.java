package com.tricol.inventory_management.service;

import com.tricol.inventory_management.dto.request.create.SupplierCreateRequestDTO;
import com.tricol.inventory_management.dto.request.update.SupplierUpdateRequestDTO;
import com.tricol.inventory_management.dto.response.SupplierResponseDTO;
import com.tricol.inventory_management.exception.DuplicateResourceException;
import com.tricol.inventory_management.exception.ResourceNotFoundException;
import com.tricol.inventory_management.mapper.SupplierMapper;
import com.tricol.inventory_management.model.Supplier;
import com.tricol.inventory_management.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public SupplierResponseDTO createSupplier(SupplierCreateRequestDTO dto) {
        if (supplierRepository.findByIce(dto.getIce()).isPresent()) {
            throw new DuplicateResourceException("Supplier with ICE " + dto.getIce() + " already exists");
        }

        Supplier supplier = supplierMapper.toEntity(dto);
        try {
            Supplier saved = supplierRepository.save(supplier);
            return supplierMapper.toDTO(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Supplier with ICE " + dto.getIce() + " already exists", e);
        }
    }

    @Transactional(readOnly = true)
    public SupplierResponseDTO findById(Long id) {
        Supplier s = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with id " + id + " does not exist"));
        return supplierMapper.toDTO(s);
    }

    @Transactional(readOnly = true)
    public List<SupplierResponseDTO> findAllSuppliers() {
        return supplierRepository.findAll()
                .stream()
                .map(supplierMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponseDTO findByIce(String ice) {
        Supplier s = supplierRepository.findByIce(ice)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with ICE '" + ice + "' not found"));
        return supplierMapper.toDTO(s);
    }

    @Transactional
    public SupplierResponseDTO updateSupplier(Long id, SupplierUpdateRequestDTO dto) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with id " + id + " does not exist"));

        if (!existing.getIce().equals(dto.getIce())) {
            Optional<Supplier> byIce = supplierRepository.findByIce(dto.getIce());
            if (byIce.isPresent() && !byIce.get().getId().equals(id)) {
                throw new DuplicateResourceException("Supplier with ICE " + dto.getIce() + " already exists");
            }
        }

        supplierMapper.updateEntity(dto, existing);

        try {
            Supplier saved = supplierRepository.save(existing);
            return supplierMapper.toDTO(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Supplier with ICE " + dto.getIce() + " already exists", e);
        }
    }

    @Transactional
    public void deleteSupplier(Long id) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with id " + id + " does not exist"));
        supplierRepository.delete(existing);
    }
}

