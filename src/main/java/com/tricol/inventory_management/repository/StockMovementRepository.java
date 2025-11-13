package com.tricol.inventory_management.repository;

import com.tricol.inventory_management.enums.MovementType;
import com.tricol.inventory_management.model.Product;
import com.tricol.inventory_management.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductOrderByMovementDateDesc(Product product);


    List<StockMovement> findAllByOrderByMovementDateDesc();
}


