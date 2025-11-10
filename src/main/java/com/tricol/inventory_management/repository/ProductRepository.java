package com.tricol.inventory_management.repository;

import com.tricol.inventory_management.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
    // Spring Data JPA will automatically generate the implementation for this method
    // It will query the database: SELECT * FROM product WHERE reference = ?
public interface ProductRepository extends JpaRepository<Product, Long>{
    // Spring Data JPA will automatically generate the implementation for this method
    // It will query the database: SELECT * FROM product WHERE reference = ?
    Optional<Product> findByReference(String reference);
}