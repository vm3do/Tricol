package com.tricol.inventory_management.service;


import com.tricol.inventory_management.exception.DuplicateResourceException;
import com.tricol.inventory_management.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import com.tricol.inventory_management.dto.request.create.ProductCreateRequestDTO;
import com.tricol.inventory_management.dto.request.update.ProductUpdateRequestDTO;
import com.tricol.inventory_management.dto.response.ProductResponseDTO;
import com.tricol.inventory_management.mapper.ProductMapper;
import com.tricol.inventory_management.model.Product;
import com.tricol.inventory_management.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductResponseDTO createProduct(ProductCreateRequestDTO requestDTO){
        if (productRepository.findByReference(requestDTO.getReference()).isPresent()) {
            throw new DuplicateResourceException("Product with reference " + requestDTO.getReference() + " already exists");
        }

        // we received the request which is a dto, and to save to db, we have to make it an entity
        Product product = productMapper.toEntity(requestDTO);
        try {
            Product savedProduct = productRepository.save(product);
        //  convert saved entity to dto and return it as a response
            return productMapper.toDTO(savedProduct);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Product with reference " + requestDTO.getReference() + " already exists", e);
        }
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " does not exist"));
        return productMapper.toDTO(product);
    }

    @Transactional(readOnly = true)
    public Product findEntityById(Long id){
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " does not exist"));
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAllProducts(){
        // as always, find all gives us a response, so  we go from entity to dto
        return productRepository.findAll()
                .stream()
                .map(productMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findByReference(String reference) {
        Product product = productRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Product with reference '" + reference + "' not found"));
        return productMapper.toDTO(product);
    }

    public ProductResponseDTO updateProduct(Long id, ProductUpdateRequestDTO dto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " does not exist"));

        if (!existing.getReference().equals(dto.getReference())) {
            Optional<Product> byRef = productRepository.findByReference(dto.getReference());
            if (byRef.isPresent() && !byRef.get().getId().equals(id)) {
                throw new DuplicateResourceException("Product with reference " + dto.getReference() + " already exists");
            }
        }

        productMapper.updateEntity(dto, existing);

        try {
            Product saved = productRepository.save(existing);
            return productMapper.toDTO(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Product with reference " + dto.getReference() + " already exists", e);
        }
    }

    public void deleteProduct(Long id) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " does not exist"));
        productRepository.delete(existing);
    }


}