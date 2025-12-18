package com.tricol.Tricol.repository;

import com.tricol.Tricol.model.Product;
import com.tricol.Tricol.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long>, JpaSpecificationExecutor<StockMovement> {

    List<StockMovement> findByProductOrderByMovementDateDesc(Product product);

    List<StockMovement> findAllByOrderByMovementDateDesc();

}


