package com.tricol.Tricol.repository;

import com.tricol.Tricol.model.Product;
import com.tricol.Tricol.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long>, JpaSpecificationExecutor<StockMovement> {

    List<StockMovement> findByProductOrderByMovementDateDesc(Product product);

    List<StockMovement> findAllByOrderByMovementDateDesc();

    @Query("select p.name, s.movementType, COUNT(s.id) from StockMovement s inner join s.product p GROUP BY p.name, s.movementType")
    List<Object[]> findAllWithTypeAndNombreMovement();

}


