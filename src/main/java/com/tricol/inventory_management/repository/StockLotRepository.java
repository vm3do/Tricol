package com.tricol.inventory_management.repository;

import com.tricol.inventory_management.model.Product;
import com.tricol.inventory_management.model.StockLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockLotRepository extends JpaRepository<StockLot, Long> {

    @Query("SELECT sl FROM StockLot sl WHERE sl.product.id = :productId AND sl.remainingQuantity > 0 ORDER BY sl.entryDate ASC, sl.id ASC")
    List<StockLot> findAvailableLotsByProductOrderByEntryDate(@Param("productId") Long productId);

    List<StockLot> findByProduct(Product product);

    boolean existsByLotNumber(String lotNumber);
}


